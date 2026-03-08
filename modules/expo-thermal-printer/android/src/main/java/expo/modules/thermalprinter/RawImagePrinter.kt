package expo.modules.thermalprinter

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import com.dantsu.escposprinter.connection.DeviceConnection
import com.dantsu.escposprinter.exceptions.EscPosConnectionException

/**
 * Motor de Impressão de Imagem de Alta Fidelidade
 * 
 * Implementa o padrão RawBT para impressão térmica usando comandos ESC/POS brutos.
 * Ignora o parser da biblioteca DantSu para preservar 100% da qualidade do dithering.
 * 
 * Baseado no comando oficial Epson: GS v 0 (Rasterização de Bitmap)
 * 
 * @see <a href="https://reference.epson-biz.com/modules/ref_escpos/index.php?content_id=94">Epson ESC/POS Reference</a>
 */
object RawImagePrinter {
    
    private const val TAG = "RawImagePrinter"
    
    /**
     * Comandos ESC/POS para alinhamento de texto/imagem
     */
    object Alignment {
        /** ESC a 0 - Alinhamento à esquerda */
        val LEFT = byteArrayOf(0x1B.toByte(), 0x61.toByte(), 0x00.toByte())
        
        /** ESC a 1 - Alinhamento centralizado */
        val CENTER = byteArrayOf(0x1B.toByte(), 0x61.toByte(), 0x01.toByte())
        
        /** ESC a 2 - Alinhamento à direita */
        val RIGHT = byteArrayOf(0x1B.toByte(), 0x61.toByte(), 0x02.toByte())
    }
    
    /**
     * Converte um Bitmap em comandos ESC/POS brutos usando o comando GS v 0.
     * 
     * Este método implementa a rasterização bit-a-bit, onde cada pixel preto
     * do bitmap é convertido em um bit ligado (1) no array de bytes ESC/POS.
     * 
     * Formato do comando GS v 0:
     * - 0x1D (GS)
     * - 0x76 (v)
     * - 0x30 (0) - Modo normal
     * - 0x00 - Parâmetro m (normal)
     * - xL, xH - Largura em bytes (Little Endian)
     * - yL, yH - Altura em pixels (Little Endian)
     * - [dados] - Array de bits representando a imagem
     * 
     * @param bitmap Bitmap já processado com dithering Floyd-Steinberg
     * @return ByteArray contendo o comando GS v 0 completo + dados da imagem
     */
    fun bitmapToRawBytes(bitmap: Bitmap): ByteArray {
        val width = bitmap.width
        val height = bitmap.height
        
        // Calcula quantos bytes são necessários para cada linha
        // Cada byte armazena 8 pixels (1 bit por pixel)
        val xBytes = (width + 7) / 8
        
        Log.d(TAG, "Convertendo bitmap ${width}x${height} para raw bytes")
        Log.d(TAG, "Bytes por linha: $xBytes, Total de bytes: ${xBytes * height}")
        
        // Monta o cabeçalho do comando GS v 0
        val command = byteArrayOf(
            0x1D.toByte(), // GS
            0x76.toByte(), // v
            0x30.toByte(), // 0 (modo normal)
            0x00.toByte(), // m (parâmetro)
            (xBytes and 0xFF).toByte(),        // xL (Low byte da largura)
            ((xBytes shr 8) and 0xFF).toByte(), // xH (High byte da largura)
            (height and 0xFF).toByte(),         // yL (Low byte da altura)
            ((height shr 8) and 0xFF).toByte()  // yH (High byte da altura)
        )
        
        // Array para armazenar os dados da imagem
        val imageData = ByteArray(xBytes * height)
        
        // Converte cada pixel do bitmap em bits
        // Otimização: processa linha por linha para melhor cache locality
        for (y in 0 until height) {
            val lineOffset = y * xBytes
            
            for (x in 0 until width) {
                val pixel = bitmap.getPixel(x, y)
                
                // Determina se o pixel é preto
                // Pixel é preto somente se for OPACO (alpha >= 128) E ESCURO (luminância < 128)
                // Pixels transparentes são tratados como BRANCO (cor do papel)
                val alpha = Color.alpha(pixel)
                val isBlack = alpha >= 128 && Color.red(pixel) < 128
                
                if (isBlack) {
                    // Calcula a posição do bit dentro do byte
                    val byteIndex = lineOffset + (x / 8)
                    val bitOffset = 7 - (x % 8) // MSB primeiro
                    
                    // Liga o bit correspondente usando OR bitwise
                    imageData[byteIndex] = (imageData[byteIndex].toInt() or (1 shl bitOffset)).toByte()
                }
            }
        }
        
        // Combina comando + dados em um único array
        return command + imageData
    }
    
