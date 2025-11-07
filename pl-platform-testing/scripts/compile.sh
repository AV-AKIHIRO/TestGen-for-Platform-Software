#!/bin/bash
# Shell script to compile using javac directly (for WSL when Maven has issues)
# Usage: ./compile.sh or bash scripts/compile.sh

# Change to project root directory
cd "$(dirname "$0")/.."

echo ""
echo "========================================"
echo "  Compiling with javac (direct)"
echo "========================================"
echo ""

# Check if target directory exists, create if not
if [ ! -d "target/classes" ]; then
    echo "Creating target/classes directory..."
    mkdir -p target/classes
fi

# Compile Java files
javac -d target/classes -sourcepath src/main/java \
    src/main/java/in/ac/iiitb/plproject/ast/*.java \
    src/main/java/in/ac/iiitb/plproject/parser/ast/*.java \
    src/main/java/in/ac/iiitb/plproject/parser/*.java \
    src/main/java/in/ac/iiitb/plproject/atc/*.java \
    src/main/java/in/ac/iiitb/plproject/symex/*.java

if [ $? -ne 0 ]; then
    echo ""
    echo "Compilation failed!"
    exit 1
fi

echo ""
echo "Compilation successful!"
echo ""

