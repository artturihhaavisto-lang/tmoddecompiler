#!/bin/bash
# Setup script for tMod Decompiler on Linux (Ubuntu/Kubuntu)
# This installs all dependencies and gets the app ready to run.

set -e

echo "=================================="
echo "  tMod Decompiler — Setup"
echo "=================================="
echo

# 1. Check Python
if ! command -v python3 &> /dev/null; then
    echo "ERROR: Python 3 is required but not installed."
    echo "Install it with: sudo apt install python3 python3-pip python3-venv"
    exit 1
fi
echo "[OK] Python 3 found: $(python3 --version)"

# 2. Create virtual environment
if [ ! -d "venv" ]; then
    echo "[..] Creating Python virtual environment..."
    python3 -m venv venv
    echo "[OK] Virtual environment created"
else
    echo "[OK] Virtual environment already exists"
fi

# 3. Install Python packages
echo "[..] Installing Python dependencies..."
./venv/bin/pip install --quiet -r requirements.txt
echo "[OK] Python dependencies installed"

# 4. Check for .NET SDK and ilspycmd
echo
echo "--- .NET Decompiler Setup ---"
if command -v dotnet &> /dev/null; then
    echo "[OK] .NET SDK found: $(dotnet --version)"

    if command -v ilspycmd &> /dev/null || [ -f "$HOME/.dotnet/tools/ilspycmd" ]; then
        echo "[OK] ilspycmd is installed"
    else
        echo "[..] Installing ilspycmd (ILSpy command-line decompiler)..."
        dotnet tool install -g ilspycmd
        echo "[OK] ilspycmd installed"
        echo "     Make sure ~/.dotnet/tools is in your PATH:"
        echo '     export PATH="$PATH:$HOME/.dotnet/tools"'
    fi
else
    echo "[!!] .NET SDK not found. DLL decompilation will be DISABLED."
    echo "     The app will still extract all files from .tmod mods."
    echo ""
    echo "     To enable decompilation, install the .NET SDK:"
    echo "       sudo apt install dotnet-sdk-8.0"
    echo "     Then run this setup script again."
fi

echo
echo "=================================="
echo "  Setup complete!"
echo ""
echo "  To run the app:"
echo "    ./venv/bin/python app.py"
echo ""
echo "  Then open http://localhost:5000"
echo "=================================="
