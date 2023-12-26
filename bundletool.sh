#!/bin/bash

# ./bundletool.sh build-apks app-release.aab app-release.apks
# ./bundletool.sh install-apks app-release.apks
# java -jar bundletool.jar build-apks --bundle=app-release.aab --output=app-release.apks
# java -jar bundletool.jar install-apks --apks=app-release.apks
command=$1
input=$2
output=$3
if [ "build-apks" = "$command" ]; then
    echo "build-apks"
    java -jar bundletool-all-1.15.6.jar build-apks --bundle="$input" --output="$output"
else
    echo "install-apks"
    java -jar bundletool-all-1.15.6.jar install-apks --apks=$$input
fi;