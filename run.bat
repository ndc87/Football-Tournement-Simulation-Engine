@echo off
:: =====================================================
:: Football Tournament Simulation Engine - Run Script
:: Yeu cau: JDK 23 tai C:\Program Files\Java\jdk-23
:: =====================================================

set JAVA_HOME=C:\Program Files\Java\jdk-23
set PATH=%JAVA_HOME%\bin;%PATH%
set MAVEN_HOME=d:\codeNDC\apache-maven-3.9.6

echo.
echo  Kiem tra moi truong...
javac -version 2>nul
if errorlevel 1 (
    echo  LOI: Khong tim thay JDK 23 tai %JAVA_HOME%
    echo  Vui long kiem tra lai duong dan.
    pause
    exit /b 1
)

echo  Dang bien dich...
call "%MAVEN_HOME%\bin\mvn.cmd" clean compile -q
if errorlevel 1 (
    echo  LOI: Compile that bai! Xem log o tren.
    pause
    exit /b 1
)

echo  Khoi dong Football Tournament Simulation Engine...
echo.
call "%MAVEN_HOME%\bin\mvn.cmd" exec:java -Dexec.mainClass=com.football.Main -q
