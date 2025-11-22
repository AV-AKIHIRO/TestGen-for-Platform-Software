#!/bin/bash
# Script to copy generated files from outputs/ to genoutput/ folder
# This follows the structure described in genoutput/README.md

# Get the project root directory (parent of scripts/)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
GENOUTPUT_DIR="$PROJECT_ROOT/../genoutput"

echo "========================================"
echo "Copying files from outputs to genoutput"
echo "========================================"
echo

# Create genoutput directory structure if it doesn't exist
if [ ! -d "$GENOUTPUT_DIR" ]; then
    echo "Creating genoutput directory..."
    mkdir -p "$GENOUTPUT_DIR"
fi

if [ ! -d "$GENOUTPUT_DIR/in/ac/iiitb/plproject/atc/generated" ]; then
    echo "Creating package directory structure..."
    mkdir -p "$GENOUTPUT_DIR/in/ac/iiitb/plproject/atc/generated"
fi

# Copy GeneratedATCs.java to package structure
if [ -f "$PROJECT_ROOT/outputs/GeneratedATCs.java" ]; then
    echo "Copying GeneratedATCs.java..."
    cp "$PROJECT_ROOT/outputs/GeneratedATCs.java" "$GENOUTPUT_DIR/in/ac/iiitb/plproject/atc/generated/GeneratedATCs.java"
    echo "  [OK] GeneratedATCs.java copied"
else
    echo "  [WARNING] GeneratedATCs.java not found in outputs/"
fi

# Copy .jpf files to genoutput root (only copy files that exist)
for jpf_file in "$PROJECT_ROOT/outputs/GeneratedATCs_"*"_helper.jpf"; do
    if [ -f "$jpf_file" ]; then
        filename=$(basename "$jpf_file")
        echo "Copying $filename..."
        cp "$jpf_file" "$GENOUTPUT_DIR/$filename"
        echo "  [OK] $filename copied"
    fi
done

# Copy main.jpf file (primary test file for the complete sequence)
if [ -f "$PROJECT_ROOT/outputs/GeneratedATCs_main.jpf" ]; then
    echo "Copying GeneratedATCs_main.jpf..."
    cp "$PROJECT_ROOT/outputs/GeneratedATCs_main.jpf" "$GENOUTPUT_DIR/GeneratedATCs_main.jpf"
    echo "  [OK] GeneratedATCs_main.jpf copied (PRIMARY TEST FILE)"
else
    echo "  [WARNING] GeneratedATCs_main.jpf not found in outputs/"
fi

# Copy Helper.java from outputs to genoutput
if [ -f "$PROJECT_ROOT/outputs/in/ac/iiitb/plproject/atc/generated/Helper.java" ]; then
    echo "Copying Helper.java..."
    cp "$PROJECT_ROOT/outputs/in/ac/iiitb/plproject/atc/generated/Helper.java" "$GENOUTPUT_DIR/in/ac/iiitb/plproject/atc/generated/Helper.java"
    echo "  [OK] Helper.java copied"
else
    echo "  [WARNING] Helper.java not found in outputs/"
fi

echo
echo "========================================"
echo "Copy completed!"
echo "========================================"
echo
echo "Files copied to: $GENOUTPUT_DIR"
echo
echo "Structure:"
echo "  genoutput/"
echo "  ├── in/ac/iiitb/plproject/atc/generated/"
echo "  │   ├── GeneratedATCs.java"
echo "  │   └── Helper.java"
echo "  ├── GeneratedATCs_main.jpf (PRIMARY - test complete sequence)"
echo "  └── GeneratedATCs_*_helper.jpf (for each helper method)"
echo

