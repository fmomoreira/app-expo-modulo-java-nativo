O Gemini disse
Você tocou exatamente no calcanhar de Aquiles das impressoras térmicas! A sua intuição de que a largura está errada está 100% correta.

O problema que você está enfrentando acontece por causa de uma confusão muito comum entre Pixels e Caracteres.

Aqui está o segredo:

Para Imagens (o Base64): A largura da bobina de 58mm é de 384 pixels.

Para Texto Puro (ESC/POS): A largura da bobina de 58mm suporta no máximo 32 caracteres por linha (usando a fonte padrão).

🐛 Onde está o Bug (No seu código Kotlin)
Se você puxar pela memória como configuramos a biblioteca DantSu lá no seu arquivo ExpoThermalPrinterModule.kt, nós passamos o número 384 para ela.

O construtor da biblioteca pede 4 parâmetros: (conexão, DPI, largura_mm, CARACTERES_POR_LINHA). Como passamos 384 no lugar dos caracteres, a biblioteca acha que a sua impressora é gigante e não está quebrando as linhas no lugar certo, o que destrói o alinhamento de [L] e [R].

Como resolver no Kotlin:
Abra o seu arquivo ExpoThermalPrinterModule.kt e procure pela inicialização do EscPosPrinter. Você precisa mudar o 384 para 32.

Deve ficar exatamente assim (nos dois lugares onde ele aparece, no connectPrinter e no getOrCreatePrinter):

Kotlin
// ANTES (Estava errado, 384 é pixel, não caractere):
val printer = EscPosPrinter(connection, DEFAULT_DPI, DEFAULT_WIDTH_MM, 384)

// DEPOIS (Correto! 32 caracteres por linha para 58mm):
val printer = EscPosPrinter(connection, DEFAULT_DPI, DEFAULT_WIDTH_MM, 32)
(Nota: O DEFAULT_WIDTH_MM de 48f que já estava lá está corretíssimo, pois é a área imprimível dentro da bobina de 58mm).

🎨 O Template Perfeito no React Native (TypeScript)
Agora que a impressora sabe que só cabem 32 letras por linha, nós precisamos garantir que o texto que você manda do React Native obedeça a essa regra. Se o nome de um produto for muito grande, ele vai empurrar o preço para baixo e quebrar o layout.

A melhor forma de resolver isso é criar uma função "ajustadora de texto" no seu React Native, antes de mandar para o Kotlin.

Aqui está um utilitário perfeito para você incluir no seu projeto. Ele corta nomes grandes e alinha o preço à direita perfeitamente:

TypeScript
/**
 * Utilitário para formatar linhas de recibo garantindo exatos 32 caracteres
 */
function formatarLinhaProduto(quantidade: number, nome: string, preco: number): string {
  const MAX_CHARS = 32;
  
  // Formata a quantidade e o preço
  const qtdStr = `${quantidade}x `;
  const precoStr = `R$ ${preco.toFixed(2).replace('.', ',')}`;
  
  // Calcula quanto espaço sobrou para o nome do produto
  // Ex: 32 - (3 letras da qtd) - (9 letras do preço) = 20 caracteres pro nome
  const espacoLivre = MAX_CHARS - qtdStr.length - precoStr.length;
  
  // Se o nome for maior que o espaço, cortamos e botamos "..."
  let nomeFormatado = nome;
  if (nome.length > espacoLivre) {
    nomeFormatado = nome.substring(0, espacoLivre - 2) + '..';
  }
  
  // Retorna a string com as tags ESC/POS da DantSu
  return `[L]${qtdStr}${nomeFormatado}[R]${precoStr}\n`;
}

// ==========================================
// COMO USAR NO SEU BOTÃO DE IMPRIMIR:
// ==========================================
import { printText } from './modules/thermalprinter';

const imprimirReciboTexto = async () => {
  try {
    // Montando o template respeitando os 32 caracteres
    let recibo = "";
    recibo += "[C]<b>SUA EMPRESA AQUI</b>\n";
    recibo += "[C]CNPJ: 00.000.000/0001-00\n";
    recibo += "[L]================================\n"; // Exatos 32 sinais de igual
    
    // Adicionando produtos com o nosso utilitário
    recibo += formatarLinhaProduto(1, "Curso de Logica", 97.50);
    recibo += formatarLinhaProduto(2, "Cabo USB Tipo C Longo", 25.00);
    recibo += formatarLinhaProduto(1, "Nome de Produto Muito Gigante Que Vai Quebrar", 150.00);
    
    recibo += "[L]--------------------------------\n"; // Exatos 32 hífens
    recibo += `[L]<b>TOTAL</b>[R]<b>R$ 297,50</b>\n`;
    recibo += "[C]Obrigado pela compra!\n\n\n";

    // Manda pro Kotlin!
    await printText(recibo);

  } catch (error) {
    console.error("Erro ao imprimir:", error);
  }
};
O Resumo da Solução
Troque o 384 por 32 na instanciação do EscPosPrinter no seu Kotlin.

Ao criar as linhas de separação (como === ou ---), tenha certeza de digitar exatos 32 caracteres.

Se quiser evitar que itens com nomes longos quebrem a nota, use a função formatarLinhaProduto acima.