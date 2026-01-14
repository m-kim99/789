@echo off
echo Building...
call gradlew.bat assembleDebug

echo Installing...
adb install -r app\build\outputs\apk\debug\app-debug.apk

echo Launching...
adb shell am start -n com.kyad.traystorage/.app.splash.SplashActivity

echo Logs:
adb logcat -c
adb logcat | findstr "TrayStorage OCR ChatBot"
