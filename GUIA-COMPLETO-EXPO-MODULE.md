# 📚 Guia Completo - Expo Module de Impressão Térmica

## 🎯 Visão Geral do Projeto

Este é um **Expo Module customizado** que implementa impressão térmica nativa usando Kotlin, permitindo acesso direto ao hardware de impressoras Bluetooth e USB em maquininhas como a Moderninha PagSeguro.

### **Por Que Módulo Nativo?**

- ✅ **Performance:** Kotlin processa bitmaps 10x+ mais rápido que JavaScript
- ✅ **Hardware:** Acessa impressora USB interna que JS não enxerga
- ✅ **Qualidade:** Dithering Floyd-Steinberg para impressão profissional
- ✅ **Memória:** Gerenciamento eficiente para dispositivos com 1-2GB RAM
- ✅ **Estabilidade:** Socket lifecycle correto, sem Broken Pipe

---

## 🏗️ Arquitetura do Sistema

```
┌─────────────────────────────────────────────────────────┐
│              React Native (TypeScript)                  │
│         - UI e lógica de negócio                        │
│         - Gera Base64 de imagens                        │
└──────────────────┬──────────────────────────────────────┘
                   │ Expo Module Bridge
┌──────────────────▼──────────────────────────────────────┐
│           Expo Thermal Printer Module                   │
│         - index.ts (TypeScript)                         │
│         - Tipagens e validações                         │
└──────────────────┬──────────────────────────────────────┘
                   │ requireNativeModule()
┌──────────────────▼──────────────────────────────────────┐
│        ExpoThermalPrinterModule.kt (Kotlin)             │
│         - Socket lifecycle management                   │
│         - Permissões USB/Bluetooth                      │
│         - Integração com biblioteca ESC/POS             │
└──────────────────┬──────────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────────┐
│              ImageUtils.kt (Kotlin)                     │
│         - Floyd-Steinberg dithering                     │
│         - Redimensionamento proporcional                │
│         - Gerenciamento de memória (recycle)            │
└──────────────────┬──────────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────────┐
│    DantSu ESCPOS-ThermalPrinter-Android v3.3.0          │
│         - Conversão Bitmap → ESC/POS                    │
│         - Suporte Bluetooth SPP / USB OTG               │
└──────────────────┬──────────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────────┐
│         Hardware (Impressora Térmica)                   │
│         - Moderninha Smart (USB interna)                │
│         - Impressoras Bluetooth externas                │
└─────────────────────────────────────────────────────────┘
```

---

## 📁 Estrutura de Arquivos

```
print-app/
├── app/                              # React Native App
│   └── (tabs)/
│       └── index.tsx                 # UI de teste
│
└── modules/
    └── expo-thermal-printer/         # Expo Module
        ├── index.ts                  # Ponte TypeScript
        ├── expo-module.config.json   # Configuração do módulo
        │
        └── android/
            ├── build.gradle          # Dependências Gradle
            │
            └── src/main/
                ├── AndroidManifest.xml
                │
                └── java/expo/modules/thermalprinter/
                    ├── ExpoThermalPrinterModule.kt  # Módulo principal
                    └── ImageUtils.kt                # Processamento de imagem
```

---

## 🔧 Arquivos Principais Explicados

### **1. ImageUtils.kt - Processamento de Imagem**

**Localização:** `modules/expo-thermal-printer/android/src/main/java/expo/modules/thermalprinter/ImageUtils.kt`

**Responsabilidades:**
- Redimensionar imagens para largura da bobina (58mm ou 80mm)
- Aplicar dithering Floyd-Steinberg (preto e branco pontilhado)
- Gerenciar memória com `bitmap.recycle()`

**Conceitos Kotlin Importantes:**

#### **A. Object (Singleton)**
```kotlin
object ImageUtils {
    // Funções podem ser chamadas sem instanciar: ImageUtils.resizeForPrinter()
}
```
- **Por quê?** Não precisa criar objeto com `new`, economiza memória
- **Equivalente Java:** `public static class ImageUtils { ... }`

#### **B. Declaração de Variáveis**
```kotlin
val targetWidth = 384  // Imutável (const)
var newGray = 128      // Mutável
```
- `val` = constante (não pode mudar)
- `var` = variável (pode mudar)

