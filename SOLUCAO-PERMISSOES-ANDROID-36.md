# ✅ SOLUÇÃO: Android API 36 Pedindo Localização

## 🔴 Problema Identificado

**Dispositivo:** Android API 36 (Android 12+)  
**Comportamento:** Pediu permissão de localização (ERRADO)  
**Esperado:** Pedir apenas BLUETOOTH_SCAN + BLUETOOTH_CONNECT

---

## 🎯 Causa Raiz

O Android **sempre solicita permissões declaradas no Manifest**, independente do código React Native.

### **Manifest Anterior (ERRADO):**
```xml
<!-- ❌ PROBLEMA: Sem limitação de versão -->
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
```

**Resultado:** Android 12+ pedia localização mesmo com `neverForLocation` configurado.

---

## ✅ Solução Aplicada: `maxSdkVersion`

### **Conceito:**
O atributo `maxSdkVersion` limita uma permissão até determinada versão do Android.

```xml
<!-- ✅ CORRETO: Localização APENAS até Android 11 (API 30) -->
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"
                 android:maxSdkVersion="30"/>
```

**Resultado:**
- ✅ Android 7-11 (API 24-30): Pede localização
- ✅ Android 12+ (API 31+): **NÃO pede** localização

---

## 📋 AndroidManifest.xml Corrigido

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
  <!-- Permissões Bluetooth para Android 7-11 -->
  <uses-permission android:name="android.permission.BLUETOOTH"/>
  <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
  
  <!-- Localização APENAS para Android 7-11 (API 24-30) -->
  <!-- maxSdkVersion=30 = NÃO pede no Android 12+ -->
  <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"
                   android:maxSdkVersion="30"/>
  
  <!-- Permissões Bluetooth para Android 12+ (API 31+) -->
  <!-- neverForLocation = NÃO usa localização para scan -->
  <uses-permission android:name="android.permission.BLUETOOTH_SCAN" 
                   android:usesPermissionFlags="neverForLocation"/>
  <uses-permission android:name="android.permission.BLUETOOTH_CONNECT"/>
</manifest>
```

---

## 🔧 Mudanças Aplicadas

### **1. AndroidManifest.xml**
- ✅ Adicionado `android:maxSdkVersion="30"` em `ACCESS_COARSE_LOCATION`
- ✅ Mantido `neverForLocation` em `BLUETOOTH_SCAN`

### **2. app/index.tsx**
- ✅ Adicionado `checkMultiple()` para verificar permissões já concedidas
- ✅ Melhorado mensagem de erro com instruções para configurações

### **3. Display de Versão**
- ✅ Adicionado `📱 Android API 36 (12+)` no topo do app
- ✅ Facilita validação da detecção de versão

---

## 📊 Comportamento Esperado

### **Android API 36 (12+):**

**Popup que DEVE aparecer:**
```
"Permitir que PDV encontre, conecte e determine a 
posição relativa de dispositivos próximos?"

[ Permitir ]  [ Negar ]
```

**Popup que NÃO DEVE aparecer:**
```
❌ "Permitir acesso à localização do dispositivo?"
```

**Logs esperados:**
```
═══════════════════════════════════════
🔍 [DEBUG] Platform.Version: 36
🔍 [DEBUG] androidVersion >= 31? true
═══════════════════════════════════════
✅ [Permissions] Android 12+ detectado (API 36)
📋 [Permissions] Solicitando BLUETOOTH_SCAN e BLUETOOTH_CONNECT
🚫 [Permissions] Localização NÃO será pedida (maxSdkVersion=30 no Manifest)
🔍 [Permissions] Permissões já concedidas: {...}
📊 [Permissions] Resultado BLUETOOTH_SCAN: true
📊 [Permissions] Resultado BLUETOOTH_CONNECT: true
```

---

## 🧪 Como Testar

### **1. Limpar dados do app (importante!):**
```bash
adb shell pm clear com.anonymous.printapp
```

### **2. Rebuild completo:**
```bash
cd print-app
npx expo run:android
```

### **3. No app:**
1. Verificar topo: `📱 Android API 36 (12+)`
2. Clicar "Buscar Impressoras"
3. **Verificar popup:**
   - ✅ Deve pedir Bluetooth (encontrar dispositivos)
   - ❌ NÃO deve pedir localização

### **4. Verificar logs:**
```bash
adb logcat | grep -E "Permissions|DEBUG"
```

---

## 📚 Referência Técnica

### **maxSdkVersion**
- **Documentação:** [Android Developers - uses-permission](https://developer.android.com/guide/topics/manifest/uses-permission-element#maxSdk)
- **Comportamento:** Permissão é **ignorada** em versões superiores ao `maxSdkVersion`
- **Uso:** Permissões que mudaram entre versões do Android

### **Tabela de Permissões por Versão:**

| Android | API | Permissões Bluetooth | Localização |
|---------|-----|---------------------|-------------|
| 7-11 | 24-30 | BLUETOOTH + BLUETOOTH_ADMIN | ACCESS_COARSE_LOCATION ✅ |
| 12+ | 31+ | BLUETOOTH_SCAN + BLUETOOTH_CONNECT | ❌ NÃO (maxSdkVersion=30) |

---

## ⚠️ Importante

### **Por que `neverForLocation` sozinho não funcionou?**

O `neverForLocation` apenas diz ao sistema que o app **não vai usar** a localização obtida pelo Bluetooth Scan. Mas se `ACCESS_COARSE_LOCATION` estiver no Manifest **sem limitação de versão**, o Android ainda pede a permissão.

**Solução completa:**
```xml
<!-- 1. Limitar localização até API 30 -->
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"
                 android:maxSdkVersion="30"/>

<!-- 2. Declarar que Bluetooth Scan não usa localização -->
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" 
                 android:usesPermissionFlags="neverForLocation"/>
```

---

## 🎯 Checklist de Validação

Após rebuild, confirme:

- [ ] Topo mostra `📱 Android API 36 (12+)`
- [ ] Popup pede **apenas** Bluetooth (não localização)
- [ ] Logs mostram `Localização NÃO será pedida`
- [ ] Consegue buscar impressoras
- [ ] Consegue conectar à impressora
- [ ] Impressão funciona

---

## 🚀 Próximos Passos

1. **Limpar dados do app:**
   ```bash
   adb shell pm clear com.anonymous.printapp
   ```

2. **Rebuild:**
   ```bash
   npx expo run:android
   ```

3. **Testar e confirmar:**
   - Qual popup apareceu?
   - Conseguiu buscar impressoras?
   - Conseguiu conectar?
   - Impressão funcionou?

Se ainda pedir localização, me envie:
- Screenshot do popup
- Logs completos do console
- Versão exibida no topo do app
