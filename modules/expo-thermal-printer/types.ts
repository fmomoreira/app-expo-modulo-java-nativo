export interface ExtraPrize {
  titulo: string;
  valor: string;
}

export interface Booklet {
  bookletNumber: number;
  lotNumber: number;
  tickets: string[];
}

export interface LotteryTicketData {
  id: string;
  customerName: string;
  customerPhone: string;
  drawerName: string;
  drawerDescription: string;
  sellerName: string;
  sellerPhone: string;
  drawTitle: string;
  drawDate: string;
  pricePerTicket: string;
  mainPrizeValue: string;
  extraPrizeValue?: ExtraPrize[];
  quantityBooklets: number;
  totalTickets: number;
  totalAmount: string;
  paymentMethod: string;
  createdAt: string;
  booklets: Booklet[];
  urlSorteio?: string;
}

export interface PrintLotteryTicketOptions {
  paperWidth?: 58 | 80;
  encoding?: 'ISO-8859-1' | 'CP850' | 'UTF-8';
}
