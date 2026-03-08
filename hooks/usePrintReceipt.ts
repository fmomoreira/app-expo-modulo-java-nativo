import { useRef } from 'react';
import { View } from 'react-native';
import { captureRef } from 'react-native-view-shot';
import * as ThermalPrinter from '../modules/expo-thermal-printer';

export interface UsePrintReceiptOptions {
  paperWidth?: number;
  dpi?: number;
  applyDithering?: boolean;
}

/**
 * Hook para capturar e imprimir templates React Native
 * 
 * Uso:
 * ```tsx
 * const { receiptRef, printReceipt, isPrinting } = usePrintReceipt();
 * 
 * <ReceiptTemplate ref={receiptRef} items={items} total={total} />
 * <Button onPress={printReceipt} disabled={isPrinting} />
 * ```
 */
export function usePrintReceipt(options: UsePrintReceiptOptions = {}) {
  const receiptRef = useRef<View>(null);
  const isPrintingRef = useRef(false);

  const {
    paperWidth = 58,
    dpi = 203,
    applyDithering = true,
  } = options;

  const printReceipt = async (): Promise<{ success: boolean; message: string }> => {
    if (isPrintingRef.current) {
      return { success: false, message: 'Impressão já em andamento' };
    }

    if (!receiptRef.current) {
      return { success: false, message: 'Template não encontrado' };
    }

    try {
      isPrintingRef.current = true;

      console.log('[usePrintReceipt] Capturando template como imagem...');
      console.log('[usePrintReceipt] Ref atual:', receiptRef.current);
      
      // Aguarda um pouco para garantir que o componente está montado
      await new Promise(resolve => setTimeout(resolve, 100));
      
      // Captura o componente como Base64
      let base64Image: string;
      try {
        base64Image = await captureRef(receiptRef, {
          format: 'jpg',
          quality: 1,
          result: 'base64',
        });
        console.log('[usePrintReceipt] ✅ Template capturado! Tamanho:', base64Image.length);
      } catch (captureError: any) {
        console.error('[usePrintReceipt] ❌ ERRO NA CAPTURA:', captureError);
        throw new Error(`Falha ao capturar template: ${captureError.message || 'unknown error'}`);
      }

      console.log('[usePrintReceipt] Enviando para impressora...');

      // Envia para o módulo nativo imprimir
      const result = await ThermalPrinter.printImage(base64Image, {
        paperWidth: paperWidth as 58 | 80,
        dpi: dpi as 203,
        applyDithering,
      });

      console.log('[usePrintReceipt] Impressão concluída:', result);

      return {
        success: true,
        message: result.message || 'Cupom impresso com sucesso!',
      };

    } catch (error: any) {
      console.error('[usePrintReceipt] Erro ao imprimir:', error);
      return {
        success: false,
        message: error.message || 'Erro ao imprimir cupom',
      };
    } finally {
      isPrintingRef.current = false;
    }
  };

  return {
    receiptRef,
    printReceipt,
    get isPrinting() {
      return isPrintingRef.current;
    },
  };
}
