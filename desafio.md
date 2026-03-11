{
    "id": "560",
    "customerName": "Fabio Junior",
    "customerPhone": "(87) 1 2358-8975",
    "drawerName": "Drawer Name",
    "drawerDescription": "Drawer Description",
    "sellerName": "João Silva",
    "sellerPhone": "(87) 9 9999-9999",
    "drawTitle": "Teste de Sorteio",
    "drawDate": "2026-03-14T19:00:00.000Z",
    "pricePerTicket": "2",
    "mainPrizeValue": "50000",
    "extraPrizeValue": [
      {
        "titulo": "Giro Extra",
        "valor": "500"
      },
      {
        "titulo": "Giro extra",
        "valor": "500"
      }
    ],
    "quantityBooklets": 2,
    "totalTickets": 40,
    "totalAmount": "4.00",
    "paymentMethod": "pix",
    "createdAt": "2026-03-11T00:33:03.479Z",
    "booklets": [
        {
            "bookletNumber": 212,
            "lotNumber": 2,
            "tickets": [
                "004038-2",
                "004155-2",
                "004394-2",
                "004565-2",
                "004654-2",
                "004729-2",
                "004943-2",
                "005202-2",
                "005303-2",
                "005406-2",
                "005620-2",
                "006263-2",
                "006279-2",
                "006287-2",
                "006491-2",
                "007100-2",
                "007229-2",
                "007508-2",
                "007639-2",
                "007762-2"
            ]
        },
        {
            "bookletNumber": 214,
            "lotNumber": 2,
            "tickets": [
                "004795-2",
                "004810-2",
                "004839-2",
                "005159-2",
                "005295-2",
                "005829-2",
                "006292-2",
                "006399-2",
                "006666-2",
                "006752-2",
                "006873-2",
                "006961-2",
                "007034-2",
                "007071-2",
                "007230-2",
                "007409-2",
                "007461-2",
                "007483-2",
                "007701-2",
                "007902-2"
            ]
        }
    ]
}




Comandos exemplo apra o kotlin

Comandos ESC/POS para Kotlin (Android)

Para utilizar no seu projeto Android/Kotlin, pode usar esta estrutura base. Note que os comandos hexadecimais correspondem exatamente ao que testámos no HTML, garantindo o mesmo layout e economia de papel.

object EscPosCommands {
    val INIT = byteArrayOf(0x1B, 0x40)
    val ALIGN_CENTER = byteArrayOf(0x1B, 0x61, 0x01)
    val ALIGN_LEFT = byteArrayOf(0x1B, 0x61, 0x00)
    val BOLD_ON = byteArrayOf(0x1B, 0x45, 0x01)
    val BOLD_OFF = byteArrayOf(0x1B, 0x45, 0x00)
    val TEXT_X2 = byteArrayOf(0x1D, 0x21, 0x11) // Dobro de largura e altura
    val TEXT_NORMAL = byteArrayOf(0x1D, 0x21, 0x00)
    val LINE_DASH = "--------------------------------\n".toByteArray(Charsets.US_ASCII)
    
    // Comandos de QR Code Nativo (Model 2, Size 6, Error Correction L)
    val QR_MODEL = byteArrayOf(0x1D, 0x28, 0x6B, 0x04, 0x00, 0x31, 0x41, 0x32, 0x00)
    val QR_SIZE = byteArrayOf(0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x43, 0x06)
    val QR_ERROR_CORR = byteArrayOf(0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x45, 0x30)
    val QR_PRINT = byteArrayOf(0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x51, 0x30)
}

/**
 * Gera os bytes para um bilhete completo baseado num talão (booklet)
 */
