# 📱 Como Gerar o APK para Testar

## ✅ Você já fez:
- ✅ `npx expo prebuild --clean` (gerou pasta android/)
- ✅ `npx expo install react-native-view-shot`
- ✅ Interface de teste criada na tela inicial

## 🚀 Agora: Gerar o APK

### Opção 1: Build de Desenvolvimento (Mais Rápido)

```bash
npx expo run:android
```

Isso vai:
- Compilar o app
- Instalar automaticamente no dispositivo conectado
- Abrir o app pronto para testar

### Opção 2: Gerar APK para Instalar Manualmente

```bash
cd android
./gradlew assembleRelease
```

O APK estará em:
```
android/app/build/outputs/apk/release/app-release.apk
```

### Opção 3: Build com EAS (Expo Application Services)

```bash
# Instalar EAS CLI
npm install -g eas-cli

# Login no Expo
eas login

# Configurar build
eas build:configure

# Gerar APK
eas build --platform android --profile preview
```

## 📲 Como Testar

### 1. Conectar Dispositivo/Moderninha

**Via USB:**
```bash
# Verificar se o dispositivo está conectado
adb devices
```

**Via Wi-Fi (opcional):**
```bash
adb tcpip 5555
adb connect <IP_DO_DISPOSITIVO>:5555
```

### 2. Executar o App

```bash
npm run android
```

Ou se já gerou o APK:
```bash
adb install android/app/build/outputs/apk/release/app-release.apk
```

### 3. Usar a Interface

Quando o app abrir, você verá:

1. **Seção "Gerenciar Impressoras"**
   - Botão "🔍 Buscar Impressoras Bluetooth"
   - Lista de impressoras encontradas
   - Toque para conectar

2. **Seção "Testar Impressão"**
   - Botão "📄 Imprimir Texto de Teste"
   - Botão "🖼️ Imprimir Imagem (com Dithering)"

### 4. Ver Logs em Tempo Real

Em outro terminal:
```bash
adb logcat -s ThermalPrinter ReactNativeJS
```

Você verá mensagens como:
```
ThermalPrinter: Iniciando processo de impressão...
ThermalPrinter: Decodificando imagem Base64...
ThermalPrinter: Aplicando algoritmo Floyd-Steinberg...
ThermalPrinter: Conectado a: InnerPrinter
ThermalPrinter: Impressão concluída com sucesso!
```

## 🔧 Troubleshooting

### Erro: "SDK location not found"

Crie o arquivo `android/local.properties`:
```properties
sdk.dir=C:\\Users\\SEU_USUARIO\\AppData\\Local\\Android\\Sdk
```

### Erro: "Execution failed for task ':app:mergeReleaseResources'"

Execute:
```bash
cd android
./gradlew clean
cd ..
npx expo run:android
```

### Erro: "Module not found: expo-thermal-printer"

Execute novamente:
```bash
npx expo prebuild --clean
```

### App não encontra impressoras

1. Verifique se Bluetooth está ativado
2. Pareie a impressora nas configurações do Android
3. Conceda permissões quando solicitado
4. Para Moderninha, procure por "InnerPrinter" ou "Mpos"

## 📊 O que Esperar

### ✅ Funcionando Corretamente:
- App abre sem erros
- Botão de buscar impressoras funciona
- Permissões são solicitadas
- Impressoras aparecem na lista
- Ao clicar em "Imprimir Texto", a impressora imprime
- Imagens saem com qualidade (pontilhado nítido)

### ❌ Se algo der errado:
1. Verifique logs: `adb logcat -s ThermalPrinter`
2. Verifique permissões concedidas
3. Teste primeiro com texto antes de imagem
4. Certifique-se que a impressora está pareada

## 🎯 Próximos Passos Após Testar

1. **Se funcionar:**
   - Integre no seu app real
   - Use `react-native-view-shot` para capturar recibos
   - Customize o layout de impressão

2. **Se não funcionar:**
   - Compartilhe os logs do Logcat
   - Verifique qual erro específico aparece
   - Teste em outra impressora Bluetooth

## 💡 Dicas Importantes

- **Moderninha**: Bobina de 58mm (384 pixels)
- **Sempre use dithering** para imagens: `applyDithering: true`
- **Teste primeiro com texto** antes de imagens
- **Logs são seus amigos**: `adb logcat -s ThermalPrinter`

## 🚀 Comando Rápido (Tudo de Uma Vez)

```bash
# Limpar, buildar e instalar
cd android && ./gradlew clean && cd .. && npx expo run:android
```

---

**Pronto!** Agora é só executar `npm run android` ou gerar o APK! 🎉
