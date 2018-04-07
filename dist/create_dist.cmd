@echo off

setlocal

set SCRIPT_DIR=%~dp0
set JDKVERSION=9

echo ------ CREATING CUSTOM JRE, USING JDK %JDKVERSION%
call "%SCRIPT_DIR%\jlink.cmd" %JDKVERSION%

echo ------ DELETING DIRECTORY "%SCRIPT_DIR%\..\out\dist"
rd /s "%SCRIPT_DIR%\..\out\dist"

echo ------ CREATING DIRECTORIES
mkdir "%SCRIPT_DIR%\..\out"
mkdir "%SCRIPT_DIR%\..\out\dist"
mkdir "%SCRIPT_DIR%\..\out\dist\withjre"
mkdir "%SCRIPT_DIR%\..\out\dist\withjre\app"
mkdir "%SCRIPT_DIR%\..\out\dist\withjre\runtime"
mkdir "%SCRIPT_DIR%\..\out\dist\tqrespec"
mkdir "%SCRIPT_DIR%\..\out\dist\tqrespec\app"
mkdir "%SCRIPT_DIR%\..\out\dist\jar"

echo COPYING FILES
xcopy /y /s /i "%SCRIPT_DIR%\..\out\jre-image\*" "%SCRIPT_DIR%\..\out\dist\withjre\runtime\"
echo deleting legal files
rd /q /s "%SCRIPT_DIR%\..\out\dist\withjre\runtime\legal"
xcopy /y /i "%SCRIPT_DIR%\..\out\artifacts\tqrespec\tqrespec\app\*" "%SCRIPT_DIR%\..\out\dist\withjre\app\"
xcopy /y /i "%SCRIPT_DIR%\..\out\artifacts\tqrespec\tqrespec\*" "%SCRIPT_DIR%\..\out\dist\withjre\"
xcopy /y /i "%SCRIPT_DIR%\..\out\artifacts\tqrespec\tqrespec\*" "%SCRIPT_DIR%\..\out\dist\tqrespec\"
xcopy /y /i "%SCRIPT_DIR%\..\out\artifacts\tqrespec\tqrespec\app\*" "%SCRIPT_DIR%\..\out\dist\tqrespec\app\"
xcopy /y /i "%SCRIPT_DIR%\..\out\artifacts\tqrespec\*" "%SCRIPT_DIR%\..\out\dist\jar\"
del /q /f "%SCRIPT_DIR%\..\out\dist\jar\tqrespec.html"
del /q /f "%SCRIPT_DIR%\..\out\dist\jar\tqrespec.jnlp"

echo CREATING APPONLY .CFG
type "%SCRIPT_DIR%\..\out\artifacts\tqrespec\tqrespec\app\tqrespec.cfg" | findstr /v app.runtime= > "%SCRIPT_DIR%\..\out\dist\tqrespec\app\tqrespec.cfg"

echo CREATING ZIP tqrespec_withjre.zip
powershell.exe Compress-Archive -Path "%SCRIPT_DIR%\..\out\dist\withjre\*" -DestinationPath "%SCRIPT_DIR%\..\out\dist\tqrespec_withjre.zip -force 
echo CREATING ZIP tqrespec.zip
powershell.exe Compress-Archive -Path "%SCRIPT_DIR%\..\out\dist\tqrespec\*" -DestinationPath "%SCRIPT_DIR%\..\out\dist\tqrespec.zip -force 
echo CREATING ZIP tqrespec_jar.zip
powershell.exe Compress-Archive -Path "%SCRIPT_DIR%\..\out\dist\jar\*" -DestinationPath "%SCRIPT_DIR%\..\out\dist\tqrespec_jar.zip -force 
