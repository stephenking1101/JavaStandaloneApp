@echo off
set "CURRENT_DIR=%cd%"

if not "%JAVA_HOME%" == "" goto gotHome
SET JAVA_HOME=C:\Program Files\Java\jre6

:gotHome
echo JAVA_HOME is %JAVA_HOME%
if exist "%JAVA_HOME%\bin\java.exe" goto okHome
SET JAVA_HOME=C:\Program Files (x86)\Java\jre6
echo JAVA_HOME is %JAVA_HOME%
if exist "%JAVA_HOME%\bin\java.exe" goto okHome
echo JAVA_HOME environment variable is required
echo e.g.
echo SET JAVA_HOME=C:\Program Files\Java\jre6
goto end

:okHome
cd "%~dp0"

echo "Important... The library dependency sequence matters"

set RACP="..\conf"

set RACP=%RACP%;"..\lib\activation-1.1.jar"
set RACP=%RACP%;"..\lib\jms-1.1.jar"
set RACP=%RACP%;"..\lib\jaxp-api-1.4.jar"
set RACP=%RACP%;"..\lib\junit-4.8.1.jar"
set RACP=%RACP%;"..\lib\log4j-1.2.15.jar"
set RACP=%RACP%;"..\lib\jmxtools-1.2.1.jar"
set RACP=%RACP%;"..\lib\jmxri-1.2.1.jar"
set RACP=%RACP%;"..\lib\commons-logging-1.1.1.jar"
set RACP=%RACP%;"..\lib\jena-core-2.7.2.jar"
set RACP=%RACP%;"..\lib\jena-iri-0.9.2.jar"
set RACP=%RACP%;"..\lib\slf4j-api-1.6.4.jar"
set RACP=%RACP%;"..\lib\slf4j-log4j12-1.6.4.jar"
set RACP=%RACP%;"..\lib\xercesImpl-2.10.0.jar"
set RACP=%RACP%;"..\lib\xml-apis-1.4.01.jar"
set RACP=%RACP%;"..\lib\*"

"%JAVA_HOME%\bin\java.exe" -cp %RACP% com.hsbc.alm.rtc.client.RTCAutomation %*

REM cd "%CURRENT_DIR%"
:end
