Running 'gradlew :app:assembleRelease' in /home/expo/workingdir/build/android
Downloading https://services.gradle.org/distributions/gradle-8.14.3-bin.zip
10%.
20%
30%
40%.
50%.
60
%.
70%.
80%.
90
%.
100%
Welcome to Gradle 8.14.3!
Here are the highlights of this release:
 - Java 24 support
- GraalVM Native Image toolchain selection
- Enhancements to test reporting
 - Build Authoring improvements
For more details see https://docs.gradle.org/8.14.3/release-notes.html
To honour the JVM settings for this build a single-use Daemon process will be forked. For more on this, please refer to https://docs.gradle.org/8.14.3/userguide/gradle_daemon.html#sec:disabling_the_daemon in the Gradle documentation.
Daemon will be stopped at the end of the build
> Configure project :expo-gradle-plugin:expo-autolinking-plugin
w: file:///home/expo/workingdir/build/node_modules/expo-modules-autolinking/android/expo-gradle-plugin/expo-autolinking-plugin/build.gradle.kts:25:3: 'kotlinOptions(KotlinJvmOptionsDeprecated /* = KotlinJvmOptions */.() -> Unit): Unit' is deprecated. Please migrate to the compilerOptions DSL. More details are here: https://kotl.in/u1r8ln
> Configure project :expo-gradle-plugin:expo-autolinking-settings-plugin
w: file:///home/expo/workingdir/build/node_modules/expo-modules-autolinking/android/expo-gradle-plugin/expo-autolinking-settings-plugin/build.gradle.kts:30:3: 'kotlinOptions(KotlinJvmOptionsDeprecated /* = KotlinJvmOptions */.() -> Unit): Unit' is deprecated. Please migrate to the compilerOptions DSL. More details are here: https://kotl.in/u1r8ln
> Task :expo-gradle-plugin:expo-autolinking-plugin-shared:checkKotlinGradlePluginConfigurationErrors SKIPPED
> Task :gradle-plugin:shared:checkKotlinGradlePluginConfigurationErrors SKIPPED
> Task :expo-gradle-plugin:expo-autolinking-settings-plugin:checkKotlinGradlePluginConfigurationErrors SKIPPED
> Task :gradle-plugin:settings-plugin:checkKotlinGradlePluginConfigurationErrors SKIPPED
> Task :gradle-plugin:settings-plugin:pluginDescriptors
> Task :expo-gradle-plugin:expo-autolinking-settings-plugin:pluginDescriptors
> Task :gradle-plugin:settings-plugin:processResources
> Task :expo-gradle-plugin:expo-autolinking-settings-plugin:processResources
> Task :expo-gradle-plugin:expo-autolinking-plugin-shared:processResources NO-SOURCE
> Task :gradle-plugin:shared:processResources NO-SOURCE
> Task :gradle-plugin:shared:compileKotlin
> Task :gradle-plugin:shared:compileJava NO-SOURCE
> Task :gradle-plugin:shared:classes UP-TO-DATE
> Task :gradle-plugin:shared:jar
> Task :expo-gradle-plugin:expo-autolinking-plugin-shared:compileKotlin
> Task :expo-gradle-plugin:expo-autolinking-plugin-shared:compileJava NO-SOURCE
> Task :expo-gradle-plugin:expo-autolinking-plugin-shared:classes UP-TO-DATE
> Task :expo-gradle-plugin:expo-autolinking-plugin-shared:jar
> Task :gradle-plugin:settings-plugin:compileKotlin
> Task :gradle-plugin:settings-plugin:compileJava
NO-SOURCE
> Task :gradle-plugin:settings-plugin:classes
> Task :gradle-plugin:settings-plugin:jar
> Task :expo-gradle-plugin:expo-autolinking-settings-plugin:compileKotlin
> Task :expo-gradle-plugin:expo-autolinking-settings-plugin:compileJava NO-SOURCE
> Task :expo-gradle-plugin:expo-autolinking-settings-plugin:classes
> Task :expo-gradle-plugin:expo-autolinking-settings-plugin:jar
> Configure project :expo-dev-launcher-gradle-plugin
w: file:///home/expo/workingdir/build/node_modules/expo-dev-launcher/expo-dev-launcher-gradle-plugin/build.gradle.kts:25:3: 'kotlinOptions(KotlinJvmOptionsDeprecated /* = KotlinJvmOptions */.() -> Unit): Unit' is deprecated. Please migrate to the compilerOptions DSL. More details are here: https://kotl.in/u1r8ln
> Configure project :expo-module-gradle-plugin
w: file:///home/expo/workingdir/build/node_modules/expo-modules-core/expo-module-gradle-plugin/build.gradle.kts:58:3: 'kotlinOptions(KotlinJvmOptionsDeprecated /* = KotlinJvmOptions */.() -> Unit): Unit' is deprecated. Please migrate to the compilerOptions DSL. More details are here: https://kotl.in/u1r8ln
> Task :expo-gradle-plugin:expo-autolinking-plugin:checkKotlinGradlePluginConfigurationErrors SKIPPED
> Task :gradle-plugin:react-native-gradle-plugin:checkKotlinGradlePluginConfigurationErrors SKIPPED
> Task :expo-module-gradle-plugin:checkKotlinGradlePluginConfigurationErrors SKIPPED
> Task :expo-dev-launcher-gradle-plugin:checkKotlinGradlePluginConfigurationErrors SKIPPED
> Task :expo-dev-launcher-gradle-plugin:pluginDescriptors
> Task :expo-module-gradle-plugin:pluginDescriptors
> Task :expo-dev-launcher-gradle-plugin:processResources
> Task :expo-module-gradle-plugin:processResources
> Task :expo-gradle-plugin:expo-autolinking-plugin:pluginDescriptors
> Task :expo-gradle-plugin:expo-autolinking-plugin:processResources
> Task :gradle-plugin:react-native-gradle-plugin:pluginDescriptors
> Task :gradle-plugin:react-native-gradle-plugin:processResources
> Task :expo-gradle-plugin:expo-autolinking-plugin:compileKotlin
> Task :expo-gradle-plugin:expo-autolinking-plugin:compileJava NO-SOURCE
> Task :expo-gradle-plugin:expo-autolinking-plugin:classes
> Task :expo-gradle-plugin:expo-autolinking-plugin:jar
> Task :gradle-plugin:react-native-gradle-plugin:compileKotlin
> Task :gradle-plugin:react-native-gradle-plugin:compileJava NO-SOURCE
> Task :gradle-plugin:react-native-gradle-plugin:classes
> Task :gradle-plugin:react-native-gradle-plugin:jar
> Task :expo-dev-launcher-gradle-plugin:compileKotlin
> Task :expo-dev-launcher-gradle-plugin:compileJava NO-SOURCE
> Task :expo-dev-launcher-gradle-plugin:classes
> Task :expo-dev-launcher-gradle-plugin:jar
> Task :expo-module-gradle-plugin:compileKotlin
w: file:///home/expo/workingdir/build/node_modules/expo-modules-core/expo-module-gradle-plugin/src/main/kotlin/expo/modules/plugin/android/AndroidLibraryExtension.kt:9:24 'var targetSdk: Int?' is deprecated. Will be removed from library DSL in v9.0. Use testOptions.targetSdk or/and lint.targetSdk instead.
> Task :expo-module-gradle-plugin:compileJava NO-SOURCE
> Task :expo-module-gradle-plugin:classes
> Task :expo-module-gradle-plugin:jar
> Configure project :
[ExpoRootProject] Using the following versions:
- buildTools:  36.0.0
  - minSdk:      24
  - compileSdk:  36
  - targetSdk:   36
  - ndk:         27.1.12297006
  - kotlin:      2.1.20
  - ksp:         2.1.20-2.0.1
> Configure project :app
 ℹ️  Applying gradle plugin 'expo-dev-launcher-gradle-plugin'
> Configure project :expo
Using expo modules
  - expo-constants (18.0.13)
  - expo-dev-client (6.0.20)
  - expo-dev-launcher (6.0.20)
- expo-dev-menu (7.0.18)
- expo-dev-menu-interface (2.0.0)
- expo-json-utils (0.15.0)
- expo-manifests (1.0.10)
- expo-modules-core (3.0.29)
- expo-thermal-printer (1.0.0)
- expo-updates-interface (2.0.0)
  - [📦] expo-asset (12.0.12)
  - [📦] expo-file-system (19.0.21)
  - [📦] expo-font (14.0.11)
  - [📦] expo-haptics (15.0.8)
  - [📦] expo-image (3.0.11)
  - [📦] expo-keep-awake (15.0.8)
  - [📦] expo-linking (8.0.11)
  - [📦] expo-splash-screen (31.0.13)
- [📦] expo-system-ui (6.0.9)
  - [📦] expo-web-browser (15.0.10)
