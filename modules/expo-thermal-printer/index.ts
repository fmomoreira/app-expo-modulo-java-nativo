import { requireNativeModule } from 'expo-modules-core';

// Import the native module
const ExpoThermalPrinterModule = requireNativeModule('ExpoThermalPrinter');

export interface PrinterDevice {
  name: string;
  address: string;
  type: 'bluetooth' | 'usb' | 'network';
}

export interface PrintOptions {
  paperWidth?: 58 | 80; // mm
  dpi?: 203;
  encoding?: 'ISO-8859-1' | 'CP850' | 'UTF-8';
  applyDithering?: boolean;
}

export interface PrintResult {
  success: boolean;
  message: string;
}

/**
 * Print an image using thermal printer with Floyd-Steinberg dithering
 * @param base64Image - Base64 encoded image string
 * @param options - Print options
 * @returns Promise with print result
 */
export async function printImage(
  base64Image: string,
  options?: PrintOptions
): Promise<PrintResult> {
  const cleanBase64 = base64Image.replace(/^data:image\/\w+;base64,/, '');
  
  return await ExpoThermalPrinterModule.printImage(cleanBase64, {
    paperWidth: options?.paperWidth || 58,
    dpi: options?.dpi || 203,
    encoding: options?.encoding || 'ISO-8859-1',
    applyDithering: options?.applyDithering !== false,
  });
}

/**
 * Print text using thermal printer
 * @param text - Text to print
 * @param options - Print options
 * @returns Promise with print result
 */
export async function printText(
  text: string,
  options?: PrintOptions
): Promise<PrintResult> {
  return await ExpoThermalPrinterModule.printText(text, {
    paperWidth: options?.paperWidth || 58,
    encoding: options?.encoding || 'ISO-8859-1',
  });
}

/**
 * Get list of available paired Bluetooth printers
 * @returns Promise with array of printer devices
 */
export async function getPairedPrinters(): Promise<PrinterDevice[]> {
  return await ExpoThermalPrinterModule.getPairedPrinters();
}

/**
 * Connect to a specific printer
 * @param address - Printer Bluetooth address
 * @returns Promise with connection result
 */
export async function connectPrinter(address: string): Promise<PrintResult> {
  return await ExpoThermalPrinterModule.connectPrinter(address);
}

/**
 * Disconnect from current printer
 * @returns Promise with disconnection result
 */
export async function disconnectPrinter(): Promise<PrintResult> {
  return await ExpoThermalPrinterModule.disconnectPrinter();
}

/**
 * Execute printer self-test
 * Prints a diagnostic page with alignments, formatting, accents and barcode
 * @returns Promise with test result
 */
export async function selfTest(): Promise<PrintResult> {
  return await ExpoThermalPrinterModule.selfTest();
}

/**
 * Print a test image hardcoded in Kotlin (no data sent from React Native)
 * Used to isolate if the problem is in RN→Kotlin communication or Kotlin processing
 * @param options - Print options (paperWidth, dpi, applyDithering)
 * @returns Promise with print result
 */
export async function printTestImage(options?: PrintOptions): Promise<PrintResult> {
  return await ExpoThermalPrinterModule.printTestImage(
    options?.paperWidth || 58,
    options?.dpi || 203,
    options?.applyDithering !== false
  );
}

export interface ReceiptItem {
  name: string;
  price: number;
  quantity: number;
}

export interface ReceiptOptions {
  cpf?: string;
  total: number;
  qrCodeUrl?: string;
}

/**
 * Print a formatted fiscal receipt with products and QR code
 * @param items - Array of products to print
 * @param options - Receipt options (cpf, total, qrCodeUrl)
 * @returns Promise with print result
 */
export async function printReceipt(
  items: ReceiptItem[],
  options: ReceiptOptions
): Promise<PrintResult> {
  return await ExpoThermalPrinterModule.printReceipt(items, {
    cpf: options.cpf || '',
    total: options.total,
    qrCodeUrl: options.qrCodeUrl || 'https://reinodasorte.com.br',
  });
}

/**
 * Print a lottery ticket (Reino da Sorte) with all booklets
 * @param ticketData - Complete lottery ticket data with booklets
 * @param options - Print options (paperWidth, encoding)
 * @returns Promise with print result
 */
export async function printLotteryTicket(
  ticketData: any,
  options?: { paperWidth?: 58 | 80; encoding?: string }
): Promise<PrintResult> {
  return await ExpoThermalPrinterModule.printLotteryTicket(ticketData, {
    paperWidth: options?.paperWidth || 58,
    encoding: options?.encoding || 'ISO-8859-1',
  });
}

export * from './types';
export { ExpoThermalPrinterModule };

