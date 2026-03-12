package expo.modules.thermalprinter

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.bluetooth.BluetoothAdapter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.util.Base64
import android.util.Log
import java.io.IOException
import java.nio.charset.Charset
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.DeviceConnection
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
import com.dantsu.escposprinter.connection.usb.UsbConnection
import com.dantsu.escposprinter.connection.usb.UsbPrintersConnections
import com.dantsu.escposprinter.textparser.PrinterTextParserImg
import expo.modules.kotlin.Promise
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition

/**
 * Módulo Expo para impressão térmica com suporte ESC/POS
 * 
 * Implementa funcionalidades de impressão robusta similar ao RawBT:
 * - Dithering Floyd-Steinberg para qualidade profissional
 * - Suporte a impressoras Bluetooth (incluindo Moderninha)
 * - Conversão automática de imagens para comandos ESC/POS
 * - Gerenciamento de conexão persistente
 */
class ExpoThermalPrinterModule : Module() {
    
    companion object {
        private const val TAG = "ThermalPrinter"
        private const val DEFAULT_DPI = 203
        private const val DEFAULT_WIDTH_MM = 48f
        private const val ACTION_USB_PERMISSION = "expo.modules.thermalprinter.USB_PERMISSION"
        private const val MAX_RETRY_ATTEMPTS = 2
        
        // Imagem Base64 de teste (1x1 pixel preto PNG)
        // Usado para testar se o problema está na comunicação RN→Kotlin ou no processamento Kotlin
        private const val TEST_IMAGE_BASE64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
    }
    
    private var currentConnection: DeviceConnection? = null
    private var currentPrinter: EscPosPrinter? = null // DEPRECATED: Usar textPrinter
    private var textPrinter: EscPosPrinter? = null // Dedicado para textos ESC/POS
    private var usbPermissionLatch: CountDownLatch? = null
    private var usbPermissionGranted = false
    