Checking the license for package Android SDK Build-Tools 36 in /home/expo/Android/Sdk/licenses
License for package Android SDK Build-Tools 36 accepted.
Preparing "Install Android SDK Build-Tools 36 v.36.0.0".
"Install Android SDK Build-Tools 36 v.36.0.0" ready.
Installing Android SDK Build-Tools 36 in /home/expo/Android/Sdk/build-tools/36.0.0
"Install Android SDK Build-Tools 36 v.36.0.0" complete.
"Install Android SDK Build-Tools 36 v.36.0.0" finished.
[=======================================] 100% Fetch remote repository...       
> Task :expo-dev-client:preBuild UP-TO-DATE
> Task :expo-dev-launcher:preBuild UP-TO-DATE
> Task :expo-dev-menu:preBuild UP-TO-DATE
> Task :expo-dev-menu-interface:preBuild UP-TO-DATE
> Task :expo-json-utils:preBuild UP-TO-DATE
> Task :expo-manifests:preBuild UP-TO-DATE
> Task :expo-modules-core:preBuild UP-TO-DATE
> Task :expo-thermal-printer:preBuild UP-TO-DATE
> Task :expo-updates-interface:preBuild UP-TO-DATE
> Task :app:generateAutolinkingNewArchitectureFiles
> Task :app:generateAutolinkingPackageList
> Task :app:generateCodegenSchemaFromJavaScript SKIPPED
> Task :app:generateCodegenArtifactsFromSchema SKIPPED
> Task :app:generateReactNativeEntryPoint
> Task :expo-constants:createExpoConfig
> Task :expo-constants:preBuild
> Task :react-native-reanimated:assertMinimalReactNativeVersionTask
> Task :react-native-reanimated:assertNewArchitectureEnabledTask SKIPPED
The NODE_ENV environment variable is required but was not specified. Ensure the project is bundled with Expo CLI or NODE_ENV is set. Using only .env.local and .env
> Task :react-native-reanimated:assertWorkletsVersionTask
> Task :react-native-gesture-handler:generateCodegenSchemaFromJavaScript
> Task :react-native-safe-area-context:generateCodegenSchemaFromJavaScript
> Task :react-native-reanimated:generateCodegenSchemaFromJavaScript
> Task :expo:generatePackagesList
> Task :expo:preBuild
> Task :react-native-gesture-handler:generateCodegenArtifactsFromSchema
> Task :react-native-gesture-handler:preBuild
> Task :react-native-safe-area-context:generateCodegenArtifactsFromSchema
> Task :react-native-safe-area-context:preBuild
> Task :react-native-reanimated:generateCodegenArtifactsFromSchema
> Task :react-native-reanimated:prepareReanimatedHeadersForPrefabs
> Task :react-native-reanimated:preBuild
> Task :react-native-view-shot:generateCodegenSchemaFromJavaScript
> Task :react-native-screens:generateCodegenSchemaFromJavaScript
> Task :react-native-worklets:assertMinimalReactNativeVersionTask
> Task :react-native-worklets:assertNewArchitectureEnabledTask SKIPPED
> Task :react-native-svg:generateCodegenSchemaFromJavaScript
> Task :react-native-view-shot:generateCodegenArtifactsFromSchema
> Task :react-native-view-shot:preBuild
> Task :react-native-reanimated:preReleaseBuild
> Task :expo:preReleaseBuild
> Task :react-native-worklets:generateCodegenSchemaFromJavaScript
> Task :react-native-screens:generateCodegenArtifactsFromSchema
> Task :react-native-screens:preBuild
> Task :expo-constants:preReleaseBuild
> Task :expo:mergeReleaseJniLibFolders
> Task :expo-constants:mergeReleaseJniLibFolders
> Task :expo-constants:mergeReleaseNativeLibs NO-SOURCE
> Task :expo:mergeReleaseNativeLibs NO-SOURCE
> Task :expo-constants:copyReleaseJniLibsProjectOnly
> Task :expo:copyReleaseJniLibsProjectOnly
> Task :expo-dev-client:preReleaseBuild UP-TO-DATE
> Task :expo-dev-launcher:preReleaseBuild UP-TO-DATE
> Task :expo-dev-client:mergeReleaseJniLibFolders
> Task :expo-dev-launcher:mergeReleaseJniLibFolders
> Task :expo-dev-client:mergeReleaseNativeLibs NO-SOURCE
> Task :expo-dev-launcher:mergeReleaseNativeLibs NO-SOURCE
> Task :expo-dev-client:copyReleaseJniLibsProjectOnly
> Task :expo-dev-menu:preReleaseBuild UP-TO-DATE
> Task :expo-dev-launcher:copyReleaseJniLibsProjectOnly
> Task :expo-dev-menu-interface:preReleaseBuild UP-TO-DATE
> Task :expo-dev-menu:mergeReleaseJniLibFolders
> Task :expo-dev-menu-interface:mergeReleaseJniLibFolders
> Task :expo-dev-menu:mergeReleaseNativeLibs NO-SOURCE
> Task :expo-dev-menu-interface:mergeReleaseNativeLibs NO-SOURCE
> Task :react-native-svg:generateCodegenArtifactsFromSchema
> Task :react-native-svg:preBuild
> Task :expo-dev-menu:copyReleaseJniLibsProjectOnly
> Task :expo-json-utils:preReleaseBuild UP-TO-DATE
> Task :expo-manifests:preReleaseBuild UP-TO-DATE
> Task :expo-dev-menu-interface:copyReleaseJniLibsProjectOnly
> Task :expo-modules-core:preReleaseBuild UP-TO-DATE
> Task :expo-json-utils:mergeReleaseJniLibFolders
> Task :expo-manifests:mergeReleaseJniLibFolders
> Task :expo-json-utils:mergeReleaseNativeLibs NO-SOURCE
> Task :expo-manifests:mergeReleaseNativeLibs NO-SOURCE
> Task :expo-json-utils:copyReleaseJniLibsProjectOnly
> Task :expo-thermal-printer:preReleaseBuild UP-TO-DATE
> Task :expo-manifests:copyReleaseJniLibsProjectOnly
> Task :expo-updates-interface:preReleaseBuild UP-TO-DATE
> Task :expo-thermal-printer:mergeReleaseJniLibFolders
> Task :expo-thermal-printer:mergeReleaseNativeLibs NO-SOURCE
> Task :expo-updates-interface:mergeReleaseJniLibFolders
> Task :expo-updates-interface:mergeReleaseNativeLibs NO-SOURCE
> Task :expo-thermal-printer:copyReleaseJniLibsProjectOnly
> Task :react-native-gesture-handler:preReleaseBuild
> Task :expo-updates-interface:copyReleaseJniLibsProjectOnly
> Task :react-native-gesture-handler:mergeReleaseJniLibFolders
> Task :react-native-safe-area-context:preReleaseBuild
> Task :react-native-reanimated:mergeReleaseJniLibFolders
> Task :react-native-screens:preReleaseBuild
> Task :react-native-safe-area-context:mergeReleaseJniLibFolders
> Task :react-native-safe-area-context:mergeReleaseNativeLibs NO-SOURCE
> Task :react-native-safe-area-context:copyReleaseJniLibsProjectOnly
> Task :react-native-svg:preReleaseBuild
> Task :react-native-svg:mergeReleaseJniLibFolders
> Task :react-native-svg:mergeReleaseNativeLibs NO-SOURCE
> Task :react-native-svg:copyReleaseJniLibsProjectOnly
> Task :react-native-view-shot:preReleaseBuild
> Task :react-native-view-shot:mergeReleaseJniLibFolders
> Task :react-native-view-shot:mergeReleaseNativeLibs NO-SOURCE
> Task :react-native-view-shot:copyReleaseJniLibsProjectOnly
> Task :react-native-gesture-handler:checkKotlinGradlePluginConfigurationErrors SKIPPED
> Task :react-native-worklets:generateCodegenArtifactsFromSchema
> Task :react-native-gesture-handler:generateReleaseBuildConfig
> Task :react-native-gesture-handler:generateReleaseResValues
> Task :react-native-gesture-handler:generateReleaseResources
> Task :react-native-worklets:prepareWorkletsHeadersForPrefabs
> Task :react-native-worklets:preBuild
> Task :app:preBuild
> Task :app:preReleaseBuild
> Task :react-native-worklets:preReleaseBuild
> Task :react-native-gesture-handler:packageReleaseResources
> Task :app:mergeReleaseJniLibFolders
> Task :react-native-gesture-handler:parseReleaseLocalResources
> Task :react-native-gesture-handler:generateReleaseRFile
> Task :react-native-reanimated:generateReleaseBuildConfig
> Task :react-native-reanimated:generateReleaseResValues
> Task :react-native-reanimated:generateReleaseResources
> Task :react-native-reanimated:packageReleaseResources
> Task :react-native-reanimated:parseReleaseLocalResources
> Task :react-native-reanimated:generateReleaseRFile
> Task :react-native-reanimated:javaPreCompileRelease
> Task :react-native-svg:generateReleaseBuildConfig
> Task :react-native-svg:generateReleaseResValues
> Task :react-native-svg:generateReleaseResources
> Task :react-native-svg:packageReleaseResources
> Task :react-native-svg:parseReleaseLocalResources
> Task :react-native-svg:generateReleaseRFile
> Task :react-native-svg:javaPreCompileRelease
> Task :expo-modules-core:configureCMakeRelWithDebInfo[arm64-v8a]
Checking the license for package CMake 3.22.1 in /home/expo/Android/Sdk/licenses
License for package CMake 3.22.1 accepted.
Preparing "Install CMake 3.22.1 v.3.22.1".
"Install CMake 3.22.1 v.3.22.1" ready.
Installing CMake 3.22.1 in /home/expo/Android/Sdk/cmake/3.22.1
"Install CMake 3.22.1 v.3.22.1" complete.
"Install CMake 3.22.1 v.3.22.1" finished.
> Task :react-native-svg:compileReleaseJavaWithJavac
> Task :react-native-worklets:configureCMakeRelWithDebInfo[arm64-v8a]
Note: Some input files use or override a deprecated API.
Note: Recompile with -Xlint:deprecation for details.
Note: Some input files use unchecked or unsafe operations.
Note: Recompile with -Xlint:unchecked for details.
> Task :react-native-screens:configureCMakeRelWithDebInfo[arm64-v8a]
> Task :react-native-svg:bundleLibCompileToJarRelease
> Task :react-native-gesture-handler:javaPreCompileRelease
> Task :react-native-safe-area-context:checkKotlinGradlePluginConfigurationErrors SKIPPED
> Task :react-native-safe-area-context:generateReleaseBuildConfig
> Task :react-native-safe-area-context:generateReleaseResValues
> Task :react-native-safe-area-context:generateReleaseResources
> Task :react-native-safe-area-context:packageReleaseResources
> Task :react-native-safe-area-context:parseReleaseLocalResources
> Task :react-native-safe-area-context:generateReleaseRFile
> Task :react-native-worklets:configureCMakeRelWithDebInfo[armeabi-v7a]
> Task :react-native-worklets:configureCMakeRelWithDebInfo[x86]
> Task :react-native-worklets:configureCMakeRelWithDebInfo[x86_64]
> Task :react-native-worklets:generateJsonModelRelease
> Task :react-native-worklets:prefabReleaseConfigurePackage
> Task :react-native-reanimated:configureCMakeRelWithDebInfo[arm64-v8a]
> Task :react-native-reanimated:configureCMakeRelWithDebInfo[armeabi-v7a]
> Task :react-native-reanimated:configureCMakeRelWithDebInfo[x86]
> Task :react-native-screens:buildCMakeRelWithDebInfo[arm64-v8a]
> Task :react-native-reanimated:configureCMakeRelWithDebInfo[x86_64]
> Task :react-native-reanimated:generateJsonModelRelease
> Task :react-native-reanimated:prefabReleaseConfigurePackage
> Task :react-native-safe-area-context:compileReleaseKotlin
w: file:///home/expo/workingdir/build/node_modules/react-native-safe-area-context/android/src/main/java/com/th3rdwave/safeareacontext/SafeAreaView.kt:59:23 'val uiImplementation: UIImplementation!' is deprecated. Deprecated in Java.
> Task :react-native-gesture-handler:configureCMakeRelWithDebInfo[arm64-v8a]
> Task :app:configureCMakeRelWithDebInfo[arm64-v8a]
> Task :react-native-gesture-handler:configureCMakeRelWithDebInfo[armeabi-v7a]
> Task :app:configureCMakeRelWithDebInfo[armeabi-v7a]
> Task :react-native-gesture-handler:configureCMakeRelWithDebInfo[x86]
> Task :app:configureCMakeRelWithDebInfo[x86]
> Task :react-native-gesture-handler:configureCMakeRelWithDebInfo[x86_64]
> Task :expo-modules-core:buildCMakeRelWithDebInfo[arm64-v8a]
> Task :app:configureCMakeRelWithDebInfo[x86_64]
> Task :react-native-safe-area-context:javaPreCompileRelease
> Task :react-native-safe-area-context:compileReleaseJavaWithJavac
> Task :react-native-safe-area-context:bundleLibRuntimeToDirRelease
> Task :react-native-screens:configureCMakeRelWithDebInfo[armeabi-v7a]
> Task :expo:checkKotlinGradlePluginConfigurationErrors SKIPPED
> Task :expo:generateReleaseBuildConfig
> Task :expo:generateReleaseResValues
> Task :expo:generateReleaseResources
> Task :expo:packageReleaseResources
> Task :expo:parseReleaseLocalResources
> Task :expo:generateReleaseRFile
> Task :expo-constants:checkKotlinGradlePluginConfigurationErrors SKIPPED
> Task :expo-constants:generateReleaseBuildConfig
> Task :expo-constants:generateReleaseResValues
> Task :expo-constants:generateReleaseResources
> Task :expo-constants:packageReleaseResources
> Task :expo-constants:parseReleaseLocalResources
> Task :expo-constants:generateReleaseRFile
> Task :expo-constants:javaPreCompileRelease
> Task :expo-dev-client:checkKotlinGradlePluginConfigurationErrors SKIPPED
> Task :expo-dev-client:dataBindingMergeDependencyArtifactsRelease
> Task :expo-dev-client:generateReleaseResValues
> Task :expo-dev-client:generateReleaseResources
> Task :expo-dev-client:packageReleaseResources
> Task :expo-dev-client:parseReleaseLocalResources
> Task :expo-dev-client:dataBindingGenBaseClassesRelease
> Task :expo-dev-client:generateReleaseBuildConfig
> Task :expo-dev-client:generateReleaseRFile
> Task :expo-dev-client:javaPreCompileRelease
> Task :expo-dev-launcher:checkKotlinGradlePluginConfigurationErrors SKIPPED
> Task :react-native-worklets:buildCMakeRelWithDebInfo[arm64-v8a][worklets]
> Task :expo-modules-core:configureCMakeRelWithDebInfo[armeabi-v7a]
> Task :react-native-screens:buildCMakeRelWithDebInfo[armeabi-v7a]
> Task :react-native-screens:configureCMakeRelWithDebInfo[x86]
> Task :expo-dev-launcher:dataBindingMergeDependencyArtifactsRelease
> Task :expo-dev-launcher:generateReleaseResValues
> Task :expo-dev-launcher:generateReleaseResources
> Task :expo-dev-launcher:packageReleaseResources
> Task :expo-dev-launcher:parseReleaseLocalResources
> Task :expo-dev-menu:generateReleaseResValues
> Task :expo-dev-menu:generateReleaseResources
> Task :expo-dev-menu:packageReleaseResources
> Task :expo-dev-menu:parseReleaseLocalResources
> Task :expo-dev-menu:generateReleaseRFile
> Task :expo-dev-menu-interface:generateReleaseResValues
> Task :expo-dev-menu-interface:generateReleaseResources
> Task :expo-dev-menu-interface:packageReleaseResources
> Task :expo-dev-menu-interface:parseReleaseLocalResources
> Task :expo-dev-menu-interface:generateReleaseRFile
> Task :expo-json-utils:generateReleaseResValues
> Task :expo-json-utils:generateReleaseResources
> Task :expo-json-utils:packageReleaseResources
> Task :expo-json-utils:parseReleaseLocalResources
> Task :expo-json-utils:generateReleaseRFile
> Task :expo-manifests:generateReleaseResValues
> Task :expo-manifests:generateReleaseResources
> Task :expo-manifests:packageReleaseResources
> Task :expo-manifests:parseReleaseLocalResources
> Task :expo-manifests:generateReleaseRFile
> Task :expo-updates-interface:generateReleaseResValues
> Task :expo-updates-interface:generateReleaseResources
> Task :expo-updates-interface:packageReleaseResources
> Task :expo-updates-interface:parseReleaseLocalResources
> Task :expo-updates-interface:generateReleaseRFile
> Task :expo-dev-launcher:dataBindingGenBaseClassesRelease
> Task :expo-dev-launcher:generateReleaseBuildConfig
> Task :expo-dev-launcher:generateReleaseRFile
> Task :expo-dev-launcher:checkApolloVersions
> Task :expo-dev-launcher:generateServiceApolloOptions
> Task :expo-dev-launcher:generateServiceApolloSources
w: /home/expo/workingdir/build/node_modules/expo-dev-launcher/android/src/main/graphql/GetBranches.graphql: (21, 11): Apollo: Use of deprecated field `runtimeVersion`
w: /home/expo/workingdir/build/node_modules/expo-dev-launcher/android/src/main/graphql/GetBranches.graphql: (34, 3): Apollo: Variable `platform` is unused
w: /home/expo/workingdir/build/node_modules/expo-dev-launcher/android/src/main/graphql/GetUpdates.graphql: (14, 11): Apollo: Use of deprecated field `runtimeVersion`
> Task :expo-dev-menu:checkKotlinGradlePluginConfigurationErrors SKIPPED
> Task :expo-dev-menu:generateReleaseBuildConfig
> Task :expo-dev-menu-interface:checkKotlinGradlePluginConfigurationErrors SKIPPED
> Task :expo-dev-menu-interface:generateReleaseBuildConfig
> Task :expo-dev-menu-interface:javaPreCompileRelease
> Task :expo-json-utils:checkKotlinGradlePluginConfigurationErrors SKIPPED
> Task :expo-json-utils:generateReleaseBuildConfig
> Task :expo-json-utils:javaPreCompileRelease
> Task :expo-manifests:checkKotlinGradlePluginConfigurationErrors SKIPPED
> Task :expo-manifests:generateReleaseBuildConfig
> Task :expo-manifests:javaPreCompileRelease
> Task :expo-dev-menu:javaPreCompileRelease
> Task :expo-updates-interface:checkKotlinGradlePluginConfigurationErrors SKIPPED
> Task :expo-updates-interface:generateReleaseBuildConfig
> Task :expo-updates-interface:javaPreCompileRelease
> Task :expo-dev-launcher:javaPreCompileRelease
> Task :expo-thermal-printer:checkKotlinGradlePluginConfigurationErrors SKIPPED
> Task :expo-thermal-printer:generateReleaseBuildConfig
> Task :expo-thermal-printer:generateReleaseResValues
> Task :expo-thermal-printer:generateReleaseResources
> Task :expo-thermal-printer:packageReleaseResources
> Task :expo-thermal-printer:parseReleaseLocalResources
> Task :expo-thermal-printer:generateReleaseRFile
> Task :expo-thermal-printer:javaPreCompileRelease
> Task :expo:javaPreCompileRelease
> Task :react-native-svg:bundleLibRuntimeToDirRelease
> Task :react-native-screens:buildCMakeRelWithDebInfo[x86]
> Task :react-native-screens:configureCMakeRelWithDebInfo[x86_64]
> Task :react-native-view-shot:generateReleaseBuildConfig
> Task :react-native-view-shot:generateReleaseResValues
> Task :react-native-view-shot:generateReleaseResources
> Task :react-native-view-shot:packageReleaseResources
> Task :react-native-view-shot:parseReleaseLocalResources
> Task :react-native-view-shot:generateReleaseRFile
> Task :react-native-view-shot:javaPreCompileRelease
> Task :react-native-view-shot:compileReleaseJavaWithJavac
> Task :react-native-view-shot:bundleLibRuntimeToDirRelease
Note: Some input files use or override a deprecated API.
Note: Recompile with -Xlint:deprecation for details.
> Task :expo-modules-core:buildCMakeRelWithDebInfo[armeabi-v7a]
> Task :app:checkReleaseDuplicateClasses
> Task :app:buildKotlinToolingMetadata
> Task :app:checkKotlinGradlePluginConfigurationErrors SKIPPED
> Task :app:generateReleaseBuildConfig
> Task :expo:writeReleaseAarMetadata
> Task :expo-constants:writeReleaseAarMetadata
> Task :expo-dev-client:writeReleaseAarMetadata
> Task :expo-dev-launcher:writeReleaseAarMetadata
> Task :expo-dev-menu:writeReleaseAarMetadata
> Task :expo-dev-menu-interface:writeReleaseAarMetadata
> Task :expo-json-utils:writeReleaseAarMetadata
> Task :expo-manifests:writeReleaseAarMetadata
> Task :expo-thermal-printer:writeReleaseAarMetadata
> Task :expo-updates-interface:writeReleaseAarMetadata
> Task :react-native-gesture-handler:writeReleaseAarMetadata
> Task :react-native-reanimated:writeReleaseAarMetadata
> Task :react-native-safe-area-context:writeReleaseAarMetadata
> Task :react-native-svg:writeReleaseAarMetadata
> Task :react-native-view-shot:writeReleaseAarMetadata
> Task :react-native-screens:buildCMakeRelWithDebInfo[x86_64]
> Task :react-native-screens:mergeReleaseJniLibFolders
> Task :react-native-screens:mergeReleaseNativeLibs
> Task :react-native-screens:copyReleaseJniLibsProjectOnly
> Task :react-native-screens:checkKotlinGradlePluginConfigurationErrors SKIPPED
> Task :react-native-screens:generateReleaseBuildConfig
> Task :react-native-screens:generateReleaseResValues
> Task :react-native-screens:generateReleaseResources
> Task :react-native-screens:packageReleaseResources
> Task :react-native-screens:parseReleaseLocalResources
> Task :react-native-screens:generateReleaseRFile
> Task :app:createBundleReleaseJsAndAssets
React Compiler enabled
Starting Metro Bundler
> Task :expo-modules-core:configureCMakeRelWithDebInfo[x86]
> Task :app:createBundleReleaseJsAndAssets
Android node_modules/expo-router/entry.js ░░░░░░░░░░░░░░░░  0.0% (0/1)
Android node_modules/expo-router/entry.js ▓▓▓▓▓▓▓▓▓▓▓▓▓░░░ 86.6% ( 964/1036)
> Task :react-native-worklets:buildCMakeRelWithDebInfo[armeabi-v7a][worklets]
> Task :react-native-screens:compileReleaseKotlin
> Task :expo-modules-core:buildCMakeRelWithDebInfo[x86]
> Task :app:createBundleReleaseJsAndAssets
Android Bundled 5970ms node_modules/expo-router/entry.js (1277 modules)
Writing bundle output to: /home/expo/workingdir/build/android/app/build/generated/assets/createBundleReleaseJsAndAssets/index.android.bundle
Writing sourcemap output to: /home/expo/workingdir/build/android/app/build/intermediates/sourcemaps/react/release/index.android.bundle.packager.map
Copying 26 asset files
Done writing bundle output
Done writing sourcemap output
> Task :react-native-screens:javaPreCompileRelease
> Task :react-native-screens:writeReleaseAarMetadata
> Task :app:generateReleaseResValues
> Task :app:generateReleaseResources
> Task :app:packageReleaseResources
> Task :app:parseReleaseLocalResources
> Task :app:createReleaseCompatibleScreenManifests
> Task :app:extractDeepLinksRelease
> Task :expo:extractDeepLinksRelease
> Task :expo:processReleaseManifest
> Task :expo-constants:extractDeepLinksRelease
> Task :expo-constants:processReleaseManifest
> Task :expo-dev-client:extractDeepLinksRelease
> Task :expo-dev-client:processReleaseManifest
> Task :expo-dev-launcher:extractDeepLinksRelease
> Task :expo-dev-launcher:processReleaseManifest
> Task :expo-dev-menu:extractDeepLinksRelease
> Task :expo-dev-menu:processReleaseManifest
> Task :expo-dev-menu-interface:extractDeepLinksRelease
> Task :expo-dev-menu-interface:processReleaseManifest
> Task :expo-json-utils:extractDeepLinksRelease
> Task :expo-json-utils:processReleaseManifest
> Task :expo-manifests:extractDeepLinksRelease
> Task :expo-manifests:processReleaseManifest
> Task :expo-thermal-printer:extractDeepLinksRelease
> Task :expo-thermal-printer:processReleaseManifest
> Task :expo-updates-interface:extractDeepLinksRelease
> Task :expo-updates-interface:processReleaseManifest
> Task :react-native-gesture-handler:extractDeepLinksRelease
> Task :react-native-gesture-handler:processReleaseManifest
> Task :react-native-reanimated:extractDeepLinksRelease
> Task :react-native-reanimated:processReleaseManifest
> Task :react-native-safe-area-context:extractDeepLinksRelease
> Task :react-native-safe-area-context:processReleaseManifest
package="com.th3rdwave.safeareacontext" found in source AndroidManifest.xml: /home/expo/workingdir/build/node_modules/react-native-safe-area-context/android/src/main/AndroidManifest.xml.
Setting the namespace via the package attribute in the source AndroidManifest.xml is no longer supported, and the value is ignored.
Recommendation: remove package="com.th3rdwave.safeareacontext" from the source AndroidManifest.xml: /home/expo/workingdir/build/node_modules/react-native-safe-area-context/android/src/main/AndroidManifest.xml.
> Task :react-native-screens:extractDeepLinksRelease
> Task :react-native-screens:processReleaseManifest
> Task :react-native-svg:extractDeepLinksRelease
> Task :react-native-svg:processReleaseManifest
> Task :react-native-view-shot:extractDeepLinksRelease
> Task :react-native-view-shot:processReleaseManifest
package="fr.greweb.reactnativeviewshot" found in source AndroidManifest.xml: /home/expo/workingdir/build/node_modules/react-native-view-shot/android/src/main/AndroidManifest.xml.
Setting the namespace via the package attribute in the source AndroidManifest.xml is no longer supported, and the value is ignored.
Recommendation: remove package="fr.greweb.reactnativeviewshot" from the source AndroidManifest.xml: /home/expo/workingdir/build/node_modules/react-native-view-shot/android/src/main/AndroidManifest.xml.
> Task :expo:compileReleaseLibraryResources
> Task :expo-constants:compileReleaseLibraryResources
> Task :expo-dev-client:compileReleaseLibraryResources
> Task :expo-dev-launcher:compileReleaseLibraryResources
> Task :expo-dev-menu:compileReleaseLibraryResources
> Task :expo-dev-menu-interface:compileReleaseLibraryResources
> Task :expo-json-utils:compileReleaseLibraryResources
> Task :expo-manifests:compileReleaseLibraryResources
> Task :expo-thermal-printer:compileReleaseLibraryResources
> Task :expo-updates-interface:compileReleaseLibraryResources
> Task :react-native-gesture-handler:compileReleaseLibraryResources
> Task :react-native-reanimated:compileReleaseLibraryResources
> Task :react-native-safe-area-context:compileReleaseLibraryResources
> Task :react-native-screens:compileReleaseLibraryResources
> Task :react-native-svg:compileReleaseLibraryResources
> Task :react-native-view-shot:compileReleaseLibraryResources
> Task :react-native-safe-area-context:bundleLibCompileToJarRelease
> Task :react-native-view-shot:bundleLibCompileToJarRelease
> Task :app:javaPreCompileRelease
> Task :app:desugarReleaseFileDependencies
> Task :app:mergeReleaseStartupProfile
> Task :react-native-screens:compileReleaseKotlin
w: file:///home/expo/workingdir/build/node_modules/react-native-screens/android/src/main/java/com/swmansion/rnscreens/RNScreensPackage.kt:56:9 The corresponding parameter in the supertype 'BaseReactPackage' is named 'name'. This may cause problems when calling this function with named arguments.
w: file:///home/expo/workingdir/build/node_modules/react-native-screens/android/src/main/java/com/swmansion/rnscreens/RNScreensPackage.kt:57:9 The corresponding parameter in the supertype 'BaseReactPackage' is named 'reactContext'. This may cause problems when calling this function with named arguments.
w: file:///home/expo/workingdir/build/node_modules/react-native-screens/android/src/main/java/com/swmansion/rnscreens/RNScreensPackage.kt:70:17 'constructor(name: String, className: String, canOverrideExistingModule: Boolean, needsEagerInit: Boolean, hasConstants: Boolean, isCxxModule: Boolean, isTurboModule: Boolean): ReactModuleInfo' is deprecated. This constructor is deprecated and will be removed in the future. Use ReactModuleInfo(String, String, boolean, boolean, boolean, boolean)].
w: file:///home/expo/workingdir/build/node_modules/react-native-screens/android/src/main/java/com/swmansion/rnscreens/Screen.kt:48:77 Unchecked cast of '(CoordinatorLayout.Behavior<View!>?..CoordinatorLayout.Behavior<*>?)' to 'BottomSheetBehavior<Screen>'.
w: file:///home/expo/workingdir/build/node_modules/react-native-screens/android/src/main/java/com/swmansion/rnscreens/Screen.kt:383:36 'fun setTranslucent(screen: Screen, activity: Activity?, context: ReactContext?): Unit' is deprecated. For apps targeting SDK 35 or above this prop has no effect because edge-to-edge is enabled by default and the status bar is always translucent.
w: file:///home/expo/workingdir/build/node_modules/react-native-screens/android/src/main/java/com/swmansion/rnscreens/Screen.kt:402:36 'fun setColor(screen: Screen, activity: Activity?, context: ReactContext?): Unit' is deprecated. For apps targeting SDK 35 or above this prop has no effect because edge-to-edge is enabled by default and the status bar is always translucent.
w: file:///home/expo/workingdir/build/node_modules/react-native-screens/android/src/main/java/com/swmansion/rnscreens/Screen.kt:420:36 'fun setNavigationBarColor(screen: Screen, activity: Activity?): Unit' is deprecated. For all apps targeting Android SDK 35 or above edge-to-edge is enabled by default.
w: file:///home/expo/workingdir/build/node_modules/react-native-screens/android/src/main/java/com/swmansion/rnscreens/Screen.kt:437:36 'fun setNavigationBarTranslucent(screen: Screen, activity: Activity?): Unit' is deprecated. For all apps targeting Android SDK 35 or above edge-to-edge is enabled by default.
w: file:///home/expo/workingdir/build/node_modules/react-native-screens/android/src/main/java/com/swmansion/rnscreens/ScreenStackFragment.kt:217:31 'var targetElevation: Float' is deprecated. Deprecated in Java.
w: file:///home/expo/workingdir/build/node_modules/react-native-screens/android/src/main/java/com/swmansion/rnscreens/ScreenStackFragment.kt:220:13 'fun setHasOptionsMenu(p0: Boolean): Unit' is deprecated. Deprecated in Java.
w: file:///home/expo/workingdir/build/node_modules/react-native-screens/android/src/main/java/com/swmansion/rnscreens/ScreenStackFragment.kt:397:18 This declaration overrides a deprecated member but is not marked as deprecated itself. Add the '@Deprecated' annotation or suppress the diagnostic.
w: file:///home/expo/workingdir/build/node_modules/react-native-screens/android/src/main/java/com/swmansion/rnscreens/ScreenStackFragment.kt:404:22 'fun onPrepareOptionsMenu(p0: Menu): Unit' is deprecated. Deprecated in Java.
w: file:///home/expo/workingdir/build/node_modules/react-native-screens/android/src/main/java/com/swmansion/rnscreens/ScreenStackFragment.kt:407:18 This declaration overrides a deprecated member but is not marked as deprecated itself. Add the '@Deprecated' annotation or suppress the diagnostic.
w: file:///home/expo/workingdir/build/node_modules/react-native-screens/android/src/main/java/com/swmansion/rnscreens/ScreenStackFragment.kt:412:22 'fun onCreateOptionsMenu(p0: Menu, p1: MenuInflater): Unit' is deprecated. Deprecated in Java.
w: file:///home/expo/workingdir/build/node_modules/react-native-screens/android/src/main/java/com/swmansion/rnscreens/ScreenStackHeaderConfig.kt:435:18 'val reactNativeHost: ReactNativeHost' is deprecated. You should not use ReactNativeHost directly in the New Architecture. Use ReactHost instead.
w: file:///home/expo/workingdir/build/node_modules/react-native-screens/android/src/main/java/com/swmansion/rnscreens/ScreenViewManager.kt:203:14 'var statusBarColor: Int?' is deprecated. For apps targeting SDK 35 or above this prop has no effect because edge-to-edge is enabled by default and the status bar is always translucent.
w: file:///home/expo/workingdir/build/node_modules/react-native-screens/android/src/main/java/com/swmansion/rnscreens/ScreenViewManager.kt:220:14 'var isStatusBarTranslucent: Boolean?' is deprecated. For apps targeting SDK 35 or above this prop has no effect because edge-to-edge is enabled by default and the status bar is always translucent.
w: file:///home/expo/workingdir/build/node_modules/react-native-screens/android/src/main/java/com/swmansion/rnscreens/ScreenViewManager.kt:237:14 'var navigationBarColor: Int?' is deprecated. For all apps targeting Android SDK 35 or above edge-to-edge is enabled by default.
w: file:///home/expo/workingdir/build/node_modules/react-native-screens/android/src/main/java/com/swmansion/rnscreens/ScreenViewManager.kt:246:14 'var isNavigationBarTranslucent: Boolean?' is deprecated. For all apps targeting Android SDK 35 or above edge-to-edge is enabled by default.
w: file:///home/expo/workingdir/build/node_modules/react-native-screens/android/src/main/java/com/swmansion/rnscreens/ScreenWindowTraits.kt:55:42 'fun replaceSystemWindowInsets(p0: Int, p1: Int, p2: Int, p3: Int): WindowInsetsCompat' is deprecated. Deprecated in Java.
w: file:///home/expo/workingdir/build/node_modules/react-native-screens/android/src/main/java/com/swmansion/rnscreens/ScreenWindowTraits.kt:56:39 'val systemWindowInsetLeft: Int' is deprecated. Deprecated in Java.
w: file:///home/expo/workingdir/build/node_modules/react-native-screens/android/src/main/java/com/swmansion/rnscreens/ScreenWindowTraits.kt:58:39 'val systemWindowInsetRight: Int' is deprecated. Deprecated in Java.
w: file:///home/expo/workingdir/build/node_modules/react-native-screens/android/src/main/java/com/swmansion/rnscreens/ScreenWindowTraits.kt:59:39 'val systemWindowInsetBottom: Int' is deprecated. Deprecated in Java.
w: file:///home/expo/workingdir/build/node_modules/react-native-screens/android/src/main/java/com/swmansion/rnscreens/ScreenWindowTraits.kt:102:53 'var statusBarColor: Int' is deprecated. Deprecated in Java.
w: file:///home/expo/workingdir/build/node_modules/react-native-screens/android/src/main/java/com/swmansion/rnscreens/ScreenWindowTraits.kt:106:37 'var statusBarColor: Int?' is deprecated. For apps targeting SDK 35 or above this prop has no effect because edge-to-edge is enabled by default and the status bar is always translucent.
w: file:///home/expo/workingdir/build/node_modules/react-native-screens/android/src/main/java/com/swmansion/rnscreens/ScreenWindowTraits.kt:113:48 'var statusBarColor: Int' is deprecated. Deprecated in Java.
w: file:///home/expo/workingdir/build/node_modules/react-native-screens/android/src/main/java/com/swmansion/rnscreens/ScreenWindowTraits.kt:116:32 'var statusBarColor: Int' is deprecated. Deprecated in Java.
w: file:///home/expo/workingdir/build/node_modules/react-native-screens/android/src/main/java/com/swmansion/rnscreens/ScreenWindowTraits.kt:162:49 'var isStatusBarTranslucent: Boolean?' is deprecated. For apps targeting SDK 35 or above this prop has no effect because edge-to-edge is enabled by default and the status bar is always translucent.
w: file:///home/expo/workingdir/build/node_modules/react-native-screens/android/src/main/java/com/swmansion/rnscreens/ScreenWindowTraits.kt:218:43 'var navigationBarColor: Int?' is deprecated. For all apps targeting Android SDK 35 or above edge-to-edge is enabled by default.
w: file:///home/expo/workingdir/build/node_modules/react-native-screens/android/src/main/java/com/swmansion/rnscreens/ScreenWindowTraits.kt:218:72 'var navigationBarColor: Int' is deprecated. Deprecated in Java.
w: file:///home/expo/workingdir/build/node_modules/react-native-screens/android/src/main/java/com/swmansion/rnscreens/ScreenWindowTraits.kt:224:16 'var navigationBarColor: Int' is deprecated. Deprecated in Java.
w: file:///home/expo/workingdir/build/node_modules/react-native-screens/android/src/main/java/com/swmansion/rnscreens/ScreenWindowTraits.kt:241:55 'var isNavigationBarTranslucent: Boolean?' is deprecated. For all apps targeting Android SDK 35 or above edge-to-edge is enabled by default.
w: file:///home/expo/workingdir/build/node_modules/react-native-screens/android/src/main/java/com/swmansion/rnscreens/ScreenWindowTraits.kt:283:13 'fun setColor(screen: Screen, activity: Activity?, context: ReactContext?): Unit' is deprecated. For apps targeting SDK 35 or above this prop has no effect because edge-to-edge is enabled by default and the status bar is always translucent.
w: file:///home/expo/workingdir/build/node_modules/react-native-screens/android/src/main/java/com/swmansion/rnscreens/ScreenWindowTraits.kt:285:13 'fun setTranslucent(screen: Screen, activity: Activity?, context: ReactContext?): Unit' is deprecated. For apps targeting SDK 35 or above this prop has no effect because edge-to-edge is enabled by default and the status bar is always translucent.
w: file:///home/expo/workingdir/build/node_modules/react-native-screens/android/src/main/java/com/swmansion/rnscreens/ScreenWindowTraits.kt:289:13 'fun setNavigationBarColor(screen: Screen, activity: Activity?): Unit' is deprecated. For all apps targeting Android SDK 35 or above edge-to-edge is enabled by default.
w: file:///home/expo/workingdir/build/node_modules/react-native-screens/android/src/main/java/com/swmansion/rnscreens/ScreenWindowTraits.kt:290:13 'fun setNavigationBarTranslucent(screen: Screen, activity: Activity?): Unit' is deprecated. For all apps targeting Android SDK 35 or above edge-to-edge is enabled by default.
w: file:///home/expo/workingdir/build/node_modules/react-native-screens/android/src/main/java/com/swmansion/rnscreens/ScreenWindowTraits.kt:354:42 'var statusBarColor: Int?' is deprecated. For apps targeting SDK 35 or above this prop has no effect because edge-to-edge is enabled by default and the status bar is always translucent.
w: file:///home/expo/workingdir/build/node_modules/react-native-screens/android/src/main/java/com/swmansion/rnscreens/ScreenWindowTraits.kt:356:48 'var isStatusBarTranslucent: Boolean?' is deprecated. For apps targeting SDK 35 or above this prop has no effect because edge-to-edge is enabled by default and the status bar is always translucent.
w: file:///home/expo/workingdir/build/node_modules/react-native-screens/android/src/main/java/com/swmansion/rnscreens/ScreenWindowTraits.kt:359:57 'var navigationBarColor: Int?' is deprecated. For all apps targeting Android SDK 35 or above edge-to-edge is enabled by default.
w: file:///home/expo/workingdir/build/node_modules/react-native-screens/android/src/main/java/com/swmansion/rnscreens/ScreenWindowTraits.kt:360:63 'var isNavigationBarTranslucent: Boolean?' is deprecated. For all apps targeting Android SDK 35 or above edge-to-edge is enabled by default.
w: file:///home/expo/workingdir/build/node_modules/react-native-screens/android/src/main/java/com/swmansion/rnscreens/bottomsheet/BottomSheetDialogRootView.kt:7:8 'object ReactFeatureFlags : Any' is deprecated. Use com.facebook.react.internal.featureflags.ReactNativeFeatureFlags instead.
w: file:///home/expo/workingdir/build/node_modules/react-native-screens/android/src/main/java/com/swmansion/rnscreens/bottomsheet/BottomSheetDialogRootView.kt:25:13 'object ReactFeatureFlags : Any' is deprecated. Use com.facebook.react.internal.featureflags.ReactNativeFeatureFlags instead.
w: file:///home/expo/workingdir/build/node_modules/react-native-screens/android/src/main/java/com/swmansion/rnscreens/bottomsheet/BottomSheetDialogRootView.kt:32:9 The corresponding parameter in the supertype 'ReactViewGroup' is named 'left'. This may cause problems when calling this function with named arguments.
w: file:///home/expo/workingdir/build/node_modules/react-native-screens/android/src/main/java/com/swmansion/rnscreens/bottomsheet/BottomSheetDialogRootView.kt:33:9 The corresponding parameter in the supertype 'ReactViewGroup' is named 'top'. This may cause problems when calling this function with named arguments.
w: file:///home/expo/workingdir/build/node_modules/react-native-screens/android/src/main/java/com/swmansion/rnscreens/bottomsheet/BottomSheetDialogRootView.kt:34:9 The corresponding parameter in the supertype 'ReactViewGroup' is named 'right'. This may cause problems when calling this function with named arguments.
w: file:///home/expo/workingdir/build/node_modules/react-native-screens/android/src/main/java/com/swmansion/rnscreens/bottomsheet/BottomSheetDialogRootView.kt:35:9 The corresponding parameter in the supertype 'ReactViewGroup' is named 'bottom'. This may cause problems when calling this function with named arguments.
w: file:///home/expo/workingdir/build/node_modules/react-native-screens/android/src/main/java/com/swmansion/rnscreens/bottomsheet/BottomSheetDialogRootView.kt:71:9 The corresponding parameter in the supertype 'RootView' is named 'childView'. This may cause problems when calling this function with named arguments.
w: file:///home/expo/workingdir/build/node_modules/react-native-screens/android/src/main/java/com/swmansion/rnscreens/bottomsheet/BottomSheetDialogRootView.kt:72:9 The corresponding parameter in the supertype 'RootView' is named 'ev'. This may cause problems when calling this function with named arguments.
w: file:///home/expo/workingdir/build/node_modules/react-native-screens/android/src/main/java/com/swmansion/rnscreens/bottomsheet/BottomSheetDialogRootView.kt:79:46 The corresponding parameter in the supertype 'RootView' is named 'ev'. This may cause problems when calling this function with named arguments.
w: file:///home/expo/workingdir/build/node_modules/react-native-screens/android/src/main/java/com/swmansion/rnscreens/bottomsheet/BottomSheetDialogRootView.kt:83:9 The corresponding parameter in the supertype 'RootView' is named 'childView'. This may cause problems when calling this function with named arguments.
w: file:///home/expo/workingdir/build/node_modules/react-native-screens/android/src/main/java/com/swmansion/rnscreens/bottomsheet/BottomSheetDialogRootView.kt:84:9 The corresponding parameter in the supertype 'RootView' is named 'ev'. This may cause problems when calling this function with named arguments.
w: file:///home/expo/workingdir/build/node_modules/react-native-screens/android/src/main/java/com/swmansion/rnscreens/bottomsheet/BottomSheetDialogRootView.kt:95:34 The corresponding parameter in the supertype 'RootView' is named 't'. This may cause problems when calling this function with named arguments.
w: file:///home/expo/workingdir/build/node_modules/react-native-screens/android/src/main/java/com/swmansion/rnscreens/bottomsheet/DimmingView.kt:63:9 The corresponding parameter in the supertype 'ReactCompoundView' is named 'touchX'. This may cause problems when calling this function with named arguments.
w: file:///home/expo/workingdir/build/node_modules/react-native-screens/android/src/main/java/com/swmansion/rnscreens/bottomsheet/DimmingView.kt:64:9 The corresponding parameter in the supertype 'ReactCompoundView' is named 'touchY'. This may cause problems when calling this function with named arguments.
w: file:///home/expo/workingdir/build/node_modules/react-native-screens/android/src/main/java/com/swmansion/rnscreens/bottomsheet/DimmingView.kt:68:9 The corresponding parameter in the supertype 'ReactCompoundViewGroup' is named 'touchX'. This may cause problems when calling this function with named arguments.
w: file:///home/expo/workingdir/build/node_modules/react-native-screens/android/src/main/java/com/swmansion/rnscreens/bottomsheet/DimmingView.kt:69:9 The corresponding parameter in the supertype 'ReactCompoundViewGroup' is named 'touchY'. This may cause problems when calling this function with named arguments.
w: file:///home/expo/workingdir/build/node_modules/react-native-screens/android/src/main/java/com/swmansion/rnscreens/gamma/tabs/TabsHostViewManager.kt:37:9 The corresponding parameter in the supertype 'TabsHostViewManager' is named 'view'. This may cause problems when calling this function with named arguments.
> Task :expo-modules-core:configureCMakeRelWithDebInfo[x86_64]
> Task :react-native-worklets:buildCMakeRelWithDebInfo[x86][worklets]
> Task :app:mergeExtDexRelease
> Task :expo-modules-core:buildCMakeRelWithDebInfo[x86_64]
> Task :react-native-worklets:buildCMakeRelWithDebInfo[x86_64][worklets]
> Task :expo-modules-core:mergeReleaseJniLibFolders
> Task :react-native-worklets:externalNativeBuildRelease
> Task :react-native-worklets:prefabReleasePackage
> Task :react-native-reanimated:buildCMakeRelWithDebInfo[arm64-v8a][reanimated]
> Task :react-native-worklets:mergeReleaseJniLibFolders
> Task :react-native-worklets:mergeReleaseNativeLibs
> Task :react-native-worklets:copyReleaseJniLibsProjectOnly
> Task :react-native-worklets:generateReleaseBuildConfig
> Task :react-native-worklets:generateReleaseResValues
> Task :react-native-worklets:generateReleaseResources
> Task :react-native-worklets:packageReleaseResources
> Task :react-native-worklets:parseReleaseLocalResources
> Task :expo-modules-core:mergeReleaseNativeLibs
> Task :react-native-worklets:generateReleaseRFile
> Task :react-native-worklets:javaPreCompileRelease
> Task :expo-modules-core:copyReleaseJniLibsProjectOnly
> Task :react-native-worklets:compileReleaseJavaWithJavac
> Task :react-native-worklets:bundleLibCompileToJarRelease
> Task :expo-modules-core:checkKotlinGradlePluginConfigurationErrors SKIPPED
> Task :expo-modules-core:generateReleaseBuildConfig
> Task :expo-modules-core:generateReleaseResValues
> Task :expo-modules-core:generateReleaseResources
Note: /home/expo/workingdir/build/node_modules/react-native-worklets/android/src/main/java/com/swmansion/worklets/WorkletsPackage.java uses unchecked or unsafe operations.
Note: Recompile with -Xlint:unchecked for details.
> Task :expo-modules-core:packageReleaseResources
> Task :expo-modules-core:parseReleaseLocalResources
> Task :expo-modules-core:generateReleaseRFile
> Task :react-native-screens:compileReleaseJavaWithJavac
> Task :react-native-screens:bundleLibRuntimeToDirRelease
> Task :expo-modules-core:javaPreCompileRelease
> Task :react-native-worklets:bundleLibRuntimeToDirRelease
> Task :expo-modules-core:writeReleaseAarMetadata
> Task :react-native-worklets:writeReleaseAarMetadata
> Task :app:checkReleaseAarMetadata
> Task :app:mapReleaseSourceSetPaths
> Task :expo-modules-core:extractDeepLinksRelease
> Task :expo-modules-core:processReleaseManifest
/home/expo/workingdir/build/node_modules/expo-modules-core/android/src/main/AndroidManifest.xml:8:9-11:45 Warning:
	meta-data#com.facebook.soloader.enabled@android:value was tagged at AndroidManifest.xml:8 to replace other declarations but no other declaration present
