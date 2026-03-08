Estudo de Implementação: React Native + Kotlin (Moderninha Smart 2)
Este estudo detalha a análise da arquitetura atual do app React Native e do módulo nativo Kotlin focado na compatibilidade com terminal Moderninha Smart 2 (sistema baseado no Android 7 - API 24).

1. Arquitetura Atual
O projeto é construído através do Expo com a New Architecture ativada e utiliza chamadas diretas com um módulo nativo construído com as abstrações do expo-modules-core. No universo nativo, há uma abstração rica focada na impressão via ESC/POS através da biblioteca com.github.DantSu:ESCPOS-ThermalPrinter-Android:3.3.0.

A descoberta ocorre priorizando requisições Bluetooth primeiro, e USB como fallback, listando e buscando dispositivos cujos nomes match (innerprinter, mpos, moderninha, printer).

2. Desafios de Compatibilidade e Permissões (Android 7 a Android 14)
2.1 Conflito de Permissões de Bluetooth
O 
app.json
 declara um conjunto robusto de permissões:

json
"android.permission.BLUETOOTH",
"android.permission.BLUETOOTH_ADMIN",
"android.permission.BLUETOOTH_CONNECT",
"android.permission.BLUETOOTH_SCAN",
"android.permission.ACCESS_COARSE_LOCATION",
"android.permission.ACCESS_FINE_LOCATION"
Análise para a Moderninha (Android 7 - SDK 24):

As permissões BLUETOOTH_CONNECT e BLUETOOTH_SCAN são exclusivas do Android 12 (SDK 31+). Sistemas antigos como o Android 7 irão simplesmente ignorar essas permissões na manifest, pois os tokens são desconhecidos para eles.
Para o Android 7, a busca e pareamento de impressoras operam perfeitamente apenas com a dupla genérica de BLUETOOTH/ADMIN e de ACCESS_COARSE/FINE_LOCATION.
Melhoria no App Nível React Native: Se você usar bibliotecas Javascript que acionam popups de autorização (como expo-location ou checagem manual pelo PermissionsAndroid do RN), certifique-se de não exigir que as permissões do Android 12 retornem "GRANDE/AUTHORIZED" quando rodarem na maquininha, pois resultarão sempre em undefined ou erro, bloqueando seu usuário.
2.2 Descoberta do Broadcast no Android (PendigIntent Mutável)
No arquivo 
ExpoThermalPrinterModule.kt
, para criar prompt ao usuário caso uma impressora USB seja detectada:

kotlin
val permissionIntent = PendingIntent.getBroadcast(
    context, 0, Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_MUTABLE
)
A const FLAG_MUTABLE foi padronizada no API 31+ para reforço de segurança. Com o targetSdkVersion 34 do módulo (no gradle), isso passa no build Kotlin, porém rodando de fato em um terminal Android 7 algumas bibliotecas ou ROMs customizadas do fabricante UOL/PagSeguro às vezes enfrentam crashes com flags que não conhecem.
Melhoria: O ideal e mais seguro para suportar de Android 7 a 14 é identificar e condicionar isso no runtime, utilizando por exemplo abstrações mais genéricas ou os modificadores providos pelo PendingIntent.FLAG_UPDATE_CURRENT adicionados condicionalmente à versão rodando se houver travamento ao chamar o RequestPermission do USB.
3. Melhorias no Fluxo de Conexões e Impressoras Internas (InnerPrinters)
3.1 Caching de Estado do Socket (Fuga de Bateria e "Broken Pipe")
Atualmente, no Android 7 agressivo em seu Doze Mode (Dormência), conexões Bluetooth contínuas ativas podem receber um encerramento forçado do SO para salvar bateria. Se isso ocorre, a Promise do tipo "printImage" via o currentPrinter?.printFormattedText disparará uma Exception de Broken Pipe.

Melhoria no Kotlin: No método connectPrinter e dentro dos prints, envolver os processos destas impressoras Moderninhas em blocos com "Mecanismo de Auto-Reconnect (Retry)". Desconecte forçadamente o socket e instancie um novo EscPosPrinter(connection...) imediatamente se o bloco try falhar com socket fechado, prevenindo que o usuário tenha que clicar num botão "Sincronizar Impressoras" de novo visualmente.
3.2 Melhor Comunicação com os Terminais POS UOL / PagSeguro
Esses terminais rodam Androids severamente engessados ("locked down" devices).

O código atual assume no método 
findAnyAvailablePrinter()
 que a impressora está sempre presente nas interfaces listadas via adapter Bluetooth tradicional (como pareados) ou via barramento USB.
Para essas impressoras embutidas, a biblioteca Dantsu consegue enviar comandos corretamente contanto que as impressoras internas se auto-apresentem pro Adapter Bluetooth do Android! E elas geralmente fazem. A linha com name.contains("innerprinter") é uma ótima heurística. Cuidado constante apenas para que as máquinas menores (que não Moderninha Smart 2 e sim concorrentes) não tentem acessar via USB simultâneo limitando os modems.
3.3 Particularidades dos Terminais SUNMI (Sunmi Inner Printer)
Muitas máquinas de cartão (incluindo modelos base da própria Moderninha e similares do mercado, como V2, P2) são, na verdade, hardwares fabricados pela SUNMI.

Como aparecem na listagem: A SUNMI implementa sua impressora térmica interna emulando um dispositivo Bluetooth virtual. Esse dispositivo já vem pré-pareado e cravado na ROM do Android, exposto frequentemente sob o nome "InnerPrinter" ou "SUNMI_PRINTER".
Diferença de Listagem e Scan: Faz extrema diferença, pois por serem Bluetooth Virtuais, elas não aparecerão se listarmos apenas portas USB e não requerem um scan ativo de redes (BLUETOOTH_SCAN). Elas sempre figuram na lista de dispositivos Bound/Paired do BluetoothAdapter nativo.
O que o atual código já faz de melhor: No Kotlin, a heurística de fallback name.contains("innerprinter") aplicada na função 
findAnyAvailablePrinter()
 é precisamente o "atalho" excelente para auto-detectar o terminal Sunmi e iniciar a instância ESC/POS sem o usuário intervir.
Dica Futura: Se os fabricantes bloquearem a porta Bluetooth virtual (como algumas custom-ROMs bancárias trazem), a alternativa final oficial da Sunmi obriga integração via SunmiPrinterService (serviço AIDL rodando em background no app), mas para 99% das variantes, usar a ponte de Bluetooth virtual poupa muito tempo.
3.4 Sugestão Rápida para Estabilidade Visual e de Memória
A Moderninha Smart 2 detém escassez de recursos RAM em relação a celulares atuais:

O fluxo de Dithering do Kotlin (o arquivo 
ImageUtils.kt
) lidando com arrays de dimensões enormes (Imagens Base64 ou de Câmera) pode ocasionar "Out Of Memory" (OOM).
A sua precaução atual implementada ao final de printImage com bitmap?.recycle() está muito bem feita. Permaneça focando nestes frees de memória nos Modulos do Kotlin para otimizar aparelhos mais fracos como os Androids 7.

Comment
Ctrl+Alt+M
