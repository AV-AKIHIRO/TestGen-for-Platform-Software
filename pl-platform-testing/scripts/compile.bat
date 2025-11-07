@echo off
REM Batch file to compile using javac directly (no Maven dependency)
REM Run from project root or scripts folder

setlocal

REM Change to project root directory (parent of scripts folder)
cd /d "%~dp0.."

echo.
echo ========================================
echo   Compiling with javac (direct)
echo ========================================
echo.

REM Check if target directory exists, create if not
if not exist "target\classes" (
    echo Creating target\classes directory...
    mkdir target\classes
)

REM Compile Java files
javac -d target/classes -sourcepath src/main/java ^
    src/main/java/in/ac/iiitb/plproject/ast/*.java ^
    src/main/java/in/ac/iiitb/plproject/parser/ast/*.java ^
    src/main/java/in/ac/iiitb/plproject/parser/*.java ^
    src/main/java/in/ac/iiitb/plproject/atc/*.java ^
    src/main/java/in/ac/iiitb/plproject/symex/*.java

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Compilation failed!
    pause
    exit /b 1
)

echo.
echo Compilation successful!
echo.
pause

