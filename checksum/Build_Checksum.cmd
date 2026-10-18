@echo off
setlocal
pushd "%~dp0"
if errorlevel 1 exit /b 1
if not exist "..\RomRaider.jar" goto missing
where javac >nul 2>nul
if errorlevel 1 goto missing
where jar >nul 2>nul
if errorlevel 1 goto missing
if not exist "build" mkdir "build"
if errorlevel 1 goto failed
javac --release 11 -g:none -encoding UTF-8 -cp "..\RomRaider.jar" -d "build" "source\ChecksumMAZDAL38K.java"
if errorlevel 1 goto failed
jar -J-Djava.io.tmpdir="%CD%\build" --create --file "MazdaL38KChecksum.jar" --no-manifest -C "build" "com/romraider/maps/checksum/ChecksumMAZDAL38K.class" -C "." "LICENSE.txt"
if errorlevel 1 goto failed
echo Checksum module built successfully.
popd
exit /b 0
:missing
echo Place this bundle beside RomRaider.jar and make a JDK available on PATH.
:failed
popd
pause
exit /b 1
