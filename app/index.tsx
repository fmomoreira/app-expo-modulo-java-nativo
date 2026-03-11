import React, { useState } from 'react';
import {
  ActivityIndicator,
  Alert,
  Platform,
  ScrollView,
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
} from 'react-native';
import {
  connectPrinter,
  disconnectPrinter,
  getPairedPrinters,
  LotteryTicketData,
  PrinterDevice,
  printLotteryTicket,
} from '../modules/expo-thermal-printer';

const SAMPLE_TICKET_DATA: LotteryTicketData = {
  id: "560",
  customerName: "Fabio Junior",
  customerPhone: "(87) 1 2358-8975",
  drawerName: "Drawer Name",
  drawerDescription: "Drawer Description",
  sellerName: "João Silva",
  sellerPhone: "(87) 9 9999-9999",
  drawTitle: "Teste de Sorteio",
  drawDate: "2026-03-14T19:00:00.000Z",
  pricePerTicket: "2",
  mainPrizeValue: "50000",
  extraPrizeValue: [
    {
      titulo: "Giro Extra",
      valor: "500"
    },
    {
      titulo: "Giro extra",
      valor: "500"
    }
  ],
  quantityBooklets: 2,
  totalTickets: 40,
  totalAmount: "4.00",
  paymentMethod: "pix",
  createdAt: "2026-03-11T00:33:03.479Z",
  booklets: [
    {
      bookletNumber: 212,
      lotNumber: 2,
      tickets: [
        "004038-2", "004155-2", "004394-2", "004565-2", "004654-2",
        "004729-2", "004943-2", "005202-2", "005303-2", "005406-2",
        "005620-2", "006263-2", "006279-2", "006287-2", "006491-2",
        "007100-2", "007229-2", "007508-2", "007639-2", "007762-2"
      ]
    },
    {
      bookletNumber: 214,
      lotNumber: 2,
      tickets: [
        "004795-2", "004810-2", "004839-2", "005159-2", "005295-2",
        "005829-2", "006292-2", "006399-2", "006666-2", "006752-2",
        "006873-2", "006961-2", "007034-2", "007071-2", "007230-2",
        "007409-2", "007461-2", "007483-2", "007701-2", "007902-2"
      ]
    }
  ],
  urlSorteio: "https://reinodasorte.com.br"
};

