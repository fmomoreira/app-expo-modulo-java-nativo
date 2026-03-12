# 🔍 DEBUG: Permissões Android

## ⚠️ Problema Reportado
- **Dispositivo:** Android 12+
- **Erro:** "Impressora recusou conexão ou pode estar desligada ou ocupada"
- **Suspeita:** Ainda está pedindo permissão errada

---

## 📋 Checklist de Verificação

### 1. ✅ Código foi alterado?
- [x] `app/index.tsx` - Lógica de permissões corrigida
- [x] `AndroidManifest.xml` - Adicionado `neverForLocation`
- [x] `ExpoThermalPrinterModule.kt` - Verificação de permissões adicionada

### 2. ⚠️ App foi reconstruído?
**CRÍTICO:** Mudanças no `AndroidManifest.xml` exigem rebuild completo!

```bash
# ❌ NÃO FUNCIONA: Apenas reload
# Pressionar 'r' no Metro

# ✅ CORRETO: Rebuild completo
cd print-app
npx expo run:android
```

### 3. 🧪 Como Testar

#### Passo 1: Limpar cache e rebuild
```bash
cd print-app
npx expo run:android --clear
```

#### Passo 2: Abrir Logcat em outro terminal
```bash
# Terminal 2
adb logcat | grep -E "Permissions|ThermalPrinter"
```

#### Passo 3: No app, clicar em "Buscar Impressoras"

#### Passo 4: Verificar logs

**Logs esperados no React Native:**
```
═══════════════════════════════════════
🔍 [DEBUG] Platform.OS: android
🔍 [DEBUG] Platform.Version: 33 (ou 31, 32, 34)
🔍 [DEBUG] Tipo: number
🔍 [DEBUG] androidVersion >= 31? true
═══════════════════════════════════════
✅ [Permissions] Android 12+ detectado (API 33)
📋 [Permissions] Solicitando BLUETOOTH_SCAN e BLUETOOTH_CONNECT
🚫 [Permissions] NÃO solicitando localização (neverForLocation)
📊 [Permissions] Resultado BLUETOOTH_SCAN: true
📊 [Permissions] Resultado BLUETOOTH_CONNECT: true
```

**Logs esperados no Kotlin:**
```
═══════════════════════════════════════
🔍 Buscando impressoras disponíveis...
🔍 Android SDK: 33
═══════════════════════════════════════
📡 Verificando impressoras Bluetooth...
🔐 [Android 12+] BLUETOOTH_SCAN: true
🔐 [Android 12+] BLUETOOTH_CONNECT: true
✅ Bluetooth está LIGADO
📱 Dispositivos pareados: 2
```

---

## 🚨 Possíveis Causas do Erro

### Causa 1: App não foi reconstruído
**Sintoma:** Código mudou mas comportamento é o mesmo

**Solução:**
```bash
# Parar Metro (Ctrl+C)
cd print-app
npx expo run:android --clear
```

### Causa 2: Permissões foram negadas
**Sintoma:** Logs mostram `false` nas permissões

**Solução:**
```bash
# Limpar permissões do app
adb shell pm clear com.anonymous.printapp

# Reinstalar
npx expo run:android
```

### Causa 3: Platform.Version retorna string em vez de number
**Sintoma:** Log mostra `Tipo: string`

**Solução:** Converter para número:
```typescript
const androidVersion = Number(Platform.Version);
```

### Causa 4: Bluetooth desligado
**Sintoma:** Log mostra "Bluetooth está DESLIGADO"

**Solução:** Ligar Bluetooth nas configurações do dispositivo

### Causa 5: Nenhum dispositivo pareado
**Sintoma:** Log mostra "Dispositivos pareados: 0"

**Solução:** Parear impressora nas configurações Bluetooth

---

## 🔧 Comandos Úteis

### Ver logs em tempo real
```bash
# Filtrar apenas permissões
adb logcat | grep Permissions

# Filtrar apenas Kotlin
adb logcat | grep ThermalPrinter

# Ver tudo junto
adb logcat | grep -E "Permissions|ThermalPrinter"
```

### Limpar dados do app
```bash
adb shell pm clear com.anonymous.printapp
```

### Ver permissões concedidas
```bash
adb shell dumpsys package com.anonymous.printapp | grep permission
```

### Forçar concessão de permissões (apenas debug)
```bash
# Android 12+
adb shell pm grant com.anonymous.printapp android.permission.BLUETOOTH_SCAN
adb shell pm grant com.anonymous.printapp android.permission.BLUETOOTH_CONNECT

# Android 7-11
adb shell pm grant com.anonymous.printapp android.permission.ACCESS_COARSE_LOCATION
```

---

## 📊 Análise de Logs

### ✅ Logs Corretos (Android 12+)
```
Platform.Version: 33
androidVersion >= 31? true
Android 12+ detectado
Solicitando BLUETOOTH_SCAN e BLUETOOTH_CONNECT
NÃO solicitando localização
BLUETOOTH_SCAN: true
BLUETOOTH_CONNECT: true
```

### ❌ Logs Incorretos (ainda pedindo localização)
```
Platform.Version: 33
androidVersion >= 31? true
Android 12+ detectado
Solicitando ACCESS_FINE_LOCATION  ❌ ERRADO!
```

### ❌ Logs Incorretos (versão não detectada)
```
Platform.Version: "33"  ❌ String em vez de number!
androidVersion >= 31? false  ❌ Comparação falhou!
Android 7-11 detectado  ❌ Detectou versão errada!
```

---

## 🎯 Próximos Passos

1. **Rebuild completo:**
   ```bash
   npx expo run:android --clear
   ```

2. **Abrir Logcat:**
   ```bash
   adb logcat | grep -E "Permissions|ThermalPrinter"
   ```

3. **No app:** Clicar em "Buscar Impressoras"

4. **Copiar e colar aqui:**
   - Todos os logs que aparecerem
   - Qual popup de permissão apareceu
   - Qual erro exato apareceu

5. **Verificar configurações do dispositivo:**
   - Bluetooth está ligado?
   - Impressora está pareada?
   - App tem permissões concedidas?

---

## 📝 Template de Resposta

Por favor, me envie:

```
1. Versão do Android: ___

2. Logs do console (React Native):
[Cole aqui os logs que começam com 🔍 [DEBUG]]

3. Logs do Logcat (Kotlin):
[Cole aqui os logs do adb logcat]

4. Popup que apareceu:
[ ] "Permitir acesso à localização?" ❌ ERRADO
[ ] "Permitir encontrar dispositivos próximos?" ✅ CORRETO
[ ] Nenhum popup apareceu

5. Erro exato:
[Cole a mensagem de erro]

6. Rebuild foi feito?
[ ] Sim, com npx expo run:android --clear
[ ] Não, apenas reload (r)
```
