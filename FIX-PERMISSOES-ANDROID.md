# 🔧 FIX: Detecção de Versão Android e Permissões Bluetooth

## 🔴 Problema Identificado

O app estava **sempre pedindo permissão de localização**, mesmo no Android 12+, onde isso não é necessário.

### Causa Raiz
```typescript
// ❌ CÓDIGO ANTERIOR (app/index.tsx linha 60)
if (androidVersion >= 31) {
  const granted = await PermissionsAndroid.requestMultiple([
    PermissionsAndroid.PERMISSIONS.BLUETOOTH_SCAN,
    PermissionsAndroid.PERMISSIONS.BLUETOOTH_CONNECT,
    PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION, // ❌ DESNECESSÁRIO!
  ]);
```

**Problema:** Android 12+ (API 31+) **NÃO precisa** de permissão de localização quando usa `neverForLocation` no Manifest.

---

## ✅ Solução Aplicada

### **1. Permissões Corretas por Versão**

| Versão Android | API Level | Permissões Necessárias |
|----------------|-----------|------------------------|
| **Android 7-11** | 24-30 | `ACCESS_COARSE_LOCATION` |
| **Android 12+** | 31+ | `BLUETOOTH_SCAN` + `BLUETOOTH_CONNECT` |

### **2. Código Corrigido (app/index.tsx)**

```typescript
const requestBluetoothPermissions = async (): Promise<boolean> => {
  if (Platform.OS !== 'android') return true;

  try {
    const androidVersion = Platform.Version;
    
    // Android 12+ (API 31+): BLUETOOTH_SCAN + BLUETOOTH_CONNECT
    // NÃO precisa de localização se usar neverForLocation no Manifest
    if (androidVersion >= 31) {
      console.log('[Permissions] Android 12+ detectado - Solicitando BLUETOOTH_SCAN e BLUETOOTH_CONNECT');
      const granted = await PermissionsAndroid.requestMultiple([
        PermissionsAndroid.PERMISSIONS.BLUETOOTH_SCAN,
        PermissionsAndroid.PERMISSIONS.BLUETOOTH_CONNECT,
      ]);
      
      const scanGranted = granted['android.permission.BLUETOOTH_SCAN'] === PermissionsAndroid.RESULTS.GRANTED;
      const connectGranted = granted['android.permission.BLUETOOTH_CONNECT'] === PermissionsAndroid.RESULTS.GRANTED;
      
      console.log('[Permissions] BLUETOOTH_SCAN:', scanGranted);
      console.log('[Permissions] BLUETOOTH_CONNECT:', connectGranted);
      
      return scanGranted && connectGranted;
    } 
    // Android 7-11 (API 24-30): ACCESS_COARSE_LOCATION
    else {
      console.log('[Permissions] Android 7-11 detectado - Solicitando ACCESS_COARSE_LOCATION');
      const granted = await PermissionsAndroid.request(
        PermissionsAndroid.PERMISSIONS.ACCESS_COARSE_LOCATION
      );
      
      const locationGranted = granted === PermissionsAndroid.RESULTS.GRANTED;
      console.log('[Permissions] ACCESS_COARSE_LOCATION:', locationGranted);
      
      return locationGranted;
    }
  } catch (err) {
    console.error('[Permissions] Erro ao solicitar permissões:', err);
    return false;
  }
};
```

### **3. Manifest Já Estava Correto**

`modules/expo-thermal-printer/android/src/main/AndroidManifest.xml`:

```xml
<!-- Android 12+ - NÃO precisa de localização -->
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" 
                 android:usesPermissionFlags="neverForLocation" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />

<!-- Android 7-11 - Precisa de localização -->
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

---

## 🎯 Benefícios da Correção

### **Antes (❌ Errado):**
- Android 7: Pedia `ACCESS_COARSE_LOCATION` ✅
- Android 12: Pedia `BLUETOOTH_SCAN` + `BLUETOOTH_CONNECT` + `ACCESS_FINE_LOCATION` ❌

### **Depois (✅ Correto):**
- Android 7-11: Pede **apenas** `ACCESS_COARSE_LOCATION` ✅
- Android 12+: Pede **apenas** `BLUETOOTH_SCAN` + `BLUETOOTH_CONNECT` ✅

---

## 📊 Como Funciona a Detecção

### **Platform.Version (React Native)**
```typescript
const androidVersion = Platform.Version; // Retorna número da API
// Android 7.0 = 24
// Android 8.0 = 26
// Android 11 = 30
// Android 12 = 31
// Android 13 = 33
// Android 14 = 34
```

### **Build.VERSION.SDK_INT (Kotlin)**
```kotlin
val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // S = API 31
    PendingIntent.FLAG_MUTABLE
} else {
    PendingIntent.FLAG_UPDATE_CURRENT
}
```

---

## 🧪 Como Testar

### **1. Android 7 (Moderninha Smart 2)**
```bash
npx expo run:android
```

**Esperado:**
- Popup: "Permitir acesso à localização do dispositivo?"
- Apenas 1 popup
- Logs: `[Permissions] Android 7-11 detectado - Solicitando ACCESS_COARSE_LOCATION`

### **2. Android 12+ (Emulador ou dispositivo moderno)**
```bash
npx expo run:android
```

**Esperado:**
- Popup: "Permitir que [App] encontre, conecte e determine a posição relativa de dispositivos próximos?"
- Apenas 1 popup (sem pedir localização)
- Logs: `[Permissions] Android 12+ detectado - Solicitando BLUETOOTH_SCAN e BLUETOOTH_CONNECT`

---

## 📝 Logs de Debug

Adicionados logs para facilitar debug:

```
[Permissions] Android 12+ detectado - Solicitando BLUETOOTH_SCAN e BLUETOOTH_CONNECT
[Permissions] BLUETOOTH_SCAN: true
[Permissions] BLUETOOTH_CONNECT: true
```

ou

```
[Permissions] Android 7-11 detectado - Solicitando ACCESS_COARSE_LOCATION
[Permissions] ACCESS_COARSE_LOCATION: true
```

---

## ✅ Checklist de Verificação

- [x] Removido `ACCESS_FINE_LOCATION` do Android 12+
- [x] Mantido `ACCESS_COARSE_LOCATION` no Android 7-11
- [x] Adicionados logs de debug
- [x] Verificado que Manifest tem `neverForLocation`
- [x] Testado detecção de versão com `Platform.Version`
- [ ] Testar em dispositivo Android 7 (Moderninha)
- [ ] Testar em dispositivo Android 12+

---

## 🚀 Próximos Passos

1. Rebuild do app:
   ```bash
   cd print-app
   npx expo run:android
   ```

2. Testar em Moderninha (Android 7):
   - Deve pedir **apenas** localização
   - Não deve pedir Bluetooth Scan/Connect

3. Verificar logs no Logcat:
   ```bash
   adb logcat -s Permissions
   ```

---

## 📚 Referências

- [Android Bluetooth Permissions](https://developer.android.com/guide/topics/connectivity/bluetooth/permissions)
- [BLUETOOTH_SCAN neverForLocation](https://developer.android.com/reference/android/Manifest.permission#BLUETOOTH_SCAN)
- [React Native Platform.Version](https://reactnative.dev/docs/platform-specific-code#platform-module)
