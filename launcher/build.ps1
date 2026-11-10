$ErrorActionPreference = "Stop"
$launcherDir = "C:\Users\Arjid\.gemini\antigravity\scratch\nothing-tv\launcher"
Set-Location $launcherDir

$SDK = "C:\Users\Arjid\android-sdk"
$BT = "$SDK\build-tools\34.0.0"
$JAR = "$SDK\platforms\android-34\android.jar"
$B = "$launcherDir\build"

Write-Host "=== Building Nothing OS TV Launcher ===" -ForegroundColor Cyan

if (Test-Path $B) { Remove-Item $B -Recurse -Force }
New-Item -ItemType Directory -Path "$B\gen" -Force | Out-Null
New-Item -ItemType Directory -Path "$B\classes" -Force | Out-Null
New-Item -ItemType Directory -Path "$B\dex" -Force | Out-Null

Write-Host "[1/6] Compiling resources with aapt2..." -ForegroundColor Yellow
& "$BT\aapt2.exe" compile --dir res -o "$B\res.zip"

Write-Host "[2/6] Linking APK with aapt2..." -ForegroundColor Yellow
& "$BT\aapt2.exe" link -o "$B\base.apk" -I $JAR --manifest AndroidManifest.xml --java "$B\gen" -A assets --min-sdk-version 30 --target-sdk-version 30 "$B\res.zip"

Write-Host "[3/6] Compiling Java sources with javac..." -ForegroundColor Yellow
$javaFiles = Get-ChildItem -Path "src", "$B\gen" -Filter "*.java" -Recurse | Select-Object -ExpandProperty FullName
& javac -encoding UTF-8 -nowarn -Xlint:-options --release 11 -classpath $JAR -d "$B\classes" $javaFiles

Write-Host "[4/6] Dexing classes with d8..." -ForegroundColor Yellow
$classFiles = Get-ChildItem -Path "$B\classes" -Filter "*.class" -Recurse | Select-Object -ExpandProperty FullName
$classesArg = ($classFiles | ForEach-Object { "`"$_`"" }) -join ' '
cmd.exe /c "`"$BT\d8.bat`" --min-api 30 --lib `"$JAR`" --output `"$B\dex`" $classesArg"

Write-Host "[5/6] Packaging classes.dex into APK..." -ForegroundColor Yellow
Copy-Item "$B\base.apk" "$B\unsigned.apk" -Force
Set-Location "$B\dex"
& jar -uf "$B\unsigned.apk" classes.dex
Set-Location $launcherDir

Write-Host "[6/6] Zipalign & Sign APK..." -ForegroundColor Yellow
& "$BT\zipalign.exe" -f 4 "$B\unsigned.apk" "$B\aligned.apk"

if (-not (Test-Path "$launcherDir\debug.keystore")) {
    & keytool -genkeypair -keystore "$launcherDir\debug.keystore" -storepass android -keypass android -alias tvhome -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=tvhome"
}

cmd.exe /c "`"$BT\apksigner.bat`" sign --ks `"$launcherDir\debug.keystore`" --ks-pass pass:android --out `"$B\tvhome.apk`" `"$B\aligned.apk`""

$apk = Get-Item "$B\tvhome.apk"
Write-Host "SUCCESS! Built $($apk.FullName) ($($apk.Length) bytes)" -ForegroundColor Green
