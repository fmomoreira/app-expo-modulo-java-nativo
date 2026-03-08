import React, { forwardRef } from 'react';
import { View, Text, StyleSheet } from 'react-native';

export interface ReceiptItem {
  name: string;
  price: number;
  quantity: number;
}

export interface ReceiptTemplateProps {
  storeName?: string;
  storeAddress?: string;
  storeCNPJ?: string;
  cpf?: string;
  items: ReceiptItem[];
  total: number;
  paymentMethod?: string;
  date?: Date;
  receiptNumber?: string;
}

/**
 * Template de Cupom Fiscal em React Native
 * Renderiza um layout visual que será capturado como imagem e impresso
 */
export const ReceiptTemplate = forwardRef<View, ReceiptTemplateProps>((props, ref) => {
  const {
    storeName = 'LOJA EXEMPLO',
    storeAddress = 'Rua Exemplo, 123 - Centro',
    storeCNPJ = '00.000.000/0001-00',
    cpf,
    items,
    total,
    paymentMethod = 'Dinheiro',
    date = new Date(),
    receiptNumber = '000001',
  } = props;

  const formatCurrency = (value: number) => {
    return `R$ ${value.toFixed(2).replace('.', ',')}`;
  };

  const formatDate = (date: Date) => {
    return date.toLocaleDateString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <View ref={ref} collapsable={false} style={styles.container}>
      {/* Cabeçalho */}
      <View style={styles.header}>
        <Text style={styles.storeName}>{storeName}</Text>
        <Text style={styles.storeInfo}>{storeAddress}</Text>
        <Text style={styles.storeInfo}>CNPJ: {storeCNPJ}</Text>
      </View>

      <View style={styles.divider} />

      {/* Informações do Cupom */}
      <View style={styles.section}>
        <Text style={styles.label}>CUPOM FISCAL</Text>
        <Text style={styles.info}>Nº {receiptNumber}</Text>
        <Text style={styles.info}>{formatDate(date)}</Text>
        {cpf && <Text style={styles.info}>CPF: {cpf}</Text>}
      </View>

      <View style={styles.divider} />

      {/* Produtos */}
      <View style={styles.section}>
        <View style={styles.tableHeader}>
          <Text style={styles.tableHeaderText}>PRODUTO</Text>
          <Text style={styles.tableHeaderText}>VALOR</Text>
        </View>

        {items.map((item, index) => {
          const itemTotal = item.price * item.quantity;
          return (
            <View key={index} style={styles.itemContainer}>
              <View style={styles.itemRow}>
                <Text style={styles.itemName}>{item.name}</Text>
                <Text style={styles.itemPrice}>{formatCurrency(itemTotal)}</Text>
              </View>
              <Text style={styles.itemDetails}>
                {item.quantity}x {formatCurrency(item.price)}
              </Text>
            </View>
          );
        })}
      </View>

      <View style={styles.divider} />

      {/* Total */}
      <View style={styles.totalSection}>
        <Text style={styles.totalLabel}>TOTAL</Text>
        <Text style={styles.totalValue}>{formatCurrency(total)}</Text>
      </View>

      <View style={styles.divider} />

      {/* Pagamento */}
      <View style={styles.section}>
        <Text style={styles.info}>Forma de Pagamento: {paymentMethod}</Text>
      </View>

      <View style={styles.divider} />

      {/* Rodapé */}
      <View style={styles.footer}>
        <Text style={styles.footerText}>Obrigado pela preferência!</Text>
        <Text style={styles.footerText}>Volte sempre!</Text>
      </View>
    </View>
  );
});

const styles = StyleSheet.create({
  container: {
    width: 384, // 58mm em pixels (203 DPI)
    backgroundColor: '#FFFFFF',
    padding: 16,
  },
  header: {
    alignItems: 'center',
    marginBottom: 12,
  },
  storeName: {
    fontSize: 20,
    fontWeight: 'bold',
    textAlign: 'center',
    marginBottom: 4,
  },
  storeInfo: {
    fontSize: 12,
    textAlign: 'center',
    color: '#333',
  },
  divider: {
    height: 1,
    backgroundColor: '#000',
    marginVertical: 8,
  },
  section: {
    marginVertical: 8,
  },
  label: {
    fontSize: 16,
    fontWeight: 'bold',
    textAlign: 'center',
    marginBottom: 4,
  },
  info: {
    fontSize: 12,
    textAlign: 'center',
    color: '#333',
  },
  tableHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 8,
    paddingBottom: 4,
    borderBottomWidth: 1,
    borderBottomColor: '#000',
  },
  tableHeaderText: {
    fontSize: 12,
    fontWeight: 'bold',
  },
  itemContainer: {
    marginBottom: 8,
  },
  itemRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 2,
  },
  itemName: {
    fontSize: 12,
    fontWeight: '600',
    flex: 1,
  },
  itemPrice: {
    fontSize: 12,
    fontWeight: '600',
  },
  itemDetails: {
    fontSize: 10,
    color: '#666',
    marginLeft: 8,
  },
  totalSection: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 8,
  },
  totalLabel: {
    fontSize: 18,
    fontWeight: 'bold',
  },
  totalValue: {
    fontSize: 18,
    fontWeight: 'bold',
  },
  footer: {
    alignItems: 'center',
    marginTop: 12,
  },
  footerText: {
    fontSize: 12,
    textAlign: 'center',
    marginBottom: 4,
  },
});
