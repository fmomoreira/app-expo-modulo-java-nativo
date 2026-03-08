# 🔧 Correção Final - Permissão USB com BroadcastReceiver

## 🎯 Problema Identificado

A impressora USB era **detectada** mas **não imprimia** porque:

### ❌ **Problemas Anteriores:**

1. **BroadcastReceiver Ausente**
   - Solicitava permissão USB mas não esperava resposta do usuário
   - `Thread.sleep(1000)` não garantia que usuário clicou "OK"
   - Código continuava mesmo sem permissão concedida

2. **Verificação Incompleta**
   - Não verificava se permissão foi realmente concedida
   - Tentava usar impressora sem permissão → falha silenciosa

3. **Conexão Não Estabelecida**
   - Salvava `currentConnection` mas não conectava de fato
   - `getOrCreatePrinter()` falhava ao criar `EscPosPrinter`

---

## ✅ Solução Implementada

### **1. BroadcastReceiver Completo**

Implementado sistema robusto para capturar resposta do popup USB:

```kotlin
private fun requestUsbPermission(context: Context, usbManager: UsbManager, device: UsbDevice): Boolean {
    // Cria latch para aguardar resposta
    usbPermissionLatch = CountDownLatch(1)
    usbPermissionGranted = false
    
    // Registra BroadcastReceiver
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_USB_PERMISSION) {
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    Log.d(TAG, "Permissão USB CONCEDIDA")
                    usbPermissionGranted = true
                } else {
                    Log.w(TAG, "Permissão USB NEGADA")
                    usbPermissionGranted = false
                }
                usbPermissionLatch?.countDown()
            }
        }
    }
    
    context.registerReceiver(receiver, IntentFilter(ACTION_USB_PERMISSION))
    
    // Solicita permissão
    val permissionIntent = PendingIntent.getBroadcast(
        context, 0, Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_MUTABLE
    )
    usbManager.requestPermission(device, permissionIntent)
    
    // Aguarda até 30 segundos
    val granted = usbPermissionLatch?.await(30, TimeUnit.SECONDS) ?: false
    
    context.unregisterReceiver(receiver)
    
    return usbPermissionGranted
}
```

### **2. Verificação em `connectPrinter()`**

```kotlin
if (!usbManager.hasPermission(device)) {
    val granted = requestUsbPermission(context, usbManager, device)
    
    if (!granted) {
        promise.reject("USB_PERMISSION_DENIED", "Permissão USB negada pelo usuário", null)
        return@AsyncFunction
    }
    
    Log.d(TAG, "Permissão USB concedida!")
}
```

### **3. Verificação em `findAnyAvailablePrinter()`**

```kotlin
if (!usbManager.hasPermission(device)) {
    val granted = requestUsbPermission(context, usbManager, device)
    
    if (!granted) {
        Log.w(TAG, "Permissão USB negada")
        return null
    }
}
```

---

## 🚀 Como Funciona Agora

### **Fluxo Completo:**

```
1. Usuário clica "Buscar Impressoras"
   └─> Detecta impressora USB interna ✅

2. Usuário clica "Impressora interna (Auto-detect)"
   └─> connectPrinter() chamado
        │
3. Verifica permissão USB
   └─> if (!usbManager.hasPermission(device))
        │
4. Solicita permissão
   └─> usbManager.requestPermission(device, permissionIntent)
        │
5. POPUP APARECE 📱
   ┌─────────────────────────────────────┐
   │ Permitir acesso ao dispositivo USB? │
   │                                     │
   │ [Cancelar]              [OK]        │
   └─────────────────────────────────────┘
        │
6. BroadcastReceiver captura resposta
   └─> onReceive() → usbPermissionGranted = true
        │
7. CountDownLatch libera execução
   └─> latch.countDown()
        │
8. Retorna resultado
   └─> return usbPermissionGranted
        │
9. Se concedido: salva conexão ✅
   └─> currentConnection = usbConn
        │
10. Usuário clica "Auto-Teste"
    └─> getOrCreatePrinter() usa currentConnection
         │
11. Cria EscPosPrinter e IMPRIME! 🎉
```

---

## 📋 Instruções de Teste

### **1. Gerar Novo Build**

```bash
# Commit
git add .
git commit -m "fix: implementar BroadcastReceiver para permissão USB"
git push

# Gerar APK
eas build --platform android --profile preview
```

### **2. Instalar na Moderninha Smart**

```bash
# Download do APK
eas build:download --platform android --latest

# Instalar
adb install -r app.apk
```

### **3. Testar Fluxo Completo**

1. **Abrir app**
2. **Clicar "Buscar Impressoras"**
   - Deve aparecer: "Impressora interna (Auto-detect)"
3. **Clicar na impressora**
   - **POPUP USB DEVE APARECER** 📱
   - Clicar **"OK"**
