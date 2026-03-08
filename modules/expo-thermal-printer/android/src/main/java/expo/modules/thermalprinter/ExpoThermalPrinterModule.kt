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
    private var currentPrinter: EscPosPrinter? = null
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
            try {
                Log.d(TAG, "Iniciando processo de impressão de imagem...")
                
                // Extrai opções
                val paperWidth = (options["paperWidth"] as? Int) ?: 58
                val dpi = (options["dpi"] as? Int) ?: DEFAULT_DPI
                val applyDithering = (options["applyDithering"] as? Boolean) ?: true
                
                Log.d(TAG, "Configurações: paperWidth=$paperWidth mm, dpi=$dpi, dithering=$applyDithering")
                
                // Decodifica Base64 para Bitmap
                Log.d(TAG, "Decodificando imagem Base64...")
                val decodedBytes = Base64.decode(base64Image, Base64.DEFAULT)
                bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                
                if (bitmap == null) {
                    Log.e(TAG, "Falha ao decodificar imagem Base64")
                    promise.reject("DECODE_ERROR", "Falha ao decodificar imagem Base64", null)
                    return@AsyncFunction
                }
                
                Log.d(TAG, "Imagem decodificada: ${bitmap.width}x${bitmap.height} pixels")
                
                // Redimensiona para a largura da bobina
                Log.d(TAG, "Redimensionando imagem para bobina de $paperWidth mm...")
                bitmap = ImageUtils.resizeForPrinter(bitmap, paperWidth)
                Log.d(TAG, "Imagem redimensionada: ${bitmap.width}x${bitmap.height} pixels")
                
                // Aplica dithering se solicitado
                if (applyDithering) {
                    Log.d(TAG, "Aplicando algoritmo Floyd-Steinberg...")
                    bitmap = ImageUtils.applyFloydSteinbergDithering(bitmap)
                    Log.d(TAG, "Dithering aplicado com sucesso")
                }
                
                // Captura bitmap final para uso no retry
                val finalBitmap = bitmap
                
                // Usa printWithRetry para auto-recover de Doze Mode
                printWithRetry(paperWidth, dpi) { printer ->
                    Log.d(TAG, "Convertendo bitmap para comandos ESC/POS...")
                    Log.d(TAG, "Dimensões finais do bitmap: ${finalBitmap.width}x${finalBitmap.height}")
                    
                    val imageString = PrinterTextParserImg.bitmapToHexadecimalString(printer, finalBitmap)
                    Log.d(TAG, "Bitmap convertido para hexadecimal (${imageString.length} caracteres)")
                    
                    // Imprime a imagem centralizada
                    printer.printFormattedText(
                        "[C]<img>$imageString</img>\n\n\n"
                    )
                    
                    Log.d(TAG, "Impressão de imagem concluída com sucesso!")
                }
                
                promise.resolve(
                    mapOf(
                        "success" to true,
                        "message" to "Imagem impressa com sucesso"
                    )
                )
                
            } catch (e: Exception) {
                Log.e(TAG, "Erro durante impressão: ${e.message}", e)
                promise.reject("PRINT_ERROR", e.message ?: "Erro desconhecido durante impressão", e)
            } finally {
                // Libera memória do Bitmap SEMPRE (importante para maquininhas com pouca RAM)
                bitmap?.recycle()
                Log.d(TAG, "Memória do Bitmap liberada.")
            }
        }
        
        /**
         * Imprime texto simples
         * 
         * @param text Texto a ser impresso
         * @param options Opções de impressão
         * @param promise Promise para retornar resultado
         */
        AsyncFunction("printText") { text: String, options: Map<String, Any>, promise: Promise ->
            try {
                Log.d(TAG, "Imprimindo texto: $text")
                
                val paperWidth = (options["paperWidth"] as? Int) ?: 58
                val encoding = (options["encoding"] as? String) ?: "ISO-8859-1"
                val dpi = (options["dpi"] as? Int) ?: DEFAULT_DPI
                
                val charset = when(encoding.uppercase()) {
                    "UTF-8" -> Charsets.UTF_8
                    "CP850" -> Charset.forName("CP850")
                    else -> Charsets.ISO_8859_1
                }
                
                // Usa printWithRetry para auto-recover de Doze Mode
                printWithRetry(paperWidth, dpi) { printer ->
                    printer.printFormattedText(text)
                    Log.d(TAG, "Texto impresso com sucesso")
                }
                
                promise.resolve(
                    mapOf(
                        "success" to true,
                        "message" to "Texto impresso com sucesso"
                    )
                )
                
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao imprimir texto: ${e.message}", e)
                promise.reject("PRINT_ERROR", e.message ?: "Erro ao imprimir texto", e)
            }
        }
        
        /**
         * Retorna lista de impressoras (Bluetooth e USB)
         * 
         * @param promise Promise para retornar lista de dispositivos
         */
        AsyncFunction("getPairedPrinters") { promise: Promise ->
            try {
                Log.d(TAG, "Buscando impressoras disponíveis...")
                val printersList = mutableListOf<Map<String, String>>()
                
                // 1. Buscar impressoras Bluetooth
                try {
                    Log.d(TAG, "Verificando impressoras Bluetooth...")
                    
                    // Verificar se Bluetooth está habilitado
                    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                    if (bluetoothAdapter == null) {
                        Log.w(TAG, "⚠️ Dispositivo não possui Bluetooth")
                    } else if (!bluetoothAdapter.isEnabled) {
                        Log.w(TAG, "⚠️ Bluetooth está DESLIGADO. Peça ao usuário para ligar.")
                    } else {
                        Log.d(TAG, "✓ Bluetooth está LIGADO")
                        val bondedDevices = bluetoothAdapter.bondedDevices
                        Log.d(TAG, "Dispositivos pareados: ${bondedDevices?.size ?: 0}")
                        
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
            try {
                Log.d(TAG, "=== TESTE DE IMPRESSÃO COM IMAGEM HARDCODED ===")
                Log.d(TAG, "Usando imagem Base64 interna do Kotlin")
                Log.d(TAG, "Tamanho do Base64: ${TEST_IMAGE_BASE64.length}")
                
                val printer = getOrCreatePrinter(paperWidth, dpi)
                
                if (printer == null) {
                    promise.reject("NO_PRINTER", "Nenhuma impressora disponível", null)
                    return@AsyncFunction
                }
                
                Log.d(TAG, "Decodificando Base64 interno...")
                val decodedBytes = Base64.decode(TEST_IMAGE_BASE64, Base64.DEFAULT)
                Log.d(TAG, "Bytes decodificados: ${decodedBytes.size}")
                
                val originalBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                
                if (originalBitmap == null) {
                    promise.reject("DECODE_ERROR", "Falha ao decodificar imagem Base64 interna", null)
                    return@AsyncFunction
                }
                
                Log.d(TAG, "Bitmap decodificado: ${originalBitmap.width}x${originalBitmap.height}")
                
                val processedBitmap = if (applyDithering) {
                    Log.d(TAG, "Aplicando dithering Floyd-Steinberg...")
                    val resized = ImageUtils.resizeForPrinter(originalBitmap, paperWidth, dpi)
                    val dithered = ImageUtils.applyFloydSteinbergDithering(resized)
                    resized.recycle()
                    dithered
                } else {
                    ImageUtils.resizeForPrinter(originalBitmap, paperWidth, dpi)
                }
                
                originalBitmap.recycle()
                
                Log.d(TAG, "Convertendo para comandos ESC/POS...")
                val imageHex = PrinterTextParserImg.bitmapToHexadecimalString(printer, processedBitmap)
                
                Log.d(TAG, "Imprimindo imagem de teste...")
                printer.printFormattedText(
                    "[C]<b>TESTE DE IMAGEM HARDCODED</b>\n" +
                    "[C]Imagem Base64 interna do Kotlin\n" +
                    "[C]<img>$imageHex</img>\n" +
                    "[C]Se isso imprimiu, o Kotlin está OK!\n" +
                    "[C]O problema está na comunicação RN→Kotlin\n\n\n"
                )
                
                processedBitmap.recycle()
                
                Log.d(TAG, "✅ Imagem de teste impressa com sucesso!")
                
                promise.resolve(
                    mapOf(
                        "success" to true,
                        "message" to "Imagem de teste hardcoded impressa com sucesso!"
                    )
                )
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erro ao imprimir imagem de teste: ${e.message}", e)
                promise.reject("PRINT_TEST_ERROR", e.message ?: "Erro ao imprimir imagem de teste", e)
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
            // Se já temos a impressora pronta e conectada, apenas devolve ela!
            if (currentPrinter != null) {
                Log.d(TAG, "Reutilizando impressora já conectada")
                return currentPrinter
            }
            
            Log.d(TAG, "Nenhuma impressora ativa. Tentando auto-detectar...")
            val connection = findAnyAvailablePrinter()
            
            if (connection == null) {
                Log.w(TAG, "Auto-detect falhou.")
                return null
            }
            
            // Se achou no auto-detect, TEM QUE CONECTAR O SOCKET AQUI TAMBÉM
            try {
                connection.connect()
                Log.d(TAG, "Socket auto-detect aberto com sucesso!")
            } catch (e: Exception) {
                Log.e(TAG, "Falha ao abrir porta no auto-detect", e)
                return null
            }
            
            // IMPORTANTE: O 4º parâmetro é CARACTERES por linha, não pixels!
            // 58mm = 32 caracteres, 80mm = 48 caracteres
            val widthChars = if (paperWidth == 58) 32 else 48
            val printer = EscPosPrinter(connection, dpi, DEFAULT_WIDTH_MM, widthChars)
            
            currentConnection = connection
            currentPrinter = printer
            
            return printer
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao resgatar printer: ${e.message}", e)
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
     * Executa impressão com retry automático para recuperar de Doze Mode
     * Android 7 fecha sockets Bluetooth inativos, causando Broken Pipe
     * 
     * @param paperWidth Largura do papel (58 ou 80mm)
     * @param dpi DPI da impressora
     * @param printAction Ação de impressão a executar
     */
    private fun printWithRetry(paperWidth: Int, dpi: Int, printAction: (EscPosPrinter) -> Unit) {
        var attempts = 0
        var lastException: Exception? = null
        
        while (attempts < MAX_RETRY_ATTEMPTS) {
            try {
                val printer = getOrCreatePrinter(paperWidth, dpi)
                
                if (printer == null) {
                    throw Exception("Nenhuma impressora conectada")
                }
                
                printAction(printer)
                return // Sucesso!
                
            } catch (e: IOException) {
                lastException = e
                val isBrokenPipe = e.message?.contains("Broken pipe", ignoreCase = true) == true ||
                                   e.message?.contains("Socket closed", ignoreCase = true) == true
                
                if (isBrokenPipe && attempts < MAX_RETRY_ATTEMPTS - 1) {
                    Log.w(TAG, "Socket fechado pelo Doze Mode. Reconectando... (tentativa ${attempts + 1}/$MAX_RETRY_ATTEMPTS)")
                    
                    // Força reconexão
                    try {
                        currentConnection?.disconnect()
                    } catch (disconnectError: Exception) {
                        Log.w(TAG, "Erro ao desconectar: ${disconnectError.message}")
                    }
                    
                    currentConnection = null
                    currentPrinter = null
                    
                    // Aguarda 500ms antes de reconectar
                    Thread.sleep(500)
                    
                    attempts++
                } else {
                    throw e // Erro não recuperável ou tentativas esgotadas
                }
            } catch (e: Exception) {
                lastException = e
                throw e // Outros erros não são recuperáveis
            }
        }
        
        // Se chegou aqui, esgotou tentativas
        throw lastException ?: Exception("Falha após $MAX_RETRY_ATTEMPTS tentativas")
    }
}
