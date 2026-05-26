#!/usr/bin/env bash
# Build Unity Background Keeper.
#
# Requirements (point these at your Android SDK / JDK):
#   ANDROID_JAR  - path to an android.jar (compileSdk; API 34+ recommended)
#   BUILD_TOOLS  - path to an Android build-tools dir (containing aapt2, apksigner, d8)
#   JAVA         - JDK 11+ (javac, keytool, java)
# The real Xposed API is in libs/api-82.jar (compile-only; NOT bundled in the dex).
set -euo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"

JAVA="${JAVA:-java}"
JAVAC="${JAVAC:-javac}"
ANDROID_JAR="${ANDROID_JAR:?set ANDROID_JAR to an android.jar}"
BUILD_TOOLS="${BUILD_TOOLS:?set BUILD_TOOLS to an Android build-tools dir}"
API_JAR="$HERE/libs/api-82.jar"
KS="${KS:-$HERE/debug.keystore}"

AAPT2="$BUILD_TOOLS/aapt2"
D8="$BUILD_TOOLS/d8"
APKSIGNER="$BUILD_TOOLS/apksigner"
ZIPALIGN="$BUILD_TOOLS/zipalign"

OUT="$HERE/build"
rm -rf "$OUT"; mkdir -p "$OUT/classes" "$OUT/apk"

echo "== javac (compile against android.jar + real api-82.jar) =="
"$JAVAC" -d "$OUT/classes" -cp "$ANDROID_JAR:$API_JAR" $(find "$HERE/src" -name '*.java')

echo "== d8 (api/android are libs only, NOT bundled) =="
"$D8" --min-api 29 --lib "$ANDROID_JAR" --classpath "$API_JAR" \
    --output "$OUT/apk" $(find "$OUT/classes" -name '*.class')

echo "== aapt2 link (manifest only) =="
"$AAPT2" link -o "$OUT/apk/base.apk" -I "$ANDROID_JAR" \
    --manifest "$HERE/AndroidManifest.xml" --min-sdk-version 29 --target-sdk-version 36

echo "== package dex + xposed init files =="
cd "$OUT/apk"
mkdir -p assets META-INF/xposed
printf 'com.unitybgkeeper.MainHook\n' > assets/xposed_init
printf 'com.unitybgkeeper.MainHook\n' > META-INF/xposed/java_init.list
zip -q -X base.apk classes.dex assets/xposed_init META-INF/xposed/java_init.list
"$ZIPALIGN" -f 4 base.apk aligned.apk

echo "== sign =="
if [ ! -f "$KS" ]; then
    keytool -genkeypair -v -keystore "$KS" -alias key -keyalg RSA -keysize 2048 \
        -validity 10000 -storepass android -keypass android \
        -dname "CN=UnityBackgroundKeeper"
fi
"$APKSIGNER" sign --ks "$KS" --ks-pass pass:android --ks-key-alias key \
    --out "$OUT/UnityBackgroundKeeper.apk" aligned.apk
echo "Built: $OUT/UnityBackgroundKeeper.apk"
