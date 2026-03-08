# 📡 Comunicação React Native → Kotlin: Impressão de Imagens

## 🎯 Como o Módulo Kotlin Recebe e Processa Imagens

### **Fluxo Completo (5 Etapas)**

```
React Native (JS)  →  Ponte Nativa  →  Kotlin  →  Dithering  →  Impressora
     Base64              expo-modules    Bitmap    Floyd-Steinberg   ESC/POS
```

---

## 📋 **Etapa por Etapa**

### **1️⃣ React Native: Captura da View**

```typescript
// hooks/usePrintReceipt.ts
const base64Image = await captureRef(receiptRef, {
  format: 'jpg',        // ✅ JPG tem melhor compatibilidade
  quality: 1,           // ✅ Qualidade máxima
  result: 'base64',     // ✅ Retorna string Base64 pura
});

// Resultado: "iVBORw0KGgoAAAANSUhEUgAA..." (sem prefixo)
```

**⚠️ IMPORTANTE:** `react-native-view-shot` retorna Base64 **SEM** o prefixo `data:image/jpg;base64,`

---

### **2️⃣ Ponte Nativa: Limpeza e Envio**

```typescript
// modules/expo-thermal-printer/index.ts (linha 30-42)
export async function printImage(base64Image: string, options?: PrintOptions) {
  // Remove prefixo se existir (proteção extra)
  const cleanBase64 = base64Image.replace(/^data:image\/\w+;base64,/, '');
  
  return await ExpoThermalPrinterModule.printImage(cleanBase64, {
    paperWidth: options?.paperWidth || 58,
    dpi: options?.dpi || 203,
    applyDithering: options?.applyDithering !== false,
  });
}
```

**✅ A função já limpa o Base64 automaticamente!**

---

### **3️⃣ Kotlin: Decodificação Base64 → Bitmap**

```kotlin
// ExpoThermalPrinterModule.kt (linha 77-86)
AsyncFunction("printImage") { base64Image: String, options: Map<String, Any>, promise: Promise ->
    
    // Decodifica Base64 para array de bytes
    val decodedBytes = Base64.decode(base64Image, Base64.DEFAULT)
    
    // Converte bytes para Bitmap (imagem Android)
    bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    
    if (bitmap == null) {
        promise.reject("DECODE_ERROR", "Falha ao decodificar imagem Base64", null)
        return@AsyncFunction
    }
}
```

**🔍 Formato Aceito:**
- ✅ Base64 puro: `iVBORw0KGgoAAAANSUhEUgAA...`
- ✅ Com prefixo: `data:image/jpg;base64,iVBORw0KGgo...` (removido pela ponte)
- ❌ String vazia ou corrompida

---

### **4️⃣ Kotlin: Processamento da Imagem**

```kotlin
// ExpoThermalPrinterModule.kt (linha 90-100)

// A) REDIMENSIONA para largura da bobina
bitmap = ImageUtils.resizeForPrinter(bitmap, paperWidth)
// 58mm → 384 pixels
// 80mm → 576 pixels

// B) APLICA DITHERING Floyd-Steinberg
if (applyDithering) {
    bitmap = ImageUtils.applyFloydSteinbergDithering(bitmap)
}
// Converte para preto e branco puro (1-bit)
```

**📐 Algoritmo Floyd-Steinberg (ImageUtils.kt):**

```kotlin
// ImageUtils.kt (linha 24-89)
fun applyFloydSteinbergDithering(source: Bitmap): Bitmap {
    // 1. Converte RGB → Luminância (0-255)
    //    Fórmula: L = 0.299*R + 0.587*G + 0.114*B
    
    // 2. Quantiza para preto (0) ou branco (255)
    val newPixel = if (oldPixel < 128) 0 else 255
    
    // 3. Distribui erro para pixels vizinhos
    //    Direita:        7/16 do erro
    //    Baixo-Esquerda: 3/16 do erro
    //    Baixo:          5/16 do erro
    //    Baixo-Direita:  1/16 do erro
}
```

---

### **5️⃣ Kotlin: Conversão ESC/POS e Impressão**

```kotlin
// ExpoThermalPrinterModule.kt (linha 106-119)

// Converte Bitmap → Hexadecimal ESC/POS
val imageString = PrinterTextParserImg.bitmapToHexadecimalString(printer, finalBitmap)

// Imprime centralizado
printer.printFormattedText("[C]<img>$imageString</img>\n\n\n")
```

**🖨️ Comandos ESC/POS:**
- `[C]` = Centraliza
- `<img>...</img>` = Insere imagem
- `\n\n\n` = Avança 3 linhas (corte do papel)

---

## 🧪 **Teste de Comunicação**

### **Teste 1: Verificar se o módulo está carregado**

