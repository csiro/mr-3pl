@echo off

set this="%0"
for %%a in (%this%) do set base=%%~dpa
for %%a in (%this%) do set script=%%~nxa

rem elide trailing bin\
for %%a in (%base:~0,-1%) do if %%~nxa equ bin (set base=%%~dpa)

rem elide trailing \
set base=%base:~0,-1%

if "%THREEPL_JAVA%"=="" set THREEPL_JAVA=java
if "%THREEPL_JAVA_OPTS%"=="" set THREEPL_JAVA_OPTS=-Xmx800M
if "%THREEPL_INCLUDE_DIRS%"=="" set THREEPL_INCLUDE_DIRS=%base%\include
if "%THREEPL_JAR%"=="" set THREEPL_JAR=%base%\lib\3pl.jar

%THREEPL_JAVA% %THREEPL_JAVA_OPTS% -jar %THREEPL_JAR%^
 -I%THREEPL_INCLUDE_DIRS% %*
