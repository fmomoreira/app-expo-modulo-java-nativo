import React, { useRef, useState } from 'react';
import { View, Text, TouchableOpacity, ActivityIndicator, Alert, StyleSheet } from 'react-native';
import ViewShot, { captureRef } from 'react-native-view-shot';
import * as ThermalPrinter from '@/modules/expo-thermal-printer'; // Seu módulo

export default function CheckoutScreen() {
  // 1. A referência para a "câmera" do ViewShot
  const receiptRef = useRef(null);
  const [imprimindo, setImprimindo] = useState(false);

  const finalizarVenda = async () => {
    setImprimindo(true);
    try {
      console.log("1. Tirando a foto do layout...");
      
      // Captura o layout do React Native em Base64
      const base64Image = await captureRef(receiptRef, {
        format: 'jpg',   // JPG evita problemas de transparência do PNG
        quality: 1,      // Qualidade máxima
        result: 'base64' // Pede a string pura
      });

      console.log("2. Enviando para o Kotlin processar (Dithering)...");
      
      // Conecta (caso não esteja) e manda para o seu motor Kotlin
      await ThermalPrinter.connectPrinter('internal_auto');
      const resultado = await ThermalPrinter.printImage(base64Image, {
        paperWidth: 58, 
        applyDithering: true // Deixa o Kotlin fazer o trabalho pesado!
      });

      Alert.alert("Sucesso!", resultado.message);

    } catch (error: any) {
      console.error("Erro no fluxo:", error);
      Alert.alert("Falha na Impressão", error.message);
    } finally {
      setImprimindo(false);
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Finalizar Compra</Text>

      {/* ⚠️ O SEGREDO ESTÁ AQUI ⚠️
        Para esconder o recibo do usuário sem quebrar o view-shot no Android:
        1. collapsable={false} impede o Android de apagar a View da memória.
        2. position: 'absolute' e zIndex: -1 escondem atrás de tudo.
      */}
      <View style={styles.hiddenWrapper} collapsable={false}>
        <ViewShot ref={receiptRef} options={{ format: 'jpg', quality: 1 }}>
          
          {/* O Seu Template (Largura fixa de 384px para bobinas de 58mm) */}
          <View collapsable={false} style={styles.receiptPaper}>
            <Text style={styles.logo}>SOLO TECNOLOGIA</Text>
            <Text style={styles.textCenter}>CNPJ: 00.000.000/0001-00</Text>
            <Text style={styles.divider}>--------------------------------</Text>
            
            <View style={styles.row}>
              <Text style={styles.item}>1x Sistema Pre-Matricula</Text>
              <Text style={styles.price}>R$ 900,00</Text>
            </View>
            
            <Text style={styles.divider}>--------------------------------</Text>
            <Text style={styles.total}>TOTAL: R$ 900,00</Text>
            <Text style={styles.textCenter}>Agradecemos a preferência!</Text>
          </View>

        </ViewShot>
      </View>

      <TouchableOpacity style={styles.btn} onPress={finalizarVenda} disabled={imprimindo}>
        {imprimindo ? <ActivityIndicator color="#FFF" /> : <Text style={styles.btnText}>Imprimir Recibo</Text>}
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, padding: 20, justifyContent: 'center', backgroundColor: '#F5F5F5' },
  title: { fontSize: 24, fontWeight: 'bold', textAlign: 'center', marginBottom: 40 },
  
  // O invólucro que esconde o recibo da tela do celular, mas deixa ele renderizado para o ViewShot
  hiddenWrapper: {
    position: 'absolute',
    top: 0,
    zIndex: -1, 
    opacity: 0, 
  },
  
  // O papel do recibo. Fundo branco é obrigatório para a foto sair certa.
  receiptPaper: {
    width: 384, // 58mm = 384 pixels
    backgroundColor: '#FFFFFF',
    padding: 10,
  },
  
  logo: { fontSize: 26, fontWeight: 'bold', textAlign: 'center', color: '#000' },
  textCenter: { textAlign: 'center', fontSize: 16, color: '#000' },
  divider: { textAlign: 'center', marginVertical: 10, color: '#000' },
  row: { flexDirection: 'row', justifyContent: 'space-between', marginVertical: 5 },
  item: { fontSize: 16, color: '#000', flex: 1 },
  price: { fontSize: 16, color: '#000', fontWeight: 'bold' },
  total: { fontSize: 22, fontWeight: 'bold', textAlign: 'right', color: '#000', marginTop: 10 },
  
  btn: { backgroundColor: '#007AFF', padding: 15, borderRadius: 8, alignItems: 'center' },
  btnText: { color: '#FFF', fontSize: 18, fontWeight: 'bold' }
});