import React, { useState, useMemo } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  StyleSheet,
  ScrollView,
  Alert,
  ActivityIndicator,
  PermissionsAndroid,
  Platform,
  FlatList,
} from 'react-native';
import {
  getPairedPrinters,
  connectPrinter,
  disconnectPrinter,
  printText,
  PrinterDevice,
} from '../modules/expo-thermal-printer';

interface Product {
  id: string;
  name: string;
  quantity: number;
  price: number;
}

export default function PDVScreen() {
  // Estados de conexão
  const [loading, setLoading] = useState(false);
  const [printers, setPrinters] = useState<PrinterDevice[]>([]);
  const [connectedPrinter, setConnectedPrinter] = useState<string | null>(null);

  // Estados do cupom
  const [cpf, setCpf] = useState('');
  const [products, setProducts] = useState<Product[]>([]);
  
  // Estados do formulário de produto
  const [productName, setProductName] = useState('');
  const [productQuantity, setProductQuantity] = useState('1');
  const [productPrice, setProductPrice] = useState('');

  // Calcula total automaticamente
  const total = useMemo(() => {
    return products.reduce((sum, p) => sum + (p.quantity * p.price), 0);
  }, [products]);

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

  const handleAddProduct = () => {
    if (!productName.trim()) {
      Alert.alert('Erro', 'Digite o nome do produto');
      return;
    }
    if (!productPrice || parseFloat(productPrice) <= 0) {
      Alert.alert('Erro', 'Digite um preço válido');
      return;
    }

    const newProduct: Product = {
      id: Date.now().toString(),
      name: productName.trim(),
      quantity: parseInt(productQuantity) || 1,
      price: parseFloat(productPrice),
    };

    setProducts([...products, newProduct]);
    
    // Limpa formulário
    setProductName('');
    setProductQuantity('1');
    setProductPrice('');
  };

  const handleRemoveProduct = (id: string) => {
    setProducts(products.filter(p => p.id !== id));
  };

  const handlePrint = async () => {
    if (products.length === 0) {
      Alert.alert('Erro', 'Adicione pelo menos um produto');
      return;
    }

    if (!connectedPrinter) {
      Alert.alert('Erro', 'Conecte-se a uma impressora primeiro');
      return;
    }

    setLoading(true);
    try {
      // Gera cupom ESC/POS
      let cupom = '';
      
      // Cabeçalho
      cupom += '[C]<b>CUPOM FISCAL</b>\n';
      cupom += '[C]================================\n';
      cupom += '[C]LOJA EXEMPLO LTDA\n';
      cupom += '[C]Rua Principal, 123 - Centro\n';
      cupom += '[C]CNPJ: 12.345.678/0001-90\n';
      cupom += '[C]================================\n\n';

      // CPF (se informado)
      if (cpf.trim()) {
        cupom += `[L]CPF: ${cpf}\n\n`;
      }

      // Data/Hora
      const now = new Date();
      const dataHora = now.toLocaleString('pt-BR');
      cupom += `[L]Data: ${dataHora}\n`;
      cupom += '[L]================================\n\n';

      // Produtos
      cupom += '[L]<b>PRODUTOS</b>\n';
      cupom += '[L]--------------------------------\n';
      
      products.forEach((p) => {
        const subtotal = p.quantity * p.price;
        cupom += `[L]${p.name}\n`;
        cupom += `[L]${p.quantity} x R$ ${p.price.toFixed(2)} = R$ ${subtotal.toFixed(2)}\n`;
        cupom += '[L]--------------------------------\n';
      });

      // Total
      cupom += '\n';
      cupom += `[L]<font size='tall'><b>TOTAL: R$ ${total.toFixed(2)}</b></font>\n\n`;

      // QR Code grande centralizado
      cupom += "[C]<qrcode size='40'>https://exemplo.com.br/cupom</qrcode>\n\n";

      // Rodapé
      cupom += '[C]================================\n';
      cupom += '[C]Obrigado pela preferência!\n';
      cupom += '[C]Volte sempre!\n';
      cupom += '[C]================================\n\n\n\n';

      // Imprime via ESC/POS texto
      const result = await printText(cupom, {
        paperWidth: 58,
      });

      if (result.success) {
        Alert.alert('✅ Sucesso', 'Cupom impresso com sucesso!', [
          {
            text: 'OK',
            onPress: () => {
              // Limpa pedido
              setProducts([]);
              setCpf('');
            },
          },
        ]);
      } else {
        Alert.alert('❌ Erro', result.message || 'Falha ao imprimir');
      }
    } catch (error: any) {
      Alert.alert('❌ Erro', error.message || 'Erro ao imprimir cupom');
    } finally {
      setLoading(false);
    }
  };

  const formatCurrency = (value: number) => {
    return `R$ ${value.toFixed(2)}`;
  };

  return (
    <View style={styles.container}>
      {/* Header */}
      <View style={styles.header}>
        <Text style={styles.headerTitle}>🖨️ PDV - Cupom Fiscal</Text>
        {connectedPrinter && (
          <Text style={styles.headerSubtitle}>✓ {connectedPrinter}</Text>
        )}
      </View>

      <ScrollView style={styles.content}>
        {/* Seção de Conexão */}
        {!connectedPrinter && (
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
        )}

        {connectedPrinter && (
          <>
            {/* CPF */}
            <View style={styles.section}>
              <Text style={styles.sectionTitle}>📄 CPF na Nota (Opcional)</Text>
              <TextInput
                style={styles.input}
                placeholder="000.000.000-00"
                value={cpf}
                onChangeText={setCpf}
                keyboardType="numeric"
                maxLength={14}
              />
            </View>

            {/* Adicionar Produto */}
            <View style={styles.section}>
              <Text style={styles.sectionTitle}>➕ Adicionar Produto</Text>
              
              <TextInput
                style={styles.input}
                placeholder="Nome do produto"
                value={productName}
                onChangeText={setProductName}
              />

              <View style={styles.row}>
                <TextInput
                  style={[styles.input, styles.inputSmall]}
                  placeholder="Qtd"
                  value={productQuantity}
                  onChangeText={setProductQuantity}
                  keyboardType="numeric"
                />
                <TextInput
                  style={[styles.input, styles.inputLarge]}
                  placeholder="Preço (R$)"
                  value={productPrice}
                  onChangeText={setProductPrice}
                  keyboardType="decimal-pad"
                />
              </View>

              <TouchableOpacity
                style={[styles.button, styles.addButton]}
                onPress={handleAddProduct}
              >
                <Text style={styles.buttonText}>➕ Adicionar</Text>
              </TouchableOpacity>
            </View>

            {/* Lista de Produtos */}
            {products.length > 0 && (
              <View style={styles.section}>
                <Text style={styles.sectionTitle}>🛒 Produtos ({products.length})</Text>
                
                {products.map((product) => (
                  <View key={product.id} style={styles.productItem}>
                    <View style={styles.productInfo}>
                      <Text style={styles.productName}>{product.name}</Text>
                      <Text style={styles.productDetails}>
                        {product.quantity} x {formatCurrency(product.price)} = {formatCurrency(product.quantity * product.price)}
                      </Text>
                    </View>
                    <TouchableOpacity
                      style={styles.removeButton}
                      onPress={() => handleRemoveProduct(product.id)}
                    >
                      <Text style={styles.removeButtonText}>✕</Text>
                    </TouchableOpacity>
                  </View>
                ))}

                {/* Total */}
                <View style={styles.totalContainer}>
                  <Text style={styles.totalLabel}>TOTAL:</Text>
                  <Text style={styles.totalValue}>{formatCurrency(total)}</Text>
                </View>

                {/* Botão Imprimir */}
                <TouchableOpacity
                  style={[styles.button, styles.printButton]}
                  onPress={handlePrint}
                  disabled={loading}
                >
                  <Text style={styles.buttonText}>
                    {loading ? 'Imprimindo...' : '🖨️ IMPRIMIR CUPOM'}
                  </Text>
                </TouchableOpacity>
              </View>
            )}

            {/* Botão Desconectar */}
            <TouchableOpacity
              style={[styles.button, styles.disconnectButton]}
              onPress={handleDisconnect}
              disabled={loading}
            >
              <Text style={styles.buttonText}>🔌 Desconectar Impressora</Text>
            </TouchableOpacity>
          </>
        )}
      </ScrollView>

      {loading && (
        <View style={styles.loadingOverlay}>
          <ActivityIndicator size="large" color="#007AFF" />
          <Text style={styles.loadingText}>Processando...</Text>
        </View>
      )}
    </View>
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
  headerTitle: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#fff',
  },
  headerSubtitle: {
    fontSize: 14,
    color: '#e0e0e0',
    marginTop: 5,
  },
  content: {
    flex: 1,
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
  input: {
    backgroundColor: '#f9f9f9',
    borderWidth: 1,
    borderColor: '#ddd',
    borderRadius: 8,
    padding: 12,
    fontSize: 16,
    marginBottom: 10,
  },
  row: {
    flexDirection: 'row',
    gap: 10,
  },
  inputSmall: {
    flex: 1,
  },
  inputLarge: {
    flex: 2,
  },
  button: {
    backgroundColor: '#007AFF',
    padding: 15,
    borderRadius: 8,
    alignItems: 'center',
    marginTop: 10,
  },
  addButton: {
    backgroundColor: '#34C759',
  },
  printButton: {
    backgroundColor: '#FF9500',
    marginTop: 15,
  },
  disconnectButton: {
    backgroundColor: '#FF3B30',
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
  productItem: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    backgroundColor: '#f9f9f9',
    padding: 12,
    borderRadius: 8,
    marginBottom: 10,
  },
  productInfo: {
    flex: 1,
  },
  productName: {
    fontSize: 16,
    fontWeight: '600',
    color: '#333',
  },
  productDetails: {
    fontSize: 14,
    color: '#666',
    marginTop: 4,
  },
  removeButton: {
    backgroundColor: '#FF3B30',
    width: 32,
    height: 32,
    borderRadius: 16,
    justifyContent: 'center',
    alignItems: 'center',
  },
  removeButtonText: {
    color: '#fff',
    fontSize: 18,
    fontWeight: 'bold',
  },
  totalContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    backgroundColor: '#007AFF',
    padding: 15,
    borderRadius: 8,
    marginTop: 15,
  },
  totalLabel: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#fff',
  },
  totalValue: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#fff',
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
});