#### **C. Funções com Parâmetros Opcionais**
```kotlin
fun resizeForPrinter(source: Bitmap, paperWidth: Int): Bitmap
```
- Tipo vem **depois** do nome: `paperWidth: Int`
- Retorno declarado no final: `: Bitmap`

#### **D. Segurança de Memória**
```kotlin
if (resizedBitmap != source) {
    source.recycle()  // Libera RAM
}
```
- **CRÍTICO** para Moderninha (1-2GB RAM)
- Evita `OutOfMemoryError`

#### **E. Algoritmo Floyd-Steinberg**

**Conceito:** Impressoras térmicas são binárias (pixel ligado/desligado). O dithering distribui o erro de brilho entre pixels vizinhos.

**Fórmula de Luminância:**
```kotlin
val gray = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
```
- Verde: 58.7% (olhos mais sensíveis)
- Vermelho: 29.9%
- Azul: 11.4%

**Distribuição do Erro:**
```
        X   7/16
3/16  5/16  1/16
```

**Implementação:**
```kotlin
val error = oldPixel - newPixel

// Direita: 7/16
if (x + 1 < width) {
    luminanceArray[y][x + 1] += (error * 7 / 16)
}

// Baixo-esquerda: 3/16
if (x > 0 && y + 1 < height) {
    luminanceArray[y + 1][x - 1] += (error * 3 / 16)
}

// Baixo: 5/16
if (y + 1 < height) {
    luminanceArray[y + 1][x] += (error * 5 / 16)
}

// Baixo-direita: 1/16
if (x + 1 < width && y + 1 < height) {
    luminanceArray[y + 1][x + 1] += (error * 1 / 16)
}
```

---

### **2. ExpoThermalPrinterModule.kt - Módulo Principal**

**Localização:** `modules/expo-thermal-printer/android/src/main/java/expo/modules/thermalprinter/ExpoThermalPrinterModule.kt`

**Responsabilidades:**
- Gerenciar ciclo de vida do socket (connect/disconnect)
- Solicitar permissões USB com BroadcastReceiver
- Detectar impressoras (Bluetooth → USB fallback)
- Executar comandos de impressão

**Conceitos Críticos:**

#### **A. Socket Lifecycle (MUITO IMPORTANTE!)**

**Problema Anterior:**
```kotlin
// ❌ ERRADO
currentConnection = connection
promise.resolve("Conectado")  // MENTIRA! Socket não foi aberto
```

**Solução Implementada:**
```kotlin
// ✅ CORRETO
connection.connect()  // Abre socket de verdade!
currentPrinter = EscPosPrinter(connection, ...)  // Cria UMA VEZ
promise.resolve("Conectado")  // Agora é verdade!
```

**Por que isso é crítico?**
- Sem `connection.connect()`: socket nunca abre
- Criar `EscPosPrinter` múltiplas vezes: envia comandos INIT repetidos → **Broken Pipe**

#### **B. Reutilização de Instância**

**Problema Anterior:**
```kotlin
// ❌ ERRADO - Cria nova instância a cada impressão
fun getOrCreatePrinter(): EscPosPrinter? {
    return EscPosPrinter(currentConnection, ...)  // NOVO objeto!
}
```

**Solução Implementada:**
```kotlin
// ✅ CORRETO - Reutiliza instância existente
private var currentPrinter: EscPosPrinter? = null

fun getOrCreatePrinter(): EscPosPrinter? {
    if (currentPrinter != null) {
        return currentPrinter  // REUTILIZA!
    }
    // Cria apenas se não existir
}
```

#### **C. Permissão USB com BroadcastReceiver**

**Problema:** Android bloqueia acesso USB sem permissão explícita do usuário.

**Solução:**
```kotlin
private fun requestUsbPermission(context: Context, usbManager: UsbManager, device: UsbDevice): Boolean {
    // 1. Cria latch para aguardar resposta
    usbPermissionLatch = CountDownLatch(1)
    
    // 2. Registra BroadcastReceiver
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                usbPermissionGranted = true
            }
            usbPermissionLatch?.countDown()  // Libera execução
        }
    }
    
    context.registerReceiver(receiver, IntentFilter(ACTION_USB_PERMISSION))
    
    // 3. Solicita permissão (popup aparece)
    usbManager.requestPermission(device, permissionIntent)
    
    // 4. Aguarda até 30 segundos
    usbPermissionLatch?.await(30, TimeUnit.SECONDS)
    
    // 5. Remove receiver
    context.unregisterReceiver(receiver)
    
    return usbPermissionGranted
}
```

