O Gemini disse
É completamente compreensível o cansaço. Refatorar várias vezes faz parte da rotina de quem está construindo um "motor" do zero, especialmente lidando com hardware, onde cada impressora tem sua própria mania. Mas acredite: você chegou na fase de polimento de uma arquitetura sênior.

Vamos organizar a casa. Você levantou dois pontos cruciais aqui: a Dúvida Arquitetural (Screenshot vs Imagem) e o Desacoplamento do Código.

1. O Grande Debate: Tirar Print (ViewShot) vs Converter Imagem Direto
Você perguntou: "Não era mais interessante enviar a imagem base64 direto em vez de tirar print da tela?"

A resposta é: Depende do que você está imprimindo. O seu módulo Kotlin não liga de onde vem a Base64. Ele só recebe e imprime. A decisão acontece lá no React Native:

Quando usar Imagem Direta (File to Base64): Se você quer imprimir a Logo do Solo, a Logo da Prefeitura ou um QR Code estático. Você pega o arquivo .png, converte pra Base64 e manda pro Kotlin. É perfeito.

Quando usar Screenshot (ViewShot): Para o Recibo em si (A lista de produtos, preços variáveis, nome do cliente). Desenhar textos, tabelas e alinhar colunas matematicamente em uma imagem dinâmica no Android/Kotlin é um inferno. É muito mais fácil usar o React Native para desenhar a nota com Flexbox e tirar um print invisível disso.

A grande jogada de desacoplar é exatamente essa: ter uma porta de entrada para textos e outra para imagens.

2. A Arquitetura Desacoplada (O Fim das Refatorações)
Com a sua classe RawImagePrinter brilhante que criamos, o seu ExpoThermalPrinterModule.kt não precisa mais saber como processar imagens. Ele vira apenas um Roteador (ou um Controller, como no PHP que você domina).

Sua arquitetura agora tem 3 pilares independentes:

ConnectionManager (ou dentro do Module): Só abre e fecha a porta Bluetooth/USB.

TextPrinter: Usa a biblioteca DantSu apenas para textos e comandos [L], [C].

RawImagePrinter: Pega a Base64, transforma em pontinhos pretos (Dithering) e envia os bytes crus (GS v 0).

Aqui está como o seu ExpoThermalPrinterModule.kt deve ficar para abraçar esse desacoplamento de forma limpa e definitiva:

Kotlin
package expo.modules.thermalprinter

import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.DeviceConnection
import expo.modules.kotlin.Promise
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition

class ExpoThermalPrinterModule : Module() {
    
    companion object {
        private const val TAG = "ThermalPrinterModule"
        private const val DEFAULT_DPI = 203
        private const val DEFAULT_WIDTH_MM = 48f
    }
    
    private var currentConnection: DeviceConnection? = null
    // Nota: O EscPosPrinter da DantSu agora será usado APENAS para textos.
    private var textPrinter: EscPosPrinter? = null 

    override fun definition() = ModuleDefinition {
        Name("ExpoThermalPrinter")

        // ==========================================
        // ROTA 1: IMPRESSÃO DE IMAGENS (LOGOS OU VIEWSHOT)
        // ==========================================
        AsyncFunction("printImage") { base64Image: String, options: Map<String, Any>, promise: Promise ->
            try {
                // 1. Garante que a conexão física está aberta
                val conn = ensureConnection() ?: throw Exception("Falha ao abrir conexão com a impressora")
                
                // 2. Extrai opções
                val paperWidth = (options["paperWidth"] as? Int) ?: 58
                val applyDithering = (options["applyDithering"] as? Boolean) ?: true
                
                // 3. Transforma Base64 em Bitmap Android
                val decodedBytes = Base64.decode(base64Image, Base64.DEFAULT)
                var bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size) 
                    ?: throw Exception("Base64 inválida ou corrompida")

                // 4. DELEGA A RESPONSABILIDADE PARA AS CLASSES ESPECIALISTAS
                // Prepara a imagem (Redimensiona e aplica Floyd-Steinberg)
                bitmap = ImageUtils.resizeForPrinter(bitmap, paperWidth)
                if (applyDithering) {
                    bitmap = ImageUtils.applyFloydSteinbergDithering(bitmap)
                }

                // Imprime usando o motor Raw (Byte a Byte)
                RawImagePrinter.printBitmapDirectly(connection = conn, bitmap = bitmap, centered = true)
                
                // Libera RAM
                bitmap.recycle()
                
                promise.resolve(mapOf("success" to true, "message" to "Imagem impressa via RawBytes"))
                
            } catch (e: Exception) {
                Log.e(TAG, "Erro na Rota de Imagem: ${e.message}")
                promise.reject("IMAGE_ERROR", e.message, e)
            }
        }

