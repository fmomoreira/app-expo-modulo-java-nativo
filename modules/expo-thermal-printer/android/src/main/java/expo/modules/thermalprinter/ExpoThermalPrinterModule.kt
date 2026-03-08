package expo.modules.thermalprinter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.usb.UsbManager
import android.util.Base64
import android.util.Log
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
    }
    
    private var currentConnection: DeviceConnection? = null
    
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
                var bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                
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
                
                // Conecta à impressora
                val printer = getOrCreatePrinter(paperWidth, dpi)
                
                if (printer == null) {
                    Log.e(TAG, "Nenhuma impressora disponível")
                    promise.reject("NO_PRINTER", "Nenhuma impressora Bluetooth pareada encontrada", null)
                    return@AsyncFunction
                }
                
                // Converte bitmap para comando ESC/POS e imprime
                Log.d(TAG, "Convertendo bitmap para comandos ESC/POS...")
                val imageHex = PrinterTextParserImg.bitmapToHexadecimalString(printer, bitmap)
                
                Log.d(TAG, "Enviando dados para impressora...")
                printer.printFormattedText(
                    "[C]<img>$imageHex</img>\n" +
                    "[L]\n" +
                    "[L]\n" +
                    "[L]\n"
                )
                
                Log.d(TAG, "Impressão concluída com sucesso!")
                
                promise.resolve(
                    mapOf(
                        "success" to true,
                        "message" to "Imagem impressa com sucesso"
                    )
                )
                
            } catch (e: Exception) {
                Log.e(TAG, "Erro durante impressão: ${e.message}", e)
                promise.reject("PRINT_ERROR", e.message ?: "Erro desconhecido durante impressão", e)
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
                
                val printer = getOrCreatePrinter(paperWidth, DEFAULT_DPI)
                
                if (printer == null) {
                    promise.reject("NO_PRINTER", "Nenhuma impressora disponível", null)
                    return@AsyncFunction
                }
                
                printer.printFormattedText(
                    "[C]<b>$text</b>\n" +
                    "[L]\n" +
                    "[L]\n"
                )
                
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
                    val bluetoothConnections = BluetoothPrintersConnections().list ?: emptyArray()
                    
                    bluetoothConnections.forEach { connection ->
                        val device = connection.device
                        printersList.add(mapOf(
                            "name" to (device.name ?: "Bluetooth Desconhecido"),
                            "address" to device.address,
                            "type" to "bluetooth"
                        ))
                    }
                    
                    Log.d(TAG, "Encontradas ${bluetoothConnections.size} impressoras Bluetooth")
                } catch (e: Exception) {
                    Log.w(TAG, "Erro ao buscar Bluetooth: ${e.message}")
                }
                
                // 2. Buscar impressoras USB (Moderninha Smart)
                try {
                    Log.d(TAG, "Verificando impressoras USB...")
                    val context = appContext.reactContext ?: throw Exception("Context não disponível")
                    val usbManager = context.getSystemService(Context.USB_SERVICE) as? UsbManager
                    
                    if (usbManager != null) {
                        val usbConnections = UsbPrintersConnections(usbManager).list ?: emptyArray()
                        
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
                        val usbConnections = UsbPrintersConnections(usbManager).list ?: emptyArray()
                        val deviceId = address.removePrefix("usb_").toIntOrNull()
                        
                        connection = usbConnections.find { it.device.deviceId == deviceId }
                        printerName = connection?.device?.deviceName ?: "USB Printer"
                    }
                } 
                // Bluetooth
                else {
                    Log.d(TAG, "Buscando impressora Bluetooth...")
                    val bluetoothConnections = BluetoothPrintersConnections().list ?: emptyArray()
                    connection = bluetoothConnections.find { it.device.address == address }
                    printerName = (connection as? BluetoothConnection)?.device?.name ?: "Bluetooth Printer"
                }
                
                if (connection == null) {
                    promise.reject("NOT_FOUND", "Impressora não encontrada: $address", null)
                    return@AsyncFunction
                }
                
                currentConnection = connection
                
                Log.d(TAG, "Conectado à impressora: $printerName")
                
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
         * Desconecta da impressora atual
         * 
         * @param promise Promise para retornar resultado
         */
        AsyncFunction("disconnectPrinter") { promise: Promise ->
            try {
                currentConnection?.disconnect()
                currentConnection = null
                
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
     * Obtém ou cria uma instância do printer
     * Reutiliza conexão existente ou busca automaticamente (Bluetooth → USB)
     */
    private fun getOrCreatePrinter(paperWidth: Int, dpi: Int): EscPosPrinter? {
        try {
            val connection = currentConnection ?: findAnyAvailablePrinter()
            
            if (connection == null) {
                Log.w(TAG, "Nenhuma impressora disponível")
                return null
            }
            
            // Calcula largura em pixels (203 DPI)
            val widthPixels = if (paperWidth == 58) 384 else 576
            
            return EscPosPrinter(connection, dpi, DEFAULT_WIDTH_MM, widthPixels)
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao criar printer: ${e.message}", e)
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
            val bluetoothConnections = BluetoothPrintersConnections().list
            
            if (!bluetoothConnections.isNullOrEmpty()) {
                // Prioriza impressoras com nomes conhecidos
                val preferredBluetooth = bluetoothConnections.find { 
                    val name = it.device.name?.lowercase() ?: ""
                    name.contains("innerprinter") || 
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
                    val usbConnections = UsbPrintersConnections(usbManager).list
                    
                    if (!usbConnections.isNullOrEmpty()) {
                        val usbPrinter = usbConnections.first()
                        Log.d(TAG, "✓ USB encontrado: ${usbPrinter.device.deviceName}")
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
}