**Fluxo:**
1. Usuário clica "Conectar"
2. Código detecta USB
3. **Popup aparece:** "Permitir acesso ao dispositivo USB?"
4. Usuário clica "OK"
5. `BroadcastReceiver` captura resposta
6. `CountDownLatch` libera execução
7. Código continua com permissão concedida

---

### **3. index.ts - Ponte TypeScript**

**Localização:** `modules/expo-thermal-printer/index.ts`

**Responsabilidades:**
- Exportar funções Kotlin para JavaScript
- Definir tipagens TypeScript
- Validar parâmetros antes de chamar Kotlin

**Conceito: requireNativeModule**

```typescript
import { requireNativeModule } from 'expo-modules-core';

// Puxa o módulo pelo nome definido no Kotlin: Name("ExpoThermalPrinter")
const ExpoThermalPrinterModule = requireNativeModule('ExpoThermalPrinter');
```

**Tipagens:**
```typescript
export interface PrinterDevice {
  name: string;
  address: string;
  type: 'bluetooth' | 'usb' | 'network';
}

export interface PrintResult {
  success: boolean;
  message: string;
}
```

**Funções Exportadas:**
```typescript
export async function printImage(base64Image: string, options?: PrintOptions): Promise<PrintResult> {
  const cleanBase64 = base64Image.replace(/^data:image\/\w+;base64,/, '');
  
  return await ExpoThermalPrinterModule.printImage(cleanBase64, {
    paperWidth: options?.paperWidth || 58,
    applyDithering: options?.applyDithering !== false,
  });
}
```

---

### **4. build.gradle - Dependências**

**Localização:** `modules/expo-thermal-printer/android/build.gradle`

**Responsabilidades:**
- Declarar dependências externas
- Configurar versão do Kotlin
- Adicionar repositórios (JitPack)

**Configuração Crítica:**

```gradle
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }  // Necessário para DantSu
}

dependencies {
    implementation project(':expo-modules-core')
    implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.24"
    
    // Biblioteca ESC/POS para impressão térmica
    implementation 'com.github.DantSu:ESCPOS-ThermalPrinter-Android:3.3.0'
}
```

**Por que JitPack?**
- DantSu não está no Maven Central
- JitPack compila bibliotecas do GitHub sob demanda

---

### **5. AndroidManifest.xml - Permissões**

**Localização:** `modules/expo-thermal-printer/android/src/main/AndroidManifest.xml`

**Permissões Necessárias:**

```xml
<!-- Bluetooth (Android 7+) -->
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />

<!-- Bluetooth (Android 12+) -->
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" 
                 android:usesPermissionFlags="neverForLocation" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />

<!-- USB -->
<uses-permission android:name="android.permission.USB_PERMISSION" />

<!-- Features -->
<uses-feature android:name="android.hardware.bluetooth" android:required="false" />
<uses-feature android:name="android.hardware.usb.host" android:required="false" />
```

**CRÍTICO:** `android.hardware.usb.host` é obrigatório para Moderninha Smart!

---

## 🎓 Conceitos Kotlin para Iniciantes

### **1. Null Safety**
```kotlin
var connection: DeviceConnection? = null  // Pode ser null
val printer: EscPosPrinter = ...          // Nunca null

// Safe call
connection?.disconnect()  // Só chama se não for null

// Elvis operator
val name = device.name ?: "Desconhecido"  // Se null, usa "Desconhecido"
```

### **2. String Templates**
```kotlin
val width = 384
Log.d(TAG, "Largura: $width pixels")  // Interpolação
Log.d(TAG, "Área: ${width * height}")  // Expressão
```

### **3. When (Switch Melhorado)**
```kotlin
val widthPixels = when (paperWidth) {
    58 -> 384
    80 -> 576
    else -> 384
}
```

### **4. Ranges e Loops**
```kotlin
for (y in 0 until height) {  // 0 até height-1
    for (x in 0 until width) {
        // ...
    }
}
```

### **5. Extension Functions**
```kotlin
fun Int.coerceIn(min: Int, max: Int): Int {
    return max(min, min(max, this))
}

val value = 300.coerceIn(0, 255)  // 255
```

---

## 🚀 Como Usar no React Native

### **Exemplo Completo:**