> Task :react-native-worklets:extractDeepLinksRelease
> Task :app:mergeReleaseResources
> Task :react-native-worklets:processReleaseManifest
> Task :expo-modules-core:compileReleaseLibraryResources
> Task :react-native-worklets:compileReleaseLibraryResources
> Task :react-native-screens:bundleLibCompileToJarRelease
> Task :expo:prepareReleaseArtProfile
> Task :expo-constants:prepareReleaseArtProfile
> Task :expo-dev-client:prepareReleaseArtProfile
> Task :expo-dev-launcher:prepareReleaseArtProfile
> Task :expo-dev-menu:prepareReleaseArtProfile
> Task :expo-dev-menu-interface:prepareReleaseArtProfile
> Task :expo-json-utils:prepareReleaseArtProfile
> Task :expo-manifests:prepareReleaseArtProfile
> Task :expo-modules-core:prepareReleaseArtProfile
> Task :expo-thermal-printer:prepareReleaseArtProfile
> Task :expo-updates-interface:prepareReleaseArtProfile
> Task :react-native-gesture-handler:prepareReleaseArtProfile
> Task :react-native-safe-area-context:prepareReleaseArtProfile
> Task :react-native-screens:prepareReleaseArtProfile
> Task :react-native-svg:prepareReleaseArtProfile
> Task :react-native-view-shot:prepareReleaseArtProfile
> Task :react-native-worklets:prepareReleaseArtProfile
> Task :react-native-safe-area-context:bundleLibRuntimeToJarRelease
> Task :react-native-screens:bundleLibRuntimeToJarRelease
> Task :app:processReleaseMainManifest
/home/expo/workingdir/build/android/app/src/main/AndroidManifest.xml Warning:
	provider#expo.modules.filesystem.FileSystemFileProvider@android:authorities was tagged at AndroidManifest.xml:0 to replace other declarations but no other declaration present