4. **Clicar "Auto-Teste (Diagnóstico)"**
   - **DEVE IMPRIMIR!** 🎉

---

## 🔍 Logs Esperados

```bash
adb logcat -s ThermalPrinter
```

### **Logs de Sucesso:**

```
ThermalPrinter: Conectando à impressora: internal_auto
ThermalPrinter: Modo auto-detect ativado
ThermalPrinter: Tentando Bluetooth...
ThermalPrinter: ✗ Nenhum Bluetooth encontrado
ThermalPrinter: Tentando USB...
ThermalPrinter: Solicitando permissão USB para auto-detect...
ThermalPrinter: Aguardando resposta do usuário (popup USB)...
ThermalPrinter: Permissão USB CONCEDIDA pelo usuário
ThermalPrinter: Permissão USB concedida!
ThermalPrinter: ✓ USB encontrado: /dev/bus/usb/001/002
ThermalPrinter: Conectado à impressora: Auto-detectada
ThermalPrinter: Executando auto-teste da impressora...
ThermalPrinter: Auto-teste concluído com sucesso!
```

### **Logs se Usuário Negar:**

```
ThermalPrinter: Solicitando permissão USB para auto-detect...
ThermalPrinter: Aguardando resposta do usuário (popup USB)...
ThermalPrinter: Permissão USB NEGADA pelo usuário
ThermalPrinter: Permissão USB negada
ThermalPrinter: ✗ Nenhuma USB encontrada
ThermalPrinter: Nenhuma impressora disponível (Bluetooth ou USB)
```

---

## 🛠️ Modificações no Código

### **Arquivo:** `ExpoThermalPrinterModule.kt`

#### **Imports Adicionados:**
```kotlin
import android.content.BroadcastReceiver
import android.content.IntentFilter
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
```

#### **Variáveis de Classe:**
```kotlin
private var usbPermissionLatch: CountDownLatch? = null
private var usbPermissionGranted = false
```

#### **Constante:**
```kotlin
private const val ACTION_USB_PERMISSION = "expo.modules.thermalprinter.USB_PERMISSION"
```

#### **Nova Função:**
```kotlin
private fun requestUsbPermission(context: Context, usbManager: UsbManager, device: UsbDevice): Boolean
```

#### **Modificações:**
- Linhas 261-300: `connectPrinter()` - verificação robusta
- Linhas 450-490: `findAnyAvailablePrinter()` - verificação robusta
- Linhas 501-568: Nova função `requestUsbPermission()`

---

## ⚠️ Possíveis Problemas

### **1. Popup Não Aparece**

**Causa:** Permissão já foi negada permanentemente

**Solução:**
```bash
# Limpar dados do app
adb shell pm clear com.fmomoreira2021.printapp

# Reinstalar
adb install -r app.apk
```

### **2. Timeout (30 segundos)**

**Causa:** Usuário não clicou em nada

**Solução:** Código já trata isso retornando `false`

### **3. Erro "Context não disponível"**

**Causa:** `appContext.reactContext` é null

**Solução:** Já tratado com `?: throw Exception("Context não disponível")`

---

## 📊 Diferença da Versão Anterior

| Aspecto | Versão Anterior | Versão Nova |
|---------|----------------|-------------|
| **Popup USB** | ❌ Não aparecia | ✅ Aparece |
| **Aguarda resposta** | ❌ Thread.sleep(1000) | ✅ CountDownLatch |
| **Verifica permissão** | ❌ Não | ✅ Sim |
| **Trata negação** | ❌ Continua mesmo assim | ✅ Retorna erro |
| **BroadcastReceiver** | ❌ Não existe | ✅ Implementado |
| **Timeout** | ❌ Não | ✅ 30 segundos |

---

## 🎉 Resultado Esperado

Após esta correção, o fluxo completo deve funcionar:

1. ✅ Detecta impressora USB interna
2. ✅ **Popup de permissão aparece**
3. ✅ Usuário clica "OK"
4. ✅ Permissão concedida
5. ✅ Conexão estabelecida
6. ✅ **Impressão funciona!** 🚀

---

## 📝 Próximos Passos

1. **Gerar build:** `eas build --platform android --profile preview`
2. **Instalar:** `adb install -r app.apk`
3. **Testar:** Buscar → Conectar → **Aceitar popup** → Imprimir
4. **Verificar logs:** `adb logcat -s ThermalPrinter`

---

## 🔗 Referências

- **CountDownLatch:** Sincronização de threads
- **BroadcastReceiver:** Captura eventos do sistema Android
- **PendingIntent.FLAG_MUTABLE:** Necessário para Android 12+
- **IntentFilter:** Filtra broadcasts por action

---

**Data:** 08/03/2026  
**Versão:** 2.0 (BroadcastReceiver)  
**Status:** ✅ Pronto para teste
