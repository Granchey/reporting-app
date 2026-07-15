#!/bin/bash

# Define paths
SDK_DIR="/Users/granitkuqi/Library/Android/sdk"
EMULATOR_BIN="$SDK_DIR/emulator/emulator"
ADB_BIN="$SDK_DIR/platform-tools/adb"
AVD_NAME="Pixel_8_Pro_API_35"

# 1. Start the emulator in the background if it's not already running
if ! $ADB_BIN devices | grep -q "emulator"; then
    echo "🚀 Starting emulator: $AVD_NAME..."
    $EMULATOR_BIN -avd "$AVD_NAME" > /dev/null 2>&1 &
    
    # Wait for adb to see the device
    echo "⏳ Waiting for emulator to connect to adb..."
    $ADB_BIN wait-for-device
    
    # Wait for the system to finish booting
    echo "⏳ Waiting for Android system to boot up (this may take a moment)..."
    while [ "$($ADB_BIN shell getprop sys.boot_completed | tr -d '\r')" != "1" ]; do
        sleep 2
    done
    echo "✨ Emulator is ready!"
else
    echo "📱 Emulator is already running."
fi

# 2. Build and install the app
echo "🔨 Building and installing the app..."
./gradlew installDebug

# 3. Launch the app's main activity
echo "🏃 Launching the app..."
$ADB_BIN shell am start -n "com.vivocloud.reporting_app/com.vivocloud.reporting_app.MainActivity"

echo "🎉 Done!"
