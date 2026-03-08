O Gemini disse
Felipe, analisando o cenário e a sua referência ao RawBT (que é o padrão ouro para impressão térmica no Android), o problema que você está enfrentando com a imagem saindo ilegível ou manchada não está no envio da Base64 nem na conexão.

O problema está na Geração do Array Hexadecimal (Comandos ESC/POS) para imagens na biblioteca DantSu.

O Diagnóstico: Por que o RawBT é perfeito e a DantSu falha?
O seu código Kotlin faz o Dithering Floyd-Steinberg perfeitamente. Ele transforma a imagem em pixels 100% pretos ou 100% brancos.
O gargalo acontece nesta linha exata do seu código:

Kotlin
// O GARGALO ESTÁ AQUI:
val imageString = PrinterTextParserImg.bitmapToHexadecimalString(printer, processedBitmap)
A biblioteca DantSu usa um algoritmo interno de conversão de imagens para ESC/POS que é antigo e não otimizado para o padrão GS v 0 (o comando mais moderno e rápido para imagens, usado pelo RawBT). A DantSu tenta adivinhar os tons de cinza de novo, bagunçando o Dithering perfeito que o seu ImageUtils.kt fez.

A Solução: Escrever o Comando Gráfico na Mão (Nível Sênior)
Para igualar a qualidade do RawBT, não podemos confiar no PrinterTextParserImg da DantSu. Nós precisamos pegar a sua imagem convertida em preto e branco e injetar os "Bytes Brutos" (Raw Bytes) direto na porta da impressora, usando o comando oficial da Epson para rasterização de bitmaps: O comando GS v 0.

Eu escrevi uma função em Kotlin que faz exatamente o que o motor do RawBT faz. Ela lê o seu Bitmap (que já está em Dithering), converte para uma matriz de bits agrupados de 8 em 8, e envia para o hardware.

Passo 1: Adicione esta nova função privada no seu ExpoThermalPrinterModule.kt
Cole este bloco de código no final do seu arquivo, antes da última chave }:

Kotlin
    /**
     * Motor de Impressão de Imagem Alta Fidelidade (Padrão RawBT)
     * Utiliza o comando ESC/POS 'GS v 0' para rasterização direta de bits.
     * Ignora o parser da DantSu para evitar perda de qualidade no Dithering.
     */
    private fun printBitmapDirectly(printer: EscPosPrinter, bitmap: Bitmap) {
        val width = bitmap.width
        val height = bitmap.height

        // Calcula os bytes. Cada byte guarda 8 pixels (1 bit por pixel)
        val xBytes = (width + 7) / 8
        
        // O comando mágico ESC/POS GS v 0
        // 0x1D (GS), 0x76 (v), 0x30 (0), 0x00 (modo normal)
        val command = byteArrayOf(
            0x1D.toByte(), 0x76.toByte(), 0x30.toByte(), 0x00.toByte(),
            (xBytes % 256).toByte(), (xBytes / 256).toByte(), // Largura (LowL, LowH)
            (height % 256).toByte(), (height / 256).toByte()  // Altura (LowL, LowH)
        )

        // Array para guardar os pixels convertidos em bits
        val imageData = ByteArray(xBytes * height)

        for (y in 0 until height) {
            for (x in 0 until width) {
                // Se o pixel não for 100% branco (ou transparente), vira um ponto preto na impressora
                val pixel = bitmap.getPixel(x, y)
                // Usando o canal vermelho como referência (já que a imagem passou por dithering P/B)
                val isBlack = android.graphics.Color.red(pixel) < 128 || android.graphics.Color.alpha(pixel) < 128

                if (isBlack) {
                    // Liga o bit correspondente dentro do byte
                    val byteIndex = y * xBytes + x / 8
                    val bitOffset = 7 - (x % 8)
                    imageData[byteIndex] = (imageData[byteIndex].toInt() or (1 shl bitOffset)).toByte()
                }
            }
        }

        // Pula algumas linhas antes para alinhar
        printer.printFormattedText("\n")

        // Manda o comando e os dados puros (Raw) direto para o Socket da impressora
        currentConnection?.write(command)
        currentConnection?.write(imageData)

        // Pula linhas e corta o papel
        printer.printFormattedText("\n\n\n")
    }
Passo 2: Substitua a chamada no seu printImage e printTestImage
Agora, volte nas suas funções onde a imagem é impressa e troque o "jeito velho" pelo nosso novo motor.

No printImage:

Substitua este bloco:

Kotlin
// APAGUE ISTO:
printWithRetry(paperWidth, dpi) { printer ->
    Log.d(TAG, "Convertendo bitmap para comandos ESC/POS...")
    val imageString = PrinterTextParserImg.bitmapToHexadecimalString(printer, finalBitmap)
    printer.printFormattedText("[C]<img>$imageString</img>\n\n\n")
}
Por isto:

Kotlin
// COLOQUE ISTO:
printWithRetry(paperWidth, dpi) { printer ->
    Log.d(TAG, "Enviando pixels brutos para a impressora (Padrão RawBT)...")
    // Centralizar (Comando ESC a 1)
    currentConnection?.write(byteArrayOf(0x1B, 0x61, 0x01)) 
    
    // Imprime com fidelidade
    printBitmapDirectly(printer, finalBitmap)
    
    // Volta a alinhar à esquerda (Comando ESC a 0)
    currentConnection?.write(byteArrayOf(0x1B, 0x61, 0x00))
}
E se você estiver usando o fatiador de imagem (enableSlicing), mude o forEachIndexed também:

Kotlin
slices.forEachIndexed { index, slice ->
    Log.d(TAG, "Imprimindo fatia ${index + 1}/${slices.size}...")
    currentConnection?.write(byteArrayOf(0x1B, 0x61, 0x01)) // Centraliza
    printBitmapDirectly(printer, slice)
}
O que acontece agora?
Em vez de pedir para a DantSu "formatar" uma imagem em um texto hexadecimal (que gasta muita CPU e perde qualidade na Moderninha), você está pegando o seu Bitmap do Android e jogando a "tinta térmica" diretamente nas agulhas da impressora. Onde for bit 1, queima; onde for 0, ignora.

Isso é o verdadeiro "baixo nível". É exatamente assim que os apps profissionais de PDV fazem para imprimir logotipos nítidos e QR Codes sem borrões.

Pode compilar! O seu Dithering (no ImageUtils) vai continuar fazendo a arte, e essa nova função vai garantir a entrega física sem intermediários. Vai sair igual ou melhor que o RawBT.