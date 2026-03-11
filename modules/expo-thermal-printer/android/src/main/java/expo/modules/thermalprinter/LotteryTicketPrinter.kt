package expo.modules.thermalprinter

import android.util.Log
import com.dantsu.escposprinter.connection.DeviceConnection
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

object LotteryTicketPrinter {
    private const val TAG = "LotteryTicketPrinter"
    
    object EscPosCommands {
        val INIT = byteArrayOf(0x1B, 0x40)
        val ALIGN_CENTER = byteArrayOf(0x1B, 0x61, 0x01)
        val ALIGN_LEFT = byteArrayOf(0x1B, 0x61, 0x00)
        val BOLD_ON = byteArrayOf(0x1B, 0x45, 0x01)
        val BOLD_OFF = byteArrayOf(0x1B, 0x45, 0x00)
        val TEXT_X2 = byteArrayOf(0x1D, 0x21, 0x11)
        val TEXT_NORMAL = byteArrayOf(0x1D, 0x21, 0x00)
        val LINE_DASH = "--------------------------------\n".toByteArray(Charsets.ISO_8859_1)
        
        val QR_MODEL = byteArrayOf(0x1D, 0x28, 0x6B, 0x04, 0x00, 0x31, 0x41, 0x32, 0x00)
        val QR_SIZE_MEDIUM = byteArrayOf(0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x43, 0x06)
        val QR_ERROR_CORR = byteArrayOf(0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x45, 0x31)
        val QR_PRINT = byteArrayOf(0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x51, 0x30)
    }
    
    data class ExtraPrize(
        val titulo: String,
        val valor: String
    )
    
    data class Booklet(
        val bookletNumber: Int,
        val lotNumber: Int,
        val tickets: List<String>
    )
    
    data class TicketData(
        val id: String,
        val customerName: String,
        val customerPhone: String,
        val sellerName: String,
        val sellerPhone: String,
        val drawTitle: String,
        val drawDate: String,
        val mainPrizeValue: String,
        val extraPrizeValue: List<ExtraPrize>?,
        val createdAt: String,
        val booklets: List<Booklet>,
        val urlSorteio: String? = null
    )
    
    fun printAllBooklets(
        connection: DeviceConnection,
        ticketData: TicketData
    ) {
        try {
            Log.d(TAG, "=== INICIANDO IMPRESSÃO DE BILHETES REINO DA SORTE ===")
            Log.d(TAG, "Total de talões: ${ticketData.booklets.size}")
            
            ticketData.booklets.forEachIndexed { index, booklet ->
                Log.d(TAG, "Imprimindo talão ${index + 1}/${ticketData.booklets.size}")
                val bytes = generateTicketBytes(ticketData, booklet, index, ticketData.booklets.size)
                connection.write(bytes)
                Thread.sleep(300)
            }
            
            connection.send()
            
            val feedCmd = byteArrayOf(0x1B, 0x64, 0x04)
            connection.write(feedCmd)
            connection.send()
            
            Log.d(TAG, "✓ Todos os bilhetes impressos com sucesso!")
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao imprimir bilhetes: ${e.message}", e)
            throw e
        }
    }
    
