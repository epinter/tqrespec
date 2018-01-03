@echo off

setlocal

set SCRIPT_DIR=%~dp0

::- Get the Java Version
set KEY="HKLM\SOFTWARE\JavaSoft\JDK"
set VALUE=CurrentVersion
reg query %KEY% /v %VALUE% 2>nul || (
    echo JRE not installed 
    exit /b 1
)
set JRE_VERSION=
for /f "tokens=2,*" %%a in ('reg query %KEY% /v %VALUE% ^| findstr %VALUE%') do (
    set JRE_VERSION=%%b
)

echo JRE VERSION: %JRE_VERSION%

::- Get the JavaHome
set KEY="HKLM\SOFTWARE\JavaSoft\JDK\%JRE_VERSION%"
set VALUE=JavaHome
reg query %KEY% /v %VALUE% 2>nul || (
    echo JavaHome not installed
    exit /b 1
)

set JAVAHOME=
for /f "tokens=2,*" %%a in ('reg query %KEY% /v %VALUE% ^| findstr %VALUE%') do (
    set JAVAHOME=%%b
)





echo DELETING DIRECTORY "%SCRIPT_DIR%\..\out\jre-image"
rd /s "%SCRIPT_DIR%\..\out\jre-image"
echo JavaHome: %JAVAHOME%
"%JAVAHOME%\bin\jlink" --compress=2 --output "%SCRIPT_DIR%\..\out\jre-image" --module-path "%JAVAHOME%\jmods" --add-modules javafx.base,javafx.fxml,javafx.graphics,javafx.controls,java.prefs,java.base,jdk.zipfs
endlocal


rem jdeps --print-module-deps tqrespec.jar


rem c:jlink --module-path C:\Program Files\Java\jdk-9.0.1\jmods --add-modules br.com.pinter.tqrespec.save,com.sun.jna.platform.win32,java.io,java.lang,java.lang.invoke,java.net,java.nio.file,java.util,java.util.regex,javafx.application,javafx.fxml,javafx.scene,javafx.stage,org.apache.commons.lang3,java.lang,javafx.application,javafx.fxml,javafx.scene,javafx.scene.control,javafx.scene.input,javafx.scene.layout,javafx.stage,br.com.pinter.tqrespec,java.io,java.lang,java.lang.invoke,java.nio,java.nio.channels,java.nio.charset,java.util,org.apache.commons.lang3


