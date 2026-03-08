# 🔧 Correção: Permissão USB para Impressora Interna

## 🎯 Problema Identificado

**Sintoma:** App detecta "Impressora interna (Auto-detect)" mas ao tentar imprimir retorna "Nenhuma impressora disponível"

**Causa:** Impressoras USB no Android precisam de **permissão explícita do usuário** antes de serem acessadas.

---

## ✅ Correções Implementadas

### 1. **Solicitação de Permissão USB**

Adicionado código para solicitar permissão USB quando conectar à impressora:

```kotlin
// Em connectPrinter() - linha 274-288
if (!usbManager.hasPermission(device)) {
    Log.d(TAG, "Solicitando permissão USB...")
    val permissionIntent = PendingIntent.getBroadcast(
        context,
        0,
        Intent("com.android.example.USB_PERMISSION"),
        PendingIntent.FLAG_IMMUTABLE
    )
    usbManager.requestPermission(device, permissionIntent)
    Thread.sleep(1000) // Aguarda permissão
}
```

### 2. **Permissão no Auto-Detect**

Mesma lógica aplicada na função `findAnyAvailablePrinter()` (linha 465-477)

---

## 📱 Como Testar

### **Passo 1: Gerar Novo APK**

```bash
git add .
git commit -m "fix: adicionar solicitação de permissão USB"
git push
eas build --platform android --profile preview
```

### **Passo 2: Instalar na Moderninha Smart**

```bash
adb install -r app.apk
```

### **Passo 3: Testar Fluxo Completo**

1. **Abrir app** na Moderninha Smart
2. **Clicar "Buscar Impressoras"**
   - Deve aparecer: "Impressora interna (Auto-detect)"
3. **Clicar na impressora**
   - ⚠️ **IMPORTANTE:** Vai aparecer um popup do Android perguntando:
     ```
     "Permitir que este app acesse o dispositivo USB?"
     [Cancelar] [OK]
     ```
   - ✅ **Clicar "OK"**
4. **Clicar "Auto-Teste (Diagnóstico)"**
   - Deve imprimir página de teste com:
     - Alinhamentos
     - Negrito/Itálico
     - Acentuação brasileira
     - Código de barras

---

## 🔍 Logs para Debug

Se ainda não funcionar, execute:

```bash
adb logcat -s ThermalPrinter
```

**Logs esperados:**
```
ThermalPrinter: Conectando à impressora: internal_auto
ThermalPrinter: Modo auto-detect ativado
ThermalPrinter: Tentando Bluetooth...
ThermalPrinter: ✗ Nenhum Bluetooth encontrado
ThermalPrinter: Tentando USB...
ThermalPrinter: Solicitando permissão USB para auto-detect...
ThermalPrinter: ✓ USB encontrado: /dev/bus/usb/001/002
ThermalPrinter: Conectado à impressora: Auto-detectada
ThermalPrinter: Executando auto-teste da impressora...
ThermalPrinter: Auto-teste concluído com sucesso!
```

---

## ⚠️ Possíveis Problemas

### **Problema 1: Permissão Negada**
**Sintoma:** Usuário clica "Cancelar" no popup de permissão

**Solução:** 
- Ir em Configurações → Apps → Seu App → Permissões
- Ativar permissão USB manualmente
- Ou desinstalar e reinstalar o app

### **Problema 2: Timeout na Permissão**
**Sintoma:** App trava por 1 segundo mas não imprime

**Solução:** Aumentar timeout:
```kotlin
Thread.sleep(2000) // Aumentar de 1000 para 2000ms
```

### **Problema 3: Impressora Não Detectada**
**Sintoma:** Não aparece "Impressora interna" na lista

**Solução:** Verificar se impressora está ligada:
```bash
adb shell ls -l /dev/bus/usb/
```

---

## 📊 Arquivos Modificados

- `ExpoThermalPrinterModule.kt` (linhas 3-11, 274-288, 465-477)
  - Adicionados imports: `PendingIntent`, `Intent`, `UsbDevice`
  - Adicionada solicitação de permissão USB em 2 locais

---

## 🎉 Resultado Esperado

Após essas correções:

1. ✅ App detecta impressora USB interna
2. ✅ Solicita permissão USB ao usuário
3. ✅ Conecta após permissão concedida
4. ✅ Imprime texto, imagem e auto-teste
5. ✅ Funciona em Moderninha Smart e outras maquininhas

**Estamos muito perto! 🚀**
