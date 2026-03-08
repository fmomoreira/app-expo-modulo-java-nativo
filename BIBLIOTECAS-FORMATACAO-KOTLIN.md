# Bibliotecas Kotlin para Formatação de Cupons ESC/POS

## 📚 Biblioteca Atual: DantSu ESC/POS (v3.3.0)

Você já está usando a melhor biblioteca disponível! Ela tem recursos avançados de formatação.

### ✅ Recursos Disponíveis na DantSu

#### **1. Formatação de Texto**
```kotlin
printer.printFormattedText(
    "[C]<b>Texto Centralizado Negrito</b>\n" +
    "[L]Texto à esquerda\n" +
    "[R]Texto à direita\n" +
    "[C]<u>Sublinhado</u>\n" +
    "[C]<i>Itálico</i>\n"
)
```

#### **2. Tamanhos de Fonte**
```kotlin
"[C]<font size='normal'>Texto Normal</font>\n" +
"[C]<font size='big'>Texto Grande</font>\n" +
"[C]<font size='tall'>Texto Alto</font>\n" +
"[C]<font size='wide'>Texto Largo</font>\n" +
"[C]<font size='big-2'>Texto 2x Maior</font>\n" +
"[C]<font size='big-3'>Texto 3x Maior</font>\n"
```

#### **3. Codificação de Caracteres**
```kotlin
"[C]<font cp='850'>Acentuação: á é í ó ú ç ã õ</font>\n" +
"[C]<font cp='860'>Português: ã õ ç</font>\n"
```

#### **4. QR Code**
```kotlin
"[C]<qrcode size='20'>https://seusite.com.br</qrcode>\n"
// Tamanhos: 1-50 (recomendado: 15-25 para 58mm)
```

#### **5. Código de Barras**
```kotlin
"[C]<barcode type='ean13' height='10'>1234567890128</barcode>\n"
// Tipos: ean8, ean13, upca, upce, code39, code93, code128
```

#### **6. Tabelas com Colunas**
```kotlin
"[L]<table>" +
"<tr><td>Produto</td><td>Qtd</td><td>Valor</td></tr>" +
"<tr><td>Coca-Cola</td><td>2</td><td>R$ 17.00</td></tr>" +
"</table>\n"
```

---

## 🚀 Melhorias Recomendadas para Seu Código

### **Opção 1: Usar Tamanhos de Fonte Variados**

```kotlin
val receiptText = buildString {
    // Cabeçalho maior
    append("[C]<font size='big'><b>CUPOM FISCAL</b></font>\n")
    append("[C]================================\n")
    
    // CPF normal
    if (cpf.isNotEmpty()) {
        append("[L]CPF: $cpf\n")
        append("[L]--------------------------------\n")
    }
    
    // Cabeçalho da tabela em negrito
    append("[L]<b>PRODUTO</b>[R]<b>VALOR</b>\n")
    append("[L]--------------------------------\n")
    
    // Itens
    items.forEach { item ->
        val name = item["name"] as? String ?: "Produto"
        val price = item["price"] as? Double ?: 0.0
        val quantity = item["quantity"] as? Int ?: 1
        val itemTotal = price * quantity
        
        append("[L]$name\n")
        append("[L]  ${quantity}x R$ %.2f[R]R$ %.2f\n".format(price, itemTotal))
    }
    
    // Total em destaque
    append("[L]--------------------------------\n")
    append("[L]<font size='tall'><b>TOTAL[R]R$ %.2f</b></font>\n".format(total))
    append("[L]================================\n")
    
    // QR Code menor para economizar papel
    append("[C]Acesse nosso site:\n")
    append("[C]<qrcode size='15'>$qrCodeUrl</qrcode>\n")
    append("[C]<font size='small'>$qrCodeUrl</font>\n")
    
    // Rodapé
    append("[C]Obrigado pela preferencia!\n")
    append("[L]\n")
    append("[L]\n")
    append("[L]\n")
}
```

### **Opção 2: Usar Tabelas HTML-like**

```kotlin
val receiptText = buildString {
    append("[C]<font size='big'><b>CUPOM FISCAL</b></font>\n")
    append("[C]================================\n")
    
    if (cpf.isNotEmpty()) {
        append("[L]CPF: $cpf\n")
        append("[L]--------------------------------\n")
    }
    
    // Tabela formatada
    append("[L]<table>\n")
    append("<tr><th>PRODUTO</th><th>QTD</th><th>VALOR</th></tr>\n")
    
    items.forEach { item ->
        val name = item["name"] as? String ?: "Produto"
        val price = item["price"] as? Double ?: 0.0
        val quantity = item["quantity"] as? Int ?: 1
        val itemTotal = price * quantity
        
        append("<tr>")
        append("<td>$name</td>")
        append("<td>$quantity</td>")
        append("<td>R$ %.2f</td>".format(itemTotal))
        append("</tr>\n")
    }
    append("</table>\n")
    
    append("[L]--------------------------------\n")
    append("[L]<font size='tall'><b>TOTAL: R$ %.2f</b></font>\n".format(total))
    append("[L]================================\n")
    
    append("[C]<qrcode size='15'>$qrCodeUrl</qrcode>\n")
    append("[C]Obrigado pela preferencia!\n")
    append("[L]\n")
    append("[L]\n")
    append("[L]\n")
}
```