```typescript
import * as ThermalPrinter from '@/modules/expo-thermal-printer';

console.log('Módulo carregado:', ThermalPrinter);
console.log('Função printImage:', typeof ThermalPrinter.printImage);
// Esperado: "function"
```

### **Teste 2: Testar com Base64 simples**

```typescript
// Imagem 1x1 pixel branco em Base64 (PNG)
const testBase64 = 'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==';

try {
  const result = await ThermalPrinter.printImage(testBase64, {
    paperWidth: 58,
    dpi: 203,
    applyDithering: true,
  });
  
  console.log('✅ Teste passou:', result);
} catch (error) {
  console.error('❌ Teste falhou:', error);
}
```

### **Teste 3: Verificar logs do Kotlin**

```bash
# Terminal (Android Debug Bridge)
adb logcat -s ThermalPrinter

# Logs esperados:
# [ThermalPrinter] Iniciando processo de impressão de imagem...
# [ThermalPrinter] Decodificando imagem Base64...
# [ThermalPrinter] Imagem decodificada: 384x500 pixels
# [ThermalPrinter] Redimensionando imagem para bobina de 58 mm...
# [ThermalPrinter] Aplicando algoritmo Floyd-Steinberg...
# [ThermalPrinter] Convertendo bitmap para comandos ESC/POS...
# [ThermalPrinter] Impressão de imagem concluída com sucesso!
```

---

## 🔧 **Problemas Comuns e Soluções**

### **❌ Erro: "DECODE_ERROR: Falha ao decodificar imagem Base64"**

**Causas:**
1. Base64 corrompido ou incompleto
2. Formato de imagem não suportado
3. String vazia

**Solução:**
```typescript
// Validar Base64 antes de enviar
if (!base64Image || base64Image.length < 100) {
  throw new Error('Base64 inválido ou muito pequeno');
}

console.log('Tamanho do Base64:', base64Image.length);
console.log('Primeiros 50 caracteres:', base64Image.substring(0, 50));
```

---

### **❌ Erro: "NO_PRINTER: Nenhuma impressora conectada"**

**Causa:** Não chamou `connectPrinter()` antes de imprimir

**Solução:**
```typescript
// 1. Conectar primeiro
await ThermalPrinter.connectPrinter(printerAddress);

// 2. Depois imprimir
await ThermalPrinter.printImage(base64Image, options);
```

---

### **❌ Erro: "unknown error: 2" (react-native-view-shot)**

**Causas:**
1. View não está montada
2. View está fora da tela (`position: absolute, left: -9999`)
3. View foi otimizada pelo Android (falta `collapsable={false}`)

**Solução:**
```tsx
// ✅ CORRETO
<View collapsable={false} style={{ opacity: 0, height: 0 }}>
  <ReceiptTemplate ref={receiptRef} {...props} />
</View>

// ❌ ERRADO
<View style={{ position: 'absolute', left: -9999 }}>
  <ReceiptTemplate ref={receiptRef} {...props} />
</View>
```

---

## 📊 **Formato de Dados Aceitos**

### **Opções de Impressão (PrintOptions)**

```typescript
interface PrintOptions {
  paperWidth?: 58 | 80;        // Largura da bobina (mm)
  dpi?: 203;                    // DPI da impressora (fixo)
  applyDithering?: boolean;     // Aplicar Floyd-Steinberg (padrão: true)
}
```

### **Resultado da Impressão (PrintResult)**

```typescript
interface PrintResult {
  success: boolean;
  message: string;
}

// Exemplo de sucesso:
{
  success: true,
  message: "Imagem impressa com sucesso"
}

// Exemplo de erro:
{
  success: false,
  message: "Falha ao decodificar imagem Base64"
}
```

---

## 🎓 **Resumo Técnico**

### **O que o Kotlin ACEITA:**
✅ String Base64 pura (sem prefixo)  
✅ String Base64 com prefixo `data:image/...` (removido automaticamente)  
✅ Qualquer formato de imagem suportado por `BitmapFactory` (JPG, PNG, BMP, WEBP)  

### **O que o Kotlin FAZ:**
1. Decodifica Base64 → Bitmap
2. Redimensiona para largura da bobina (384px ou 576px)
3. Aplica dithering Floyd-Steinberg (P&B puro)
4. Converte para comandos ESC/POS hexadecimal
5. Envia para impressora via Bluetooth/USB

### **O que o Kotlin RETORNA:**
- `Promise<PrintResult>` com `success` e `message`

---

## 🚀 **Próximos Passos**

1. ✅ Módulo Kotlin está **100% funcional**
2. ✅ Ponte TypeScript está **correta**
3. ⚠️ Problema está no **react-native-view-shot**
4. 🔧 Aplicar correções: `collapsable={false}` + `format: 'jpg'` + `opacity: 0`
5. 🧪 Testar novamente

---

**Última atualização:** 8 de março de 2026
