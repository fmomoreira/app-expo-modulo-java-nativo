package expo.modules.thermalprinter

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import com.dantsu.escposprinter.connection.DeviceConnection
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
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
        try {
            Log.d(TAG, "Gerando QR Code BITMAP para: $data")
            
            val qrSize = 200
            val hints = mapOf(
                EncodeHintType.MARGIN to 1,
                EncodeHintType.CHARACTER_SET to "UTF-8"
            )
            
            val bitMatrix = QRCodeWriter().encode(data, BarcodeFormat.QR_CODE, qrSize, qrSize, hints)
            val width = bitMatrix.width
            val height = bitMatrix.height
            
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            for (y in 0 until height) {
                for (x in 0 until width) {
                    bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }
            
            val rawBytes = RawImagePrinter.bitmapToRawBytes(bitmap)
            bitmap.recycle()
            
            Log.d(TAG, "✓ QR Code bitmap gerado: ${width}x${height}px, ${rawBytes.size} bytes")
            return rawBytes
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao gerar QR code bitmap: ${e.message}", e)
            return ByteArray(0)
        }
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
