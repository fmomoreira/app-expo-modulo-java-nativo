# 🧪 Teste de Comunicação React Native → Kotlin

## Objetivo
Verificar se o módulo Kotlin está recebendo corretamente as imagens Base64 do React Native.

## Teste Rápido

Adicione este código temporário no seu `app/(tabs)/index.tsx`:

```typescript
// Adicionar no topo, junto com os outros imports
import * as ThermalPrinter from '@/modules/expo-thermal-printer';

// Adicionar esta função de teste
const testarComunicacaoKotlin = async () => {
  try {
    console.log('=== TESTE DE COMUNICAÇÃO KOTLIN ===');
    
    // Imagem 1x1 pixel branco em Base64 (PNG válido)
    const testBase64 = 'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==';
    
    console.log('1. Tamanho do Base64:', testBase64.length);
    console.log('2. Primeiros 20 caracteres:', testBase64.substring(0, 20));
    console.log('3. Enviando para Kotlin...');
    
    const result = await ThermalPrinter.printImage(testBase64, {
      paperWidth: 58,
      dpi: 203,
      applyDithering: true,
    });
    
    console.log('4. ✅ SUCESSO! Resultado:', result);
    Alert.alert('✅ Comunicação OK', JSON.stringify(result, null, 2));
    
  } catch (error: any) {
    console.error('5. ❌ ERRO:', error);
    Alert.alert('❌ Erro na Comunicação', error.message || String(error));
  }
};

// Adicionar botão na UI (dentro do ScrollView, antes do template)
<TouchableOpacity
  style={[styles.button, { backgroundColor: '#9C27B0' }]}
  onPress={testarComunicacaoKotlin}
>
  <Text style={styles.buttonText}>🧪 Testar Comunicação Kotlin</Text>
</TouchableOpacity>
```

## O que este teste faz:

1. ✅ Cria uma imagem Base64 válida (1x1 pixel branco)
2. ✅ Envia para o módulo Kotlin
3. ✅ Verifica se o Kotlin consegue decodificar
4. ✅ Mostra logs detalhados

## Resultados Esperados:

### ✅ Se funcionar:
```
=== TESTE DE COMUNICAÇÃO KOTLIN ===
1. Tamanho do Base64: 68
2. Primeiros 20 caracteres: iVBORw0KGgoAAAANSU
3. Enviando para Kotlin...
4. ✅ SUCESSO! Resultado: { success: true, message: "Imagem impressa com sucesso" }
```

### ❌ Se falhar:
```
=== TESTE DE COMUNICAÇÃO KOTLIN ===
1. Tamanho do Base64: 68
2. Primeiros 20 caracteres: iVBORw0KGgoAAAANSU
3. Enviando para Kotlin...
5. ❌ ERRO: NO_PRINTER: Nenhuma impressora conectada
```

## Logs do Kotlin (adb logcat):

```bash
# Rodar no terminal:
adb logcat -s ThermalPrinter

# Logs esperados:
[ThermalPrinter] Iniciando processo de impressão de imagem...
[ThermalPrinter] Configurações: paperWidth=58 mm, dpi=203, dithering=true
[ThermalPrinter] Decodificando imagem Base64...
[ThermalPrinter] Imagem decodificada: 1x1 pixels
[ThermalPrinter] Redimensionando imagem para bobina de 58 mm...
[ThermalPrinter] Imagem redimensionada: 1x1 pixels
[ThermalPrinter] Aplicando algoritmo Floyd-Steinberg...
[ThermalPrinter] Dithering aplicado com sucesso
[ThermalPrinter] Convertendo bitmap para comandos ESC/POS...
[ThermalPrinter] Dimensões finais do bitmap: 1x1
[ThermalPrinter] Bitmap convertido para hexadecimal (X caracteres)
[ThermalPrinter] Impressão de imagem concluída com sucesso!
```

## Interpretação dos Resultados:

### Cenário 1: "NO_PRINTER: Nenhuma impressora conectada"
- ✅ **Comunicação Kotlin está OK!**
- ❌ Falta conectar impressora antes
- **Solução:** Conectar impressora primeiro, depois testar

### Cenário 2: "DECODE_ERROR: Falha ao decodificar imagem Base64"
- ❌ **Problema na comunicação!**
- Base64 não está chegando corretamente
- **Solução:** Verificar ponte TypeScript

### Cenário 3: "Imagem impressa com sucesso"
- ✅ **TUDO FUNCIONANDO!**
- Kotlin recebeu, decodificou e processou
- **Conclusão:** Problema está no `react-native-view-shot`

---

**Rode este teste AGORA e me envie o resultado!**
