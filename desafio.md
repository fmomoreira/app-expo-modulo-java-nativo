

Agora imprimiu o cupom de restaurante 

um garnde avanca 

a imagem base64 imrpimiu um testo dizendoq ue nao era erro no kotlin e sim na comunicacao do RN com o kotlin so que nao imprimiu a imagem o serto nao era ele pegar essa imagem base64 fazer aquele lance das cores e imprimir a imagem em si?

Essee  o primiro coisa a ser resolvido imprimir a imagem deveria fazer o fluxo competo ate imprimir a imagem dentro dos padros e margem denifinos


2 ponto e criar um template no react native e mandar par ala para imprimir

o template e esse converta para react antive e envie para o kotlin o ESC/POS para imprimir o bilhete 

<!DOCTYPE html>
<html lang="pt-BR">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bilhete - Reino da Sorte</title>

    <!-- Tailwind CSS para o layout flexível e espaçamentos -->
    <script src="https://cdn.tailwindcss.com"></script>
    <script src="https://unpkg.com/lucide@latest"></script>

    <!-- 1. IMPORTAÇÕES OBRIGATÓRIAS -->
    <script src="https://cdnjs.cloudflare.com/ajax/libs/html2canvas/1.4.1/html2canvas.min.js"></script>
    <!-- Biblioteca de QR Code -->
    <script src="https://cdnjs.cloudflare.com/ajax/libs/qrcodejs/1.0.0/qrcode.min.js"></script>
    <script src="ThermalPrinterWeb.js"></script>

    <style>
        /* Fonte do título para ficar idêntico à imagem */
        @import url('https://fonts.googleapis.com/css2?family=PT+Serif:wght@700&display=swap');

        body {
            background-color: #e2e8f0;
            display: flex;
            flex-direction: column;
            align-items: center;
            min-height: 100vh;
            padding: 2rem;
            font-family: Arial, Helvetica, sans-serif;
            color: #000;
        }

        /* 
        ==================================================================
        CSS DO BILHETE (REGRA DE OURO DA IMPRESSORA)
        ==================================================================
        */
        .area-de-impressao {
            width: 384px;
            /* LARGURA FIXA E ABSOLUTA (58mm) */
            background: #ffffff !important;
            color: #000000 !important;
            padding: 6px;
            /* Diminuído para aproveitar as bordas */
            box-sizing: border-box;
            border: 1px solid #ccc;
            /* Só para ver na tela */
        }

        /* Força que tudo dentro do bilhete seja P&B puro */
        .area-de-impressao * {
            color: #000000 !important;
            border-color: #000000 !important;
            background-color: transparent;
        }

        .title-font {
            font-family: 'PT Serif', serif;
        }

        /* Estilos específicos das linhas tracejadas do bilhete */
        .dashed-box {
            border: 2px dashed #000;
            margin-top: 10px;
        }

        .dashed-line {
            border-top: 2px dashed #000;
        }

        /* Força QR Code puro P&B sem anti-aliasing */
        #qrcode img {
            image-rendering: pixelated;
            margin: 0 auto;
        }

        /* Estilos do Painel de Controle na Web (Não imprime) */
        .painel-controle {
            background: white;
            padding: 20px;
            border-radius: 12px;
            width: 100%;
            max-width: 384px;
            margin-bottom: 20px;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
        }

        .btn {
            width: 100%;
            padding: 15px;
            margin: 5px 0;
            font-weight: bold;
            border-radius: 8px;
            border: none;
            cursor: pointer;
            font-size: 16px;
            display: flex;
            justify-content: center;
            align-items: center;
            gap: 8px;
        }

        .btn-conectar {
            background: #1e293b;
            color: white;
        }

        .btn-imprimir {
            background: #2563eb;
            color: white;
        }

        .btn-imprimir:disabled {
            background: #94a3b8;
            cursor: not-allowed;
        }
    </style>
</head>