fun generateTicketBytes(ticket: TicketData, booklet: Booklet, index: Int, total: Int): ByteArray {
    val output = java.io.ByteArrayOutputStream()

    // 1. Inicialização e Cabeçalho (Fonte Grande)
    output.write(EscPosCommands.INIT)
    output.write(EscPosCommands.ALIGN_CENTER)
    output.write(EscPosCommands.TEXT_X2)
    output.write(EscPosCommands.BOLD_ON)
    output.write("REINO DA SORTE\n".toByteArray(Charsets.ISO_8859_1))
    
    // 2. Subtítulo e Contagem de Talões (Fonte Normal)
    output.write(EscPosCommands.TEXT_NORMAL)
    output.write("${ticket.drawTitle?.uppercase()}\n".toByteArray(Charsets.ISO_8859_1))
    output.write("TALAO ${index + 1} DE $total\n".toByteArray(Charsets.ISO_8859_1))
    output.write(EscPosCommands.LINE_DASH)

    // 3. Detalhes de Prémios e Datas (Alinhado à Esquerda)
    output.write(EscPosCommands.ALIGN_LEFT)
    output.write("Premio: ${ticket.mainPrizeValue}\n".toByteArray(Charsets.ISO_8859_1))
    ticket.extraPrizeValue?.forEach { extra ->
        output.write("${extra.titulo}: ${extra.valor}\n".toByteArray(Charsets.ISO_8859_1))
    }
    output.write("Sorteio: ${ticket.drawDate}\n".toByteArray(Charsets.ISO_8859_1))
    output.write("Venda: ${ticket.createdAt}\n".toByteArray(Charsets.ISO_8859_1))
    output.write(EscPosCommands.LINE_DASH)

    // 4. Info do Cliente
    output.write("Cliente: ${ticket.customerName}\n".toByteArray(Charsets.ISO_8859_1))
    output.write("Tel: ${ticket.customerPhone}\n".toByteArray(Charsets.ISO_8859_1))
    output.write(EscPosCommands.LINE_DASH)

    // 5. NÚMEROS DA SORTE (Fonte Normal + Negrito para economizar espaço)
    output.write(EscPosCommands.ALIGN_CENTER)
    output.write(EscPosCommands.BOLD_ON)
    output.write("NUMEROS DA SORTE\n\n".toByteArray(Charsets.ISO_8859_1))
    
    output.write(EscPosCommands.TEXT_NORMAL)
    output.write(EscPosCommands.BOLD_ON)

    val tickets = booklet.tickets
    for (i in tickets.indices step 2) {
        val num1 = tickets[i].split("-")[0].chunked(1).joinToString(" ")
        val num2 = if (i + 1 < tickets.size) {
            tickets[i + 1].split("-")[0].chunked(1).joinToString(" ")
        } else ""
        
        // Espaçamento de 4 espaços entre as colunas para clareza
        val line = "$num1    $num2".trimEnd() + "\n"
        output.write(line.toByteArray(Charsets.ISO_8859_1))
    }
    
    // ID do Bilhete (número do talão)
    val bookletId = "${booklet.bookletNumber}-${booklet.lotNumber}".chunked(1).joinToString(" ")
    output.write("\nbilhete: $bookletId\n".toByteArray(Charsets.ISO_8859_1))

    output.write(EscPosCommands.BOLD_OFF)
    output.write(EscPosCommands.LINE_DASH)

    // 6. Info do Vendedor
    output.write(EscPosCommands.ALIGN_LEFT)
    output.write("Vendedor: ${ticket.sellerName}\n".toByteArray(Charsets.ISO_8859_1))
    output.write("Tel: ${ticket.sellerPhone}\n".toByteArray(Charsets.ISO_8859_1))
    output.write(EscPosCommands.LINE_DASH)

    // 7. QR Code Nativo
    output.write(EscPosCommands.ALIGN_CENTER)
    output.write("Escaneie para validar:\n\n".toByteArray(Charsets.ISO_8859_1))
    
    val qrData = "[https://reinodasorte.com.br/valida/$](https://reinodasorte.com.br/valida/$){ticket.id}/${booklet.bookletNumber}"
    output.write(generateQrCodeBytes(qrData))
    
    // 8. Rodapé e Termos
    output.write("\n\nPRAZO P/ GANHADOR SE APRESENTAR\n".toByteArray(Charsets.ISO_8859_1))
    output.write("ATE AS 09H DO DIA SEGUINTE\n".toByteArray(Charsets.ISO_8859_1))
    output.write("ID VENDA: ${ticket.id}\n\n".toByteArray(Charsets.ISO_8859_1))

    // 9. Avanço de papel reduzido (2 linhas) para o próximo bilhete
    output.write(byteArrayOf(0x1B, 0x64, 0x02)) 

    return output.toByteArray()
}

