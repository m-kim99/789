@echo off

echo 에뮬레이터 시작...
start "" "C:\Android\emulator\emulator.exe" -avd test_phone

echo 30초 대기...
timeout /t 30 /nobreak

cd /d "C:\Users\hp01\Downloads\TrayStorage_250724\TrayStorage_250724"

echo 빌드...
call gradlew.bat assembleDebug

echo 설치...
adb install -r app\build\outputs\apk\debug\app-debug.apk

echo 권한 부여 (선언된 권한만)...
adb shell pm grant com.kyad.traystorage android.permission.CAMERA 2>nul
adb shell pm grant com.kyad.traystorage android.permission.READ_EXTERNAL_STORAGE 2>nul
adb shell pm grant com.kyad.traystorage android.permission.WRITE_EXTERNAL_STORAGE 2>nul

echo 실행!
adb shell am start -n com.kyad.traystorage/.app.splash.SplashActivity

timeout /t 3 /nobreak
echo 권한 팝업 자동 승인...
adb shell input tap 360 780

pause