    /**
     * Imprime um bitmap diretamente na impressora usando raw bytes.
     * 
     * Este método:
     * 1. Converte o bitmap para comandos ESC/POS brutos
     * 2. Centraliza a imagem
     * 3. Envia os bytes diretamente para a conexão Bluetooth/USB
     * 4. Restaura o alinhamento padrão
     * 5. Adiciona espaçamento após a imagem
     * 
     * @param connection Conexão ativa com a impressora
     * @param bitmap Bitmap processado (com dithering)
     * @param centered Se true, centraliza a imagem (padrão: true)
     * @param feedLines Número de linhas em branco após a imagem (padrão: 3)
     * @throws Exception Se houver erro na comunicação com a impressora
     */
    fun printBitmapDirectly(
        connection: DeviceConnection,
        bitmap: Bitmap,
        centered: Boolean = true,
        feedLines: Int = 3
    ) {
        Log.d(TAG, "=== INICIANDO IMPRESSÃO RAW ===")
        Log.d(TAG, "Bitmap: ${bitmap.width}x${bitmap.height}, Centralizado: $centered")
        
        try {
            // 1. Centraliza se solicitado
            if (centered) {
                Log.d(TAG, "Enviando comando de centralização (ESC a 1)")
                connection.write(Alignment.CENTER)
            }
            
            // 2. Converte bitmap para raw bytes
            val rawBytes = bitmapToRawBytes(bitmap)
            Log.d(TAG, "Comando GS v 0 gerado: ${rawBytes.size} bytes totais")
            
            // 3. Envia os bytes brutos para a impressora
            Log.d(TAG, "Enviando dados para impressora...")
            connection.write(rawBytes)
            
            // 4. Restaura alinhamento à esquerda
            if (centered) {
                Log.d(TAG, "Restaurando alinhamento à esquerda (ESC a 0)")
                connection.write(Alignment.LEFT)
            }
            
            // 5. Adiciona espaçamento (line feed)
            if (feedLines > 0) {
                val lineFeed = "\n".repeat(feedLines).toByteArray(Charsets.US_ASCII)
                connection.write(lineFeed)
                Log.d(TAG, "Adicionado $feedLines linhas de espaçamento")
            }
            
            // 6. FLUSH: Envia TUDO que está no buffer para a impressora
            // CRÍTICO: Sem isso, os dados ficam presos no buffer interno da DantSu
            Log.d(TAG, "Flush: enviando buffer para impressora...")
            connection.send()
            
            Log.d(TAG, "✅ Impressão raw concluída com sucesso!")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao imprimir bitmap raw: ${e.message}", e)
            throw e
        }
    }
    
    /**
     * Imprime múltiplas fatias de imagem (para imagens grandes).
     * 
     * Útil quando a imagem é muito alta e precisa ser dividida para evitar
     * buffer overflow na impressora.
     * 
     * @param connection Conexão ativa com a impressora
     * @param slices Lista de bitmaps (fatias da imagem original)
     * @param centered Se true, centraliza cada fatia
     * @param feedLinesBetweenSlices Linhas em branco entre fatias (padrão: 0)
     * @param feedLinesAfterLast Linhas em branco após última fatia (padrão: 3)
     */
    fun printBitmapSlices(
        connection: DeviceConnection,
        slices: List<Bitmap>,
        centered: Boolean = true,
        feedLinesBetweenSlices: Int = 0,
        feedLinesAfterLast: Int = 3
    ) {
        Log.d(TAG, "=== IMPRESSÃO DE ${slices.size} FATIAS ===")
        
        slices.forEachIndexed { index, slice ->
            val isLast = index == slices.size - 1
            val feedLines = if (isLast) feedLinesAfterLast else feedLinesBetweenSlices
            
            Log.d(TAG, "Imprimindo fatia ${index + 1}/${slices.size} (${slice.width}x${slice.height})")
            
            printBitmapDirectly(
                connection = connection,
                bitmap = slice,
                centered = centered,
                feedLines = feedLines
            )
        }
        
        Log.d(TAG, "✅ Todas as fatias impressas com sucesso!")
    }
    
    /**
     * Calcula o tamanho estimado em bytes que a imagem ocupará na impressora.
     * Útil para debug e otimização.
     * 
     * @param bitmap Bitmap a ser analisado
     * @return Tamanho total em bytes (comando + dados)
     */
    fun estimateRawBytesSize(bitmap: Bitmap): Int {
        val xBytes = (bitmap.width + 7) / 8
        val commandSize = 8 // Tamanho fixo do cabeçalho GS v 0
        val dataSize = xBytes * bitmap.height
        return commandSize + dataSize
    }
}