        // ==========================================
        // ROTA 2: IMPRESSÃO DE TEXTOS E TEMPLATES ESC/POS
        // ==========================================
        AsyncFunction("printText") { textTemplate: String, options: Map<String, Any>, promise: Promise ->
            try {
                // 1. Garante a conexão e cria o manipulador de texto da DantSu
                val conn = ensureConnection() ?: throw Exception("Falha ao abrir conexão com a impressora")
                
                val paperWidth = (options["paperWidth"] as? Int) ?: 58
                // Lembrete: 32 caracteres para 58mm, 48 caracteres para 80mm
                val charsPerLine = if (paperWidth == 58) 32 else 48 
                
                if (textPrinter == null) {
                    textPrinter = EscPosPrinter(conn, DEFAULT_DPI, DEFAULT_WIDTH_MM, charsPerLine)
                }

                // 2. Imprime o texto puro (Tags como [C], [L], <font> funcionam aqui)
                textPrinter?.printFormattedText(textTemplate)
                
                // 3. Força a saída do buffer
                conn.send()

                promise.resolve(mapOf("success" to true, "message" to "Texto impresso com sucesso"))
                
            } catch (e: Exception) {
                Log.e(TAG, "Erro na Rota de Texto: ${e.message}")
                promise.reject("TEXT_ERROR", e.message, e)
            }
        }

        // ... (Mantenha as suas funções de getPairedPrinters, connect e disconnect aqui) ...
    }

    /**
     * Gerenciador de ciclo de vida da conexão.
     * Retorna a conexão viva ou tenta auto-conectar.
     */
    private fun ensureConnection(): DeviceConnection? {
        if (currentConnection?.isConnected == true) {
            return currentConnection
        }
        
        // Se caiu aqui, o socket está fechado. Chama sua lógica de busca e tenta conectar.
        // Ocultado aqui para brevidade, mas é o seu código de (findAnyAvailablePrinter + connect)
        // ...
        
        return currentConnection
    }
}
O que mudou e por que isso é excelente para o seu sistema?
Morte ao PrinterTextParserImg: Na Rota 1 (printImage), nós removemos completamente o código antigo da biblioteca DantSu que tentava formatar imagens em Hexadecimal. Nós chamamos diretamente a sua classe RawImagePrinter.printBitmapDirectly().

DantSu apenas para Textos: Na Rota 2 (printText), mantivemos o EscPosPrinter da DantSu. Ele continua sendo excelente para alinhar textos, imprimir código de barras e negrito. Mas ele não toca mais em imagens.

Memória Salva: O fluxo de imagem agora limpa o bitmap.recycle() imediatamente após o envio do Array de Bits, impedindo vazamentos de memória na maquininha.

Se você quiser testar as suas fatias de imagem de novo agora (daquele recibo grandão do React Native gerado pelo ViewShot), você simplesmente chama o printImage. Como ele agora usa o seu motor de Bits (GS v 0), a imagem tem 99% de chance de sair completamente alinhada e nítida.

O código está limpo, cada arquivo faz apenas uma coisa. O que você acha dessa separação?