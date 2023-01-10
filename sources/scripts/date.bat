@echo off

REM time zone code comes from: https://stackoverflow.com/a/43146279

REM Obtain the ActiveBias value and convert to decimal
set rkey=HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Control\TimeZoneInformation
for /f "tokens=3 usebackq" %%a in (`reg query %rkey% /v ActiveTimeBias ^| grep -i ActiveTimeBias`) do set /a abias=%%a
REM for /f "tokens=3-8 usebackq" %%n in (`reg query %rkey% /v TimeZoneKeyName`) do set tzn=%%n %%o %%p %%q %%r %%s 2>NUL

REM Set the + or - sign variable to reflect the timezone offset
IF "%abias:~0,1%"=="-" (set si=+) ELSE (set si=-)
for /f "tokens=1 delims=-" %%t in ('echo %abias%') do set tzc=%%t

REM Calculate to obtain floating points (decimal values)
set /a tzd=100*%tzc%/60

REM Calculate the active bias to obtain the hour
set /a tze=%tzc%/60

REM Set the minutes based on the result of the floating point calculation
IF "%tzd%"=="0" (set en=00 && set si=)
IF "%tzd:~1%"=="00" (set en=00) ELSE IF "%tzd:~2%"=="00" (set en=00 && set tz=%tzd:~0,2%)
IF "%tzd:~1%"=="50" (set en=30) ELSE IF "%tzd:~2%"=="50" (set en=30 && set tz=%tzd:~0,2%)
IF "%tzd:~1%"=="75" (set en=45) ELSE IF "%tzd:~2%"=="75" (set en=45 && set tz=%tzd:~0,2%)

REM Adding a 0 to the beginning of a single digit hour value
IF %tze% LSS 10 (set tz=0%tze%)

REM Display timezone name and offset (modified for 3pl build system - includes date and time)
echo %date:~10,4%-%date:~7,2%-%date:~4,2% %time:~0,8% %si%%tz%%en%
