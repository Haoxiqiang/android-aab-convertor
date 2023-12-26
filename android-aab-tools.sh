#!/bin/bash

# ./android-aab-tools.sh convert a.apk/a.aab b.aab/b.apk
command=$1
input=$2
output=$3
java -jar build/libs/android-aab-tools-1.0.jar "$command" -i "$input" -o "$output"