export default function ReinoSorteScreen() {
  const [loading, setLoading] = useState(false);
  const [printers, setPrinters] = useState<PrinterDevice[]>([]);
  const [connectedPrinter, setConnectedPrinter] = useState<string | null>(null);

  const handleGetPrinters = async () => {
    setLoading(true);
    try {
      const devices = await getPairedPrinters();
      setPrinters(devices);
      
      if (devices.length === 0) {
        Alert.alert('Aviso', 'Nenhuma impressora Bluetooth pareada encontrada');
      }
    } catch (error: any) {
      Alert.alert('Erro', error.message || 'Erro ao buscar impressoras');
    } finally {
      setLoading(false);
    }
  };

  const handleConnectPrinter = async (address: string, name: string) => {
    setLoading(true);
    try {
      const result = await connectPrinter(address);
      if (result.success) {
        setConnectedPrinter(name);
        Alert.alert('Sucesso', `Conectado à ${name}`);
      }
    } catch (error: any) {
      Alert.alert('Erro', error.message || 'Erro ao conectar');
    } finally {
      setLoading(false);
    }
  };

  const handleDisconnect = async () => {
    setLoading(true);
    try {
      const result = await disconnectPrinter();
      if (result.success) {
        setConnectedPrinter(null);
        Alert.alert('Sucesso', 'Desconectado da impressora');
      }
    } catch (error: any) {
      Alert.alert('Erro', error.message || 'Erro ao desconectar');
    } finally {
      setLoading(false);
    }
  };

  const handlePrintLotteryTicket = async () => {
    if (!connectedPrinter) {
      Alert.alert('Erro', 'Conecte-se a uma impressora primeiro');
      return;
    }

    setLoading(true);
    try {
      const result = await printLotteryTicket(SAMPLE_TICKET_DATA, {
        paperWidth: 58,
      });

      if (result.success) {
        Alert.alert('✅ Sucesso', result.message || 'Bilhetes impressos com sucesso!');
      } else {
        Alert.alert('❌ Erro', result.message || 'Falha ao imprimir bilhetes');
      }
    } catch (error: any) {
      Alert.alert('❌ Erro', error.message || 'Erro ao imprimir bilhetes');
    } finally {
      setLoading(false);
    }
  };

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.headerTitle}>🎰 Reino da Sorte</Text>
        <Text style={styles.headerSubtitle}>Impressão de Bilhetes</Text>
        
        <Text style={styles.androidVersion}>
          📱 Android API {Platform.Version}
        </Text>
        
        {connectedPrinter && (
          <Text style={styles.connectedText}>✓ Conectado: {connectedPrinter}</Text>
        )}
      </View>

      <ScrollView style={styles.content}>
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>📡 Conectar Impressora</Text>
          
          <TouchableOpacity
            style={styles.button}
            onPress={handleGetPrinters}
            disabled={loading}
          >
            <Text style={styles.buttonText}>
              {loading ? 'Buscando...' : '🔍 Buscar Impressoras'}
            </Text>
          </TouchableOpacity>

          {printers.length > 0 && (
            <View style={styles.printerList}>
              {printers.map((printer, index) => (
                <TouchableOpacity
                  key={index}
                  style={styles.printerItem}
                  onPress={() => handleConnectPrinter(printer.address, printer.name)}
                  disabled={loading}
                >
                  <Text style={styles.printerName}>{printer.name}</Text>
                  <Text style={styles.printerAddress}>{printer.address}</Text>
                </TouchableOpacity>
              ))}
            </View>
          )}
        </View>

        {connectedPrinter && (
          <>
            <View style={styles.section}>
              <Text style={styles.sectionTitle}>🖨️ Imprimir Bilhete</Text>
              
              <TouchableOpacity
                style={[styles.button, styles.printButton]}
                onPress={handlePrintLotteryTicket}
                disabled={loading}
              >
                <Text style={styles.buttonText}>
                  {loading ? 'Imprimindo...' : '🎫 IMPRIMIR BILHETE REINO DA SORTE'}
                </Text>
              </TouchableOpacity>

              <TouchableOpacity
                style={[styles.button, styles.disconnectButton]}
                onPress={handleDisconnect}
                disabled={loading}
              >
                <Text style={styles.buttonText}>🔌 Desconectar Impressora</Text>
              </TouchableOpacity>
            </View>
          </>
        )}
      </ScrollView>

      {loading && (
        <View style={styles.loadingOverlay}>
          <ActivityIndicator size="large" color="#FFD700" />
          <Text style={styles.loadingText}>Processando...</Text>
        </View>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#1a1a2e',
  },
  header: {
    backgroundColor: '#16213e',
    padding: 20,
    paddingTop: 60,
    borderBottomWidth: 3,
    borderBottomColor: '#FFD700',
  },
  headerTitle: {
    fontSize: 32,
    fontWeight: 'bold',
    color: '#FFD700',
  },
  headerSubtitle: {
    fontSize: 18,
    color: '#e0e0e0',
    marginTop: 5,
  },
  androidVersion: {
    fontSize: 12,
    color: '#FFD700',
    marginTop: 8,
    fontWeight: '600',
    backgroundColor: 'rgba(255, 215, 0, 0.2)',
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 4,
    alignSelf: 'flex-start',
  },
  connectedText: {
    fontSize: 14,
    color: '#4ade80',
    marginTop: 8,
    fontWeight: '600',
  },
  content: {
    flex: 1,
  },
  section: {
    backgroundColor: '#16213e',
    margin: 15,
    padding: 20,
    borderRadius: 10,
    borderWidth: 2,
    borderColor: '#FFD700',
  },
  sectionTitle: {
    fontSize: 20,
    fontWeight: '600',
    marginBottom: 15,
    color: '#FFD700',
  },
  button: {
    backgroundColor: '#0f3460',
    padding: 18,
    borderRadius: 8,
    alignItems: 'center',
    marginTop: 10,
    borderWidth: 1,
    borderColor: '#FFD700',
  },
  printButton: {
    backgroundColor: '#e94560',
    paddingVertical: 20,
  },
  disconnectButton: {
    backgroundColor: '#533483',
    marginTop: 10,
  },
  buttonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: '600',
  },
  printerList: {
    marginTop: 15,
  },
  printerItem: {
    backgroundColor: '#0f3460',
    padding: 15,
    borderRadius: 6,
    marginBottom: 10,
    borderLeftWidth: 4,
    borderLeftColor: '#FFD700',
  },
  printerName: {
    fontSize: 16,
    fontWeight: '600',
    color: '#fff',
  },
  printerAddress: {
    fontSize: 12,
    color: '#a0a0a0',
    marginTop: 4,
  },
  loadingOverlay: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    backgroundColor: 'rgba(0,0,0,0.8)',
    justifyContent: 'center',
    alignItems: 'center',
  },
  loadingText: {
    color: '#FFD700',
    marginTop: 10,
    fontSize: 16,
    fontWeight: '600',
  },
});
