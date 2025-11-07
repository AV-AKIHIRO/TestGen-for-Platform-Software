@echo off
REM Batch file to compile and run tests using javac directly (no Maven dependency)
REM Usage: run-tests.bat [simple|complex|all]
REM Run from project root or scripts folder

setlocal

REM Change to project root directory (parent of scripts folder)
cd /d "%~dp0.."

echo.
echo ========================================
echo   NewGenATC Algorithm - Test Runner
echo ========================================
echo.

REM Check if target directory exists, create if not
if not exist "target\classes" (
    echo Creating target\classes directory...
    mkdir target\classes
)

REM Compile Java files
echo Compiling Java files...
javac -d target/classes -sourcepath src/main/java ^
    src/main/java/in/ac/iiitb/plproject/ast/*.java ^
    src/main/java/in/ac/iiitb/plproject/parser/ast/*.java ^
    src/main/java/in/ac/iiitb/plproject/parser/*.java ^
    src/main/java/in/ac/iiitb/plproject/atc/*.java ^
    src/main/java/in/ac/iiitb/plproject/symex/*.java

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Compilation failed!
    pause
    exit /b 1
)

echo Compilation successful!
echo.

REM Determine test case to run
set TEST_CASE=%1
if "%TEST_CASE%"=="" set TEST_CASE=simple

REM Run the test
echo Running test case: %TEST_CASE%
echo.
java -cp target/classes in.ac.iiitb.plproject.atc.IncrementalTestExample %TEST_CASE%

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Test execution failed!
    pause
    exit /b 1
)

echo.
echo Test completed successfully!
echo.
pause