> Task :app:processReleaseManifest
> Task :app:processReleaseManifestForPackage
> Task :react-native-svg:bundleLibRuntimeToJarRelease
> Task :react-native-view-shot:bundleLibRuntimeToJarRelease
> Task :react-native-worklets:bundleLibRuntimeToJarRelease
> Task :expo-modules-core:compileReleaseKotlin
> Task :app:mergeReleaseShaders
> Task :app:compileReleaseShaders NO-SOURCE
> Task :app:generateReleaseAssets UP-TO-DATE
> Task :expo:mergeReleaseShaders
> Task :expo:compileReleaseShaders
NO-SOURCE
> Task :expo:generateReleaseAssets UP-TO-DATE
> Task :expo:mergeReleaseAssets
> Task :expo-constants:mergeReleaseShaders
> Task :expo-constants:compileReleaseShaders NO-SOURCE
> Task :expo-constants:generateReleaseAssets UP-TO-DATE
> Task :expo-constants:mergeReleaseAssets
> Task :expo-dev-client:mergeReleaseShaders
> Task :expo-dev-client:compileReleaseShaders NO-SOURCE
> Task :expo-dev-client:generateReleaseAssets UP-TO-DATE
> Task :expo-dev-client:mergeReleaseAssets
> Task :expo-dev-launcher:mergeReleaseShaders
> Task :expo-dev-launcher:compileReleaseShaders NO-SOURCE
> Task :expo-dev-launcher:generateReleaseAssets UP-TO-DATE
> Task :expo-dev-launcher:mergeReleaseAssets
> Task :expo-dev-menu:mergeReleaseShaders
> Task :expo-dev-menu:compileReleaseShaders NO-SOURCE
> Task :expo-dev-menu:generateReleaseAssets UP-TO-DATE
> Task :expo-dev-menu:mergeReleaseAssets
> Task :expo-dev-menu-interface:mergeReleaseShaders
> Task :expo-dev-menu-interface:compileReleaseShaders NO-SOURCE
> Task :expo-dev-menu-interface:generateReleaseAssets UP-TO-DATE
> Task :expo-dev-menu-interface:mergeReleaseAssets
> Task :expo-json-utils:mergeReleaseShaders
> Task :expo-json-utils:compileReleaseShaders NO-SOURCE
> Task :expo-json-utils:generateReleaseAssets UP-TO-DATE
> Task :expo-json-utils:mergeReleaseAssets
> Task :expo-manifests:mergeReleaseShaders
> Task :expo-manifests:compileReleaseShaders NO-SOURCE
> Task :expo-manifests:generateReleaseAssets UP-TO-DATE
> Task :expo-manifests:mergeReleaseAssets
> Task :expo-modules-core:mergeReleaseShaders
> Task :expo-modules-core:compileReleaseShaders NO-SOURCE
> Task :expo-modules-core:generateReleaseAssets UP-TO-DATE
> Task :expo-modules-core:mergeReleaseAssets
> Task :expo-thermal-printer:mergeReleaseShaders
> Task :expo-thermal-printer:compileReleaseShaders NO-SOURCE
> Task :expo-thermal-printer:generateReleaseAssets UP-TO-DATE
> Task :expo-thermal-printer:mergeReleaseAssets
> Task :expo-updates-interface:mergeReleaseShaders
> Task :expo-updates-interface:compileReleaseShaders NO-SOURCE
> Task :expo-updates-interface:generateReleaseAssets UP-TO-DATE
> Task :expo-updates-interface:mergeReleaseAssets
> Task :react-native-gesture-handler:mergeReleaseShaders
> Task :react-native-gesture-handler:compileReleaseShaders NO-SOURCE
> Task :react-native-gesture-handler:generateReleaseAssets UP-TO-DATE
> Task :react-native-gesture-handler:mergeReleaseAssets
> Task :react-native-safe-area-context:mergeReleaseShaders
> Task :react-native-safe-area-context:compileReleaseShaders NO-SOURCE
> Task :react-native-safe-area-context:generateReleaseAssets UP-TO-DATE
> Task :react-native-safe-area-context:mergeReleaseAssets
> Task :react-native-screens:mergeReleaseShaders
> Task :react-native-screens:compileReleaseShaders NO-SOURCE
> Task :react-native-screens:generateReleaseAssets UP-TO-DATE
> Task :react-native-screens:mergeReleaseAssets
> Task :react-native-svg:mergeReleaseShaders
> Task :react-native-svg:compileReleaseShaders NO-SOURCE
> Task :react-native-svg:generateReleaseAssets UP-TO-DATE
> Task :react-native-svg:mergeReleaseAssets
> Task :react-native-view-shot:mergeReleaseShaders
> Task :react-native-view-shot:compileReleaseShaders NO-SOURCE
> Task :react-native-view-shot:generateReleaseAssets UP-TO-DATE
> Task :react-native-view-shot:mergeReleaseAssets
> Task :react-native-worklets:mergeReleaseShaders
> Task :react-native-worklets:compileReleaseShaders NO-SOURCE
> Task :react-native-worklets:generateReleaseAssets UP-TO-DATE
> Task :react-native-worklets:mergeReleaseAssets
> Task :app:extractReleaseVersionControlInfo
> Task :app:extractProguardFiles
> Task :expo:extractProguardFiles
> Task :expo-constants:extractProguardFiles
> Task :expo-modules-core:extractProguardFiles
> Task :expo-modules-core:prepareLintJarForPublish
> Task :expo-constants:prepareLintJarForPublish
> Task :expo-dev-client:extractProguardFiles
> Task :expo-dev-client:prepareLintJarForPublish
> Task :expo-dev-launcher:extractProguardFiles
> Task :expo-dev-menu:extractProguardFiles
> Task :expo-dev-menu-interface:extractProguardFiles
> Task :expo-dev-menu-interface:prepareLintJarForPublish
> Task :expo-json-utils:extractProguardFiles
> Task :expo-json-utils:prepareLintJarForPublish
> Task :expo-manifests:extractProguardFiles
> Task :expo-manifests:prepareLintJarForPublish
> Task :expo-dev-menu:prepareLintJarForPublish
> Task :expo-updates-interface:extractProguardFiles
> Task :expo-updates-interface:prepareLintJarForPublish
> Task :expo-dev-launcher:prepareLintJarForPublish
> Task :expo-thermal-printer:extractProguardFiles
> Task :expo-thermal-printer:prepareLintJarForPublish
> Task :expo:prepareLintJarForPublish
> Task :react-native-gesture-handler:extractProguardFiles
> Task :react-native-worklets:processReleaseJavaRes NO-SOURCE
> Task :react-native-worklets:createFullJarRelease
> Task :react-native-worklets:extractProguardFiles
> Task :app:processReleaseResources
> Task :react-native-svg:processReleaseJavaRes NO-SOURCE
> Task :react-native-svg:createFullJarRelease
> Task :react-native-svg:extractProguardFiles
> Task :react-native-svg:generateReleaseLintModel
> Task :react-native-svg:prepareLintJarForPublish
> Task :react-native-gesture-handler:prepareLintJarForPublish
> Task :react-native-safe-area-context:processReleaseJavaRes
> Task :react-native-safe-area-context:createFullJarRelease
> Task :react-native-safe-area-context:extractProguardFiles
> Task :react-native-safe-area-context:generateReleaseLintModel
> Task :react-native-safe-area-context:prepareLintJarForPublish
> Task :react-native-screens:processReleaseJavaRes
> Task :react-native-screens:createFullJarRelease
> Task :react-native-screens:extractProguardFiles
> Task :react-native-screens:generateReleaseLintModel
> Task :react-native-screens:prepareLintJarForPublish
> Task :react-native-view-shot:processReleaseJavaRes NO-SOURCE
> Task :react-native-view-shot:createFullJarRelease
> Task :react-native-view-shot:extractProguardFiles
> Task :react-native-view-shot:generateReleaseLintModel
> Task :react-native-view-shot:prepareLintJarForPublish
> Task :react-native-gesture-handler:extractDeepLinksForAarRelease
> Task :react-native-safe-area-context:stripReleaseDebugSymbols NO-SOURCE
> Task :react-native-safe-area-context:copyReleaseJniLibsProjectAndLocalJars
> Task :react-native-safe-area-context:extractDeepLinksForAarRelease
> Task :react-native-worklets:generateReleaseLintModel
> Task :react-native-worklets:prepareLintJarForPublish
> Task :react-native-screens:stripReleaseDebugSymbols
> Task :react-native-screens:copyReleaseJniLibsProjectAndLocalJars
> Task :react-native-screens:extractDeepLinksForAarRelease
> Task :expo-modules-core:compileReleaseKotlin
w: file:///home/expo/workingdir/build/node_modules/expo-modules-core/android/src/main/java/expo/modules/adapters/react/apploader/RNHeadlessAppLoader.kt:48:87 'val reactNativeHost: ReactNativeHost' is deprecated. You should not use ReactNativeHost directly in the New Architecture. Use ReactHost instead.
w: file:///home/expo/workingdir/build/node_modules/expo-modules-core/android/src/main/java/expo/modules/adapters/react/apploader/RNHeadlessAppLoader.kt:91:85 'val reactNativeHost: ReactNativeHost' is deprecated. You should not use ReactNativeHost directly in the New Architecture. Use ReactHost instead.
w: file:///home/expo/workingdir/build/node_modules/expo-modules-core/android/src/main/java/expo/modules/adapters/react/apploader/RNHeadlessAppLoader.kt:120:83 'val reactNativeHost: ReactNativeHost' is deprecated. You should not use ReactNativeHost directly in the New Architecture. Use ReactHost instead.
w: file:///home/expo/workingdir/build/node_modules/expo-modules-core/android/src/main/java/expo/modules/apploader/AppLoaderProvider.kt:34:52 Unchecked cast of 'Class<*>!' to 'Class<out HeadlessAppLoader>'.
w: file:///home/expo/workingdir/build/node_modules/expo-modules-core/android/src/main/java/expo/modules/kotlin/AppContext.kt:30:8 'typealias ErrorManagerModule = JSLoggerModule' is deprecated. Use JSLoggerModule instead.
w: file:///home/expo/workingdir/build/node_modules/expo-modules-core/android/src/main/java/expo/modules/kotlin/AppContext.kt:253:21 'typealias ErrorManagerModule = JSLoggerModule' is deprecated. Use JSLoggerModule instead.
w: file:///home/expo/workingdir/build/node_modules/expo-modules-core/android/src/main/java/expo/modules/kotlin/AppContext.kt:343:21 'val DEFAULT: Int' is deprecated. UIManagerType.DEFAULT will be deleted in the next release of React Native. Use [LEGACY] instead.
w: file:///home/expo/workingdir/build/node_modules/expo-modules-core/android/src/main/java/expo/modules/kotlin/defaultmodules/NativeModulesProxyModule.kt:16:5 'fun Constants(legacyConstantsProvider: () -> Map<String, Any?>): Unit' is deprecated. Use `Constant` or `Property` instead.
w: file:///home/expo/workingdir/build/node_modules/expo-modules-core/android/src/main/java/expo/modules/kotlin/jni/PromiseImpl.kt:65:51 'val errorManager: JSLoggerModule?' is deprecated. Use AppContext.jsLogger instead.
w: file:///home/expo/workingdir/build/node_modules/expo-modules-core/android/src/main/java/expo/modules/kotlin/jni/PromiseImpl.kt:69:22 'fun reportExceptionToLogBox(codedException: CodedException): Unit' is deprecated. Use appContext.jsLogger.error(...) instead.
w: file:///home/expo/workingdir/build/node_modules/expo-modules-core/android/src/main/java/expo/modules/kotlin/views/ViewDefinitionBuilder.kt:464:16 'val errorManager: JSLoggerModule?' is deprecated. Use AppContext.jsLogger instead.
w: file:///home/expo/workingdir/build/node_modules/expo-modules-core/android/src/main/java/expo/modules/kotlin/views/ViewDefinitionBuilder.kt:464:30 'fun reportExceptionToLogBox(codedException: CodedException): Unit' is deprecated. Use appContext.jsLogger.error(...) instead.
w: file:///home/expo/workingdir/build/node_modules/expo-modules-core/android/src/main/java/expo/modules/kotlin/views/ViewManagerDefinition.kt:41:16 'val errorManager: JSLoggerModule?' is deprecated. Use AppContext.jsLogger instead.
w: file:///home/expo/workingdir/build/node_modules/expo-modules-core/android/src/main/java/expo/modules/kotlin/views/ViewManagerDefinition.kt:41:30 'fun reportExceptionToLogBox(codedException: CodedException): Unit' is deprecated. Use appContext.jsLogger.error(...) instead.
> Task :react-native-safe-area-context:extractReleaseAnnotations
> Task :react-native-screens:extractReleaseAnnotations
> Task :react-native-screens:mergeReleaseGeneratedProguardFiles
> Task :react-native-safe-area-context:mergeReleaseGeneratedProguardFiles
> Task :react-native-screens:mergeReleaseConsumerProguardFiles
> Task :react-native-safe-area-context:mergeReleaseConsumerProguardFiles
> Task :react-native-screens:mergeReleaseJavaResource
> Task :react-native-safe-area-context:mergeReleaseJavaResource
> Task :react-native-safe-area-context:syncReleaseLibJars
> Task :react-native-safe-area-context:bundleReleaseLocalLintAar
> Task :expo:stripReleaseDebugSymbols NO-SOURCE
> Task :expo:copyReleaseJniLibsProjectAndLocalJars
> Task :expo:extractDeepLinksForAarRelease
> Task :react-native-svg:stripReleaseDebugSymbols NO-SOURCE
> Task :react-native-svg:copyReleaseJniLibsProjectAndLocalJars
> Task :react-native-svg:extractDeepLinksForAarRelease
> Task :react-native-svg:extractReleaseAnnotations
> Task :react-native-screens:syncReleaseLibJars
> Task :react-native-svg:mergeReleaseGeneratedProguardFiles
> Task :react-native-svg:mergeReleaseConsumerProguardFiles
> Task :react-native-screens:bundleReleaseLocalLintAar
> Task :react-native-svg:mergeReleaseJavaResource
> Task :react-native-view-shot:stripReleaseDebugSymbols NO-SOURCE
> Task :react-native-view-shot:copyReleaseJniLibsProjectAndLocalJars
> Task :react-native-view-shot:extractDeepLinksForAarRelease
> Task :react-native-svg:syncReleaseLibJars
> Task :react-native-svg:bundleReleaseLocalLintAar
> Task :react-native-view-shot:mergeReleaseGeneratedProguardFiles
> Task :react-native-view-shot:mergeReleaseConsumerProguardFiles
> Task :react-native-view-shot:mergeReleaseJavaResource
> Task :react-native-worklets:stripReleaseDebugSymbols
> Task :react-native-worklets:copyReleaseJniLibsProjectAndLocalJars
> Task :react-native-worklets:extractDeepLinksForAarRelease
> Task :react-native-worklets:extractReleaseAnnotations
> Task :react-native-worklets:mergeReleaseGeneratedProguardFiles
> Task :react-native-worklets:mergeReleaseConsumerProguardFiles
> Task :react-native-worklets:mergeReleaseJavaResource
> Task :react-native-worklets:syncReleaseLibJars
> Task :expo-modules-core:compileReleaseJavaWithJavac
Note: Some input files use or override a deprecated API.
Note: Recompile with -Xlint:deprecation for details.
> Task :react-native-worklets:bundleReleaseLocalLintAar
> Task :expo-dev-launcher:stripReleaseDebugSymbols NO-SOURCE
> Task :expo-dev-launcher:copyReleaseJniLibsProjectAndLocalJars
> Task :expo-dev-launcher:extractDeepLinksForAarRelease
> Task :expo-dev-menu:stripReleaseDebugSymbols NO-SOURCE
> Task :expo-dev-menu:copyReleaseJniLibsProjectAndLocalJars
> Task :expo-dev-menu:extractDeepLinksForAarRelease
> Task :expo-dev-menu-interface:stripReleaseDebugSymbols NO-SOURCE
> Task :expo-dev-menu-interface:copyReleaseJniLibsProjectAndLocalJars
> Task :expo-dev-menu-interface:extractDeepLinksForAarRelease
> Task :expo-thermal-printer:stripReleaseDebugSymbols NO-SOURCE
> Task :expo-thermal-printer:copyReleaseJniLibsProjectAndLocalJars
> Task :expo-thermal-printer:extractDeepLinksForAarRelease
> Task :expo-constants:stripReleaseDebugSymbols NO-SOURCE
> Task :expo-constants:copyReleaseJniLibsProjectAndLocalJars
> Task :expo-constants:extractDeepLinksForAarRelease
> Task :expo-dev-client:stripReleaseDebugSymbols NO-SOURCE
> Task :expo-dev-client:copyReleaseJniLibsProjectAndLocalJars
> Task :expo-dev-client:extractDeepLinksForAarRelease
> Task :expo-manifests:stripReleaseDebugSymbols NO-SOURCE
> Task :expo-manifests:copyReleaseJniLibsProjectAndLocalJars
> Task :expo-manifests:extractDeepLinksForAarRelease
> Task :expo-json-utils:stripReleaseDebugSymbols NO-SOURCE
> Task :expo-json-utils:copyReleaseJniLibsProjectAndLocalJars
> Task :expo-json-utils:extractDeepLinksForAarRelease
> Task :expo-updates-interface:stripReleaseDebugSymbols NO-SOURCE
> Task :expo-updates-interface:copyReleaseJniLibsProjectAndLocalJars
> Task :expo-updates-interface:extractDeepLinksForAarRelease
> Task :expo-constants:writeReleaseLintModelMetadata
> Task :expo-dev-client:writeReleaseLintModelMetadata
> Task :expo-dev-launcher:writeReleaseLintModelMetadata
> Task :expo-dev-menu:writeReleaseLintModelMetadata
> Task :expo-dev-menu-interface:writeReleaseLintModelMetadata
> Task :expo-json-utils:writeReleaseLintModelMetadata
> Task :expo-manifests:writeReleaseLintModelMetadata
> Task :expo-thermal-printer:writeReleaseLintModelMetadata
> Task :expo-updates-interface:writeReleaseLintModelMetadata
> Task :expo:writeReleaseLintModelMetadata
> Task :react-native-svg:writeReleaseLintModelMetadata
> Task :react-native-worklets:writeReleaseLintModelMetadata
> Task :react-native-gesture-handler:writeReleaseLintModelMetadata
> Task :expo-modules-core:bundleLibCompileToJarRelease
> Task :expo-constants:compileReleaseKotlin
w: file:///home/expo/workingdir/build/node_modules/expo-constants/android/src/main/java/expo/modules/constants/ConstantsModule.kt:12:5 'fun Constants(legacyConstantsProvider: () -> Map<String, Any?>): Unit' is deprecated. Use `Constant` or `Property` instead.
> Task :expo-constants:compileReleaseJavaWithJavac
> Task :expo-constants:bundleLibCompileToJarRelease
> Task :expo-dev-client:compileReleaseKotlin NO-SOURCE
> Task :expo-dev-client:compileReleaseJavaWithJavac
> Task :expo-dev-client:bundleLibCompileToJarRelease
> Task :expo-dev-menu-interface:compileReleaseKotlin
w: file:///home/expo/workingdir/build/node_modules/expo-dev-menu-interface/android/src/main/java/expo/interfaces/devmenu/DevMenuInterfacePackage.kt:14:16 This declaration overrides a deprecated member but is not marked as deprecated itself. Add the '@Deprecated' annotation or suppress the diagnostic.
w: file:///home/expo/workingdir/build/node_modules/expo-dev-menu-interface/android/src/main/java/expo/interfaces/devmenu/ReactHostWrapper.kt:5:8 'class ReactNativeHost : Any' is deprecated. Deprecated in Java.
w: file:///home/expo/workingdir/build/node_modules/expo-dev-menu-interface/android/src/main/java/expo/interfaces/devmenu/ReactHostWrapper.kt:19:41 'class ReactNativeHost : Any' is deprecated. Deprecated in Java.
w: file:///home/expo/workingdir/build/node_modules/expo-dev-menu-interface/android/src/main/java/expo/interfaces/devmenu/ReactHostWrapper.kt:20:33 'class ReactNativeHost : Any' is deprecated. Deprecated in Java.
> Task :expo-dev-menu-interface:compileReleaseJavaWithJavac
> Task :expo-dev-menu-interface:bundleLibCompileToJarRelease
> Task :react-native-view-shot:extractReleaseAnnotations
Warning: fr.greweb.reactnativeviewshot.ViewShot.Formats: The typedef annotation should have @Retention(RetentionPolicy.SOURCE)
Warning: fr.greweb.reactnativeviewshot.ViewShot.Results: The typedef annotation should have @Retention(RetentionPolicy.SOURCE)
> Task :expo-json-utils:compileReleaseKotlin
> Task :expo-json-utils:compileReleaseJavaWithJavac
> Task :expo-json-utils:bundleLibCompileToJarRelease
> Task :expo-updates-interface:compileReleaseKotlin
> Task :expo-updates-interface:compileReleaseJavaWithJavac
> Task :expo-updates-interface:bundleLibCompileToJarRelease
> Task :expo-manifests:compileReleaseKotlin
w: file:///home/expo/workingdir/build/node_modules/expo-manifests/android/src/main/java/expo/modules/manifests/core/EmbeddedManifest.kt:19:16 This declaration overrides a deprecated member but is not marked as deprecated itself. Add the '@Deprecated' annotation or suppress the diagnostic.
w: file:///home/expo/workingdir/build/node_modules/expo-manifests/android/src/main/java/expo/modules/manifests/core/EmbeddedManifest.kt:19:86 'fun getLegacyID(): String' is deprecated. Prefer scopeKey or projectId depending on use case.
w: file:///home/expo/workingdir/build/node_modules/expo-manifests/android/src/main/java/expo/modules/manifests/core/ExpoUpdatesManifest.kt:16:16 This declaration overrides a deprecated member but is not marked as deprecated itself. Add the '@Deprecated' annotation or suppress the diagnostic.
w: file:///home/expo/workingdir/build/node_modules/expo-manifests/android/src/main/java/expo/modules/manifests/core/Manifest.kt:13:3 Deprecations and opt-ins on a method overridden from 'Any' may not be reported.
w: file:///home/expo/workingdir/build/node_modules/expo-manifests/android/src/main/java/expo/modules/manifests/core/Manifest.kt:15:12 'fun getRawJson(): JSONObject' is deprecated. Prefer to use specific field getters.
> Task :expo-manifests:compileReleaseJavaWithJavac
> Task :expo-manifests:bundleLibCompileToJarRelease
> Task :expo-thermal-printer:compileReleaseKotlin
e: file:///home/expo/workingdir/build/modules/expo-thermal-printer/android/src/main/java/expo/modules/thermalprinter/LotteryTicketPrinter.kt:64:33 Argument type mismatch: actual type is 'ByteArray', but 'Int' was expected.
e: file:///home/expo/workingdir/build/modules/expo-thermal-printer/android/src/main/java/expo/modules/thermalprinter/LotteryTicketPrinter.kt:69:29 Argument type mismatch: actual type is 'ByteArray', but 'Int' was expected.
> Task :expo-thermal-printer:compileReleaseKotlin FAILED
> Task :expo-dev-menu:compileReleaseKotlin
w: file:///home/expo/workingdir/build/node_modules/expo-dev-menu/android/src/main/java/com/facebook/react/devsupport/DevMenuSettingsBase.kt:6:8 'class PreferenceManager : Any' is deprecated. Deprecated in Java.
w: file:///home/expo/workingdir/build/node_modules/expo-dev-menu/android/src/main/java/com/facebook/react/devsupport/DevMenuSettingsBase.kt:18:51 'class PreferenceManager : Any' is deprecated. Deprecated in Java.
w: file:///home/expo/workingdir/build/node_modules/expo-dev-menu/android/src/main/java/com/facebook/react/devsupport/DevMenuSettingsBase.kt:18:69 'static fun getDefaultSharedPreferences(p0: Context!): SharedPreferences!' is deprecated. Deprecated in Java.
w: file:///home/expo/workingdir/build/node_modules/expo-dev-menu/android/src/main/java/com/facebook/react/devsupport/DevMenuSettingsBase.kt:51:13 This code uses error suppression for 'NOTHING_TO_OVERRIDE'. While it might compile and work, the compiler behavior is UNSPECIFIED and WILL NOT BE PRESERVED. Please report your use case to the Kotlin issue tracker instead: https://kotl.in/issue
w: file:///home/expo/workingdir/build/node_modules/expo-dev-menu/android/src/main/java/com/facebook/react/devsupport/DevMenuSettingsBase.kt:58:13 This code uses error suppression for 'NOTHING_TO_OVERRIDE'. While it might compile and work, the compiler behavior is UNSPECIFIED and WILL NOT BE PRESERVED. Please report your use case to the Kotlin issue tracker instead: https://kotl.in/issue
w: file:///home/expo/workingdir/build/node_modules/expo-dev-menu/android/src/main/java/expo/modules/devmenu/DevMenuPackage.kt:28:16 This declaration overrides a deprecated member but is not marked as deprecated itself. Add the '@Deprecated' annotation or suppress the diagnostic.
w: file:///home/expo/workingdir/build/node_modules/expo-dev-menu/android/src/main/java/expo/modules/devmenu/DevMenuPackage.kt:47:78 'val reactNativeHost: ReactNativeHost' is deprecated. You should not use ReactNativeHost directly in the New Architecture. Use ReactHost instead.
w: file:///home/expo/workingdir/build/node_modules/expo-dev-menu/android/src/release/java/expo/modules/devmenu/DevMenuManager.kt:80:43 The corresponding parameter in the supertype 'DevMenuManagerInterface' is named 'shouldAutoLaunch'. This may cause problems when calling this function with named arguments.
> Task :react-native-safe-area-context:lintVitalAnalyzeRelease
[Incubating] Problems report is available at: file:///home/expo/workingdir/build/android/build/reports/problems/problems-report.html
FAILURE: Build failed with an exception.
* What went wrong:
Execution failed for task ':expo-thermal-printer:compileReleaseKotlin'.
> A failure occurred while executing org.jetbrains.kotlin.compilerRunner.GradleCompilerRunnerWithWorkers$GradleKotlinCompilerWorkAction
   > Compilation error. See log for more details
* Try:
> Run with --stacktrace option to get the stack trace.
> Run with --info or --debug option to get more log output.
> Run with --scan to get full insights.
> Get more help at https://help.gradle.org.
BUILD FAILED in 7m 56s
Deprecated Gradle features were used in this build, making it incompatible with Gradle 9.0.
You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.
For more on this, please refer to https://docs.gradle.org/8.14.3/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.
576 actionable tasks: 576 executed
Error: Gradle build failed with unknown error. See logs for the "Run gradlew" phase for more information.