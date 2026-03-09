O Gemini disse
O seu AndroidManifest.xml está no caminho certo, mas existem alguns detalhes cruciais para que ele funcione perfeitamente tanto na declaração quanto na execução (solicitação dinâmica) entre as versões do Android.

Aqui está uma análise do que está bom e o que precisa ser ajustado:

1. Localização e Bluetooth (O divisor de águas: Android 12)
A sua lógica de separar por API está correta, mas para encontrar impressoras via Bluetooth no Android 12+ (API 31), você precisa garantir que o sistema entenda que você não quer rastrear a localização do usuário, apenas achar o hardware.

O ajuste no Manifest:
No Android 12+, a permissão BLUETOOTH_ADVERTISE também costuma ser necessária se a impressora precisar "ver" o seu celular para emparelhar.

XML
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" 
                 android:usesPermissionFlags="neverForLocation" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.BLUETOOTH_ADVERTISE" />
Atenção: Ao usar neverForLocation, você está prometendo ao Google que seu app não usará dados de Bluetooth para derivar a localização física. Se o seu app precisar da localização por outros motivos, você deve remover esse flag e pedir a permissão de localização normalmente.

2. Permissões de Armazenamento (Android 10, 11 e 13+)
Você incluiu READ_EXTERNAL_STORAGE e WRITE_EXTERNAL_STORAGE.

Android 10 e 11: O Google introduziu o "Scoped Storage". Você pode precisar adicionar android:requestLegacyExternalStorage="true" na tag <application> se estiver lidando com arquivos de forma antiga.

Android 13+ (API 33): Essas permissões de armazenamento genéricas pararam de funcionar para mídia. Se você for imprimir fotos ou PDFs salvos, precisará de READ_MEDIA_IMAGES ou READ_MEDIA_VISUAL_USER_SELECTED.

3. O Manifest Corrigido e Otimizado
Aqui está a versão ajustada para garantir a compatibilidade com impressoras térmicas/Bluetooth:

XML
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.BLUETOOTH" android:maxSdkVersion="30" />
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" android:maxSdkVersion="30" />

    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" android:maxSdkVersion="30" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" android:maxSdkVersion="30" />

    <uses-permission android:name="android.permission.BLUETOOTH_SCAN" 
                     android:usesPermissionFlags="neverForLocation" />
    <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
    <uses-permission android:name="android.permission.BLUETOOTH_ADVERTISE" />

    <uses-permission android:name="android.permission.INTERNET" />
    
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" android:maxSdkVersion="32" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" android:maxSdkVersion="29" />

    <application 
        ...
        android:requestLegacyExternalStorage="true">
        ...
    </application>
</manifest>
4. A Parte Dinâmica (Código)
O Manifest apenas declara que o app pode usar as permissões. A parte "dinâmica" acontece no seu código (Java/Kotlin ou React Native/Expo).

Para que o comportamento seja o que você descreveu, seu código deve fazer isto:

Se SDK_INT < 31 (Android 7 a 11): * Peça ACCESS_FINE_LOCATION. Sem isso, o startDiscovery() do Bluetooth retornará vazio.

Se SDK_INT >= 31 (Android 12, 13, 14+):

Não peça localização.

Peça BLUETOOTH_SCAN e BLUETOOTH_CONNECT.

Exemplo de lógica (Pseudo-código):
JavaScript
if (Platform.Version >= 31) {
  // Pede BLUETOOTH_SCAN e CONNECT
} else {
  // Pede ACCESS_FINE_LOCATION
}
Resumo das mudanças para Impressoras:
Bluetooth Connect: No Android 12+, se você não pedir BLUETOOTH_CONNECT, o app crasha ao tentar parear ou enviar dados para a impressora.

Queries: Sua seção <queries> está boa para links HTTPS, mas se você for usar algum app de terceiros para gerenciar a impressão (como o Print Service do fabricante), talvez precise adicionar o pacote dele lá.

Você está usando React Native/Expo (notei pelas meta-datas)? Se sim, você está usando o expo-dev-client para rodar esse Manifest customizado?

Disse
nao e mais facil pedir localizacao sempre e se for android 12 ou mais pedir alem da localizacao mais permisoes ? nao facilita a logica ?

O Gemini disse
Entendo perfeitamente o seu raciocínio. Do ponto de vista de "código", parece muito mais simples pedir tudo de uma vez. No entanto, para o Android 12 (API 31) em diante, o Google mudou as regras justamente para evitar que os apps fiquem pedindo a localização do usuário sem necessidade real.

Embora pareça facilitar a lógica, pedir a localização no Android 12+ para usar Bluetooth traz três problemas principais:

1. A Experiência do Usuário (UX) e Privacidade
Se você pedir a localização no Android 12+, o sistema exibirá aquela caixa de diálogo perguntando se o usuário permite a localização "Precisa" ou "Aproximada". Muitos usuários negam permissão de localização por medo de serem monitorados.
Ao usar as novas permissões (BLUETOOTH_SCAN com neverForLocation), o Android avisa ao usuário que o app quer apenas "encontrar dispositivos próximos", o que passa muito mais confiança.

2. O GPS precisa estar ligado
Se você basear sua lógica na Localização (estilo antigo):

O usuário é obrigado a manter o GPS (serviço de localização) do celular ligado nas configurações rápidas. Se o GPS estiver desligado, o scan do Bluetooth não retorna nada.

Se você usar a lógica do Android 12+ (estilo novo):

