@echo off
REM Script to copy generated files from outputs/ to genoutput/ folder
REM This follows the structure described in genoutput/README.md

setlocal

REM Get the project root directory (parent of scripts/)
cd /d "%~dp0\.."
set PROJECT_ROOT=%CD%

echo ========================================
echo Copying files from outputs to genoutput
echo ========================================
echo.

REM Create genoutput directory structure if it doesn't exist
if not exist "%PROJECT_ROOT%\..\genoutput" (
    echo Creating genoutput directory...
    mkdir "%PROJECT_ROOT%\..\genoutput"
)

if not exist "%PROJECT_ROOT%\..\genoutput\in\ac\iiitb\plproject\atc\generated" (
    echo Creating package directory structure...
    mkdir "%PROJECT_ROOT%\..\genoutput\in\ac\iiitb\plproject\atc\generated"
)

REM Copy GeneratedATCs.java to package structure
if exist "%PROJECT_ROOT%\outputs\GeneratedATCs.java" (
    echo Copying GeneratedATCs.java...
    copy /Y "%PROJECT_ROOT%\outputs\GeneratedATCs.java" "%PROJECT_ROOT%\..\genoutput\in\ac\iiitb\plproject\atc\generated\GeneratedATCs.java"
    echo   [OK] GeneratedATCs.java copied
) else (
    echo   [WARNING] GeneratedATCs.java not found in outputs/
)

REM Copy .jpf files to genoutput root (only copy files that exist)
for %%f in ("%PROJECT_ROOT%\outputs\GeneratedATCs_*_helper.jpf") do (
    if exist "%%f" (
        echo Copying %%~nxf...
        copy /Y "%%f" "%PROJECT_ROOT%\..\genoutput\%%~nxf"
        echo   [OK] %%~nxf copied
    )
)

REM Copy main.jpf file (primary test file for the complete sequence)
if exist "%PROJECT_ROOT%\outputs\GeneratedATCs_main.jpf" (
    echo Copying GeneratedATCs_main.jpf...
    copy /Y "%PROJECT_ROOT%\outputs\GeneratedATCs_main.jpf" "%PROJECT_ROOT%\..\genoutput\GeneratedATCs_main.jpf"
    echo   [OK] GeneratedATCs_main.jpf copied (PRIMARY TEST FILE)
) else (
    echo   [WARNING] GeneratedATCs_main.jpf not found in outputs/
)

REM Copy Helper.java from outputs to genoutput
if exist "%PROJECT_ROOT%\outputs\in\ac\iiitb\plproject\atc\generated\Helper.java" (
    echo Copying Helper.java...
    copy /Y "%PROJECT_ROOT%\outputs\in\ac\iiitb\plproject\atc\generated\Helper.java" "%PROJECT_ROOT%\..\genoutput\in\ac\iiitb\plproject\atc\generated\Helper.java"
    echo   [OK] Helper.java copied
) else (
    echo   [WARNING] Helper.java not found in outputs/
)

echo.
echo ========================================
echo Copy completed!
echo ========================================
echo.
echo Files copied to: %PROJECT_ROOT%\..\genoutput
echo.
echo Structure:
echo   genoutput/
echo     in/ac/iiitb/plproject/atc/generated/
echo       GeneratedATCs.java
echo       Helper.java
echo     GeneratedATCs_main.jpf (PRIMARY - test complete sequence)
echo     GeneratedATCs_*_helper.jpf (for each helper method)
echo.

pause