<body>

    <!-- PAINEL DE CONTROLE WEB -->
    <div class="painel-controle">
        <h2 style="margin-top:0; font-family: 'Arial', sans-serif;" class="font-bold flex items-center gap-2">
            <i data-lucide="settings"></i> Controle de Impressão
        </h2>
        <p class="text-sm my-2">Status: <strong id="status-texto" style="color: red;">Desconectado</strong></p>
        <button id="btn-conectar" class="btn btn-conectar">
            <i data-lucide="bluetooth"></i> Conectar Impressora
        </button>
        <button id="btn-imprimir" class="btn btn-imprimir" disabled>
            <i data-lucide="printer"></i> IMPRIMIR BILHETE
        </button>
    </div>

    <!-- CONTAINER DO BILHETE (Exatos 384px) -->
    <div id="ticket-area" class="area-de-impressao text-[18px] leading-[1.25] font-bold">

        <!-- Cabeçalho -->
        <div class="text-center mb-3 mt-1">
            <h1 class="title-font text-[32px] font-black uppercase tracking-wide mb-1 text-black">Reino da Sorte</h1>
            <div class="flex items-center justify-center gap-2 text-[16px] text-black font-black">
                <span class="w-8 border-t-[3px] border-black"></span>
                <span class="uppercase tracking-widest">Jardim – Ceará</span>
                <span class="w-8 border-t-[3px] border-black"></span>
            </div>
        </div>

        <!-- Caixa Tracejada Principal -->
        <div class="dashed-box flex flex-col text-black">

            <!-- Secção Prêmio -->
            <div class="p-2">
                <p>PRÊMIO: FIAT UNO 2013, MAIS 15</p>
                <p>GIROS DE R$ 500,00.</p>
            </div>

            <div class="dashed-line"></div>

            <!-- Secção Datas -->
            <div class="p-2">
                <p>DATA DO SORTEIO: 28/02/2026</p>
                <p>DATA DA VENDA: 28/02/2026 08:12</p>
            </div>

            <div class="dashed-line"></div>

            <!-- Secção Cliente -->
            <div class="p-2 text-[20px]">
                <p>CLIENTE: <span class="font-black">Felipe Morreira</span></p>
                <p>TELEFONE: <span class="font-black">+55 (87)9 9159-1859</span></p>
            </div>

            <div class="dashed-line"></div>

            <!-- Secção Números da Sorte -->
            <div class="p-2 pb-3">
                <p class="text-center mb-2 font-black text-[20px]">NÚMEROS DA SORTE:</p>
                <div class="grid grid-cols-2 text-center text-[24px] gap-y-1 font-black tracking-widest">
                    <span>425242</span><span>319201</span>
                    <span>683165</span><span>364418</span>
                    <span>490819</span><span>703627</span>
                    <span>565632</span><span>701317</span>
                    <span>119099</span><span>403894</span>
                    <span>425242</span><span>319201</span>
                    <span>683165</span><span>364418</span>
                    <span>490819</span><span>703627</span>
                    <span>565632</span><span>701317</span>
                    <span>119099</span><span>403894</span>
                </div>
            </div>

            <div class="dashed-line"></div>

            <!-- Secção Vendedor -->
            <div class="p-2 text-center">
                <p>VENDEDOR: Banca Enfrente a Zaza</p>
                <p class="font-black text-[20px]">(87)9 9209-0279</p>
            </div>

            <div class="dashed-line"></div>

            <!-- Secção QR Code e Rodapé da Caixa (NOVO LAYOUT CENTRALIZADO) -->
            <div class="p-3 flex flex-col items-center justify-center gap-3">
                <!-- QR Code Dinâmico MAIOR -->
                <div class="bg-white p-2">
                    <div id="qrcode"></div>
                </div>

                <!-- Frase de Prazo -->
                <p class="text-[14px] text-center leading-tight font-black uppercase max-w-[90%]">
                    Prazo para o ganhador se apresentar até as 09H do dia seguinte
                </p>
            </div>

            <div class="border-t-[3px] border-black w-full"></div>

            <!-- Numero no final da caixa -->
            <div class="p-2 text-center">
                <p class="text-[20px] font-black">Nº 006.326-7</p>
            </div>

        </div>

        <!-- Rodapé Externo (Redes Sociais e Contato) -->
        <div class="text-center mt-3 text-[18px] pb-1 text-black font-black">
            <p>Instagram: @reinodasorteoficial</p>
            <p>Escritório: (88) 98807-2177</p>
        </div>

    </div>

    <!-- LÓGICA DE IMPRESSÃO (THERMAL PRINTER WEB) -->
    <script>
        lucide.createIcons();

        // 1. Gerar o QR Code apontando para o site desejado (TAMANHO AUMENTADO)
        new QRCode(document.getElementById("qrcode"), {
            text: "https://reinodasorte.com.br",
            width: 140, // Tamanho bastante aumentado para facilitar leitura
            height: 140,
            colorDark: "#000000",
            colorLight: "#ffffff",
            correctLevel: QRCode.CorrectLevel.M // Nível médio de correção para leitura fácil em térmica
        });

        // 2. Lógica da Impressora
        const printer = new ThermalPrinterWeb({
            width: 384, // Regra da impressora 58mm
            onConnect: (nome) => {
                document.getElementById('status-texto').innerText = nome || 'Conectado';
                document.getElementById('status-texto').style.color = 'green';
                document.getElementById('btn-imprimir').disabled = false;
                document.getElementById('btn-conectar').style.display = 'none';
            },
            onDisconnect: () => {
                document.getElementById('status-texto').innerText = 'Desconectado';
                document.getElementById('status-texto').style.color = 'red';
                document.getElementById('btn-imprimir').disabled = true;
                document.getElementById('btn-conectar').style.display = 'flex';
            }
        });

        document.getElementById('btn-conectar').addEventListener('click', async () => {
            try {
                document.getElementById('btn-conectar').innerHTML = '<i data-lucide="loader-2" class="animate-spin"></i> Conectando...';
                lucide.createIcons();
                await printer.connect();
            } catch (e) {
                alert("Erro: " + e.message);
                document.getElementById('btn-conectar').innerHTML = '<i data-lucide="bluetooth"></i> Conectar Impressora';
                lucide.createIcons();
            }
        });

        document.getElementById('btn-imprimir').addEventListener('click', async () => {
            const btn = document.getElementById('btn-imprimir');
            const originalHtml = btn.innerHTML;

            btn.disabled = true;
            btn.innerHTML = '<i data-lucide="loader-2" class="animate-spin"></i> PROCESSANDO...';
            lucide.createIcons();

            try {
                // Manda a div do bilhete para o nosso motor ThermalPrinterWeb
                await printer.printHtml('#ticket-area');
            } catch (e) {
                alert("Erro ao imprimir: " + e.message);
            } finally {
                btn.disabled = false;
                btn.innerHTML = originalHtml;
                lucide.createIcons();
            }
        });
    </script>
</body>

</html>


