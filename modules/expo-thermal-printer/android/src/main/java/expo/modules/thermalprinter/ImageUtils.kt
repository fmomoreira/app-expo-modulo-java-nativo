package expo.modules.thermalprinter

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.max
import kotlin.math.min

/**
 * Utilitários para processamento de imagem para impressão térmica
 * Implementa o algoritmo Floyd-Steinberg para dithering de alta qualidade
 */
object ImageUtils {

    /**
     * Aplica o algoritmo de dithering Floyd-Steinberg para converter imagem colorida
     * em preto e branco puro (1-bit) com qualidade profissional
     * 
     * Este é o mesmo algoritmo usado pelo RawBT para garantir nitidez em impressoras térmicas
     * OTIMIZAÇÃO: Libera memória do bitmap original após criar cópia
     * 
     * @param source Bitmap original (colorido ou tons de cinza)
     * @return Bitmap processado em preto e branco puro
     */
    fun applyFloydSteinbergDithering(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height
        
        // Cria uma cópia mutável da imagem. Precisamos disso porque vamos
        // alterar pixel por pixel e a imagem original pode estar "trancada" na memória.
        val bitmap = source.copy(Bitmap.Config.ARGB_8888, true)
        
        // Libera a original, pois só vamos trabalhar na cópia a partir de agora
        // CRÍTICO para maquininhas com pouca RAM (Moderninha: 1-2GB)
        if (bitmap != source) {
            source.recycle()
        }
        
        // Array para armazenar valores de luminância (mais eficiente que acessar pixels repetidamente)
        val luminanceArray = Array(height) { IntArray(width) }
        
        // Primeira passagem: converte RGB para luminância
        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = bitmap.getPixel(x, y)
                luminanceArray[y][x] = calculateLuminance(pixel)
            }
        }
        
        // Segunda passagem: aplica Floyd-Steinberg dithering
        for (y in 0 until height) {
            for (x in 0 until width) {
                val oldPixel = luminanceArray[y][x]
                
                // Quantização: decide se o pixel será preto (0) ou branco (255)
                // Limiar de 128 (meio do caminho entre 0 e 255)
                val newPixel = if (oldPixel < 128) 0 else 255
                
                // Calcula o erro de quantização
                val error = oldPixel - newPixel
                
                // Aplica o novo valor ao bitmap
                val color = if (newPixel == 0) Color.BLACK else Color.WHITE
                bitmap.setPixel(x, y, color)
                
                // Distribui o erro para os pixels vizinhos (padrão Floyd-Steinberg)
                // Pixel à direita: 7/16 do erro
                if (x + 1 < width) {
                    luminanceArray[y][x + 1] = clamp(luminanceArray[y][x + 1] + (error * 7 / 16))
                }
                
                // Próxima linha, pixel à esquerda: 3/16 do erro
                if (x > 0 && y + 1 < height) {
                    luminanceArray[y + 1][x - 1] = clamp(luminanceArray[y + 1][x - 1] + (error * 3 / 16))
                }
                
                // Próxima linha, mesmo pixel: 5/16 do erro
                if (y + 1 < height) {
                    luminanceArray[y + 1][x] = clamp(luminanceArray[y + 1][x] + (error * 5 / 16))
                }
                
                // Próxima linha, pixel à direita: 1/16 do erro
                if (x + 1 < width && y + 1 < height) {
                    luminanceArray[y + 1][x + 1] = clamp(luminanceArray[y + 1][x + 1] + (error * 1 / 16))
                }
            }
        }
        
        return bitmap
    }
    
    /**
     * Calcula a luminância de um pixel RGB usando pesos perceptuais
     * 
     * Fórmula: L = 0.299*R + 0.587*G + 0.114*B
     * 
     * Os pesos refletem a sensibilidade do olho humano:
     * - Verde (58.7%): olhos são mais sensíveis ao verde
     * - Vermelho (29.9%): sensibilidade média
     * - Azul (11.4%): olhos são menos sensíveis ao azul
     * 
     * @param pixel Valor do pixel em formato ARGB
     * @return Valor de luminância (0-255)
     */
    private fun calculateLuminance(pixel: Int): Int {
        val r = Color.red(pixel)
        val g = Color.green(pixel)
        val b = Color.blue(pixel)
        
        return (0.299 * r + 0.587 * g + 0.114 * b).toInt()
    }
    
    /**
     * Garante que o valor fique no intervalo [0, 255]
     * 
     * @param value Valor a ser limitado
     * @return Valor limitado entre 0 e 255
     */
    private fun clamp(value: Int): Int {
        return max(0, min(255, value))
    }
    
    /**
     * Redimensiona a imagem para a largura da bobina térmica
     * OTIMIZAÇÃO: Libera memória do bitmap original para evitar OutOfMemory em maquininhas
     * 
     * @param source Bitmap original
     * @param paperWidth Largura do papel em mm (58 ou 80)
     * @return Bitmap redimensionado
     */
    fun resizeForPrinter(source: Bitmap, paperWidth: Int): Bitmap {
        // 58mm = 384 pixels @ 203 DPI
        // 80mm = 576 pixels @ 203 DPI
        val targetWidth = if (paperWidth == 58) 384 else 576
        
        // OTIMIZAÇÃO: Se a imagem já for menor ou igual à bobina, não faz nada para poupar memória
        if (source.width <= targetWidth) {
            return source
        }
        
        // Mantém a proporção da imagem
        val aspectRatio = source.height.toFloat() / source.width.toFloat()
        val targetHeight = (targetWidth * aspectRatio).toInt()
        
        val resizedBitmap = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, true)
        
        // DICA DE OURO PARA MEMÓRIA: Se a imagem nova é diferente da velha,
        // destruímos a velha para liberar RAM da maquininha (importante para Moderninha com 1-2GB RAM)
        if (resizedBitmap != source) {
            source.recycle()
        }
        
        return resizedBitmap
    }
    
    /**
     * Converte imagem para tons de cinza (sem dithering)
     * Útil para pré-processamento antes do dithering
     * 
     * @param source Bitmap original
     * @return Bitmap em tons de cinza
     */
    fun toGrayscale(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height
        val bitmap = source.copy(Bitmap.Config.ARGB_8888, true)
        
        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = bitmap.getPixel(x, y)
                val gray = calculateLuminance(pixel)
                val grayColor = Color.rgb(gray, gray, gray)
                bitmap.setPixel(x, y, grayColor)
            }
        }
        
        return bitmap
    }
    
    /**
     * Aplica threshold simples (binarização) sem dithering
     * IDEAL PARA: QR Codes, códigos de barras, textos puros
     * 
     * Diferente do Floyd-Steinberg, este método mantém bordas nítidas e não
     * cria padrões de pontilhado. Perfeito para conteúdo que precisa ser
     * escaneado por leitores laser ou câmeras de celular.
     * 
     * @param source Bitmap original
     * @param threshold Limiar de binarização (0-255). Padrão: 128
     * @return Bitmap em preto e branco puro com bordas nítidas
     */
    fun applyThreshold(source: Bitmap, threshold: Int = 128): Bitmap {
        val width = source.width
        val height = source.height
        
        // Cria cópia mutável
        val bitmap = source.copy(Bitmap.Config.ARGB_8888, true)
        
        // Libera original para economizar RAM
        if (bitmap != source) {
            source.recycle()
        }
        
        // Aplica threshold simples: pixel < threshold = preto, senão branco
        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = bitmap.getPixel(x, y)
                val luminance = calculateLuminance(pixel)
                
                // Binarização simples sem distribuição de erro
                val color = if (luminance < threshold) Color.BLACK else Color.WHITE
                bitmap.setPixel(x, y, color)
            }
        }
        
        return bitmap
    }
    
    /**
     * Fatia imagem grande em pedaços menores para evitar buffer overflow
     * CRÍTICO: Impressoras térmicas têm buffer limitado (~64KB típico)
     * 
     * @param source Bitmap original
     * @param maxHeight Altura máxima de cada fatia em pixels (padrão: 400)
     * @return Lista de bitmaps fatiados
     */
    fun sliceImage(source: Bitmap, maxHeight: Int = 400): List<Bitmap> {
        val slices = mutableListOf<Bitmap>()
        val width = source.width
        val height = source.height
        
        // Se a imagem é pequena, retorna ela inteira
        if (height <= maxHeight) {
            return listOf(source)
        }
        
        // Calcula quantas fatias serão necessárias
        val numSlices = (height + maxHeight - 1) / maxHeight
        
        for (i in 0 until numSlices) {
            val startY = i * maxHeight
            val sliceHeight = min(maxHeight, height - startY)
            
            // Cria bitmap da fatia
            val slice = Bitmap.createBitmap(source, 0, startY, width, sliceHeight)
            slices.add(slice)
        }
        
        // Libera bitmap original após fatiar
        source.recycle()
        
        return slices
    }
}
