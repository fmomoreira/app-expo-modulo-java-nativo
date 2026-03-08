# Otimização de Espaçamento ESC/POS

## Problema Identificado
A impressão de cupom fiscal estava consumindo ~80cm de papel devido a espaçamentos excessivos causados por múltiplas quebras de linha (`\n\n`).

## Mudanças Implementadas

### Antes (Desperdiçava Papel)
```kotlin
append("[C]================================\n")
append("[C]<b>CUPOM FISCAL</b>\n")
append("[C]================================\n\n")  // ❌ Quebra dupla
// ...
append("[L]================================\n\n")  // ❌ Quebra dupla
append("[C]Acesse nosso site:\n")
append("[C]<qrcode size='20'>$qrCodeUrl</qrcode>\n\n")  // ❌ Quebra dupla
append("[C]$qrCodeUrl\n\n")  // ❌ Quebra dupla
append("[C]Obrigado pela preferência!\n\n\n")  // ❌ Quebra tripla!
```

### Depois (Otimizado)
```kotlin
append("[C]================================\n")
append("[C]<b>CUPOM FISCAL</b>\n")
append("[C]================================\n")  // ✅ Quebra simples
// ...
append("[L]================================\n")  // ✅ Quebra simples
append("[C]Acesse nosso site:\n")
append("[C]<qrcode size='20'>$qrCodeUrl</qrcode>\n")  // ✅ Quebra simples
append("[C]$qrCodeUrl\n")  // ✅ Quebra simples
append("[C]Obrigado pela preferencia!\n")  // ✅ Quebra simples
append("[L]\n")  // ✅ Apenas 3 linhas no final para corte
append("[L]\n")
append("[L]\n")
```

## Redução de Papel Estimada

### Antes
- **~80cm** de papel para um cupom simples
- Múltiplas quebras duplas e triplas
- Espaçamento excessivo entre seções

### Depois
- **~15-20cm** de papel (redução de 75%)
- Quebras simples entre linhas
- Apenas 3 linhas em branco no final para facilitar o corte

## Estrutura do Cupom Otimizado

```
================================
      CUPOM FISCAL
================================
CPF: 123.456.789-00
--------------------------------
PRODUTO              VALOR
--------------------------------
Coca-Cola 2L
  2x R$ 8.50        R$ 17.00
Pão Francês (kg)
  1x R$ 12.00       R$ 12.00
Queijo Minas (kg)
  0.5x R$ 35.00     R$ 17.50
--------------------------------
TOTAL               R$ 46.50
================================
Acesse nosso site:
[QR CODE]
https://reinodasorte.com.br
Obrigado pela preferencia!
[3 linhas em branco]
```

## Comandos ESC/POS Utilizados

- `[C]` - Centralizado
- `[L]` - Alinhado à esquerda
- `[R]` - Alinhado à direita
- `<b>` - Negrito
- `<qrcode size='20'>` - QR Code tamanho 20
- `\n` - Quebra de linha simples

## Próximos Passos

1. ✅ Otimização de espaçamento concluída
2. 🔄 Testar impressão com novo formato
3. ⏳ Após confirmação, implementar teste com imagem Base64 hardcoded
4. ⏳ Resolver problema de captura de imagem React Native

## Como Testar

```bash
# Rebuild do app (Kotlin foi modificado)
npx expo run:android

# No app:
# 1. Conecte à impressora
# 2. Clique em "Imprimir Cupom (Texto ESC/POS)"
# 3. Verifique o tamanho do cupom impresso
```

## Resultado Esperado

O cupom deve imprimir de forma **compacta e legível**, ocupando aproximadamente **15-20cm** de papel em vez de 80cm.
