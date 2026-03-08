# 🧾 Exemplo de Uso - Cupom Fiscal com QR Code

## 📝 Código React Native

```typescript
import React, { useState } from 'react';
import { View, TouchableOpacity, Text, Alert } from 'react-native';
import { 
  connectPrinter, 
  printReceipt, 
  ReceiptItem 
} from './modules/expo-thermal-printer';

export default function CupomFiscalScreen() {
  const [loading, setLoading] = useState(false);

  const imprimirCupom = async () => {
    setLoading(true);
    try {
      // 1. Conectar à impressora (se ainda não conectou)
      await connectPrinter("internal_auto");
      
      // 2. Definir produtos
      const produtos: ReceiptItem[] = [
        { name: "Coca-Cola 2L", price: 8.50, quantity: 2 },
        { name: "Pão Francês (kg)", price: 12.00, quantity: 1 },
        { name: "Leite Integral 1L", price: 5.30, quantity: 3 },
        { name: "Arroz 5kg", price: 25.90, quantity: 1 },
      ];
      
      // 3. Calcular total
      const total = produtos.reduce((sum, item) => 
        sum + (item.price * item.quantity), 0
      );
      
      // 4. Imprimir cupom
      const resultado = await printReceipt(produtos, {
        cpf: "123.456.789-00",
        total: total,
        qrCodeUrl: "https://reinodasorte.com.br"
      });
      
      if (resultado.success) {
        Alert.alert("Sucesso!", "Cupom fiscal impresso!");
      }
      
    } catch (error: any) {
      Alert.alert("Erro", error.message || "Erro ao imprimir cupom");
    } finally {
      setLoading(false);
    }
  };

  return (
    <View style={{ padding: 20 }}>
      <TouchableOpacity 
        onPress={imprimirCupom}
        disabled={loading}
        style={{
          backgroundColor: '#4CAF50',
          padding: 15,
          borderRadius: 8,
          alignItems: 'center'
        }}
      >
        <Text style={{ color: '#fff', fontSize: 16, fontWeight: 'bold' }}>
          {loading ? 'Imprimindo...' : '🧾 Imprimir Cupom Fiscal'}
        </Text>
      </TouchableOpacity>
    </View>
  );
}
```

---

## 🖨️ Resultado Impresso

```
================================
      CUPOM FISCAL
================================

CPF: 123.456.789-00
--------------------------------
PRODUTO                   VALOR
--------------------------------
Coca-Cola 2L
  2x R$ 8.50          R$ 17.00
Pão Francês (kg)
  1x R$ 12.00         R$ 12.00
Leite Integral 1L
  3x R$ 5.30          R$ 15.90
Arroz 5kg
  1x R$ 25.90         R$ 25.90
--------------------------------
TOTAL                 R$ 70.80
================================

    Acesse nosso site:
    
    [QR CODE AQUI]
    
  https://reinodasorte.com.br

  Obrigado pela preferência!


```

---

## 🎨 Formatação ESC/POS

A função `printReceipt()` usa a sintaxe ESC/POS da biblioteca DantSu:

- `[C]` - Centralizado
- `[L]` - Alinhado à esquerda
- `[R]` - Alinhado à direita
- `<b>texto</b>` - Negrito
- `<qrcode size='20'>url</qrcode>` - QR Code

---

## 📱 Exemplo Completo com UI

```typescript
import React, { useState } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  Alert,
  ScrollView,
  TextInput,
} from 'react-native';
import { printReceipt, ReceiptItem } from './modules/expo-thermal-printer';

export default function VendaScreen() {
  const [cpf, setCpf] = useState('');
  const [carrinho, setCarrinho] = useState<ReceiptItem[]>([
    { name: "Produto Exemplo", price: 10.00, quantity: 1 },
  ]);

  const adicionarProduto = (nome: string, preco: number) => {
    setCarrinho([...carrinho, { name: nome, price: preco, quantity: 1 }]);
  };

  const calcularTotal = () => {
    return carrinho.reduce((sum, item) => sum + (item.price * item.quantity), 0);
  };

  const finalizarVenda = async () => {
    try {
      const total = calcularTotal();
      
      const resultado = await printReceipt(carrinho, {
        cpf: cpf,
        total: total,
        qrCodeUrl: "https://reinodasorte.com.br"
      });

      if (resultado.success) {
        Alert.alert("Venda Finalizada!", "Cupom impresso com sucesso!");
        setCarrinho([]);
        setCpf('');
      }
    } catch (error: any) {
      Alert.alert("Erro", error.message);
    }
  };

  return (
    <ScrollView style={styles.container}>
      <Text style={styles.title}>🛒 Venda</Text>

      <TextInput
        style={styles.input}
        placeholder="CPF do Cliente (opcional)"
        value={cpf}
        onChangeText={setCpf}
        keyboardType="numeric"
      />

      <View style={styles.carrinho}>
        <Text style={styles.subtitle}>Carrinho:</Text>
        {carrinho.map((item, index) => (
          <View key={index} style={styles.item}>
            <Text>{item.name}</Text>
            <Text>R$ {item.price.toFixed(2)}</Text>
          </View>
        ))}
      </View>

      <View style={styles.total}>
        <Text style={styles.totalText}>Total:</Text>
        <Text style={styles.totalValor}>R$ {calcularTotal().toFixed(2)}</Text>
      </View>

      <TouchableOpacity style={styles.button} onPress={finalizarVenda}>
        <Text style={styles.buttonText}>🧾 Finalizar e Imprimir</Text>
      </TouchableOpacity>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, padding: 20, backgroundColor: '#f5f5f5' },
  title: { fontSize: 24, fontWeight: 'bold', marginBottom: 20 },
  subtitle: { fontSize: 18, fontWeight: '600', marginBottom: 10 },
  input: {
    backgroundColor: '#fff',
    padding: 12,
    borderRadius: 8,
    marginBottom: 20,
    borderWidth: 1,
    borderColor: '#ddd',
  },
  carrinho: {
    backgroundColor: '#fff',
    padding: 15,
    borderRadius: 8,
    marginBottom: 20,
  },
  item: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    paddingVertical: 8,
    borderBottomWidth: 1,
    borderBottomColor: '#eee',
  },
  total: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    backgroundColor: '#4CAF50',
    padding: 15,
    borderRadius: 8,
    marginBottom: 20,
  },
  totalText: { fontSize: 20, fontWeight: 'bold', color: '#fff' },
  totalValor: { fontSize: 20, fontWeight: 'bold', color: '#fff' },
  button: {
    backgroundColor: '#007AFF',
    padding: 15,
    borderRadius: 8,
    alignItems: 'center',
  },
  buttonText: { color: '#fff', fontSize: 16, fontWeight: 'bold' },
});
```

---

## 🔧 Parâmetros da Função

### `printReceipt(items, options)`

**items:** Array de produtos
```typescript
{
  name: string;      // Nome do produto
  price: number;     // Preço unitário
  quantity: number;  // Quantidade
}
```

**options:** Opções do cupom
```typescript
{
  cpf?: string;           // CPF do cliente (opcional)
  total: number;          // Valor total da compra
  qrCodeUrl?: string;     // URL para o QR Code (padrão: https://reinodasorte.com.br)
}
```

---

## ✅ Funcionalidades

- ✅ Cabeçalho formatado
- ✅ CPF do cliente (opcional)
- ✅ Lista de produtos com preços
- ✅ Cálculo automático de subtotais
- ✅ Total em destaque
- ✅ QR Code para site
- ✅ Mensagem de agradecimento
- ✅ Formatação profissional ESC/POS

---

**Data:** 08/03/2026  
**Versão:** 1.0