O Bluetooth funciona para achar a impressora mesmo se o GPS do celular estiver desligado, desde que o Bluetooth esteja ativo. Isso reduz drasticamente o suporte técnico com clientes dizendo que "o app não acha a impressora".

3. Rejeição na Play Store
O Google está cada vez mais rigoroso. Se o seu app pede ACCESS_FINE_LOCATION mas a única funcionalidade dele é imprimir, eles podem questionar por que você precisa rastrear o usuário. Usar as permissões específicas de Bluetooth mostra que seu app segue as "boas práticas" de privacidade.

Como ficaria a lógica "Facilitada" (O meio termo)
Para não complicar o seu código, você pode criar uma função simples de "Check de Permissões". Veja como a lógica fica limpa:

No Manifest
Mantenha as permissões com os filtros de versão que te passei anteriormente. O Android vai ignorar o que não for para a versão dele.

No Código (Exemplo em lógica simples)
Você não precisa de 50 ifs. Você só precisa de dois blocos:

JavaScript
async function pedirPermissoesDeImpressao() {
  if (Platform.Version <= 30) {
    // Lógica para Android 11, 10, 9, 8, 7
    // Aqui a localização é OBRIGATÓRIA para o Bluetooth funcionar
    await solicitar(['ACCESS_FINE_LOCATION', 'BLUETOOTH', 'BLUETOOTH_ADMIN']);
  } else {
    // Lógica para Android 12, 13, 14+
    // Aqui a localização NÃO é necessária. Pedimos apenas o essencial.
    await solicitar(['BLUETOOTH_SCAN', 'BLUETOOTH_CONNECT']);
  }
}
Resumo: Por que não pedir localização sempre?
No Android 12+, se você pedir localização e o usuário aceitar, o Bluetooth funciona. Mas, se o usuário estiver com o GPS (botão de localização do sistema) desligado, a busca da impressora vai falhar e o seu app vai parecer quebrado. Com as permissões novas, ele funciona independente do GPS.

Dica Extra: Como você mencionou que está em São José do Belmonte e provavelmente desenvolvendo sistemas que podem ser usados em campo ou em prefeituras, economizar bateria e evitar que o usuário tenha que ativar o GPS é um grande diferencial de usabilidade.

Gostaria que eu te ajudasse a montar essa função de verificação de permissões específica para a tecnologia que você está usando (React Native, Java ou Flutter)?

Disse
eu nao tenho esse problema pois sera para meus vendedores e nao ira par aplay store mas entendo a necessidde de ter o gps ligado isso e um problema pois pode comer a bateria dos vendedores

O Gemini disse
Entendido! Como o app é para uso interno dos seus vendedores (distribuição via APK ou MDM), você tem mais liberdade, mas o ponto da bateria e da praticidade é o que realmente ganha o jogo aqui.

Se você forçar o uso da localização (GPS) em todos os aparelhos, os vendedores terão que carregar o celular com mais frequência e, se alguém esquecer o GPS desligado, vai te ligar dizendo que "a impressora estragou".

Para facilitar sua vida e proteger a bateria deles, o ideal é que o seu código se comporte de duas formas, dependendo apenas de uma "chave" (a versão do Android).

A Lógica Ideal (Focada em Bateria e Estabilidade)
Você pode centralizar tudo em uma única função. Assim, o vendedor no Android 9 terá o GPS ligado (porque é o único jeito), mas o vendedor com um celular novo (Android 12+) terá o GPS desligado e economizará bateria.

JavaScript
// Exemplo de lógica para simplificar sua vida
async function prepararConexaoImpressora() {
  if (Platform.OS === 'android') {
    const apiLevel = Platform.Version;

    if (apiLevel >= 31) {
      // ANDROID 12 OU SUPERIOR
      // Não precisa de GPS! Economiza bateria.
      return await pedirPermissoes(['BLUETOOTH_SCAN', 'BLUETOOTH_CONNECT']);
    } else {
      // ANDROID 7 ATÉ 11
      // Infelizmente o Android exige GPS para ver o Bluetooth aqui
      return await pedirPermissoes(['ACCESS_FINE_LOCATION']);
    }
  }
}
Por que isso é melhor para seus vendedores?
Economia de Energia: No Android 12+, o hardware de rádio do GPS ficará em repouso. O impacto na bateria ao longo de um dia de vendas (8h às 18h) é notável.

Menos Suporte Técnico: Você não precisará ensinar o vendedor a ativar o ícone do "alfinete" (GPS) na barra de tarefas se ele tiver um celular moderno.

Conectividade Direta: Ao usar BLUETOOTH_CONNECT, a comunicação com a impressora térmica é muito mais estável nas versões novas do Android.

O que manter no seu Manifest:
Mantenha aquela estrutura que conversamos, pois ela é "inteligente". O Android lê o arquivo e diz: "Opa, eu sou Android 13, então vou ignorar essa linha de localização que tem maxSdkVersion=30 e vou focar nas permissões de Bluetooth novas".

XML
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" android:maxSdkVersion="30"/>

<uses-permission android:name="android.permission.BLUETOOTH_SCAN" android:usesPermissionFlags="neverForLocation" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
Dica de "quem conhece o trecho":
Como seus vendedores estão na rua, outra coisa que consome muita bateria é o app ficar escaneando o Bluetooth o tempo todo. Garanta que seu código dê um stopScan() assim que encontrar a impressora ou após uns 10 segundos de busca.