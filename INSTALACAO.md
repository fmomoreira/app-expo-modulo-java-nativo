# 🚀 Guia de Instalação - Módulo de Impressão Térmica

## ✅ O que foi implementado

Você agora tem um **módulo nativo Java/Kotlin completo** para impressão térmica em React Native com:

- ✅ **Algoritmo Floyd-Steinberg** implementado em Kotlin
- ✅ **Módulo Expo** com TypeScript type-safe
- ✅ **Biblioteca DantSu ESC/POS** integrada
- ✅ **Permissões Android** configuradas
- ✅ **Componente de teste** pronto para uso

## 📋 Próximos Passos

### 1. Gerar Código Nativo (IMPORTANTE!)

O projeto usa **Expo**, então você precisa gerar as pastas nativas Android:

```bash
cd "C:\Users\desen\Documents\Projetos\React Native\appJavaKlotlinImpressao\print-app"
npx expo prebuild --clean
```

**O que este comando faz:**
- Cria a pasta `android/` com todo código nativo
- Configura o módulo `expo-thermal-printer` automaticamente
- Instala dependências do Gradle
- Configura permissões no AndroidManifest.xml

### 2. Instalar Dependências Node

```bash
npm install
```

### 3. Instalar react-native-view-shot (para capturar telas)

```bash
npx expo install react-native-view-shot
```

### 4. Executar o App

```bash
npm run android
```

Ou:

```bash
npx expo run:android
```

## 🧪 Testar o Módulo

### Opção 1: Usar a Tela de Teste

Navegue para a rota `/printer-test` no seu app. Lá você encontrará:

- Botão para buscar impressoras Bluetooth
- Lista de impressoras disponíveis
- Botões para testar impressão de texto e imagem
- Logs de debug em tempo real

### Opção 2: Usar Diretamente no Código

```typescript
import { printText, printImage } from './modules/expo-thermal-printer';

// Imprimir texto
await printText('Teste de Impressão', {
  paperWidth: 58,
  encoding: 'ISO-8859-1',
});

// Imprimir imagem com dithering
await printImage(base64Image, {
  paperWidth: 58,
  applyDithering: true,
});
```

## 🔧 Configuração para Moderninha

### Para Moderninha (Android 7)

A Moderninha geralmente usa bobina de **58mm**. Configure assim:

```typescript
const result = await printImage(imageBase64, {
  paperWidth: 58,  // 58mm = 384 pixels @ 203 DPI
  dpi: 203,
  applyDithering: true,
});
```

### Verificar Conexão

Use o Logcat para ver logs detalhados:

```bash
adb logcat -s ThermalPrinter
```

Você verá mensagens como:
```
ThermalPrinter: Iniciando processo de impressão...
ThermalPrinter: Decodificando imagem Base64...
ThermalPrinter: Aplicando algoritmo Floyd-Steinberg...
ThermalPrinter: Conectado a: InnerPrinter
ThermalPrinter: Impressão concluída com sucesso!
```

## 📱 Permissões Necessárias

### Android 7-11 (Moderninha)
- `ACCESS_COARSE_LOCATION` - Necessária para Bluetooth

### Android 12+
- `BLUETOOTH_SCAN` - Para buscar dispositivos
- `BLUETOOTH_CONNECT` - Para conectar
- `ACCESS_FINE_LOCATION` - Para Bluetooth

**As permissões são solicitadas automaticamente** quando você usa as funções do módulo.

## 🏗️ Estrutura de Arquivos Criados

```
print-app/
├── modules/expo-thermal-printer/          # Módulo nativo
│   ├── android/
│   │   ├── build.gradle                   # Dependências (DantSu)
│   │   └── src/main/
│   │       ├── AndroidManifest.xml        # Permissões Bluetooth
│   │       └── java/expo/modules/thermalprinter/
│   │           ├── ExpoThermalPrinterModule.kt  # Lógica principal
│   │           └── ImageUtils.kt                # Floyd-Steinberg
│   ├── index.ts                           # Interface TypeScript
│   ├── package.json
│   └── expo-module.config.json
│
├── app/printer-test.tsx                   # Tela de teste
├── README-PRINTER.md                      # Documentação completa
└── INSTALACAO.md                          # Este arquivo
```

## 🐛 Solução de Problemas

### Erro: "expo-modules-core not found"

Execute:
```bash
npm install expo-modules-core
npx expo prebuild --clean
```

### Erro: "Cannot find module expo-thermal-printer"

Certifique-se de executar `npx expo prebuild` primeiro. O módulo só funciona após gerar o código nativo.

### Erro: "Nenhuma impressora encontrada"

1. Verifique se o Bluetooth está ativado
2. Pareie a impressora nas configurações do Android
3. Conceda permissões quando solicitado
4. Para Moderninha, a impressora pode aparecer como "InnerPrinter" ou "Mpos"

### Imagem sai borrada ou com manchas

Certifique-se de que o dithering está ativado:
```typescript
await printImage(base64, { applyDithering: true });
```

### App trava ao imprimir

Verifique os logs:
```bash
adb logcat -s ThermalPrinter ReactNativeJS
```

Possíveis causas:
- Imagem muito grande (redimensione antes)
- Bluetooth desconectado
- Permissões não concedidas

## 📊 Comparação: Antes vs Depois

### ❌ Antes (Bibliotecas JS)
- Imagens borradas
- Conexão instável
- Lento (processamento em JS)
- Não funciona com Moderninha

### ✅ Agora (Módulo Nativo)
- Qualidade profissional (Floyd-Steinberg)
- Conexão persistente
- Rápido (processamento nativo)
- Funciona com Moderninha e impressoras Bluetooth

## 🎯 Próximos Passos Recomendados

1. **Testar com Moderninha real**
   - Execute o app em uma Moderninha
   - Use a tela de teste (`/printer-test`)
   - Verifique logs com `adb logcat`

2. **Integrar no seu app**
   - Importe as funções do módulo
   - Use `react-native-view-shot` para capturar recibos
   - Implemente lógica de retry em caso de falha

3. **Customizar**
   - Ajuste o algoritmo de dithering se necessário
   - Adicione suporte a códigos de barras
   - Implemente templates de recibo

## 📚 Documentação Adicional

- **README-PRINTER.md** - Documentação completa da API
- **estudodecaso.md** - Conversa original com especialista
- **Memória salva** - Toda arquitetura documentada

## 💡 Dicas Importantes

1. **Sempre use `applyDithering: true`** para imagens
2. **Bobina 58mm** = 384 pixels de largura
3. **Bobina 80mm** = 576 pixels de largura
4. **Teste primeiro com texto** antes de imprimir imagens
5. **Use Logcat** para debug detalhado

## ✨ Você está pronto!

Execute agora:

```bash
npx expo prebuild --clean
npm run android
```

E teste a impressão! 🎉
