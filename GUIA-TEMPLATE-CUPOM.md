# 🧾 Guia: Sistema de Templates React Native para Cupom Fiscal

## 📋 Visão Geral

Sistema que permite criar **layouts de cupom fiscal customizados em React Native** (com componentes, estilos, flexbox) e imprimi-los automaticamente na impressora térmica.

### **Como Funciona**

```
React Native Component → react-native-view-shot → Base64 → Módulo Kotlin → Impressora
```

1. **Você cria** o layout visual em React Native (JSX + StyleSheet)
2. **Sistema captura** o componente como imagem (Base64)
3. **Módulo Kotlin** aplica dithering e imprime

---

## 🚀 Uso Básico

### **1. Criar Template Customizado**

```tsx
import { ReceiptTemplate, ReceiptItem } from '@/components/ReceiptTemplate';

const items: ReceiptItem[] = [
  { name: 'Coca-Cola 2L', price: 8.50, quantity: 2 },
  { name: 'Pão Francês (kg)', price: 12.00, quantity: 1 },
];

<ReceiptTemplate
  storeName="REINO DA SORTE"
  storeAddress="Av. Principal, 456 - Centro"
  storeCNPJ="12.345.678/0001-90"
  cpf="123.456.789-00"
  items={items}
  total={29.00}
  paymentMethod="Dinheiro"
  receiptNumber="000123"
/>
```

### **2. Usar Hook para Imprimir**

```tsx
import { usePrintReceipt } from '@/hooks/usePrintReceipt';

function MyScreen() {
  const { receiptRef, printReceipt, isPrinting } = usePrintReceipt();

  const handlePrint = async () => {
    const result = await printReceipt();
    
    if (result.success) {
      Alert.alert('Sucesso', 'Cupom impresso!');
    } else {
      Alert.alert('Erro', result.message);
    }
  };

  return (
    <>
      {/* Template oculto (será capturado) */}
      <View style={{ position: 'absolute', left: -9999 }}>
        <ReceiptTemplate
          ref={receiptRef}
          items={items}
          total={total}
        />
      </View>

      {/* Botão de impressão */}
      <Button 
        title="Imprimir Cupom" 
        onPress={handlePrint}
        disabled={isPrinting}
      />
    </>
  );
}
```

---

## 🎨 Customização do Template

### **Props do ReceiptTemplate**

| Prop | Tipo | Padrão | Descrição |
|------|------|--------|-----------|
| `storeName` | `string` | `"LOJA EXEMPLO"` | Nome da loja |
| `storeAddress` | `string` | `"Rua Exemplo, 123"` | Endereço |
| `storeCNPJ` | `string` | `"00.000.000/0001-00"` | CNPJ |
| `cpf` | `string?` | - | CPF do cliente (opcional) |
| `items` | `ReceiptItem[]` | **obrigatório** | Lista de produtos |
| `total` | `number` | **obrigatório** | Valor total |
| `paymentMethod` | `string` | `"Dinheiro"` | Forma de pagamento |
| `date` | `Date` | `new Date()` | Data/hora |
| `receiptNumber` | `string` | `"000001"` | Número do cupom |

### **Interface ReceiptItem**

```typescript
interface ReceiptItem {
  name: string;      // Nome do produto
  price: number;     // Preço unitário
  quantity: number;  // Quantidade
}
```

---

## 🎯 Criar Seu Próprio Template

### **Exemplo: Template Minimalista**

```tsx
import React, { forwardRef } from 'react';
import { View, Text, StyleSheet } from 'react-native';

export const MinimalReceipt = forwardRef<View, { items: any[], total: number }>((props, ref) => {
  return (
    <View ref={ref} style={styles.container}>
      <Text style={styles.title}>CUPOM FISCAL</Text>
      
      {props.items.map((item, i) => (
        <View key={i} style={styles.row}>
          <Text>{item.name}</Text>
          <Text>R$ {item.price.toFixed(2)}</Text>
        </View>
      ))}
      
      <View style={styles.divider} />
      
      <View style={styles.row}>
        <Text style={styles.bold}>TOTAL</Text>
        <Text style={styles.bold}>R$ {props.total.toFixed(2)}</Text>
      </View>
    </View>
  );
});

const styles = StyleSheet.create({
  container: {
    width: 384, // 58mm em pixels (203 DPI)
    backgroundColor: '#FFF',
    padding: 16,
  },
  title: {
    fontSize: 18,
    fontWeight: 'bold',
    textAlign: 'center',
    marginBottom: 12,
  },
  row: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 4,
  },
  divider: {
    height: 1,
    backgroundColor: '#000',
    marginVertical: 8,
  },
  bold: {
    fontWeight: 'bold',
  },
});
```

