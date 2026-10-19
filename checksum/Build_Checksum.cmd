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
javac --release 11 -g:none -encoding UTF-8 -cp "..\RomRaider.jar" -d "build" "source\ChecksumMAZDAL38K.java" "source\ChecksumMAZDAL3M7.java" "source\ChecksumMAZDAL39S.java" "source\ChecksumMAZDAL39SF.java" "source\ChecksumMAZDAL35H.java" "source\ChecksumMAZDAL33X.java" "source\ChecksumMAZDAL39SD.java"
if errorlevel 1 goto failed
jar -J-Djava.io.tmpdir="%CD%\build" --create --file "MazdaL38KChecksum.jar" --no-manifest -C "build" "com/romraider/maps/checksum/ChecksumMAZDAL38K.class" -C "." "LICENSE.txt"
if errorlevel 1 goto failed
jar -J-Djava.io.tmpdir="%CD%\build" --create --file "MazdaL3M7Checksum.jar" --no-manifest -C "build" "com/romraider/maps/checksum/ChecksumMAZDAL3M7.class" -C "." "LICENSE.txt"
if errorlevel 1 goto failed
jar -J-Djava.io.tmpdir="%CD%\build" --create --file "MazdaL39SChecksum.jar" --no-manifest -C "build" "com/romraider/maps/checksum/ChecksumMAZDAL39S.class" -C "." "LICENSE.txt"
if errorlevel 1 goto failed
jar -J-Djava.io.tmpdir="%CD%\build" --create --file "MazdaL39SFChecksum.jar" --no-manifest -C "build" "com/romraider/maps/checksum/ChecksumMAZDAL39SF.class" -C "." "LICENSE.txt"
if errorlevel 1 goto failed
jar -J-Djava.io.tmpdir="%CD%\build" --create --file "MazdaL35HChecksum.jar" --no-manifest -C "build" "com/romraider/maps/checksum/ChecksumMAZDAL35H.class" -C "." "LICENSE.txt"
if errorlevel 1 goto failed
jar -J-Djava.io.tmpdir="%CD%\build" --create --file "MazdaL33XChecksum.jar" --no-manifest -C "build" "com/romraider/maps/checksum/ChecksumMAZDAL33X.class" -C "." "LICENSE.txt"
if errorlevel 1 goto failed
jar -J-Djava.io.tmpdir="%CD%\build" --create --file "MazdaL39SDChecksum.jar" --no-manifest -C "build" "com/romraider/maps/checksum/ChecksumMAZDAL39SD.class" -C "." "LICENSE.txt"
if errorlevel 1 goto failed
echo Checksum modules built successfully.
popd
exit /b 0
:missing
echo Place this bundle beside RomRaider.jar and make a JDK available on PATH.
:failed
popd
pause
exit /b 1
