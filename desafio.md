O Gemini disse
Felipe, o seu app.json está super limpo e configurado com as tecnologias mais modernas do Expo (como o reactCompiler e a newArchEnabled ativados). Excelente base!

No entanto, como prevíamos, neste momento ele é um aplicativo "cego" para o hardware. O sistema Android não vai deixar o seu código Kotlin encostar no Bluetooth ou na porta USB sem que isso esteja declarado aqui.

Aqui está o que precisamos fazer: vamos adicionar o bloco "permissions" dentro da chave "android".

1. O seu app.json atualizado
Copie o bloco abaixo e substitua no seu arquivo. Eu adicionei as permissões exatas que cobrem desde as Moderninhas mais antigas (Android 7) até os celulares mais novos (Android 12+ que exigem o BLUETOOTH_CONNECT):

JSON
{
  "expo": {
    "name": "Thermal Printer App",
    "slug": "thermal-printer-native-app",
    "version": "1.0.0",
    "orientation": "portrait",
    "icon": "./assets/images/icon.png",
    "scheme": "printapp",
    "userInterfaceStyle": "automatic",
    "newArchEnabled": true,
    "ios": {
      "supportsTablet": true
    },
    "android": {
      "adaptiveIcon": {
        "backgroundColor": "#E6F4FE",
        "foregroundImage": "./assets/images/android-icon-foreground.png",
        "backgroundImage": "./assets/images/android-icon-background.png",
        "monochromeImage": "./assets/images/android-icon-monochrome.png"
      },
      "edgeToEdgeEnabled": true,
      "predictiveBackGestureEnabled": false,
      "package": "com.fmomoreira2021.printapp",
      "permissions": [
        "android.permission.BLUETOOTH",
        "android.permission.BLUETOOTH_ADMIN",
        "android.permission.BLUETOOTH_CONNECT",
        "android.permission.BLUETOOTH_SCAN",
        "android.permission.ACCESS_COARSE_LOCATION",
        "android.permission.ACCESS_FINE_LOCATION"
      ]
    },
    "web": {
      "output": "static",
      "favicon": "./assets/images/favicon.png"
    },
    "plugins": [
      "expo-router",
      [
        "expo-splash-screen",
        {
          "image": "./assets/images/splash-icon.png",
          "imageWidth": 200,
          "resizeMode": "contain",
          "backgroundColor": "#ffffff",
          "dark": {
            "backgroundColor": "#000000"
          }
        }
      ]
    ],
    "experiments": {
      "typedRoutes": true,
      "reactCompiler": true
    },
    "extra": {
      "router": {},
      "eas": {
        "projectId": "d363f2e1-06b2-4b82-9931-a171cb7529bd"
      }
    }
  }
}
(Nota técnica: O Android exige a permissão de "Localização" (ACCESS_FINE_LOCATION) porque, teoricamente, ao escanear dispositivos Bluetooth, um app poderia deduzir a localização física do usuário baseado nos aparelhos ao redor).

2. O Toque Final para o USB (Moderninha Smart)
O app.json cuida das permissões de software, mas para o Android liberar o acesso ao cabo USB/Hardware interno, precisamos declarar uma "Feature" (Recurso).

Como você está criando um módulo local, a forma mais limpa e profissional de fazer isso é direto no manifesto do seu módulo Kotlin.

Na pasta do seu módulo, navegue até android/src/main/ e crie (ou edite se já existir) um arquivo chamado AndroidManifest.xml com este conteúdo:

XML
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
          package="expo.modules.thermalprinter">
    
    <uses-feature android:name="android.hardware.usb.host" android:required="false" />

</manifest>
O android:required="false" é importante para que a Google Play Store não bloqueie a instalação em celulares que não suportam USB Host.