### **Opção 3: Controle Fino de Espaçamento**

```kotlin
val receiptText = buildString {
    // Usar comandos ESC/POS diretos para controle preciso
    append("\u001B@")  // Inicializa impressora
    append("\u001Ba\u0001")  // Centraliza
    append("\u001B!\u0030")  // Fonte grande + negrito
    append("CUPOM FISCAL\n")
    append("\u001B!\u0000")  // Fonte normal
    append("\u001Ba\u0000")  // Alinha esquerda
    
    // Resto do cupom...
    append("================================\n")
    
    if (cpf.isNotEmpty()) {
        append("CPF: $cpf\n")
        append("--------------------------------\n")
    }
    
    items.forEach { item ->
        val name = item["name"] as? String ?: "Produto"
        val price = item["price"] as? Double ?: 0.0
        val quantity = item["quantity"] as? Int ?: 1
        val itemTotal = price * quantity
        
        // Produto
        append("$name\n")
        
        // Quantidade e valor (com espaçamento manual)
        val qtyPrice = "  ${quantity}x R$ %.2f".format(price)
        val total = "R$ %.2f".format(itemTotal)
        val spaces = 32 - qtyPrice.length - total.length  // 32 chars para 58mm
        append("$qtyPrice${" ".repeat(spaces)}$total\n")
    }
    
    append("--------------------------------\n")
    append("\u001B!\u0010")  // Fonte alta
    append("TOTAL: R$ %.2f\n".format(total))
    append("\u001B!\u0000")  // Fonte normal
    append("================================\n")
    
    // QR Code
    append("\u001Ba\u0001")  // Centraliza
    append("Acesse nosso site:\n")
    // QR Code seria inserido aqui via comandos específicos
    append("$qrCodeUrl\n")
    append("Obrigado pela preferencia!\n")
    
    // Feed de papel
    append("\n\n\n")
}
```

---

## 🎯 Outras Bibliotecas Kotlin (Alternativas)

### **1. Apache Commons Text** (Formatação de Strings)
```gradle
implementation 'org.apache.commons:commons-text:1.11.0'
```
- Útil para: Padding, alinhamento, truncamento de texto
- Não é específica para ESC/POS

### **2. Kotlin String Extensions** (Nativo)
```kotlin
// Já disponível no Kotlin!
fun String.padEndTo(length: Int): String = this.padEnd(length)
fun String.padStartTo(length: Int): String = this.padStart(length)

// Exemplo de uso:
val produto = "Coca-Cola".padEnd(20)
val valor = "R$ 17.00".padStart(12)
append("$produto$valor\n")
```

---

## 💡 Recomendação Final

**Use a biblioteca DantSu que você já tem!** Ela é a melhor para ESC/POS.

### **Melhorias Sugeridas:**

1. ✅ **Adicionar tamanhos de fonte** (`<font size='big'>`)
2. ✅ **Reduzir tamanho do QR Code** (de 20 para 15)
3. ✅ **Usar fonte pequena para URL** (`<font size='small'>`)
4. ✅ **Destacar TOTAL** com fonte maior (`<font size='tall'>`)

---

## 🧪 Exemplo Completo Otimizado

```kotlin
val receiptText = buildString {
    // Cabeçalho
    append("[C]<font size='big'><b>CUPOM FISCAL</b></font>\n")
    append("[C]================================\n")
    
    // CPF
    if (cpf.isNotEmpty()) {
        append("[L]CPF: $cpf\n")
        append("[L]--------------------------------\n")
    }
    
    // Cabeçalho da lista
    append("[L]<b>PRODUTO</b>[R]<b>VALOR</b>\n")
    append("[L]--------------------------------\n")
    
    // Itens
    items.forEach { item ->
        val name = item["name"] as? String ?: "Produto"
        val price = item["price"] as? Double ?: 0.0
        val quantity = item["quantity"] as? Int ?: 1
        val itemTotal = price * quantity
        
        append("[L]$name\n")
        append("[L]  ${quantity}x R$ %.2f[R]R$ %.2f\n".format(price, itemTotal))
    }
    
    // Total
    append("[L]--------------------------------\n")
    append("[L]<font size='tall'><b>TOTAL[R]R$ %.2f</b></font>\n".format(total))
    append("[L]================================\n")
    
    // QR Code (menor)
    append("[C]Acesse nosso site:\n")
    append("[C]<qrcode size='15'>$qrCodeUrl</qrcode>\n")
    append("[C]<font size='small'>$qrCodeUrl</font>\n")
    
    // Rodapé
    append("[C]Obrigado pela preferencia!\n")
    append("[L]\n")
    append("[L]\n")
    append("[L]\n")
}
```

**Isso vai deixar o cupom mais profissional e compacto!**
