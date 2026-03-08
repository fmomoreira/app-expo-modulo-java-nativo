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
  printImage,
  printText,
  getPairedPrinters,
  connectPrinter,
  disconnectPrinter,
  selfTest,
  PrinterDevice,
} from '../../modules/expo-thermal-printer';

export default function HomeScreen() {
  const [loading, setLoading] = useState(false);
  const [printers, setPrinters] = useState<PrinterDevice[]>([]);
  const [connectedPrinter, setConnectedPrinter] = useState<string | null>(null);

  const requestBluetoothPermissions = async (): Promise<boolean> => {
    if (Platform.OS !== 'android') {
      return true;
    }

    try {
      // Platform.Version é a ÚNICA fonte de verdade
      const androidVersion = Platform.Version;
      console.log(`[Permissions] Android Version: ${androidVersion}`);

      if (androidVersion >= 31) {
        // Android 12+ (API 31+) - Requer BLUETOOTH_CONNECT e BLUETOOTH_SCAN
        console.log('[Permissions] Solicitando permissões Android 12+...');
        
        try {
          const granted = await PermissionsAndroid.requestMultiple([
            PermissionsAndroid.PERMISSIONS.BLUETOOTH_SCAN,
            PermissionsAndroid.PERMISSIONS.BLUETOOTH_CONNECT,
            PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION,
          ]);

          const scanGranted = granted['android.permission.BLUETOOTH_SCAN'] === PermissionsAndroid.RESULTS.GRANTED;
          const connectGranted = granted['android.permission.BLUETOOTH_CONNECT'] === PermissionsAndroid.RESULTS.GRANTED;

          console.log(`[Permissions] BLUETOOTH_SCAN: ${scanGranted ? 'GRANTED' : 'DENIED'}`);
          console.log(`[Permissions] BLUETOOTH_CONNECT: ${connectGranted ? 'GRANTED' : 'DENIED'}`);

          return scanGranted && connectGranted;
        } catch (err) {
          console.error('[Permissions] Erro ao solicitar permissões Android 12+:', err);
          return false;
        }
      } else {
        // Android 7-11 (API 24-30) - Requer apenas ACCESS_COARSE_LOCATION
        console.log('[Permissions] Solicitando permissões Android 7-11...');
        
        try {
          const granted = await PermissionsAndroid.request(
            PermissionsAndroid.PERMISSIONS.ACCESS_COARSE_LOCATION
          );

          const isGranted = granted === PermissionsAndroid.RESULTS.GRANTED;
          console.log(`[Permissions] ACCESS_COARSE_LOCATION: ${isGranted ? 'GRANTED' : 'DENIED'}`);

          return isGranted;
        } catch (err) {
          console.error('[Permissions] Erro ao solicitar permissões Android 7-11:', err);
          return false;
        }
      }
    } catch (err) {
      console.error('[Permissions] Erro crítico ao verificar versão Android:', err);
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
      } else {
        Alert.alert('Sucesso', `${devices.length} impressora(s) encontrada(s)`);
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

  const handlePrintText = async () => {
    setLoading(true);
    try {
      const hasPermission = await requestBluetoothPermissions();
      if (!hasPermission) {
        Alert.alert('Erro', 'Permissões Bluetooth não concedidas');
        return;
      }

      const result = await printText(
        '================================\n' +
        'TESTE DE IMPRESSAO\n' +
        'Modulo Nativo React Native\n' +
        '================================\n' +
        'Algoritmo Floyd-Steinberg\n' +
        'Biblioteca DantSu ESC/POS\n' +
        '================================\n\n\n',
        {
          paperWidth: 58,
          encoding: 'ISO-8859-1',
        }
      );

      if (result.success) {
        Alert.alert('Sucesso', 'Texto impresso com sucesso!');
      }
    } catch (error: any) {
      Alert.alert('Erro', error.message || 'Erro ao imprimir texto');
    } finally {
      setLoading(false);
    }
  };

  const generateTestImage = (): string => {
    return 'iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAAFUlEQVR42mNk+M9Qz0AEYBxVSF+FAP0QDiWl0HiCAAAAAElFTkSuQmCC';
  };

  const handlePrintImage = async () => {
    setLoading(true);
    try {
      const hasPermission = await requestBluetoothPermissions();
      if (!hasPermission) {
        Alert.alert('Erro', 'Permissões Bluetooth não concedidas');
        return;
      }

      const testImage = generateTestImage();
      
      const result = await printImage(testImage, {
        paperWidth: 58,
        dpi: 203,
        applyDithering: true,
      });

      if (result.success) {
        Alert.alert('Sucesso', 'Imagem impressa com sucesso!');
      }
    } catch (error: any) {
      Alert.alert('Erro', error.message || 'Erro ao imprimir imagem');
    } finally {
      setLoading(false);
    }
  };

  const handleSelfTest = async () => {
    setLoading(true);
    try {
      const hasPermission = await requestBluetoothPermissions();
      if (!hasPermission) {
        Alert.alert('Erro', 'Permissões Bluetooth não concedidas');
        return;
      }

      const result = await selfTest();

      if (result.success) {
        Alert.alert('Sucesso', 'Auto-teste concluído! Verifique a impressão.');
      }
    } catch (error: any) {
      Alert.alert('Erro', error.message || 'Erro ao executar auto-teste');
    } finally {
      setLoading(false);
    }
  };

  return (
    <ScrollView style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>🖨️ Impressão Térmica</Text>
        <Text style={styles.subtitle}>Módulo Nativo Java/Kotlin</Text>
      </View>

      {connectedPrinter && (
        <View style={styles.connectedBanner}>
          <Text style={styles.connectedText}>
            ✓ Conectado: {connectedPrinter}
          </Text>
        </View>
      )}

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>1. Gerenciar Impressoras</Text>
        
        <TouchableOpacity
          style={styles.button}
          onPress={handleGetPrinters}
          disabled={loading}
        >
          <Text style={styles.buttonText}>
            {loading ? 'Buscando...' : '🔍 Buscar Impressoras Bluetooth'}
          </Text>
        </TouchableOpacity>

        {printers.length > 0 && (
          <View style={styles.printerList}>
            <Text style={styles.listTitle}>Impressoras Disponíveis:</Text>
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

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>2. Testar Impressão</Text>
        
        <TouchableOpacity
          style={[styles.button, styles.selfTestButton]}
          onPress={handleSelfTest}
          disabled={loading}
        >
          <Text style={styles.buttonText}>🔧 Auto-Teste (Diagnóstico)</Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={styles.button}
          onPress={handlePrintText}
          disabled={loading}
        >
          <Text style={styles.buttonText}>📄 Imprimir Texto de Teste</Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={styles.button}
          onPress={handlePrintImage}
          disabled={loading}
        >
          <Text style={styles.buttonText}>
            🖼️ Imprimir Imagem (com Dithering)
          </Text>
        </TouchableOpacity>
      </View>

      {loading && (
        <View style={styles.loadingOverlay}>
          <ActivityIndicator size="large" color="#007AFF" />
          <Text style={styles.loadingText}>Processando...</Text>
        </View>
      )}

      <View style={styles.footer}>
        <Text style={styles.footerText}>
          ✨ Algoritmo Floyd-Steinberg para qualidade profissional
        </Text>
        <Text style={styles.footerSubtext}>
          Compatível com Moderninha e impressoras ESC/POS
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
    marginBottom: 15,
    color: '#333',
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
  selfTestButton: {
    backgroundColor: '#34C759',
  },
  buttonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: '600',
  },
  printerList: {
    marginTop: 15,
  },
  listTitle: {
    fontSize: 14,
    fontWeight: '600',
    marginBottom: 10,
    color: '#666',
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
