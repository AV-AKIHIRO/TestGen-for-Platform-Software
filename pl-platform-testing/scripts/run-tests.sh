#!/bin/bash
# Shell script to compile and run tests using javac directly (for WSL when Maven has issues)
# Usage: ./run-tests.sh [simple|complex|all]

# Change to project root directory
cd "$(dirname "$0")/.."

echo ""
echo "========================================"
echo "  NewGenATC Algorithm - Test Runner (javac)"
echo "========================================"
echo ""

# Check if target directory exists, create if not
if [ ! -d "target/classes" ]; then
    echo "Creating target/classes directory..."
    mkdir -p target/classes
fi

# Compile Java files
echo "Compiling Java files..."
javac -d target/classes -sourcepath src/main/java \
    src/main/java/in/ac/iiitb/plproject/ast/*.java \
    src/main/java/in/ac/iiitb/plproject/parser/ast/*.java \
    src/main/java/in/ac/iiitb/plproject/parser/*.java \
    src/main/java/in/ac/iiitb/plproject/atc/*.java \
    src/main/java/in/ac/iiitb/plproject/symex/*.java

if [ $? -ne 0 ]; then
    echo ""
    echo "ERROR: Compilation failed!"
    exit 1
fi

echo "Compilation successful!"
echo ""

# Determine test case to run
TEST_CASE=${1:-simple}

# Run the test
echo "Running test case: $TEST_CASE"
echo ""
java -cp target/classes in.ac.iiitb.plproject.atc.IncrementalTestExample "$TEST_CASE"

if [ $? -ne 0 ]; then
    echo ""
    echo "ERROR: Test execution failed!"
    exit 1
fi

echo ""
echo "Test completed successfully!"
echo ""

