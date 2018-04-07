@echo off

SETLOCAL

set SCRIPT_DIR=%~dp0

set BASEKEY=HKLM\SOFTWARE\JavaSoft\JDK
set BASEKEYLEGACY="HKLM\SOFTWARE\JavaSoft\Java Development Kit"

if "%~1" == "" (
    set SELECTED_VERSION=newer
) else (
    set SELECTED_VERSION=%~1
)

if "%SELECTED_VERSION%" == "newer" (
    :: get the java version (newer)
    reg query "%BASEKEY%" /v CurrentVersion >nul || (
        echo JRE not installed 
        exit /b 1
    )

    for /f "tokens=2,*" %%a in ('reg query %BASEKEY% /v CurrentVersion ^| findstr CurrentVersion') do (
        set JDKKEY="HKLM\SOFTWARE\JavaSoft\JDK\%%b"
    )
) else (
    :: get the registry key for specified jdk major version, jdk <= 1.8
    for /f "tokens=*" %%a in ('reg query %BASEKEYLEGACY% /k /f * ^| findstr /r "HKEY_LOCAL_MACHINE.*\\%SELECTED_VERSION%\..*"') do (
        set JDKKEY="%%a"
    )
    for /f "tokens=*" %%a in ('reg query %BASEKEYLEGACY% /k /f * ^| findstr /r /c:"HKEY_LOCAL_MACHINE.*\\%SELECTED_VERSION%"') do (
        set JDKKEY="%%a"
    )

    :: get the registry key for specified jdk major version, > 9    
    for /f "tokens=*" %%a in ('reg query %BASEKEY% /k /f * ^| findstr /r "HKEY_LOCAL_MACHINE.*\\%SELECTED_VERSION%$"') do (
        set JDKKEY="%%a"
    )

    for /f "tokens=*" %%a in ('reg query %BASEKEY% /k /f * ^| findstr /r "HKEY_LOCAL_MACHINE.*\\%SELECTED_VERSION%\..*"') do (
        set JDKKEY="%%a"
    )
)

:: get the java home
reg query %JDKKEY% /v JavaHome >nul || (
    echo JavaHome not installed
    exit /b 1
)

set JAVAHOME=
for /f "tokens=2,*" %%a in ('reg query %JDKKEY% /v JavaHome ^| findstr JavaHome') do (
    set JAVAHOME=%%b
)

:: clean destination directory
echo DELETING DIRECTORY "%SCRIPT_DIR%\..\out\jre-image"
rd /s "%SCRIPT_DIR%\..\out\jre-image"
echo JavaHome: %JAVAHOME%

:: build runtime
"%JAVAHOME%\bin\jlink" --compress=2 --output "%SCRIPT_DIR%\..\out\jre-image" --module-path "%JAVAHOME%\jmods" --add-modules javafx.base,javafx.fxml,javafx.graphics,javafx.controls,java.prefs,java.base,jdk.zipfs
ENDLOCAL

rem jdeps --print-module-deps tqrespec.jar


rem c:jlink --module-path C:\Program Files\Java\jdk-9.0.1\jmods --add-modules br.com.pinter.tqrespec.save,com.sun.jna.platform.win32,java.io,java.lang,java.lang.invoke,java.net,java.nio.file,java.util,java.util.regex,javafx.application,javafx.fxml,javafx.scene,javafx.stage,org.apache.commons.lang3,java.lang,javafx.application,javafx.fxml,javafx.scene,javafx.scene.control,javafx.scene.input,javafx.scene.layout,javafx.stage,br.com.pinter.tqrespec,java.io,java.lang,java.lang.invoke,java.nio,java.nio.channels,java.nio.charset,java.util,org.apache.commons.lang3


