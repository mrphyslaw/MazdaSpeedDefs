@echo off
setlocal
title RomRaider - Mazda
set "RR_HOME=%~dp0"
set "MAZDA_JAVA="
if not exist "%RR_HOME%RomRaider.jar" goto missing_rr
if not exist "%RR_HOME%checksum\MazdaL38KChecksum.jar" goto missing_checksum
if not exist "%RR_HOME%checksum\MazdaL3M7Checksum.jar" goto missing_checksum
if exist "%RR_HOME%jre\bin\java.exe" set "MAZDA_JAVA=%RR_HOME%jre\bin\java.exe"
if defined MAZDA_JAVA goto launch
if exist "%RR_HOME%runtime\bin\java.exe" set "MAZDA_JAVA=%RR_HOME%runtime\bin\java.exe"
if defined MAZDA_JAVA goto launch
for /d %%J in ("%RR_HOME%jdk*" "%RR_HOME%jre*") do if exist "%%~fJ\bin\java.exe" set "MAZDA_JAVA=%%~fJ\bin\java.exe"
if defined MAZDA_JAVA goto launch
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "MAZDA_JAVA=%JAVA_HOME%\bin\java.exe"
if defined MAZDA_JAVA goto launch
for %%J in (java.exe) do set "MAZDA_JAVA=%%~$PATH:J"
if not defined MAZDA_JAVA goto missing_java
:launch
pushd "%RR_HOME%"
if errorlevel 1 goto failed
"%MAZDA_JAVA%" -Djava.library.path="%RR_HOME%lib\windows\64" -Dawt.useSystemAAFontSettings=lcd -Dswing.aatext=true -Xms64M -Xmx1024M -cp "%RR_HOME%checksum\MazdaL38KChecksum.jar;%RR_HOME%checksum\MazdaL3M7Checksum.jar;%RR_HOME%RomRaider.jar;%RR_HOME%lib\common\*;%RR_HOME%lib\windows\*;%RR_HOME%i18n;%RR_HOME%lib;%RR_HOME%." com.romraider.ECUExec
set "MAZDA_EXIT=%ERRORLEVEL%"
popd
if not "%MAZDA_EXIT%"=="0" goto failed
exit /b 0
:missing_rr
echo Extract this bundle into the RomRaider folder beside RomRaider.jar.
goto stop
:missing_checksum
echo A checksum module is missing. Restore the checksum folder from this bundle.
goto stop
:missing_java
echo Java was not found. Use the Java runtime supplied with RomRaider or set JAVA_HOME.
goto stop
:failed
echo RomRaider could not start or closed with an error. Review the message above.
:stop
pause
exit /b 1
