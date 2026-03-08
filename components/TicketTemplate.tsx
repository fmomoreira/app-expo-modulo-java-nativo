import React, { forwardRef } from 'react';
import { View, Text, StyleSheet } from 'react-native';
import QRCode from 'react-native-qrcode-svg';

interface TicketData {
  premio: string;
  dataSorteio: string;
  dataVenda: string;
  cliente: string;
  telefone: string;
  numeros: string[];
  vendedor: string;
  telefoneVendedor: string;
  numeroTicket: string;
  qrCodeUrl: string;
}

interface TicketTemplateProps {
  data: TicketData;
}

export const TicketTemplate = forwardRef<View, TicketTemplateProps>(({ data }, ref) => {
  return (
    <View style={styles.container}>
      {/* Cabeçalho */}
      <View style={styles.header}>
        <Text style={styles.title}>REINO DA SORTE</Text>
        <View style={styles.subtitle}>
          <View style={styles.line} />
          <Text style={styles.subtitleText}>JARDIM – CEARÁ</Text>
          <View style={styles.line} />
        </View>
      </View>

      {/* Caixa Principal */}
      <View style={styles.mainBox}>
        {/* Prêmio */}
        <View style={styles.section}>
          <Text style={styles.text}>{data.premio}</Text>
        </View>

        <View style={styles.dashedLine} />

        {/* Datas */}
        <View style={styles.section}>
          <Text style={styles.text}>DATA DO SORTEIO: {data.dataSorteio}</Text>
          <Text style={styles.text}>DATA DA VENDA: {data.dataVenda}</Text>
        </View>

        <View style={styles.dashedLine} />

        {/* Cliente */}
        <View style={styles.section}>
          <Text style={styles.textLarge}>CLIENTE: <Text style={styles.bold}>{data.cliente}</Text></Text>
          <Text style={styles.textLarge}>TELEFONE: <Text style={styles.bold}>{data.telefone}</Text></Text>
        </View>

        <View style={styles.dashedLine} />

        {/* Números da Sorte */}
        <View style={styles.section}>
          <Text style={[styles.textLarge, styles.bold, styles.center]}>NÚMEROS DA SORTE:</Text>
          <View style={styles.numbersGrid}>
            {data.numeros.map((numero, index) => (
              <Text key={index} style={styles.number}>{numero}</Text>
            ))}
          </View>
        </View>

        <View style={styles.dashedLine} />

        {/* Vendedor */}
        <View style={[styles.section, styles.center]}>
          <Text style={styles.text}>VENDEDOR: {data.vendedor}</Text>
          <Text style={styles.textLarge}>{data.telefoneVendedor}</Text>
        </View>

        <View style={styles.dashedLine} />

        {/* QR Code e Prazo */}
        <View style={[styles.section, styles.center]}>
          <View style={styles.qrCodeContainer}>
            <QRCode
              value={data.qrCodeUrl}
              size={140}
              color="#000000"
              backgroundColor="#FFFFFF"
            />
          </View>
          <Text style={styles.prazoText}>
            PRAZO PARA O GANHADOR SE APRESENTAR ATÉ AS 09H DO DIA SEGUINTE
          </Text>
        </View>

        <View style={styles.solidLine} />

        {/* Número do Ticket */}
        <View style={[styles.section, styles.center]}>
          <Text style={styles.textLarge}>Nº {data.numeroTicket}</Text>
        </View>
      </View>

      {/* Rodapé */}
      <View style={[styles.footer, styles.center]}>
        <Text style={styles.text}>Instagram: @reinodasorteoficial</Text>
        <Text style={styles.text}>Escritório: (88) 98807-2177</Text>
      </View>
    </View>
  );
});

const styles = StyleSheet.create({
  container: {
    width: 384,
    backgroundColor: '#FFFFFF',
    padding: 6,
  },
  header: {
    alignItems: 'center',
    marginBottom: 12,
    marginTop: 4,
  },
  title: {
    fontSize: 32,
    fontWeight: '900',
    color: '#000000',
    letterSpacing: 1,
    textTransform: 'uppercase',
  },
  subtitle: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
    marginTop: 4,
  },
  subtitleText: {
    fontSize: 16,
    fontWeight: '900',
    color: '#000000',
    letterSpacing: 2,
    textTransform: 'uppercase',
  },
  line: {
    width: 32,
    height: 3,
    backgroundColor: '#000000',
  },
  mainBox: {
    borderWidth: 2,
    borderStyle: 'dashed',
    borderColor: '#000000',
    marginTop: 10,
  },
  section: {
    padding: 8,
  },
  dashedLine: {
    height: 2,
    borderTopWidth: 2,
    borderStyle: 'dashed',
    borderColor: '#000000',
  },
  solidLine: {
    height: 3,
    backgroundColor: '#000000',
  },
  text: {
    fontSize: 18,
    fontWeight: '700',
    color: '#000000',
    lineHeight: 22,
  },
  textLarge: {
    fontSize: 20,
    fontWeight: '700',
    color: '#000000',
    lineHeight: 25,
  },
  bold: {
    fontWeight: '900',
  },
  center: {
    alignItems: 'center',
  },
  numbersGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'space-around',
    marginTop: 8,
    gap: 4,
  },
  number: {
    fontSize: 24,
    fontWeight: '900',
    color: '#000000',
    width: '45%',
    textAlign: 'center',
    letterSpacing: 2,
  },
  qrCodeContainer: {
    backgroundColor: '#FFFFFF',
    padding: 8,
    marginVertical: 12,
  },
  prazoText: {
    fontSize: 14,
    fontWeight: '900',
    color: '#000000',
    textAlign: 'center',
    textTransform: 'uppercase',
    lineHeight: 18,
    maxWidth: '90%',
  },
  footer: {
    marginTop: 12,
    paddingBottom: 4,
  },
});

TicketTemplate.displayName = 'TicketTemplate';
