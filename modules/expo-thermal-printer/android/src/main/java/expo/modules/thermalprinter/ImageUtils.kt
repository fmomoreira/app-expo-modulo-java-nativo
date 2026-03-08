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
     * 
     * @param source Bitmap original (colorido ou tons de cinza)
     * @return Bitmap processado em preto e branco puro
     */
    fun applyFloydSteinbergDithering(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height
        
        // Cria uma cópia mutável da imagem
        val bitmap = source.copy(Bitmap.Config.ARGB_8888, true)
        
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
     * 
     * @param source Bitmap original
     * @param paperWidth Largura do papel em mm (58 ou 80)
     * @return Bitmap redimensionado
     */
    fun resizeForPrinter(source: Bitmap, paperWidth: Int): Bitmap {
        // 58mm = 384 pixels @ 203 DPI
        // 80mm = 576 pixels @ 203 DPI
        val targetWidth = if (paperWidth == 58) 384 else 576
        
        // Mantém a proporção da imagem
        val aspectRatio = source.height.toFloat() / source.width.toFloat()
        val targetHeight = (targetWidth * aspectRatio).toInt()
        
        return Bitmap.createScaledBitmap(source, targetWidth, targetHeight, true)
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
}
