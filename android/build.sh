#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
: "${ANDROID_JAR:?Set ANDROID_JAR to platform android.jar}"
: "${ANDROID_BUILD_TOOLS:?Set ANDROID_BUILD_TOOLS to SDK build-tools directory}"
: "${MALTESER_KEYSTORE:?Set MALTESER_KEYSTORE to private signing keystore}"
: "${MALTESER_STORE_PASSWORD:?Set MALTESER_STORE_PASSWORD}"
python3 prepare_assets.py
mkdir -p build/classes build/dex
"$ANDROID_BUILD_TOOLS/aapt2" compile --dir app/src/main/res -o build/resources.zip
"$ANDROID_BUILD_TOOLS/aapt2" link -o build/resources.apk -I "$ANDROID_JAR" --manifest app/src/main/AndroidManifest.xml -A app/src/main/assets build/resources.zip
javac -encoding UTF-8 -source 8 -target 8 -classpath "$ANDROID_JAR" -d build/classes $(find app/src/main/java -name '*.java')
"$ANDROID_BUILD_TOOLS/d8" --lib "$ANDROID_JAR" --min-api 26 --output build/dex $(find build/classes -name '*.class')
cp build/resources.apk build/unsigned.apk
(cd build/dex && zip -q ../unsigned.apk classes.dex)
"$ANDROID_BUILD_TOOLS/zipalign" -f 4 build/unsigned.apk build/aligned.apk
"$ANDROID_BUILD_TOOLS/apksigner" sign --ks "$MALTESER_KEYSTORE" --ks-key-alias malteser --ks-pass env:MALTESER_STORE_PASSWORD --out build/Malteser-Dienstzettel-1.0.0.apk build/aligned.apk
"$ANDROID_BUILD_TOOLS/apksigner" verify --verbose build/Malteser-Dienstzettel-1.0.0.apk
