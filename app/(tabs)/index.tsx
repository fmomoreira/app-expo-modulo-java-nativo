import React, { useState } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  Alert,
  ScrollView,
  ActivityIndicator,
  PermissionsAndroid,
  Platform,
} from 'react-native';
import {
  getPairedPrinters,
  connectPrinter,
  disconnectPrinter,
  printTestImage,
  PrinterDevice,
} from '../../modules/expo-thermal-printer';
import { ReceiptTemplate, ReceiptItem } from '../../components/ReceiptTemplate';
import { TicketTemplate } from '../../components/TicketTemplate';
import { usePrintReceipt } from '../../hooks/usePrintReceipt';

export default function HomeScreen() {
  const [loading, setLoading] = useState(false);
  const [printers, setPrinters] = useState<PrinterDevice[]>([]);
  const [connectedPrinter, setConnectedPrinter] = useState<string | null>(null);
  
  // Hook para impressão do cupom restaurante
  const { receiptRef, printReceipt, isPrinting } = usePrintReceipt();
  
  // Hook para impressão do bilhete
  const { receiptRef: ticketRef, printReceipt: printTicket, isPrinting: isPrintingTicket } = usePrintReceipt();
  
  // Dados do cupom restaurante
  const sampleReceiptData: ReceiptItem[] = [
    { name: 'Coca-Cola 2L', price: 8.50, quantity: 2 },
    { name: 'Pão Francês (kg)', price: 12.00, quantity: 1 },
    { name: 'Queijo Minas (kg)', price: 35.00, quantity: 0.5 },
  ];
  const sampleTotal = 46.50;
  
  // Dados do bilhete Reino da Sorte
  const sampleTicketData = {
    premio: 'PRÊMIO: FIAT UNO 2013, MAIS 15 GIROS DE R$ 500,00.',
    dataSorteio: '28/02/2026',
    dataVenda: '28/02/2026 08:12',
    cliente: 'Felipe Morreira',
    telefone: '+55 (87)9 9159-1859',
    numeros: [
      '425242', '319201', '683165', '364418', '490819', '703627',
      '565632', '701317', '119099', '403894', '425242', '319201',
      '683165', '364418', '490819', '703627', '565632', '701317',
      '119099', '403894'
    ],
    vendedor: 'Banca Enfrente a Zaza',
    telefoneVendedor: '(87)9 9209-0279',
    numeroTicket: '006.326-7',
    qrCodeUrl: 'https://reinodasorte.com.br'
  };

  const requestBluetoothPermissions = async (): Promise<boolean> => {
    if (Platform.OS !== 'android') return true;

    try {
      const androidVersion = Platform.Version;
      
      if (androidVersion >= 31) {
        const granted = await PermissionsAndroid.requestMultiple([
          PermissionsAndroid.PERMISSIONS.BLUETOOTH_SCAN,
          PermissionsAndroid.PERMISSIONS.BLUETOOTH_CONNECT,
          PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION,
        ]);
        return granted['android.permission.BLUETOOTH_CONNECT'] === PermissionsAndroid.RESULTS.GRANTED;
      } else {
        const granted = await PermissionsAndroid.request(
          PermissionsAndroid.PERMISSIONS.ACCESS_COARSE_LOCATION
        );
        return granted === PermissionsAndroid.RESULTS.GRANTED;
      }
    } catch (err) {
      return false;
    }
  };

  const handleGetPrinters = async () => {
    setLoading(true);
    try {
      const hasPermission = await requestBluetoothPermissions();
      if (!hasPermission) {
        Alert.alert('Erro', 'Permissões Bluetooth não concedidas');
        return;
      }

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

  // 1. Imprimir Imagem Base64 Hardcoded (Kotlin)
  const handlePrintTestImage = async () => {
    setLoading(true);
    try {
      const result = await printTestImage({
        paperWidth: 58,
        dpi: 203,
        applyDithering: true,
      });
      
      if (result.success) {
        Alert.alert('✅ Sucesso', 'Imagem Base64 impressa com motor RAW!\n\nQualidade RawBT aplicada.');
      } else {
        Alert.alert('❌ Erro', result.message || 'Falha ao imprimir');
      }
    } catch (error: any) {
      Alert.alert('❌ Erro', error.message || 'Erro ao imprimir imagem');
    } finally {
      setLoading(false);
    }
  };

  // 2. Imprimir Bilhete Reino da Sorte
  const handlePrintTicket = async () => {
    setLoading(true);
    try {
      const hasPermission = await requestBluetoothPermissions();
      if (!hasPermission) {
        Alert.alert('Erro', 'Permissões Bluetooth não concedidas');
        setLoading(false);
        return;
      }

      await printTicket();
      Alert.alert('✅ Sucesso', 'Bilhete Reino da Sorte impresso!');
    } catch (error: any) {
      Alert.alert('❌ Erro', error.message || 'Falha ao imprimir bilhete');
    } finally {
      setLoading(false);
    }
  };

  // 3. Imprimir Cupom Restaurante
  const handlePrintReceipt = async () => {
    setLoading(true);
    try {
      const hasPermission = await requestBluetoothPermissions();
      if (!hasPermission) {
        Alert.alert('Erro', 'Permissões Bluetooth não concedidas');
        setLoading(false);
        return;
      }

      await printReceipt();
      Alert.alert('✅ Sucesso', 'Cupom Restaurante impresso!');
    } catch (error: any) {
      Alert.alert('❌ Erro', error.message || 'Falha ao imprimir cupom');
    } finally {
      setLoading(false);
    }
  };

  return (
    <ScrollView style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>🖨️ Impressão Térmica RAW</Text>
        <Text style={styles.subtitle}>Motor de Alta Fidelidade</Text>
      </View>

      {connectedPrinter && (
        <View style={styles.connectedBanner}>
          <Text style={styles.connectedText}>✓ Conectado: {connectedPrinter}</Text>
        </View>
      )}

      {/* Seção 1: Conectar Impressora */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>1️⃣ Conectar Impressora</Text>
        
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

        {connectedPrinter && (
          <TouchableOpacity
            style={[styles.button, styles.disconnectButton]}
            onPress={handleDisconnect}
            disabled={loading}
          >
            <Text style={styles.buttonText}>🔌 Desconectar</Text>
          </TouchableOpacity>
        )}
      </View>

      {/* Seção 2: Testes de Impressão */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>2️⃣ Testes de Impressão</Text>
        <Text style={styles.description}>
          Motor RAW com qualidade RawBT
        </Text>
        
        {/* Teste 1: Imagem Base64 Hardcoded */}
        <TouchableOpacity
          style={[styles.testButton, { backgroundColor: '#9C27B0' }]}
          onPress={handlePrintTestImage}
          disabled={loading}
        >
          <Text style={styles.testButtonTitle}>🖼️ Imagem Base64 (Kotlin)</Text>
          <Text style={styles.testButtonSubtitle}>
            Imagem hardcoded com motor RAW
          </Text>
        </TouchableOpacity>

        {/* Teste 2: Bilhete Reino da Sorte */}
        <TouchableOpacity
          style={[styles.testButton, { backgroundColor: '#8B5CF6' }]}
          onPress={handlePrintTicket}
          disabled={loading || isPrintingTicket}
        >
          <Text style={styles.testButtonTitle}>🎫 Bilhete Reino da Sorte</Text>
          <Text style={styles.testButtonSubtitle}>
            Template completo com QR Code
          </Text>
        </TouchableOpacity>

        {/* Teste 3: Cupom Restaurante */}
        <TouchableOpacity
          style={[styles.testButton, { backgroundColor: '#FF9500' }]}
          onPress={handlePrintReceipt}
          disabled={loading || isPrinting}
        >
          <Text style={styles.testButtonTitle}>🧾 Cupom Restaurante</Text>
          <Text style={styles.testButtonSubtitle}>
            Cupom fiscal com produtos
          </Text>
        </TouchableOpacity>
      </View>

      {/* Templates ocultos para captura */}
      <View style={{ position: 'absolute', top: 0, zIndex: -1, opacity: 0 }} collapsable={false}>
        <ReceiptTemplate
          ref={receiptRef}
          storeName="REINO DA SORTE"
          storeAddress="Av. Principal, 456 - Centro"
          storeCNPJ="12.345.678/0001-90"
          cpf="123.456.789-00"
          items={sampleReceiptData}
          total={sampleTotal}
          paymentMethod="Dinheiro"
          receiptNumber="000123"
        />
      </View>
      
      <View style={{ position: 'absolute', top: 0, zIndex: -1, opacity: 0 }} collapsable={false}>
        <TicketTemplate ref={ticketRef} data={sampleTicketData} />
      </View>

      {loading && (
        <View style={styles.loadingOverlay}>
          <ActivityIndicator size="large" color="#007AFF" />
          <Text style={styles.loadingText}>Processando...</Text>
        </View>
      )}

      <View style={styles.footer}>
        <Text style={styles.footerText}>
          ✨ Motor RAW de Alta Fidelidade
        </Text>
        <Text style={styles.footerSubtext}>
          Qualidade RawBT • Comando ESC/POS GS v 0
        </Text>
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  header: {
    backgroundColor: '#007AFF',
    padding: 20,
    paddingTop: 60,
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#fff',
    marginBottom: 5,
  },
  subtitle: {
    fontSize: 14,
    color: '#e0e0e0',
  },
  connectedBanner: {
    backgroundColor: '#4CAF50',
    padding: 12,
    alignItems: 'center',
  },
  connectedText: {
    color: '#fff',
    fontWeight: '600',
  },
  section: {
    backgroundColor: '#fff',
    margin: 15,
    padding: 15,
    borderRadius: 10,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: '600',
    marginBottom: 10,
    color: '#333',
  },
  description: {
    fontSize: 14,
    color: '#666',
    marginBottom: 15,
  },
  button: {
    backgroundColor: '#007AFF',
    padding: 15,
    borderRadius: 8,
    alignItems: 'center',
    marginBottom: 10,
  },
  disconnectButton: {
    backgroundColor: '#FF3B30',
  },
  buttonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: '600',
  },
  testButton: {
    padding: 20,
    borderRadius: 12,
    marginBottom: 15,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.2,
    shadowRadius: 4,
    elevation: 4,
  },
  testButtonTitle: {
    color: '#fff',
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 5,
  },
  testButtonSubtitle: {
    color: '#fff',
    fontSize: 14,
    opacity: 0.9,
  },
  printerList: {
    marginTop: 15,
  },
  printerItem: {
    backgroundColor: '#f9f9f9',
    padding: 12,
    borderRadius: 6,
    marginBottom: 8,
    borderLeftWidth: 3,
    borderLeftColor: '#007AFF',
  },
  printerName: {
    fontSize: 16,
    fontWeight: '600',
    color: '#333',
  },
  printerAddress: {
    fontSize: 12,
    color: '#666',
    marginTop: 4,
  },
  loadingOverlay: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    backgroundColor: 'rgba(0,0,0,0.7)',
    justifyContent: 'center',
    alignItems: 'center',
  },
  loadingText: {
    color: '#fff',
    marginTop: 10,
    fontSize: 16,
  },
  footer: {
    padding: 20,
    alignItems: 'center',
  },
  footerText: {
    fontSize: 12,
    color: '#999',
    textAlign: 'center',
  },
  footerSubtext: {
    fontSize: 10,
    color: '#bbb',
    textAlign: 'center',
    marginTop: 5,
  },
});