/**
 * Gera os comandos de armazenamento e impressão de QR Code
 */
fun generateQrCodeBytes(data: String): ByteArray {
    val baos = java.io.ByteArrayOutputStream()
    val storeData = data.toByteArray(Charsets.ISO_8859_1)
    val pL = ((storeData.size + 3) % 256).toByte()
    val pH = ((storeData.size + 3) / 256).toByte()

    baos.write(EscPosCommands.QR_MODEL)
    baos.write(EscPosCommands.QR_SIZE)
    baos.write(EscPosCommands.QR_ERROR_CORR)
    
    // Header de armazenamento: [GS ( k pL pH cn fn m d1...dk]
    baos.write(byteArrayOf(0x1D, 0x28, 0x6B, pL, pH, 0x31, 0x50, 0x30))
    baos.write(storeData)
    baos.write(EscPosCommands.QR_PRINT)
    
    return baos.toByteArray()
}


Notas Importantes:

Charset: Utilizei ISO_8859_1 para suportar acentos simples, que é o padrão da maioria das impressoras térmicas brasileiras.

Layout Idêntico: O uso de step 2 no loop de números e o joinToString(" ") garante que os números fiquem espaçados e em duas colunas, exatamente como no HTML.

QR Code: Incluí a função generateQrCodeBytes que utiliza os comandos GS ( k, evitando a necessidade de converter o QR Code em imagem (o que economiza processamento no Android).

Espaçamento: O comando final 0x1B, 0x64, 0x02 mantém o avanço curto entre os talões.


exemplos de como fucnionou muito bem na web 

<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bilhete - Reino da Sorte (ESC/POS Multi-Talão)</title>

    <script src="https://cdn.tailwindcss.com"></script>
    <script src="https://unpkg.com/lucide@latest"></script>
    
    <style>
        @import url('https://fonts.googleapis.com/css2?family=PT+Serif:wght@700&display=swap');
        body { background-color: #e2e8f0; display: flex; flex-direction: column; align-items: center; min-height: 100vh; padding: 2rem; font-family: Arial, sans-serif; }
        
        #tickets-container { width: 384px; display: flex; flex-direction: column; gap: 20px; }
        .area-de-impressao { width: 100%; background: #fff; padding: 10px; border: 1px solid #ccc; }
        
        .painel-controle { background: white; padding: 20px; border-radius: 12px; width: 100%; max-width: 384px; margin-bottom: 20px; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1); }
        .btn { width: 100%; padding: 15px; margin: 5px 0; font-weight: bold; border-radius: 8px; cursor: pointer; display: flex; justify-content: center; align-items: center; gap: 8px; transition: all 0.2s; }
        .btn-conectar { background: #1e293b; color: white; }
        .btn-imprimir { background: #059669; color: white; }
        .btn-avancar { background: #64748b; color: white; }
        .btn:disabled { background: #94a3b8; cursor: not-allowed; opacity: 0.7; }
        .status-badge { padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: bold; }
    </style>
</head>

<body>
    <div class="painel-controle">
        <h2 class="font-bold flex items-center gap-2 mb-2 text-xl">
            <i data-lucide="zap" class="text-yellow-500"></i> Impressão Multi-Talão
        </h2>
        <p class="text-xs text-gray-500 mb-4">Um bilhete completo por talão (ESC/POS).</p>
        
        <div class="flex items-center justify-between mb-4 bg-gray-50 p-3 rounded-lg">
            <span class="text-sm font-medium">Status da Impressora:</span>
            <span id="status-badge" class="status-badge bg-red-100 text-red-600">Desconectada</span>
        </div>
        
        <button id="btn-conectar" class="btn btn-conectar">
            <i data-lucide="bluetooth"></i> Conectar Bluetooth
        </button>
        
        <button id="btn-imprimir" class="btn btn-imprimir" disabled>
            <i data-lucide="printer"></i> IMPRIMIR TODOS (2)
        </button>

        <button id="btn-avancar" class="btn btn-avancar" disabled>
            <i data-lucide="arrow-down-to-line"></i> AVANÇAR PAPEL
        </button>
    </div>

    <div id="tickets-container">
        <div class="area-de-impressao text-sm border-dashed border-2 opacity-60">
            <p class="text-center font-bold">MODO DE IMPRESSÃO EM LOTE</p>
            <p class="text-[10px] text-center text-gray-400">Cada talão sairá como um bilhete completo.</p>
        </div>
    </div>

    <script>
        lucide.createIcons();

        const ticketsData = {
            "id": "560",
            "customerName": "Fabio Junior",
            "customerPhone": "(87) 1 2358-8975",
            "drawerName": "Drawer Name",
            "drawerDescription": "Drawer Description",
            "sellerName": "João Silva",
            "sellerPhone": "(87) 9 9999-9999",
            "drawTitle": "Teste de Sorteio",
            "drawDate": "2026-03-14T19:00:00.000Z",
            "pricePerTicket": "2",
            "mainPrizeValue": "50000",
            "extraPrizeValue": [
                { "titulo": "Giro Extra", "valor": "500" },
                { "titulo": "Giro extra", "valor": "500" }
            ],
            "quantityBooklets": 2,
            "totalTickets": 40,
            "totalAmount": "4.00",
            "paymentMethod": "pix",
            "createdAt": "2026-03-11T00:33:03.479Z",
            "booklets": [
                {
                    "bookletNumber": 212, "lotNumber": 2,
                    "tickets": ["004038-2", "004155-2", "004394-2", "004565-2", "004654-2", "004729-2", "004943-2", "005202-2", "005303-2", "005406-2", "005620-2", "006263-2", "006279-2", "006287-2", "006491-2", "007100-2", "007229-2", "007508-2", "007639-2", "007762-2"]
                },
                {
                    "bookletNumber": 214, "lotNumber": 2,
                    "tickets": ["004795-2", "004810-2", "004839-2", "005159-2", "005295-2", "005829-2", "006292-2", "006399-2", "006666-2", "006752-2", "006873-2", "006961-2", "007034-2", "007071-2", "007230-2", "007409-2", "007461-2", "007483-2", "007701-2", "007902-2"]
                }
            ]
        };

        const ESC = 0x1B;
        const GS = 0x1D;
        const Commands = {
            INIT: [ESC, 0x40],
            ALIGN_CENTER: [ESC, 0x61, 0x01],
            ALIGN_LEFT: [ESC, 0x61, 0x00],
            BOLD_ON: [ESC, 0x45, 0x01],
            BOLD_OFF: [ESC, 0x45, 0x00],
            TEXT_X2: [GS, 0x21, 0x11],
            TEXT_H2: [GS, 0x21, 0x01],
            TEXT_NORMAL: [GS, 0x21, 0x00],
            LINE_DASH: "--------------------------------\n",
        };

        function separarDigitos(texto) {
            return texto.split('').join(' ');
        }

        class RawBluetoothPrinter {
            constructor() {
                this.device = null;
                this.characteristic = null;
            }

            async connect() {
                this.device = await navigator.bluetooth.requestDevice({
                    filters: [{ services: ['0000ff00-0000-1000-8000-00805f9b34fb'] }, { namePrefix: 'MPT' }, { namePrefix: 'BT' }],
                    optionalServices: ['0000ff00-0000-1000-8000-00805f9b34fb']
                });
                const server = await this.device.gatt.connect();
                const service = await server.getPrimaryService('0000ff00-0000-1000-8000-00805f9b34fb');
                this.characteristic = await service.getCharacteristic('0000ff02-0000-1000-8000-00805f9b34fb');
                this.device.addEventListener('gattserverdisconnected', () => this.onDisconnected());
                return this.device.name;
            }

            async write(data) {
                if (!this.characteristic) return;
                const CHUNK_SIZE = 20;
                for (let i = 0; i < data.length; i += CHUNK_SIZE) {
                    const chunk = data.slice(i, i + CHUNK_SIZE);
                    await this.characteristic.writeValue(chunk);
                }
            }

            async feed(lines = 3) {
                const cmd = new Uint8Array([ESC, 0x64, lines]);
                await this.write(cmd);
            }

            async printQRCode(data) {
                const encoder = new TextEncoder();
                const dataBytes = encoder.encode(data);
                const pL = (dataBytes.length + 3) % 256;
                const pH = Math.floor((dataBytes.length + 3) / 256);
                await this.write(new Uint8Array([0x1D, 0x28, 0x6B, 0x04, 0x00, 0x31, 0x41, 0x32, 0x00]));
                await this.write(new Uint8Array([0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x43, 0x06]));
                await this.write(new Uint8Array([0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x45, 0x30]));
                let header = new Uint8Array([0x1D, 0x28, 0x6B, pL, pH, 0x31, 0x50, 0x30]);
                let storeCmd = new Uint8Array(header.length + dataBytes.length);
                storeCmd.set(header);
                storeCmd.set(dataBytes, header.length);
                await this.write(storeCmd);
                await this.write(new Uint8Array([0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x51, 0x30]));
            }

            onDisconnected() { updateStatus(false); }
        }

        const printer = new RawBluetoothPrinter();
        const encoder = new TextEncoder();

        function formatarMoeda(v) { return Number(v).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' }); }
        function formatarData(iso) {
            const d = new Date(iso);
            return `${String(d.getUTCDate()).padStart(2, '0')}/${String(d.getUTCMonth() + 1).padStart(2, '0')}/${d.getUTCFullYear()}`;
        }

        async function imprimirUmTalao(ticket, booklet, index, total) {
            await printer.write(new Uint8Array(Commands.INIT));
            await printer.write(new Uint8Array(Commands.ALIGN_CENTER));
            await printer.write(new Uint8Array(Commands.TEXT_X2));
            await printer.write(new Uint8Array(Commands.BOLD_ON));
            await printer.write(encoder.encode("REINO DA SORTE\n"));
            await printer.write(new Uint8Array(Commands.TEXT_NORMAL));
            await printer.write(encoder.encode((ticket.drawTitle || "JARDIM - CEARA").toUpperCase() + "\n"));
            await printer.write(encoder.encode(`TALÃO ${index + 1} DE ${total}\n`));
            await printer.write(encoder.encode(Commands.LINE_DASH));

            await printer.write(new Uint8Array(Commands.ALIGN_LEFT));
            await printer.write(encoder.encode(`Premio: ${formatarMoeda(ticket.mainPrizeValue)}\n`));
            if (ticket.extraPrizeValue && ticket.extraPrizeValue.length > 0) {
                for (const extra of ticket.extraPrizeValue) {
                    await printer.write(encoder.encode(`${extra.titulo}: ${formatarMoeda(extra.valor)}\n`));
                }
            }
            await printer.write(encoder.encode(`Sorteio: ${formatarData(ticket.drawDate)}\n`));
            await printer.write(encoder.encode(`Venda: ${formatarData(ticket.createdAt)}\n`));
            await printer.write(encoder.encode(Commands.LINE_DASH));

            await printer.write(encoder.encode(`Cliente: ${ticket.customerName}\n`));
            await printer.write(encoder.encode(`Tel: ${ticket.customerPhone}\n`));
            await printer.write(encoder.encode(Commands.LINE_DASH));

            await printer.write(new Uint8Array(Commands.ALIGN_CENTER));
            await printer.write(new Uint8Array(Commands.BOLD_ON));
            await printer.write(encoder.encode(`NUMEROS DA SORTE\n\n`));
            
            // Usando tamanho normal (sem dobrar altura) para economizar papel
            await printer.write(new Uint8Array(Commands.TEXT_NORMAL));
            await printer.write(new Uint8Array(Commands.BOLD_ON));
            
            const tickets = booklet.tickets;
            for (let i = 0; i < tickets.length; i += 2) {
                let rawNum1 = tickets[i].split('-')[0];
                let rawNum2 = tickets[i + 1] ? tickets[i + 1].split('-')[0] : "";
                
                let num1 = separarDigitos(rawNum1);
                let num2 = separarDigitos(rawNum2);
                
                // Mantemos o espaçamento largo (4 espaços) para clareza
                let line = `${num1}    ${num2}`.trim();
                await printer.write(encoder.encode(line + "\n"));
            }
            
            const bookletId = `${booklet.bookletNumber}-${booklet.lotNumber}`;
            await printer.write(encoder.encode("\n" + `bilhete: ${separarDigitos(bookletId)}` + "\n"));

            await printer.write(new Uint8Array(Commands.BOLD_OFF));
            await printer.write(encoder.encode(Commands.LINE_DASH));

            await printer.write(new Uint8Array(Commands.ALIGN_LEFT));
            await printer.write(encoder.encode(`Vendedor: ${ticket.sellerName}\n`));
            await printer.write(encoder.encode(`Tel: ${ticket.sellerPhone}\n`));
            await printer.write(encoder.encode(Commands.LINE_DASH));

            await printer.write(new Uint8Array(Commands.ALIGN_CENTER));
            await printer.write(encoder.encode("Escaneie para validar:\n\n"));
            await printer.printQRCode(`https://reinodasorte.com.br/valida/${ticket.id}/${booklet.bookletNumber}`);
            
            await printer.write(encoder.encode("\n\n"));
            await printer.write(encoder.encode("PRAZO P/ GANHADOR SE APRESENTAR\n"));
            await printer.write(encoder.encode("ATE AS 09H DO DIA SEGUINTE\n"));
            await printer.write(encoder.encode(`ID VENDA: ${ticket.id}\n\n`));

            // Espaçamento reduzido entre talões para maior agilidade e economia
            await printer.feed(2);
        }

        async function imprimirTodosOsTalões(data) {
            for (let i = 0; i < data.booklets.length; i++) {
                await imprimirUmTalao(data, data.booklets[i], i, data.booklets.length);
                await new Promise(r => setTimeout(r, 300)); // Pequena pausa reduzida entre os envios
            }
            // Avanço maior apenas no final de tudo para facilitar o corte manual do lote
            await printer.feed(4);
        }

        function updateStatus(connected, name = '') {
            const badge = document.getElementById('status-badge');
            const btnImprimir = document.getElementById('btn-imprimir');
            const btnAvancar = document.getElementById('btn-avancar');
            const btnConectar = document.getElementById('btn-conectar');

            if (connected) {
                badge.innerText = name || 'Conectada';
                badge.className = 'status-badge bg-green-100 text-green-600';
                btnImprimir.disabled = false;
                btnAvancar.disabled = false;
                btnConectar.style.display = 'none';
            } else {
                badge.innerText = 'Desconectada';
                badge.className = 'status-badge bg-red-100 text-red-600';
                btnImprimir.disabled = true;
                btnAvancar.disabled = true;
                btnConectar.style.display = 'flex';
            }
        }

        document.getElementById('btn-conectar').addEventListener('click', async () => {
            try {
                const name = await printer.connect();
                updateStatus(true, name);
            } catch (e) {
                alert("Erro ao conectar Bluetooth.");
            }
        });

        document.getElementById('btn-imprimir').addEventListener('click', async () => {
            const btn = document.getElementById('btn-imprimir');
            btn.disabled = true;
            btn.innerHTML = '<i data-lucide="loader-2" class="animate-spin"></i> IMPRIMINDO...';
            lucide.createIcons();
            try {
                await imprimirTodosOsTalões(ticketsData);
            } catch (e) {
                alert(e.message);
            } finally {
                btn.disabled = false;
                btn.innerHTML = `<i data-lucide="printer"></i> IMPRIMIR TODOS (${ticketsData.booklets.length})`;
                lucide.createIcons();
            }
        });

        document.getElementById('btn-avancar').addEventListener('click', () => printer.feed(4));
    </script>
</body>
</html>
            