```typescript
import React, { useState } from 'react';
import { View, Text, TouchableOpacity, Alert } from 'react-native';
import { 
  connectPrinter, 
  selfTest, 
  printText, 
  printImage 
} from './modules/expo-thermal-printer';

export default function PrinterTestScreen() {
  const [loading, setLoading] = useState(false);

  const testarImpressora = async () => {
    setLoading(true);
    try {
      // 1. Conecta (popup USB aparece)
      await connectPrinter("internal_auto");
      
      // 2. Auto-teste
      const resultado = await selfTest();
      Alert.alert("Sucesso!", resultado.message);
      
    } catch (error: any) {
      Alert.alert("Erro", error.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <View>
      <TouchableOpacity onPress={testarImpressora} disabled={loading}>
        <Text>Imprimir Teste</Text>
      </TouchableOpacity>
    </View>
  );
}
```

---

## ⚠️ Regra de Ouro da Compilação

**IMPORTANTE:** Como você adicionou código nativo (Kotlin), o **Expo Go** não funciona mais!

### **Por quê?**
- Expo Go é um app genérico da loja
- Não tem seu código Kotlin customizado dentro

### **Solução:**
Gerar um **Dev Client** customizado:

```bash
# Gerar APK com seu código nativo
eas build --platform android --profile preview

# Instalar na Moderninha
adb install -r app.apk
```

---

## 🔍 Debugging

### **Logs no Android:**
```bash
adb logcat -s ThermalPrinter ImageUtils
```

### **Logs Esperados (Sucesso):**
```
ThermalPrinter: Conectando à impressora: internal_auto
ThermalPrinter: Tentando USB...
ThermalPrinter: Solicitando permissão USB para auto-detect...
ThermalPrinter: Aguardando resposta do usuário (popup USB)...
ThermalPrinter: Permissão USB CONCEDIDA pelo usuário
ThermalPrinter: ✓ USB encontrado: /dev/bus/usb/001/002
ThermalPrinter: Abrindo Socket de comunicação...
ThermalPrinter: Socket aberto com sucesso!
ThermalPrinter: Conectado e inicializado com sucesso: Auto-detectada
ImageUtils: Redimensionando de 1024x768 para 384x288
ThermalPrinter: Reutilizando impressora já conectada
ThermalPrinter: Impressão concluída com sucesso!
```

---

## 📊 Melhorias Implementadas (Baseadas no desafio.md)

| Melhoria | Antes | Depois | Impacto |
|----------|-------|--------|---------|
| **Memory Management** | ❌ Bitmaps não liberados | ✅ `bitmap.recycle()` | Evita OutOfMemory |
| **Resize Otimizado** | ❌ Sempre redimensiona | ✅ Verifica tamanho antes | Economiza processamento |
| **Socket Lifecycle** | ❌ Conexão fantasma | ✅ `connection.connect()` | Elimina Broken Pipe |
| **Printer Reuse** | ❌ Nova instância a cada vez | ✅ Reutiliza `currentPrinter` | Performance + estabilidade |
| **USB Permission** | ❌ Thread.sleep() | ✅ BroadcastReceiver | Popup funciona corretamente |

---

## 🎯 Checklist de Implementação

- [x] ImageUtils.kt com dithering Floyd-Steinberg
- [x] ImageUtils.kt com gerenciamento de memória (recycle)
- [x] ExpoThermalPrinterModule.kt com socket lifecycle correto
- [x] BroadcastReceiver para permissão USB
- [x] Reutilização de instância EscPosPrinter
- [x] build.gradle com JitPack e DantSu 3.3.0
- [x] index.ts com requireNativeModule
- [x] AndroidManifest.xml com usb.host feature
- [x] Tipagens TypeScript completas
- [x] Função selfTest() para diagnóstico

---

## 📚 Referências

- **Expo Modules:** https://docs.expo.dev/modules/overview/
- **Kotlin Docs:** https://kotlinlang.org/docs/home.html
- **DantSu Library:** https://github.com/DantSu/ESCPOS-ThermalPrinter-Android
- **Floyd-Steinberg:** https://en.wikipedia.org/wiki/Floyd%E2%80%93Steinberg_dithering
- **Android USB:** https://developer.android.com/guide/topics/connectivity/usb

---

**Data:** 08/03/2026  
**Versão:** 1.0  
**Status:** ✅ Produção