    private fun generateTicketBytes(
        ticket: TicketData,
        booklet: Booklet,
        index: Int,
        total: Int
    ): ByteArray {
        val output = ByteArrayOutputStream()
        
        try {
            output.write(EscPosCommands.INIT)
            output.write(EscPosCommands.ALIGN_CENTER)
            output.write(EscPosCommands.TEXT_X2)
            output.write(EscPosCommands.BOLD_ON)
            output.write("REINO DA SORTE\n".toByteArray(Charsets.ISO_8859_1))
            
            output.write(EscPosCommands.TEXT_NORMAL)
            output.write("${ticket.drawTitle.uppercase()}\n".toByteArray(Charsets.ISO_8859_1))
            output.write("TALAO ${index + 1} DE $total\n".toByteArray(Charsets.ISO_8859_1))
            output.write(EscPosCommands.LINE_DASH)
            
            output.write(EscPosCommands.ALIGN_LEFT)
            output.write("Premio: ${formatCurrency(ticket.mainPrizeValue)}\n".toByteArray(Charsets.ISO_8859_1))
            
            ticket.extraPrizeValue?.forEach { extra ->
                output.write("${extra.titulo}: ${formatCurrency(extra.valor)}\n".toByteArray(Charsets.ISO_8859_1))
            }
            
            output.write("Sorteio: ${formatDate(ticket.drawDate)}\n".toByteArray(Charsets.ISO_8859_1))
            output.write("Venda: ${formatDate(ticket.createdAt)}\n".toByteArray(Charsets.ISO_8859_1))
            output.write(EscPosCommands.LINE_DASH)
            
            output.write("Cliente: ${ticket.customerName}\n".toByteArray(Charsets.ISO_8859_1))
            output.write("Tel: ${ticket.customerPhone}\n".toByteArray(Charsets.ISO_8859_1))
            output.write(EscPosCommands.LINE_DASH)
            
            output.write(EscPosCommands.ALIGN_CENTER)
            output.write(EscPosCommands.BOLD_ON)
            output.write("NUMEROS DA SORTE\n\n".toByteArray(Charsets.ISO_8859_1))
            
            output.write(EscPosCommands.TEXT_NORMAL)
            output.write(EscPosCommands.BOLD_ON)
            
            val tickets = booklet.tickets
            for (i in tickets.indices step 2) {
                val num1 = tickets[i].split("-")[0].chunked(1).joinToString(" ")
                val num2 = if (i + 1 < tickets.size) {
                    tickets[i + 1].split("-")[0].chunked(1).joinToString(" ")
                } else ""
                
                val line = "$num1    $num2".trimEnd() + "\n"
                output.write(line.toByteArray(Charsets.ISO_8859_1))
            }
            
            val bookletId = "${booklet.bookletNumber}-${booklet.lotNumber}".chunked(1).joinToString(" ")
            output.write("\nbilhete: $bookletId\n".toByteArray(Charsets.ISO_8859_1))
            
            output.write(EscPosCommands.BOLD_OFF)
            output.write(EscPosCommands.LINE_DASH)
            
            output.write(EscPosCommands.ALIGN_LEFT)
            output.write("Vendedor: ${ticket.sellerName}\n".toByteArray(Charsets.ISO_8859_1))
            output.write("Tel: ${ticket.sellerPhone}\n".toByteArray(Charsets.ISO_8859_1))
            output.write(EscPosCommands.LINE_DASH)
            
            output.write(EscPosCommands.ALIGN_CENTER)
            output.write("\nEscaneie para validar:\n\n".toByteArray(Charsets.ISO_8859_1))
            
            val qrData = ticket.urlSorteio ?: "https://reinodasorte.com.br/valida/${ticket.id}/${booklet.bookletNumber}"
            output.write(generateQrCodeBytes(qrData))
            output.write("\n".toByteArray(Charsets.ISO_8859_1))
            
            output.write("\n\nPRAZO P/ GANHADOR SE APRESENTAR\n".toByteArray(Charsets.ISO_8859_1))
            output.write("ATE AS 09H DO DIA SEGUINTE\n".toByteArray(Charsets.ISO_8859_1))
            output.write("ID VENDA: ${ticket.id}\n\n".toByteArray(Charsets.ISO_8859_1))
            
            output.write(byteArrayOf(0x1B, 0x64, 0x02))
            
            return output.toByteArray()
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao gerar bytes do bilhete: ${e.message}", e)
            throw e
        }
    }
    
    private fun generateQrCodeBytes(data: String): ByteArray {
        val baos = ByteArrayOutputStream()
        
        try {
            val qrData = data.toByteArray(Charsets.ISO_8859_1)
            val dataLen = qrData.size + 3
            val pL = (dataLen and 0xFF).toByte()
            val pH = ((dataLen shr 8) and 0xFF).toByte()
            
            Log.d(TAG, "Gerando QR Code para URL: $data")
            Log.d(TAG, "Tamanho da URL: ${data.length} caracteres, ${qrData.size} bytes")
            
            // 1. Selecionar modelo QR Code (Modelo 2)
            baos.write(byteArrayOf(0x1D, 0x28, 0x6B, 0x04, 0x00, 0x31, 0x41, 0x32, 0x00))
            
            // 2. Definir tamanho do módulo (6 = médio, ideal para 58mm com URLs longas)
            baos.write(byteArrayOf(0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x43, 0x06))
            
            // 3. Definir nível de correção de erro (0x31 = M, 15% de recuperação)
            baos.write(byteArrayOf(0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x45, 0x31))
            
            // 4. Armazenar dados no símbolo
            baos.write(byteArrayOf(0x1D, 0x28, 0x6B, pL, pH, 0x31, 0x50, 0x30))
            baos.write(qrData)
            
            // 5. Imprimir o símbolo QR Code
            baos.write(byteArrayOf(0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x51, 0x30))
            
            Log.d(TAG, "✓ QR Code gerado com sucesso (tamanho módulo: 6)")
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao gerar QR code: ${e.message}", e)
        }
        
        return baos.toByteArray()
    }
    
    private fun formatCurrency(value: String): String {
        return try {
            val numValue = value.toDoubleOrNull() ?: 0.0
            String.format(Locale("pt", "BR"), "R$ %.2f", numValue)
        } catch (e: Exception) {
            "R$ $value"
        }
    }
    
    private fun formatDate(isoDate: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(isoDate)
            
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            Log.w(TAG, "Erro ao formatar data: ${e.message}")
            isoDate
        }
    }
}