---

## ⚙️ Opções de Impressão

### **Hook usePrintReceipt**

```typescript
const { receiptRef, printReceipt, isPrinting } = usePrintReceipt({
  paperWidth: 58,        // 58mm ou 80mm
  dpi: 203,              // Resolução (sempre 203)
  applyDithering: true,  // Aplicar Floyd-Steinberg
});
```

---

## 📐 Dimensões Recomendadas

### **Largura do Template**

| Bobina | Pixels (203 DPI) | Recomendação |
|--------|------------------|--------------|
| 58mm | 384px | `width: 384` |
| 80mm | 576px | `width: 576` |

### **Tamanhos de Fonte**

```typescript
const styles = StyleSheet.create({
  title: { fontSize: 20 },      // Título principal
  subtitle: { fontSize: 16 },   // Subtítulos
  body: { fontSize: 12 },       // Texto normal
  small: { fontSize: 10 },      // Texto pequeno
});
```

---

## 🎨 Dicas de Design

### **✅ Boas Práticas**

- **Fundo branco:** `backgroundColor: '#FFFFFF'`
- **Texto preto:** `color: '#000000'`
- **Contraste alto:** Evite cinzas claros
- **Padding adequado:** `padding: 16` (mínimo)
- **Linhas divisórias:** `borderBottomWidth: 1`

### **❌ Evite**

- ❌ Imagens (podem falhar na captura)
- ❌ Gradientes (não imprimem bem)
- ❌ Sombras complexas
- ❌ Fontes muito pequenas (< 10px)
- ❌ Cores claras (cinza claro, amarelo)

---

## 🔧 Troubleshooting

### **Problema: Template não aparece na impressão**

**Solução:** Verifique se o template está com `position: 'absolute', left: -9999`:

```tsx
<View style={{ position: 'absolute', left: -9999 }}>
  <ReceiptTemplate ref={receiptRef} {...props} />
</View>
```

### **Problema: Texto cortado**

**Solução:** Ajuste a largura do container:

```typescript
container: {
  width: 384, // Para bobina 58mm
  padding: 16,
}
```

### **Problema: Qualidade ruim**

**Solução:** Ative o dithering:

```typescript
const { receiptRef, printReceipt } = usePrintReceipt({
  applyDithering: true, // ✅ Ativar Floyd-Steinberg
});
```

---

## 📦 Arquivos do Sistema

```
components/
  └── ReceiptTemplate.tsx    # Template padrão de cupom fiscal

hooks/
  └── usePrintReceipt.ts     # Hook para captura e impressão

app/(tabs)/
  └── index.tsx              # Exemplo de uso
```

---

## 🚀 Exemplo Completo

```tsx
import React, { useState } from 'react';
import { View, Button, Alert } from 'react-native';
import { ReceiptTemplate, ReceiptItem } from '@/components/ReceiptTemplate';
import { usePrintReceipt } from '@/hooks/usePrintReceipt';

export default function CheckoutScreen() {
  const { receiptRef, printReceipt, isPrinting } = usePrintReceipt();
  
  const [items] = useState<ReceiptItem[]>([
    { name: 'Produto A', price: 10.00, quantity: 2 },
    { name: 'Produto B', price: 15.50, quantity: 1 },
  ]);
  
  const total = items.reduce((sum, item) => sum + (item.price * item.quantity), 0);

  const handlePrint = async () => {
    const result = await printReceipt();
    
    if (result.success) {
      Alert.alert('✅ Sucesso', 'Cupom impresso!');
    } else {
      Alert.alert('❌ Erro', result.message);
    }
  };

  return (
    <View>
      <Button 
        title="Imprimir Cupom Fiscal" 
        onPress={handlePrint}
        disabled={isPrinting}
      />

      {/* Template oculto */}
      <View style={{ position: 'absolute', left: -9999 }}>
        <ReceiptTemplate
          ref={receiptRef}
          storeName="MINHA LOJA"
          items={items}
          total={total}
        />
      </View>
    </View>
  );
}
```

---

## ✨ Vantagens do Sistema

✅ **Flexibilidade total:** Crie layouts customizados em React Native  
✅ **Reutilizável:** Use o mesmo template em todo o app  
✅ **Qualidade profissional:** Dithering Floyd-Steinberg automático  
✅ **Fácil manutenção:** Edite o layout como qualquer componente React  
✅ **Type-safe:** TypeScript para evitar erros  

---

## 🎯 Próximos Passos

1. **Teste o exemplo** no app
2. **Customize o template** com sua marca
3. **Crie templates adicionais** (nota fiscal, recibo, etc)
4. **Integre com seu backend** para dados reais

**Pronto para imprimir! 🚀**
