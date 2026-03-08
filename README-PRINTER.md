# 🖨️ Módulo de Impressão Térmica - React Native

Este projeto implementa um **módulo nativo Java/Kotlin** para impressão térmica robusta em React Native com Expo, replicando a qualidade do app RawBT.

## 🎯 Características

- ✅ **Algoritmo Floyd-Steinberg** para dithering profissional
- ✅ **Suporte ESC/POS** completo via biblioteca DantSu
- ✅ **Bluetooth** para impressoras externas e Moderninha
- ✅ **TypeScript** com tipos completos
- ✅ **Expo Modules** - arquitetura moderna
- ✅ **Android 7+** compatível

## 📋 Pré-requisitos

1. **Node.js** 18+ instalado
2. **Android Studio** configurado
3. **Java JDK** 11+
4. **Expo CLI** instalado globalmente

```bash
npm install -g expo-cli
```

## 🚀 Instalação

### 1. Instalar Dependências

```bash
cd print-app
npm install
```

### 2. Gerar Código Nativo (Prebuild)

```bash
npx expo prebuild --clean
```

Este comando irá:
- Gerar as pastas `android/` e `ios/`
- Configurar o módulo nativo automaticamente
- Instalar dependências nativas

### 3. Instalar Dependências Android

```bash
cd android
./gradlew clean
cd ..
```

## 🏃 Executar o App

### Modo Development

```bash
npm run android
```

Ou com Expo:

```bash
npx expo run:android
```

### Build de Produção

```bash
cd android
./gradlew assembleRelease
```

## 📱 Como Usar

### Importar o Módulo

```typescript
import {
  printImage,
  printText,
  getPairedPrinters,
  connectPrinter,
  disconnectPrinter,
} from './modules/expo-thermal-printer';
```

### Exemplo: Imprimir Texto

```typescript
import { printText } from './modules/expo-thermal-printer';

const handlePrint = async () => {
  try {
    const result = await printText('Olá, Mundo!', {
      paperWidth: 58, // 58mm ou 80mm
      encoding: 'ISO-8859-1',
    });
    
    console.log(result.message);
  } catch (error) {
    console.error('Erro:', error);
  }
};
```

### Exemplo: Imprimir Imagem com Dithering

```typescript
import { printImage } from './modules/expo-thermal-printer';
import { captureRef } from 'react-native-view-shot';

const handlePrintImage = async () => {
  try {
    // Captura a tela como Base64
    const uri = await captureRef(viewRef, {
      format: 'png',
      quality: 1,
      result: 'base64',
    });
    
    // Imprime com dithering Floyd-Steinberg
    const result = await printImage(uri, {
      paperWidth: 58,
      dpi: 203,
      applyDithering: true, // Ativa o algoritmo de pontilhamento
    });
    
    console.log(result.message);
  } catch (error) {
    console.error('Erro:', error);
  }
};
```

### Exemplo: Buscar e Conectar Impressoras

```typescript
import { getPairedPrinters, connectPrinter } from './modules/expo-thermal-printer';

const setupPrinter = async () => {
  // Busca impressoras pareadas
  const printers = await getPairedPrinters();
  console.log('Impressoras:', printers);
  
  // Conecta à primeira impressora
  if (printers.length > 0) {
    await connectPrinter(printers[0].address);
    console.log('Conectado!');
  }
};
```

## 🔐 Permissões

### Android 7-11

O módulo solicita automaticamente:
- `BLUETOOTH`
- `BLUETOOTH_ADMIN`
- `ACCESS_COARSE_LOCATION`

### Android 12+

Permissões adicionais:
- `BLUETOOTH_SCAN`
- `BLUETOOTH_CONNECT`
- `ACCESS_FINE_LOCATION`

## 🛠️ Estrutura do Projeto

```
print-app/
├── modules/
│   └── expo-thermal-printer/
│       ├── android/
│       │   ├── build.gradle                    # Configuração Gradle
│       │   └── src/main/
│       │       ├── AndroidManifest.xml         # Permissões
│       │       └── java/expo/modules/thermalprinter/
│       │           ├── ExpoThermalPrinterModule.kt  # Módulo principal
│       │           └── ImageUtils.kt                # Algoritmo Floyd-Steinberg
│       ├── index.ts                            # Interface TypeScript
│       ├── package.json
│       └── expo-module.config.json
├── app/
│   └── printer-test.tsx                        # Tela de teste
└── package.json
```

## 🧮 Algoritmo Floyd-Steinberg

O módulo implementa o algoritmo de dithering Floyd-Steinberg para converter imagens coloridas em preto e branco puro (1-bit) com qualidade profissional.

### Como Funciona

1. **Luminância**: Calcula o brilho de cada pixel usando pesos perceptuais
   ```
   L = 0.299*R + 0.587*G + 0.114*B
   ```

2. **Quantização**: Decide se o pixel será preto (0) ou branco (255)

3. **Difusão de Erro**: Distribui o erro para pixels vizinhos
   - Direita: 7/16
   - Baixo-esquerda: 3/16
   - Baixo: 5/16
   - Baixo-direita: 1/16

### Resultado

Imagens com pontilhado nítido, similar a jornais impressos, ideal para impressoras térmicas que só entendem preto/branco.

## 📦 Dependências Nativas

### DantSu/ESCPOS-ThermalPrinter-Android

Biblioteca Java para comunicação ESC/POS:
- Conversão de imagens para comandos térmicos
- Suporte Bluetooth, USB e Wi-Fi
- Gerenciamento de conexão

```gradle
implementation 'com.github.DantSu:ESCPOS-ThermalPrinter-Android:3.3.0'
```

## 🐛 Troubleshooting

### Erro: "Nenhuma impressora encontrada"

1. Verifique se o Bluetooth está ativado
2. Pareie a impressora nas configurações do Android
3. Conceda as permissões de Bluetooth

### Erro: "Cannot resolve symbol"

Execute o prebuild novamente:
```bash
npx expo prebuild --clean
```

### Imagem sai borrada

Certifique-se de que `applyDithering: true` está configurado:
```typescript
await printImage(base64, { applyDithering: true });
```

### Logs de Debug

Use o Logcat para ver logs detalhados:
```bash
adb logcat -s ThermalPrinter
```

## 📚 Referências

- [Expo Modules API](https://docs.expo.dev/modules/overview/)
- [DantSu ESC/POS Library](https://github.com/DantSu/ESCPOS-ThermalPrinter-Android)
- [Floyd-Steinberg Dithering](https://en.wikipedia.org/wiki/Floyd%E2%80%93Steinberg_dithering)
- [ESC/POS Commands](https://reference.epson-biz.com/modules/ref_escpos/index.php)

## 📄 Licença

MIT

## 👨‍💻 Autor

Desenvolvido com base em estudo de caso sobre impressão térmica robusta em React Native.
