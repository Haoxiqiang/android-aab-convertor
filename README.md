## android-aab-tools

A tool for android aab file decompiler/compiler.

#### Prerequisites

Tools will use some android build-tool's utils, you should configuration your env file.

```bash
ANDROID_HOME=~/android/sdk/
sdk:
  build-tools:
    33.0.2:
      aapt2
      apksigner
      zipalign
```

[`apksigner`](https://developer.android.com/studio/command-line/apksigner)
and [`zipalign`](https://developer.android.com/studio/command-line/zipalign) installed
and available from the command line:

### Notice

It's a bundle-tools wrapper for sometimes used for convert aab <-> apk.
You should upgrade dependencies before build. AGP or AAPT2 may be upgrade the file name or types.

### Usage

```Shell
# compiler jar to build/libs/android-aab-tools-1.0.jar
./gradlew jar

# command usage
java -jar android-aab-tools-1.0.jar

# install or test, get bundletool.jar from https://github.com/google/bundletool/releases
java -jar bundletool.jar build-apks --bundle=app-release.aab --output=app-release.apks
java -jar bundletool.jar install-apks --apks=app-release.apks
```

```
Usage: aabtools [-hV] [COMMAND]
A APK/AAB File Converter.
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.
Commands:
  help     Display help information about the specified command.
  convert  A APK/AAB File Converter.
  sign     A APK/AAB File Signer.

Usage: aabtools convert [-i=<input>] [-o=<output>]
A APK/AAB File Converter.
  -i, --in=<input>     Input file
  -o, --out=<output>   Output file

Usage: aabtools sign -i=<input> -k=<keyStorePath> -kp=<keyPass>
                     -ksa=<keyStoreAlias> -ksp=<keyStorePass>
A APK/AAB File Signer.
  -i, --in=<input>          Input file
  -k, --ks=<keyStorePath>   KeyStore file
      -kp, --key-pass=<keyPass>
                            Key password
      -ksa, --ks-key-alias=<keyStoreAlias>
                            KeyStore alias name
      -ksp, --ks-pass=<keyStorePass>
                            KeyStore password

```
