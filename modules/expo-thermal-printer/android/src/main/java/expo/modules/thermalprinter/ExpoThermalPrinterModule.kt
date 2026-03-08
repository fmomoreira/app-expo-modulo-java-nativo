package expo.modules.thermalprinter

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
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
    
    private var currentConnection: BluetoothConnection? = null
    
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
         * Retorna lista de impressoras Bluetooth pareadas
         * 
         * @param promise Promise para retornar lista de dispositivos
         */
        AsyncFunction("getPairedPrinters") { promise: Promise ->
            try {
                Log.d(TAG, "Buscando impressoras Bluetooth pareadas...")
                
                val connections = BluetoothPrintersConnections().list ?: emptyArray()
                
                val printers = connections.map { connection ->
                    val device = connection.device
                    mapOf(
                        "name" to (device.name ?: "Desconhecido"),
                        "address" to device.address,
                        "type" to "bluetooth"
                    )
                }
                
                Log.d(TAG, "Encontradas ${printers.size} impressoras pareadas")
                
                promise.resolve(printers)
                
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao buscar impressoras: ${e.message}", e)
                promise.reject("BLUETOOTH_ERROR", e.message ?: "Erro ao buscar impressoras", e)
            }
        }
        
        /**
         * Conecta a uma impressora específica
         * 
         * @param address Endereço Bluetooth da impressora
         * @param promise Promise para retornar resultado
         */
        AsyncFunction("connectPrinter") { address: String, promise: Promise ->
            try {
                Log.d(TAG, "Conectando à impressora: $address")
                
                val connections = BluetoothPrintersConnections().list ?: emptyArray()
                val connection = connections.find { it.device.address == address }
                
                if (connection == null) {
                    promise.reject("NOT_FOUND", "Impressora não encontrada: $address", null)
                    return@AsyncFunction
                }
                
                currentConnection = connection
                
                Log.d(TAG, "Conectado à impressora: ${connection.device.name}")
                
                promise.resolve(
                    mapOf(
                        "success" to true,
                        "message" to "Conectado à impressora ${connection.device.name}"
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
     * Reutiliza conexão existente ou seleciona primeira impressora pareada
     */
    private fun getOrCreatePrinter(paperWidth: Int, dpi: Int): EscPosPrinter? {
        try {
            val connection = currentConnection ?: run {
                Log.d(TAG, "Buscando primeira impressora pareada...")
                val connections = BluetoothPrintersConnections().list
                
                if (connections.isNullOrEmpty()) {
                    Log.w(TAG, "Nenhuma impressora Bluetooth pareada")
                    return null
                }
                
                // Prioriza impressoras com nomes conhecidos (Moderninha)
                val preferredConnection = connections.find { 
                    val name = it.device.name?.lowercase() ?: ""
                    name.contains("innerprinter") || 
                    name.contains("mpos") || 
                    name.contains("moderninha") ||
                    name.contains("printer")
                } ?: connections.first()
                
                Log.d(TAG, "Selecionada impressora: ${preferredConnection.device.name}")
                currentConnection = preferredConnection
                preferredConnection
            }
            
            // Calcula largura em pixels (203 DPI)
            val widthPixels = if (paperWidth == 58) 384 else 576
            
            return EscPosPrinter(connection, dpi, DEFAULT_WIDTH_MM, widthPixels)
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao criar printer: ${e.message}", e)
            return null
        }
    }
}