    override fun definition() = ModuleDefinition {
        Name("ExpoThermalPrinter")
        
        /**
         * Imprime uma imagem usando dithering Floyd-Steinberg
         * 
         * @param base64Image String Base64 da imagem
         * @param options Opções de impressão (paperWidth, dpi, applyDithering)
         * @param promise Promise para retornar resultado
         */
        AsyncFunction("printImage") { base64Image: String, options: Map<String, Any>, promise: Promise ->
            var bitmap: Bitmap? = null
            val slices = mutableListOf<Bitmap>()
            try {
                Log.d(TAG, "=== ROTA 1: IMPRESSÃO DE IMAGEM (ARQUITETURA DESACOPLADA) ===")
                
                // 1. Garante conexão física
                val conn = ensureConnection() ?: throw Exception("Falha ao abrir conexão com a impressora")
                
                // 2. Extrai opções
                val paperWidth = (options["paperWidth"] as? Int) ?: 58
                val applyDithering = (options["applyDithering"] as? Boolean) ?: true
                val useThreshold = (options["useThreshold"] as? Boolean) ?: false
                val enableSlicing = (options["enableSlicing"] as? Boolean) ?: true
                val maxSliceHeight = (options["maxSliceHeight"] as? Int) ?: 400
                
                Log.d(TAG, "Configurações: paperWidth=$paperWidth mm, dithering=$applyDithering, threshold=$useThreshold, slicing=$enableSlicing")
                
                // 3. Decodifica Base64 para Bitmap
                Log.d(TAG, "Decodificando Base64...")
                val decodedBytes = Base64.decode(base64Image, Base64.DEFAULT)
                bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                    ?: throw Exception("Base64 inválida ou corrompida")
                
                Log.d(TAG, "Bitmap original: ${bitmap.width}x${bitmap.height}px")
                
                // 4. DELEGA PARA CLASSES ESPECIALISTAS
                // Redimensiona
                bitmap = ImageUtils.resizeForPrinter(bitmap, paperWidth)
                Log.d(TAG, "Redimensionado: ${bitmap.width}x${bitmap.height}px")
                
                // Aplica processamento (threshold OU dithering)
                if (useThreshold) {
                    Log.d(TAG, "Aplicando threshold (QR codes)...")
                    bitmap = ImageUtils.applyThreshold(bitmap)
                } else if (applyDithering) {
                    Log.d(TAG, "Aplicando Floyd-Steinberg (fotos)...")
                    bitmap = ImageUtils.applyFloydSteinbergDithering(bitmap)
                }
                
                // 5. Imprime usando motor RAW (com ou sem slicing)
                if (enableSlicing && bitmap.height > maxSliceHeight) {
                    Log.d(TAG, "⚠ Imagem grande (${bitmap.height}px). Fatiando em ${maxSliceHeight}px...")
                    slices.addAll(ImageUtils.sliceImage(bitmap, maxSliceHeight))
                    bitmap = null // Já reciclado
                    
                    Log.d(TAG, "🚀 Imprimindo ${slices.size} fatias via RawImagePrinter")
                    RawImagePrinter.printBitmapSlices(
                        connection = conn,
                        slices = slices,
                        centered = true,
                        feedLinesBetweenSlices = 0,
                        feedLinesAfterLast = 3
                    )
                } else {
                    Log.d(TAG, "🚀 Imprimindo imagem única via RawImagePrinter")
                    Log.d(TAG, "Tamanho estimado: ${RawImagePrinter.estimateRawBytesSize(bitmap)} bytes")
                    
                    RawImagePrinter.printBitmapDirectly(
                        connection = conn,
                        bitmap = bitmap,
                        centered = true,
                        feedLines = 3
                    )
                }
                
                promise.resolve(
                    mapOf(
                        "success" to true,
                        "message" to "Imagem impressa via RawBytes"
                    )
                )
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erro na Rota de Imagem: ${e.message}", e)
                promise.reject("IMAGE_ERROR", e.message ?: "Erro ao imprimir imagem", e)
            } finally {
                // Libera memória
                bitmap?.recycle()
                slices.forEach { it.recycle() }
                slices.clear()
                Log.d(TAG, "✓ Memória liberada")
            }
        }
        
        /**
         * ROTA 2: Impressão de Textos ESC/POS
         * Usa DantSu apenas para formatação de texto ([L], [C], <b>, etc)
         * 
         * @param text Template de texto ESC/POS
         * @param options Opções de impressão
         * @param promise Promise para retornar resultado
         */
        AsyncFunction("printText") { text: String, options: Map<String, Any>, promise: Promise ->
            try {
                Log.d(TAG, "=== ROTA 2: IMPRESSÃO DE TEXTO (DANTSU) ===")
                
                // 1. Garante conexão física
                val conn = ensureConnection() ?: throw Exception("Falha ao abrir conexão com a impressora")
                
                // 2. Extrai opções
                val paperWidth = (options["paperWidth"] as? Int) ?: 58
                val dpi = (options["dpi"] as? Int) ?: DEFAULT_DPI
                
                // 3. Cria/reutiliza textPrinter dedicado
                // Lembrete: 32 caracteres para 58mm, 48 caracteres para 80mm
                val charsPerLine = if (paperWidth == 58) 32 else 48
                
                if (textPrinter == null) {
                    Log.d(TAG, "Criando textPrinter dedicado ($charsPerLine chars/linha)")
                    textPrinter = EscPosPrinter(conn, dpi, DEFAULT_WIDTH_MM, charsPerLine)
                }
                
                // 4. Imprime texto formatado (tags ESC/POS funcionam aqui)
                Log.d(TAG, "Imprimindo texto via DantSu...")
                textPrinter?.printFormattedText(text)
                
                // 5. Força flush do buffer
                conn.send()
                
                promise.resolve(
                    mapOf(
                        "success" to true,
                        "message" to "Texto impresso com sucesso"
                    )
                )
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erro na Rota de Texto: ${e.message}", e)
                promise.reject("TEXT_ERROR", e.message ?: "Erro ao imprimir texto", e)
            }
        }
        
        /**
         * Retorna lista de impressoras (Bluetooth e USB)
         * 
         * @param promise Promise para retornar lista de dispositivos
         */
        AsyncFunction("getPairedPrinters") { promise: Promise ->
            try {
                Log.d(TAG, "═══════════════════════════════════════")
                Log.d(TAG, "🔍 Buscando impressoras disponíveis...")
                Log.d(TAG, "🔍 Android SDK: ${Build.VERSION.SDK_INT}")
                Log.d(TAG, "═══════════════════════════════════════")
                
                val printersList = mutableListOf<Map<String, String>>()
                
                // 1. Buscar impressoras Bluetooth
                try {
                    Log.d(TAG, "📡 Verificando impressoras Bluetooth...")
                    
                    // Verificar permissões Bluetooth
                    val context = appContext.reactContext
                    if (context != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val hasScan = context.checkSelfPermission(android.Manifest.permission.BLUETOOTH_SCAN) == 
                                     android.content.pm.PackageManager.PERMISSION_GRANTED
                        val hasConnect = context.checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) == 
                                        android.content.pm.PackageManager.PERMISSION_GRANTED
                        
                        Log.d(TAG, "🔐 [Android 12+] BLUETOOTH_SCAN: $hasScan")
                        Log.d(TAG, "🔐 [Android 12+] BLUETOOTH_CONNECT: $hasConnect")
                        
                        if (!hasScan || !hasConnect) {
                            Log.e(TAG, "❌ Permissões Bluetooth não concedidas! Peça ao usuário para conceder.")
                            promise.reject("PERMISSION_DENIED", "Permissões Bluetooth não concedidas", null)
                            return@AsyncFunction
                        }
                    }
                    
                    // Verificar se Bluetooth está habilitado
                    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                    if (bluetoothAdapter == null) {
                        Log.w(TAG, "⚠️ Dispositivo não possui Bluetooth")
                    } else if (!bluetoothAdapter.isEnabled) {
                        Log.w(TAG, "⚠️ Bluetooth está DESLIGADO. Peça ao usuário para ligar.")
                    } else {
                        Log.d(TAG, "✅ Bluetooth está LIGADO")
                        val bondedDevices = bluetoothAdapter.bondedDevices
                        Log.d(TAG, "📱 Dispositivos pareados: ${bondedDevices?.size ?: 0}")
                        
                        // CORREÇÃO: Listar TODOS os dispositivos pareados diretamente
                        // A biblioteca DantSu filtra demais e não detecta InnerPrinter
                        bondedDevices?.forEach { device ->
                            Log.d(TAG, "  - ${device.name} (${device.address})")
                            
                            // Adicionar TODOS os dispositivos pareados à lista
                            printersList.add(mapOf(
                                "name" to (device.name ?: "Bluetooth Desconhecido"),
                                "address" to device.address,
                                "type" to "bluetooth"
                            ))
                        }
                        
                        Log.d(TAG, "Listadas ${bondedDevices?.size ?: 0} impressoras Bluetooth pareadas")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Erro ao buscar Bluetooth: ${e.message}")
                }
                
                // 2. Buscar impressoras USB (Moderninha Smart)
                try {
                    Log.d(TAG, "Verificando impressoras USB...")
                    val context = appContext.reactContext ?: throw Exception("Context não disponível")
                    val usbManager = context.getSystemService(Context.USB_SERVICE) as? UsbManager
                    
                    if (usbManager != null) {
                        val usbConnections = UsbPrintersConnections(context).list ?: emptyArray()
                        
                        usbConnections.forEach { connection ->
                            val device = connection.device
                            printersList.add(mapOf(
                                "name" to (device.deviceName ?: "USB Printer"),
                                "address" to "usb_${device.deviceId}",
                                "type" to "usb"
                            ))
                        }
                        
                        Log.d(TAG, "Encontradas ${usbConnections.size} impressoras USB")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Erro ao buscar USB: ${e.message}")
                }
                
                Log.d(TAG, "Total de impressoras encontradas: ${printersList.size}")
                
                // Se não encontrou nada, tenta detectar impressora interna genérica
                if (printersList.isEmpty()) {
                    Log.d(TAG, "Nenhuma impressora detectada. Adicionando impressora interna genérica...")
                    printersList.add(mapOf(
                        "name" to "Impressora Interna (Auto-detect)",
                        "address" to "internal_auto",
                        "type" to "internal"
                    ))
                }
                
                promise.resolve(printersList)
                
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao buscar impressoras: ${e.message}", e)
                promise.reject("SEARCH_ERROR", e.message ?: "Erro ao buscar impressoras", e)
            }
        }
        
        /**
         * Conecta a uma impressora específica (Bluetooth, USB ou auto-detect)
         * 
         * @param address Endereço da impressora
         * @param promise Promise para retornar resultado
         */
        AsyncFunction("connectPrinter") { address: String, promise: Promise ->
            try {
                Log.d(TAG, "Conectando à impressora: $address")
                
                var connection: DeviceConnection? = null
                var printerName = "Desconhecida"
                
                // Auto-detect: tenta todas as fontes
                if (address == "internal_auto") {
                    Log.d(TAG, "Modo auto-detect ativado")
                    connection = findAnyAvailablePrinter()
                    printerName = "Auto-detectada"
                } 
                // USB
                else if (address.startsWith("usb_")) {
                    Log.d(TAG, "Buscando impressora USB...")
                    val context = appContext.reactContext ?: throw Exception("Context não disponível")
                    val usbManager = context.getSystemService(Context.USB_SERVICE) as? UsbManager
                    
                    if (usbManager != null) {
                        val usbConnections = UsbPrintersConnections(context).list ?: emptyArray()
                        val deviceId = address.removePrefix("usb_").toIntOrNull()
                        
                        val usbConn = usbConnections.find { it.device.deviceId == deviceId }
                        
                        if (usbConn != null) {
                            val device = usbConn.device
                            Log.d(TAG, "Impressora USB encontrada: ${device.deviceName} (ID: ${device.deviceId})")
                            
                            // Solicita permissão USB se necessário
                            if (!usbManager.hasPermission(device)) {
                                Log.d(TAG, "Solicitando permissão USB ao usuário...")
                                val granted = requestUsbPermission(context, usbManager, device)
                                
                                if (!granted) {
                                    Log.e(TAG, "Permissão USB NEGADA pelo usuário")
                                    promise.reject("USB_PERMISSION_DENIED", "Permissão USB negada pelo usuário", null)
                                    return@AsyncFunction
                                }
                                
                                Log.d(TAG, "✓ Permissão USB CONCEDIDA!")
                            } else {
                                Log.d(TAG, "✓ Permissão USB já concedida anteriormente")
                            }
                            
                            connection = usbConn
                            printerName = device.deviceName ?: "USB Printer"
                            Log.d(TAG, "Conexão USB preparada: $printerName")
                        } else {
                            Log.e(TAG, "Impressora USB com ID $deviceId não encontrada")
                        }
                    }
                } 
                // Bluetooth
                else {
                    Log.d(TAG, "Buscando impressora Bluetooth...")
                    
                    // Verificar permissão BLUETOOTH_CONNECT no Android 12+
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val context = appContext.reactContext
                        if (context != null) {
                            val hasConnect = context.checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) == 
                                            android.content.pm.PackageManager.PERMISSION_GRANTED
                            Log.d(TAG, "🔐 [connectPrinter] BLUETOOTH_CONNECT: $hasConnect")
                            if (!hasConnect) {
                                promise.reject("PERMISSION_DENIED", "Permissão BLUETOOTH_CONNECT não concedida. Use o botão de permissões Android 12+ primeiro.", null)
                                return@AsyncFunction
                            }
                        }
                    }
                    
                    // Tentar primeiro com a biblioteca DantSu
                    val bluetoothConnections = BluetoothPrintersConnections().list ?: emptyArray()
                    connection = bluetoothConnections.find { it.device.address == address }
                    
                    // Se não encontrou, criar conexão manualmente usando BluetoothAdapter
                    if (connection == null) {
                        Log.d(TAG, "Biblioteca DantSu não encontrou. Tentando BluetoothAdapter...")
                        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                        
                        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled) {
                            val bondedDevices = bluetoothAdapter.bondedDevices
                            val targetDevice = bondedDevices?.find { it.address == address }
                            
                            if (targetDevice != null) {
                                Log.d(TAG, "✓ Dispositivo encontrado: ${targetDevice.name}")
                                connection = BluetoothConnection(targetDevice)
                                printerName = targetDevice.name ?: "Bluetooth Printer"
                            } else {
                                Log.e(TAG, "Dispositivo $address não está pareado")
                            }
                        }
                    } else {
                        printerName = (connection as? BluetoothConnection)?.device?.name ?: "Bluetooth Printer"
                    }
                }
                
                if (connection == null) {
                    promise.reject("NOT_FOUND", "Impressora não encontrada: $address", null)
                    return@AsyncFunction
                }
                
                // ==========================================
                // CORREÇÃO CRÍTICA: Abre socket e cria printer UMA VEZ
                // ==========================================
                Log.d(TAG, "Abrindo Socket de comunicação...")
                try {
                    // Desconecta anterior se existir
                    currentConnection?.disconnect()
                    currentPrinter = null
                    
                    // 1. FAZ A CONEXÃO FÍSICA! (Abre a porta)
                    connection.connect()
                    Log.d(TAG, "Socket aberto com sucesso!")
                } catch (e: Exception) {
                    Log.e(TAG, "Falha ao abrir porta da impressora", e)
                    promise.reject("SOCKET_ERROR", "A impressora recusou a conexão (pode estar desligada ou ocupada)", e)
                    return@AsyncFunction
                }
                
                // 2. Cria a instância da impressora UMA ÚNICA VEZ
                // Bobina de 58mm = 32 caracteres por linha (não confundir com 384 pixels!)
                val printer = EscPosPrinter(connection, DEFAULT_DPI, DEFAULT_WIDTH_MM, 32)
                
                // 3. Salva globalmente para reutilizar nas impressões
                currentConnection = connection
                currentPrinter = printer
                // ==========================================
                
                Log.d(TAG, "Conectado e inicializado com sucesso: $printerName")
                
                promise.resolve(
                    mapOf(
                        "success" to true,
                        "message" to "Conectado à impressora $printerName"
                    )
                )
                
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao conectar: ${e.message}", e)
                promise.reject("CONNECTION_ERROR", e.message ?: "Erro ao conectar", e)
            }
        }
        
        /**
         * Imprime um cupom fiscal formatado com produtos e QR code
         * 
         * @param items Lista de produtos [{name, price, quantity}]
         * @param options Opções (cpf, total, qrCodeUrl)
         * @param promise Promise para retornar resultado
         */
        AsyncFunction("printReceipt") { items: List<Map<String, Any>>, options: Map<String, Any>, promise: Promise ->
            try {
                Log.d(TAG, "Iniciando impressão de cupom fiscal...")
                
                val printer = getOrCreatePrinter(58, DEFAULT_DPI)
                
                if (printer == null) {
                    promise.reject("NO_PRINTER", "Nenhuma impressora conectada", null)
                    return@AsyncFunction
                }
                
                val cpf = options["cpf"] as? String ?: ""
                val total = options["total"] as? Double ?: 0.0
                val qrCodeUrl = options["qrCodeUrl"] as? String ?: "https://reinodasorte.com.br"
                
                val receiptText = buildString {
                    // Cabeçalho com fonte maior
                    append("[C]<font size='big'><b>CUPOM FISCAL</b></font>\n")
                    append("[C]================================\n")  // 32 caracteres exatos
                    
                    if (cpf.isNotEmpty()) {
                        append("[L]CPF: $cpf\n")
                        append("[L]--------------------------------\n")  // 32 caracteres exatos
                    }
                    
                    // Cabeçalho da tabela em negrito
                    append("[L]<b>PRODUTO</b>[R]<b>VALOR</b>\n")
                    append("[L]--------------------------------\n")  // 32 caracteres exatos
                    
                    items.forEach { item ->
                        val name = item["name"] as? String ?: "Produto"
                        val price = item["price"] as? Double ?: 0.0
                        val quantity = item["quantity"] as? Int ?: 1
                        val itemTotal = price * quantity
                        
                        // Formata linha respeitando 32 caracteres
                        val qtdStr = "${quantity}x "
                        val precoStr = "R$ %.2f".format(itemTotal)
                        val espacoLivre = 32 - qtdStr.length - precoStr.length
                        
                        // Trunca nome se necessário
                        val nomeFormatado = if (name.length > espacoLivre) {
                            name.substring(0, espacoLivre - 2) + ".."
                        } else {
                            name
                        }
                        
                        append("[L]$qtdStr$nomeFormatado[R]$precoStr\n")
                    }
                    
                    // Total em destaque com fonte maior
                    append("[L]--------------------------------\n")  // 32 caracteres exatos
                    append("[L]<font size='tall'><b>TOTAL[R]R$ %.2f</b></font>\n".format(total))
                    append("[L]================================\n")  // 32 caracteres exatos
                    
                    // QR Code menor (15 em vez de 20) para economizar papel
                    append("[C]Acesse nosso site:\n")
                    append("[C]<qrcode size='15'>$qrCodeUrl</qrcode>\n")
                    append("[C]<font size='small'>$qrCodeUrl</font>\n")
                    
                    append("[C]Obrigado pela preferencia!\n")
                    append("[L]\n")
                    append("[L]\n")
                    append("[L]\n")
                }
                
                printer.printFormattedText(receiptText)
                
                Log.d(TAG, "Cupom fiscal impresso com sucesso!")
                
                promise.resolve(
                    mapOf(
                        "success" to true,
                        "message" to "Cupom fiscal impresso com sucesso!"
                    )
                )
                
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao imprimir cupom: ${e.message}", e)
                promise.reject("RECEIPT_ERROR", e.message ?: "Erro ao imprimir cupom", e)
            }
        }
        
        /**
         * Executa auto-teste da impressora
         * Imprime página de diagnóstico com alinhamentos, formatação, acentuação e código de barras
         * 
         * @param promise Promise para retornar resultado
         */
        AsyncFunction("selfTest") { promise: Promise ->
            try {
                Log.d(TAG, "Executando auto-teste da impressora...")
                
                val printer = getOrCreatePrinter(58, DEFAULT_DPI)
                
                if (printer == null) {
                    promise.reject("NO_PRINTER", "Nenhuma impressora disponível", null)
                    return@AsyncFunction
                }
                
                printer.printFormattedText(
                    "[C]<font cp=\"850\"><b>TESTE DE IMPRESSÃO</b></font>\n" +
                    "[L]--------------------------------\n" +
                    "[L]<font cp=\"850\">Alinhamento Esquerdo</font>\n" +
                    "[C]<font cp=\"850\">Alinhamento Central</font>\n" +
                    "[R]<font cp=\"850\">Alinhamento Direito</font>\n" +
                    "[L]--------------------------------\n" +
                    "[L]<b>Negrito</b> <i>Itálico</i>\n" +
                    "[L]<font cp=\"850\">Acentuação: á é í ó ú ç ã õ</font>\n" +
                    "[L]--------------------------------\n" +
                    "[C]<barcode type=\"ean13\" height=\"10\">1234567890128</barcode>\n" +
                    "[L]\n" +
                    "[L]\n" +
                    "[L]\n"
                )
                
                Log.d(TAG, "Auto-teste concluído com sucesso!")
                
                promise.resolve(
                    mapOf(
                        "success" to true,
                        "message" to "Teste de impressão concluído"
                    )
                )
                
            } catch (e: Exception) {
                Log.e(TAG, "Erro no auto-teste: ${e.message}", e)
                promise.reject("TEST_ERROR", e.message ?: "Erro no auto-teste", e)
            }
        }
        
        /**
         * Imprime uma imagem de teste hardcoded no Kotlin (sem receber do React Native)
         * Usado para isolar se o problema está na comunicação RN→Kotlin ou no processamento
         * 
         * @param paperWidth Largura do papel (58 ou 80mm)
         * @param dpi DPI da impressora (padrão 203)
         * @param applyDithering Se deve aplicar dithering Floyd-Steinberg
         * @param promise Promise para retornar resultado
         */
        AsyncFunction("printTestImage") { paperWidth: Int, dpi: Int, applyDithering: Boolean, promise: Promise ->
            var bitmap: Bitmap? = null
            try {
                Log.d(TAG, "=== TESTE: IMAGEM HARDCODED (ARQUITETURA DESACOPLADA) ===")
                
                // 1. Garante conexão
                val conn = ensureConnection() ?: throw Exception("Falha ao conectar impressora")
                
                // 2. Decodifica imagem hardcoded
                Log.d(TAG, "Decodificando Base64 interno (${TEST_IMAGE_BASE64.length} chars)...")
                val decodedBytes = Base64.decode(TEST_IMAGE_BASE64, Base64.DEFAULT)
                bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                    ?: throw Exception("Falha ao decodificar Base64 interno")
                
                Log.d(TAG, "Bitmap: ${bitmap.width}x${bitmap.height}px")
                
                // 3. Processa imagem
                bitmap = ImageUtils.resizeForPrinter(bitmap, paperWidth)
                if (applyDithering) {
                    Log.d(TAG, "Aplicando Floyd-Steinberg...")
                    bitmap = ImageUtils.applyFloydSteinbergDithering(bitmap)
                }
                
                // 4. Imprime cabeçalho via textPrinter
                val charsPerLine = if (paperWidth == 58) 32 else 48
                if (textPrinter == null) {
                    textPrinter = EscPosPrinter(conn, dpi, DEFAULT_WIDTH_MM, charsPerLine)
                }
                
                textPrinter?.printFormattedText(
                    "[C]<b>TESTE HARDCODED</b>\n" +
                    "[C]Motor RAW (RawBT)\n\n"
                )
                conn.send() // Flush texto
                
                // 5. Imprime imagem via RawImagePrinter
                Log.d(TAG, "🚀 Imprimindo via RawImagePrinter (${RawImagePrinter.estimateRawBytesSize(bitmap)} bytes)")
                RawImagePrinter.printBitmapDirectly(
                    connection = conn,
                    bitmap = bitmap,
                    centered = true,
                    feedLines = 2
                )
                
                // 6. Imprime rodapé
                textPrinter?.printFormattedText(
                    "[C]Se imprimiu: Kotlin OK!\n" +
                    "[C]Problema: RN→Kotlin\n\n\n"
                )
                conn.send() // Flush texto
                
                promise.resolve(
                    mapOf(
                        "success" to true,
                        "message" to "Teste hardcoded via arquitetura desacoplada!"
                    )
                )
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erro no teste: ${e.message}", e)
                promise.reject("TEST_ERROR", e.message ?: "Erro no teste", e)
            } finally {
                bitmap?.recycle()
            }
        }
        
        /**
         * Imprime bilhetes da loteria Reino da Sorte
         * 
         * @param ticketData Dados completos do bilhete com todos os talões
         * @param options Opções de impressão (paperWidth, encoding)
         * @param promise Promise para retornar resultado
         */
        AsyncFunction("printLotteryTicket") { ticketData: Map<String, Any>, options: Map<String, Any>, promise: Promise ->
            try {
                Log.d(TAG, "=== IMPRESSÃO DE BILHETE REINO DA SORTE ===")
                
                val conn = ensureConnection() ?: throw Exception("Falha ao conectar com a impressora")
                
                val extraPrizes = (ticketData["extraPrizeValue"] as? List<Map<String, Any>>)?.map { extra ->
                    LotteryTicketPrinter.ExtraPrize(
                        titulo = extra["titulo"] as? String ?: "",
                        valor = extra["valor"] as? String ?: ""
                    )
                } ?: emptyList()
                
                val booklets = (ticketData["booklets"] as? List<Map<String, Any>>)?.map { booklet ->
                    LotteryTicketPrinter.Booklet(
                        bookletNumber = (booklet["bookletNumber"] as? Number)?.toInt() ?: 0,
                        lotNumber = (booklet["lotNumber"] as? Number)?.toInt() ?: 0,
                        tickets = (booklet["tickets"] as? List<String>) ?: emptyList()
                    )
                } ?: emptyList()
                
                val ticket = LotteryTicketPrinter.TicketData(
                    id = ticketData["id"] as? String ?: "",
                    customerName = ticketData["customerName"] as? String ?: "",
                    customerPhone = ticketData["customerPhone"] as? String ?: "",
                    sellerName = ticketData["sellerName"] as? String ?: "",
                    sellerPhone = ticketData["sellerPhone"] as? String ?: "",
                    drawTitle = ticketData["drawTitle"] as? String ?: "",
                    drawDate = ticketData["drawDate"] as? String ?: "",
                    mainPrizeValue = ticketData["mainPrizeValue"] as? String ?: "",
                    extraPrizeValue = extraPrizes,
                    createdAt = ticketData["createdAt"] as? String ?: "",
                    booklets = booklets,
                    urlSorteio = ticketData["urlSorteio"] as? String
                )
                
                Log.d(TAG, "Dados do bilhete: ID=${ticket.id}, Talões=${ticket.booklets.size}")
                
                LotteryTicketPrinter.printAllBooklets(conn, ticket)
                
                promise.resolve(
                    mapOf(
                        "success" to true,
                        "message" to "Bilhetes impressos com sucesso! (${ticket.booklets.size} talões)"
                    )
                )
                
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao imprimir bilhete da loteria: ${e.message}", e)
                promise.reject("LOTTERY_TICKET_ERROR", e.message ?: "Erro ao imprimir bilhete", e)
            }
        }
        
        /**
         * Desconecta da impressora atual
         * 
         * @param promise Promise para retornar resultado
         */
        AsyncFunction("disconnectPrinter") { promise: Promise ->
            try {
                currentConnection?.disconnect()
                currentConnection = null
                currentPrinter = null // Limpa a instância para evitar lixo na memória
                
                Log.d(TAG, "Desconectado da impressora")
                
                promise.resolve(
                    mapOf(
                        "success" to true,
                        "message" to "Desconectado com sucesso"
                    )
                )
                
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao desconectar: ${e.message}", e)
                promise.reject("DISCONNECT_ERROR", e.message ?: "Erro ao desconectar", e)
            }
        }
    }
    
    /**
     * Obtém a instância ativa da impressora ou tenta auto-conectar
     * CORREÇÃO: Reutiliza currentPrinter para evitar Broken Pipe
     */
    private fun getOrCreatePrinter(paperWidth: Int, dpi: Int): EscPosPrinter? {
        try {
            // ==========================================
            // SOCKET RETRY LOGIC (AUTO-RECOVER)
            // ==========================================
            // Se já temos impressora, verifica se o socket ainda está vivo
            if (currentPrinter != null && currentConnection != null) {
                try {
                    // Testa se a conexão ainda está ativa
                    if (currentConnection!!.isConnected) {
                        Log.d(TAG, "✓ Reutilizando impressora já conectada")
                        return currentPrinter
                    } else {
                        Log.w(TAG, "⚠ Socket detectado como desconectado. Tentando reconectar...")
                        
                        // Tenta reconectar o socket existente
                        try {
                            currentConnection!!.connect()
                            Log.d(TAG, "✓ Socket reconectado com sucesso!")
                            return currentPrinter
                        } catch (reconnectError: Exception) {
                            Log.e(TAG, "✗ Falha ao reconectar. Limpando conexão antiga...", reconnectError)
                            // Limpa conexão quebrada
                            try {
                                currentConnection?.disconnect()
                            } catch (ignored: Exception) {}
                            currentConnection = null
                            currentPrinter = null
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "⚠ Erro ao verificar status do socket. Recriando conexão...", e)
                    // Limpa estado inconsistente
                    try {
                        currentConnection?.disconnect()
                    } catch (ignored: Exception) {}
                    currentConnection = null
                    currentPrinter = null
                }
            }
            
            // ==========================================
            // AUTO-DETECT E CRIAÇÃO DE NOVA CONEXÃO
            // ==========================================
            Log.d(TAG, "Nenhuma impressora ativa. Tentando auto-detectar...")
            val connection = findAnyAvailablePrinter()
            
            if (connection == null) {
                Log.w(TAG, "✗ Auto-detect falhou.")
                return null
            }
            
            // Conecta o socket com retry (até 2 tentativas)
            var connectAttempts = 0
            val maxAttempts = 2
            var connected = false
            
            while (connectAttempts < maxAttempts && !connected) {
                try {
                    connectAttempts++
                    Log.d(TAG, "Tentativa $connectAttempts/$maxAttempts de conectar socket...")
                    connection.connect()
                    connected = true
                    Log.d(TAG, "✓ Socket conectado com sucesso!")
                } catch (e: Exception) {
                    Log.e(TAG, "✗ Falha na tentativa $connectAttempts: ${e.message}", e)
                    if (connectAttempts < maxAttempts) {
                        Thread.sleep(500) // Aguarda 500ms antes de retry
                    }
                }
            }
            
            if (!connected) {
                Log.e(TAG, "✗ Falha ao conectar após $maxAttempts tentativas")
                return null
            }
            
            // IMPORTANTE: O 4º parâmetro é CARACTERES por linha, não pixels!
            // 58mm = 32 caracteres, 80mm = 48 caracteres
            val widthChars = if (paperWidth == 58) 32 else 48
            val printer = EscPosPrinter(connection, dpi, DEFAULT_WIDTH_MM, widthChars)
            
            currentConnection = connection
            currentPrinter = printer
            
            Log.d(TAG, "✓ Impressora criada e pronta para uso")
            return printer
            
        } catch (e: Exception) {
            Log.e(TAG, "✗ Erro crítico ao obter printer: ${e.message}", e)
            return null
        }
    }
    
    /**
     * Busca qualquer impressora disponível com fallback inteligente
     * Ordem: Bluetooth → USB → Primeira disponível
     */
    private fun findAnyAvailablePrinter(): DeviceConnection? {
        try {
            // 1. Tentar Bluetooth primeiro
            Log.d(TAG, "Tentando Bluetooth...")
            
            // Verificar se Bluetooth está habilitado
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            if (bluetoothAdapter == null) {
                Log.w(TAG, "⚠️ Dispositivo não possui Bluetooth")
                return null
            }
            if (!bluetoothAdapter.isEnabled) {
                Log.w(TAG, "⚠️ Bluetooth está DESLIGADO")
                return null
            }
            
            Log.d(TAG, "✓ Bluetooth habilitado, buscando impressoras pareadas...")
            val bluetoothConnections = BluetoothPrintersConnections().list
            
            if (!bluetoothConnections.isNullOrEmpty()) {
                // Prioriza impressoras com nomes conhecidos (incluindo SUNMI)
                val preferredBluetooth = bluetoothConnections.find { 
                    val name = it.device.name?.lowercase() ?: ""
                    name.contains("innerprinter") || 
                    name.contains("sunmi") ||
                    name.contains("mpos") || 
                    name.contains("moderninha") ||
                    name.contains("printer")
                } ?: bluetoothConnections.first()
                
                Log.d(TAG, "✓ Bluetooth encontrado: ${preferredBluetooth.device.name}")
                currentConnection = preferredBluetooth
                return preferredBluetooth
            }
            
            Log.d(TAG, "✗ Nenhum Bluetooth encontrado")
            
            // 2. Tentar USB (Moderninha Smart)
            Log.d(TAG, "Tentando USB...")
            val context = appContext.reactContext
            
            if (context != null) {
                val usbManager = context.getSystemService(Context.USB_SERVICE) as? UsbManager
                
                if (usbManager != null) {
                    val usbConnections = UsbPrintersConnections(context).list
                    
                    if (!usbConnections.isNullOrEmpty()) {
                        val usbPrinter = usbConnections.first()
                        val device = usbPrinter.device
                        
                        // Solicita permissão USB se necessário
                        if (!usbManager.hasPermission(device)) {
                            Log.d(TAG, "Solicitando permissão USB para auto-detect...")
                            val granted = requestUsbPermission(context, usbManager, device)
                            
                            if (!granted) {
                                Log.w(TAG, "Permissão USB negada")
                                return null
                            }
                            
                            Log.d(TAG, "Permissão USB concedida!")
                        }
                        
                        Log.d(TAG, "✓ USB encontrado: ${device.deviceName}")
                        currentConnection = usbPrinter
                        return usbPrinter
                    }
                }
            }
            
            Log.d(TAG, "✗ Nenhuma USB encontrada")
            
            Log.w(TAG, "Nenhuma impressora disponível (Bluetooth ou USB)")
            return null
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar impressora: ${e.message}", e)
            return null
        }
    }
    
    /**
     * Solicita permissão USB e aguarda resposta do usuário
     * Usa BroadcastReceiver para capturar a resposta do popup
     */
    private fun requestUsbPermission(context: Context, usbManager: UsbManager, device: UsbDevice): Boolean {
        try {
            // Cria latch para aguardar resposta
            usbPermissionLatch = CountDownLatch(1)
            usbPermissionGranted = false
            
            // Registra BroadcastReceiver para receber resposta
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    if (intent.action == ACTION_USB_PERMISSION) {
                        synchronized(this) {
                            val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                            
                            if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                                Log.d(TAG, "Permissão USB CONCEDIDA pelo usuário")
                                usbPermissionGranted = true
                            } else {
                                Log.w(TAG, "Permissão USB NEGADA pelo usuário")
                                usbPermissionGranted = false
                            }
                            
                            usbPermissionLatch?.countDown()
                        }
                    }
                }
            }
            
            // Registra receiver
            val filter = IntentFilter(ACTION_USB_PERMISSION)
            context.registerReceiver(receiver, filter)
            
            // Solicita permissão (compatível Android 7-14)
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_MUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val permissionIntent = PendingIntent.getBroadcast(
                context,
                0,
                Intent(ACTION_USB_PERMISSION),
                flags
            )
            usbManager.requestPermission(device, permissionIntent)
            
            Log.d(TAG, "Aguardando resposta do usuário (popup USB)...")
            
            // Aguarda até 30 segundos pela resposta
            val granted = usbPermissionLatch?.await(30, TimeUnit.SECONDS) ?: false
            
            // Remove receiver
            try {
                context.unregisterReceiver(receiver)
            } catch (e: Exception) {
                Log.w(TAG, "Erro ao remover receiver: ${e.message}")
            }
            
            if (!granted) {
                Log.w(TAG, "Timeout aguardando permissão USB")
                return false
            }
            
            return usbPermissionGranted
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao solicitar permissão USB: ${e.message}", e)
            return false
        }
    }
    
    /**
     * ARQUITETURA DESACOPLADA: Gerenciador de Ciclo de Vida da Conexão
     * 
     * Retorna a conexão viva ou tenta auto-conectar.
     * Substitui printWithRetry() com abordagem mais limpa.
     * 
     * @return DeviceConnection ativa ou null se falhar
     */
    private fun ensureConnection(): DeviceConnection? {
        try {
            // 1. Se já temos conexão ativa, reutiliza
            if (currentConnection?.isConnected == true) {
                Log.d(TAG, "✓ Reutilizando conexão existente")
                return currentConnection
            }
            
            // 2. Conexão caíu ou não existe - tenta reconectar
            if (currentConnection != null) {
                Log.w(TAG, "⚠ Conexão detectada como inativa. Tentando reconectar...")
                try {
                    currentConnection!!.connect()
                    Log.d(TAG, "✓ Reconexão bem-sucedida!")
                    return currentConnection
                } catch (e: Exception) {
                    Log.e(TAG, "✗ Falha ao reconectar: ${e.message}")
                    // Limpa conexão quebrada
                    try {
                        currentConnection?.disconnect()
                    } catch (ignored: Exception) {}
                    currentConnection = null
                    currentPrinter = null
                    textPrinter = null
                }
            }
            
            // 3. Nenhuma conexão - busca e conecta automaticamente
            Log.d(TAG, "Buscando impressora disponível...")
            val connection = findAnyAvailablePrinter()
            
            if (connection == null) {
                Log.e(TAG, "✗ Nenhuma impressora encontrada")
                return null
            }
            
            // 4. Conecta com retry
            var attempts = 0
            while (attempts < MAX_RETRY_ATTEMPTS) {
                try {
                    attempts++
                    Log.d(TAG, "Tentativa $attempts/$MAX_RETRY_ATTEMPTS de conectar...")
                    connection.connect()
                    Log.d(TAG, "✓ Conectado com sucesso!")
                    
                    currentConnection = connection
                    return connection
                    
                } catch (e: Exception) {
                    Log.e(TAG, "✗ Tentativa $attempts falhou: ${e.message}")
                    if (attempts < MAX_RETRY_ATTEMPTS) {
                        Thread.sleep(500)
                    }
                }
            }
            
            Log.e(TAG, "✗ Falha após $MAX_RETRY_ATTEMPTS tentativas")
            return null
            
        } catch (e: Exception) {
            Log.e(TAG, "✗ Erro crítico em ensureConnection: ${e.message}", e)
            return null
        }
    }
}
