import React, { useState } from 'react';
import {
  ActivityIndicator,
  Alert,
  PermissionsAndroid,
  Platform,
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
    { titulo: "Giro Extra", valor: "500" },
    { titulo: "Giro extra", valor: "500" },
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
        "007100-2", "007229-2", "007508-2", "007639-2", "007762-2",
      ],
    },
    {
      bookletNumber: 214,
      lotNumber: 2,
      tickets: [
        "004795-2", "004810-2", "004839-2", "005159-2", "005295-2",
        "005829-2", "006292-2", "006399-2", "006666-2", "006752-2",
        "006873-2", "006961-2", "007034-2", "007071-2", "007230-2",
        "007409-2", "007461-2", "007483-2", "007701-2", "007902-2",
      ],
    },
  ],
  urlSorteio: "https://reinodasorte.com.br",
};

async function ensureLocationPermissions(): Promise<boolean> {
  const fine = await PermissionsAndroid.check(
    PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION
  );
  if (fine) return true;

  const granted = await PermissionsAndroid.requestMultiple([
    PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION,
    PermissionsAndroid.PERMISSIONS.ACCESS_COARSE_LOCATION,
  ]);

  return (
    granted[PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION] ===
    PermissionsAndroid.RESULTS.GRANTED
  );
}

export default function ReinoSorteScreen() {
  const [loading, setLoading] = useState(false);
  const [status, setStatus] = useState('Aguardando...');

  const handlePrint = async () => {
    setLoading(true);
    try {
      // 1. Permissoes Android 7-11 (Localizacao para Bluetooth)
      setStatus('Verificando permissoes...');
      const hasPermission = await ensureLocationPermissions();
      if (!hasPermission) {
        Alert.alert(
          'Permissao Necessaria',
          'Para usar Bluetooth, conceda a permissao de Localizacao.\n\nVa em: Configuracoes > Apps > print-app > Permissoes > Localizacao > Permitir'
        );
        setStatus('Permissao negada');
        return;
      }

      // 2. Buscar primeira impressora disponivel
      setStatus('Buscando impressora...');
      const devices = await getPairedPrinters();

      if (devices.length === 0) {
        Alert.alert('Aviso', 'Nenhuma impressora Bluetooth pareada encontrada.\n\nPareie a impressora nas configuracoes do Android primeiro.');
        setStatus('Nenhuma impressora encontrada');
        return;
      }

      const printer = devices[0];
      setStatus(`Conectando: ${printer.name}...`);

      // 3. Conectar automaticamente na primeira impressora
      const connResult = await connectPrinter(printer.address);
      if (!connResult.success) {
        Alert.alert('Erro', 'Falha ao conectar na impressora');
        setStatus('Falha na conexao');
        return;
      }

      // 4. Imprimir bilhete
      setStatus(`Imprimindo em ${printer.name}...`);
      const printResult = await printLotteryTicket(SAMPLE_TICKET_DATA, {
        paperWidth: 58,
      });

      if (printResult.success) {
        setStatus(`Impresso em ${printer.name}`);
      } else {
        Alert.alert('Erro', printResult.message || 'Falha ao imprimir');
        setStatus('Erro na impressao');
      }

      // 5. Desconectar apos imprimir
      await disconnectPrinter();

    } catch (error: any) {
      Alert.alert('Erro', error.message || 'Erro inesperado');
      setStatus('Erro: ' + (error.message || 'desconhecido'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.headerTitle}>Reino da Sorte</Text>
        <Text style={styles.headerSubtitle}>Impressao de Bilhetes</Text>
        <Text style={styles.androidVersion}>
          Android API {Platform.Version}
        </Text>
      </View>

      <View style={styles.content}>
        <View style={styles.statusCard}>
          <Text style={styles.statusLabel}>Status:</Text>
          <Text style={styles.statusText}>{status}</Text>
        </View>

        <TouchableOpacity
          style={[styles.printButton, loading && styles.printButtonDisabled]}
          onPress={handlePrint}
          disabled={loading}
          activeOpacity={0.7}
        >
          {loading ? (
            <View style={styles.printButtonContent}>
              <ActivityIndicator size="small" color="#fff" />
              <Text style={styles.printButtonText}>IMPRIMINDO...</Text>
            </View>
          ) : (
            <Text style={styles.printButtonText}>IMPRIMIR BILHETE</Text>
          )}
        </TouchableOpacity>

        <Text style={styles.infoText}>
          Ao clicar, o app solicita permissoes, conecta na primeira impressora pareada e imprime automaticamente.
        </Text>
      </View>
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
    alignItems: 'center',
  },
  headerTitle: {
    fontSize: 32,
    fontWeight: 'bold',
    color: '#FFD700',
  },
  headerSubtitle: {
    fontSize: 16,
    color: '#e0e0e0',
    marginTop: 4,
  },
  androidVersion: {
    fontSize: 12,
    color: '#FFD700',
    marginTop: 8,
    fontWeight: '600',
    backgroundColor: 'rgba(255, 215, 0, 0.15)',
    paddingHorizontal: 12,
    paddingVertical: 4,
    borderRadius: 12,
  },
  content: {
    flex: 1,
    justifyContent: 'center',
    paddingHorizontal: 24,
  },
  statusCard: {
    backgroundColor: '#16213e',
    padding: 16,
    borderRadius: 10,
    borderWidth: 1,
    borderColor: '#333',
    marginBottom: 30,
    alignItems: 'center',
  },
  statusLabel: {
    fontSize: 13,
    color: '#888',
    marginBottom: 4,
  },
  statusText: {
    fontSize: 16,
    color: '#FFD700',
    fontWeight: '600',
    textAlign: 'center',
  },
  printButton: {
    backgroundColor: '#e94560',
    paddingVertical: 24,
    borderRadius: 12,
    alignItems: 'center',
    justifyContent: 'center',
    elevation: 4,
  },
  printButtonDisabled: {
    backgroundColor: '#7a2535',
  },
  printButtonContent: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
  },
  printButtonText: {
    color: '#fff',
    fontSize: 20,
    fontWeight: 'bold',
    letterSpacing: 1,
  },
  infoText: {
    fontSize: 13,
    color: '#666',
    textAlign: 'center',
    marginTop: 20,
    lineHeight: 18,
  },
});
