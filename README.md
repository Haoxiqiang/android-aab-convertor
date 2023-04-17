## android-aab-tools

A tool for android aab file decompiler/compiler.

#### Prerequisites

Make sure to have a recent version of
[`apktool`](https://ibotpeaches.github.io/Apktool/), Now you should use 2.7.0 or more latest version.

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

```Shell
$ apktool
apktool d test.apk
apktool b test
...
```

```Shell
$ apksigner
Usage:  apksigner <command> [options]
        apksigner --version
        apksigner --help
...
```

```Shell
$ zipalign
Zip alignment utility
Copyright (C) 2009 The Android Open Source Project
...
```

### Usage

```Shell
# compiler jar
./gradlew jar 

# should use absolute path
java -jar build/libs/android-aab-tools-1.0-SNAPSHOT.jar -i=./app-release.apk -o=./app-release.aab

```
