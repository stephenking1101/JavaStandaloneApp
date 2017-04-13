@echo off
set "CURRENT_DIR=%cd%"

if not "%JAVA_HOME%" == "" goto gotHome
SET JAVA_HOME=C:\Program Files\Java\jre6

:gotHome
echo JAVA_HOME is %JAVA_HOME%
if exist "%JAVA_HOME%\bin\keytool.exe" goto checkJRE
echo JAVA_HOME environment variable is required
echo e.g.
echo SET JAVA_HOME=C:\Program Files\Java\jre6
goto end

:checkJRE
if exist "%JAVA_HOME%\lib\security\cacerts" goto isJRE
goto checkJDK
:isJRE
echo "set JRE CACERT_PATH"
set CACERT_PATH="%JAVA_HOME%\lib\security\cacerts"

:checkJDK
if exist "%JAVA_HOME%\jre\lib\security\cacerts" goto isJDK
goto okHome
:isJDK
echo "set JDK CACERT_PATH"
set CACERT_PATH="%JAVA_HOME%\jre\lib\security\cacerts"


:okHome
"%JAVA_HOME%\bin\keytool.exe" -import -keystore %CACERT_PATH% -alias hsbc_root_ca -storepass changeit -file "%~dp0..\conf\root_ca_cert.cer"



REM cd "%CURRENT_DIR%"
:end
