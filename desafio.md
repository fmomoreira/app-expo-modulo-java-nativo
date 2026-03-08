1. O "Check-up" de Memória (Bitmap Recycling)
Como as maquininhas de cartão costumam ter pouca memória RAM (geralmente 1GB ou 2GB), processar Bitmaps repetidamente pode causar o erro OutOfMemory.

No seu método printImage, logo após o printer.printFormattedText(...), adicione este comando para liberar o hardware:

Kotlin
// No final do bloco try, após a impressão:
bitmap?.recycle() // Libera a memória da imagem processada
Log.d(TAG, "Memória do Bitmap liberada.")
2. Tratamento de Encodings (Acentuação)
No Brasil, usamos muitos acentos (ç, á, é). Se o texto sair com símbolos estranhos, é porque a impressora espera um "Code Page" específico. No seu printText, você já previu o ISO-8859-1, o que é ótimo.

Dica técnica: Se notar que os acentos ainda falham, tente forçar o comando de Code Page no início do texto:
printer.printFormattedText("[C]<font cp=\"850\"><b>$text</b></font>\n") (O CP850 é muito comum em impressoras térmicas brasileiras).

3. O Método de Auto-Teste (Diagnostic)
Adicione esta função ao seu ModuleDefinition para que o seu usuário possa testar a guilhotina e a cabeça de impressão sem precisar gerar um recibo real:

Kotlin
AsyncFunction("selfTest") { promise: Promise ->
    try {
        val printer = getOrCreatePrinter(58, 203)
        printer?.printFormattedText(
            "[C]<img>" + "..." + "</img>\n" + // Coloque um mini logo aqui
            "[C]<b>TESTE DE IMPRESSÃO</b>\n" +
            "[L]--------------------------------\n" +
            "[L]Alinhamento Esquerdo\n" +
            "[C]Alinhamento Central\n" +
            "[R]Alinhamento Direito\n" +
            "[L]<b>Negrito</b> [L]<i>Itálico</i>\n" +
            "[L]--------------------------------\n" +
            "[C]<barcode type=\"ean13\" height=\"10\">1234567890128</barcode>\n" +
            "[L]\n" + "[L]\n"
        )
        promise.resolve("Teste concluído")
    } catch (e: Exception) {
        promise.reject("TEST_ERROR", e.message, e)
    }
}


sugestao apra interface no react native

O Gemini disse
Excelente! Ter uma interface de usuário (UI) limpa para que o usuário possa escolher a impressora (caso o auto-detect não seja o que ele quer) é o que diferencia um app amador de um profissional.

Como o seu método getPairedPrinters retorna uma lista de objetos com name, address e type, vamos criar um componente que exibe esses dispositivos em "cards" elegantes.

Componente de Seleção de Impressora (React Native + TypeScript)
Este componente faz três coisas:

Busca as impressoras ao abrir a tela.

Lista os dispositivos com ícones diferentes para Bluetooth e USB.

Conecta ao clicar e salva a preferência.

TypeScript
import React, { useEffect, useState } from 'react';
import { View, Text, FlatList, TouchableOpacity, StyleSheet, ActivityIndicator, Alert } from 'react-native';
import { NativeModulesProxy } from 'expo-modules-core';

const { ExpoThermalPrinter } = NativeModulesProxy;

interface Printer {
  name: string;
  address: string;
  type: 'bluetooth' | 'usb' | 'internal';
}

export const PrinterSelector = () => {
  const [printers, setPrinters] = useState<Printer[]>([]);
  const [loading, setLoading] = useState(false);
  const [connecting, setConnecting] = useState<string | null>(null);

  const loadPrinters = async () => {
    setLoading(true);
    try {
      const list = await ExpoThermalPrinter.getPairedPrinters();
      setPrinters(list);
    } catch (error) {
      Alert.alert("Erro", "Não foi possível buscar as impressoras.");
    } finally {
      setLoading(false);
    }
  };

  const handleConnect = async (printer: Printer) => {
    setConnecting(printer.address);
    try {
      const result = await ExpoThermalPrinter.connectPrinter(printer.address);
      Alert.alert("Sucesso", result.message);
    } catch (error: any) {
      Alert.alert("Falha na Conexão", error.message);
    } finally {
      setConnecting(null);
    }
  };

  useEffect(() => {
    loadPrinters();
  }, []);

  const renderPrinterItem = ({ item }: { item: Printer }) => (
    <TouchableOpacity 
      style={styles.card} 
      onPress={() => handleConnect(item)}
      disabled={connecting !== null}
    >
      <View style={styles.info}>
        <Text style={styles.name}>{item.name}</Text>
        <Text style={styles.address}>{item.type.toUpperCase()} • {item.address}</Text>
      </View>
      {connecting === item.address ? (
        <ActivityIndicator color="#007AFF" />
      ) : (
        <Text style={styles.connectText}>Conectar</Text>
      )}
    </TouchableOpacity>
  );

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>Impressoras Disponíveis</Text>
        <TouchableOpacity onPress={loadPrinters}>
          <Text style={styles.refresh}>Atualizar</Text>
        </TouchableOpacity>
      </View>

      {loading ? (
        <ActivityIndicator size="large" style={{ marginTop: 20 }} />
      ) : (
        <FlatList
          data={printers}
          keyExtractor={(item) => item.address}
          renderItem={renderPrinterItem}
          ListEmptyComponent={<Text style={styles.empty}>Nenhuma impressora encontrada.</Text>}
        />
      )}
    </View>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, padding: 20, backgroundColor: '#f5f5f5' },
  header: { flexDirection: 'row', justifyContent: 'space-between', marginBottom: 20 },
  title: { fontSize: 18, fontWeight: 'bold' },
  refresh: { color: '#007AFF', fontWeight: '600' },
  card: { 
    backgroundColor: '#fff', 
    padding: 15, 
    borderRadius: 10, 
    flexDirection: 'row', 
    alignItems: 'center', 
    marginBottom: 10,
    elevation: 2 
  },
  info: { flex: 1 },
  name: { fontSize: 16, fontWeight: '500' },
  address: { fontSize: 12, color: '#666' },
  connectText: { color: '#007AFF', fontWeight: 'bold' },
  empty: { textAlign: 'center', marginTop: 50, color: '#999' }
});
Dicas de Ouro para a Experiência do Usuário (UX):
Feedback Visual: Note que usei um ActivityIndicator no card específico que está conectando. Isso evita que o usuário clique várias vezes enquanto o Bluetooth está negociando o "handshake".

Auto-seleção: No useEffect, se a lista retornar apenas uma impressora (comum em Moderninhas Smart onde só existe a interna), você pode disparar o handleConnect automaticamente para economizar um clique do usuário.

Persistência: Seria ideal usar um AsyncStorage para salvar o address da última impressora conectada com sucesso. Assim, da próxima vez que o app abrir, ele já tenta conectar nela em background.

Por que isso fecha o ciclo?
Com esse componente, o usuário tem controle total. Se ele estiver em uma Moderninha Smart, o seu código Kotlin vai detectar a USB interna. Se ele estiver usando uma impressora Bluetooth externa (tipo as da China ou Leopardo), ele também consegue parear e usar.