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

# 4. Install .NET SDK if not present
echo
echo "--- .NET SDK Setup ---"
if command -v dotnet &> /dev/null; then
    echo "[OK] .NET SDK found: $(dotnet --version)"
else
    echo "[..] .NET SDK not found — installing automatically..."

    # Detect distro to pick the right install method
    if [ -f /etc/os-release ]; then
        . /etc/os-release
        DISTRO_ID="$ID"
    else
        DISTRO_ID="unknown"
    fi

    case "$DISTRO_ID" in
        ubuntu|kubuntu|neon|pop|linuxmint|elementary)
            # Use Microsoft's install script (works on all Ubuntu-based distros
            # without needing to add Microsoft apt repos manually)
            echo "     Detected Ubuntu-based distro ($DISTRO_ID)"
            echo "     Using Microsoft's dotnet-install script..."
            echo
            curl -fsSL https://dot.net/v1/dotnet-install.sh -o /tmp/dotnet-install.sh
            chmod +x /tmp/dotnet-install.sh
            /tmp/dotnet-install.sh --channel 8.0 --install-dir "$HOME/.dotnet"
            rm -f /tmp/dotnet-install.sh
            ;;
        fedora|rhel|centos|rocky|alma)
            echo "     Detected RHEL-based distro ($DISTRO_ID)"
            sudo dnf install -y dotnet-sdk-8.0
            ;;
        arch|manjaro|endeavouros)
            echo "     Detected Arch-based distro ($DISTRO_ID)"
            sudo pacman -S --noconfirm dotnet-sdk-8.0
            ;;
        opensuse*|sles)
            echo "     Detected openSUSE/SLES ($DISTRO_ID)"
            sudo zypper install -y dotnet-sdk-8.0
            ;;
        *)
            echo "     Unknown distro ($DISTRO_ID) — using Microsoft install script..."
            curl -fsSL https://dot.net/v1/dotnet-install.sh -o /tmp/dotnet-install.sh
            chmod +x /tmp/dotnet-install.sh
            /tmp/dotnet-install.sh --channel 8.0 --install-dir "$HOME/.dotnet"
            rm -f /tmp/dotnet-install.sh
            ;;
    esac

    # Make sure dotnet is on PATH for the rest of this script
    export DOTNET_ROOT="$HOME/.dotnet"
    export PATH="$PATH:$HOME/.dotnet:$HOME/.dotnet/tools"

    if command -v dotnet &> /dev/null; then
        echo "[OK] .NET SDK installed: $(dotnet --version)"
    else
        echo "[!!] .NET SDK installation failed."
        echo "     The app will still work but DLLs won't be decompiled."
        echo "     You can install it manually: https://dotnet.microsoft.com/download"
    fi
fi

# 5. Install ilspycmd if not present
echo
echo "--- ILSpy Decompiler Setup ---"
export PATH="$PATH:$HOME/.dotnet/tools"

if command -v ilspycmd &> /dev/null || [ -f "$HOME/.dotnet/tools/ilspycmd" ]; then
    echo "[OK] ilspycmd is already installed"
else
    if command -v dotnet &> /dev/null; then
        echo "[..] Installing ilspycmd (ILSpy command-line decompiler)..."
        dotnet tool install -g ilspycmd
        echo "[OK] ilspycmd installed"
    else
        echo "[!!] Skipping ilspycmd — .NET SDK not available"
    fi
fi

# 6. Set up PATH in shell profile so dotnet/ilspycmd are always available
SHELL_RC=""
if [ -n "$ZSH_VERSION" ] || [ -f "$HOME/.zshrc" ]; then
    SHELL_RC="$HOME/.zshrc"
elif [ -f "$HOME/.bashrc" ]; then
    SHELL_RC="$HOME/.bashrc"
fi

if [ -n "$SHELL_RC" ]; then
    DOTNET_PATH_LINE='export PATH="$PATH:$HOME/.dotnet:$HOME/.dotnet/tools"'
    if ! grep -qF '.dotnet/tools' "$SHELL_RC" 2>/dev/null; then
        echo "" >> "$SHELL_RC"
        echo "# Added by tMod Decompiler setup" >> "$SHELL_RC"
        echo 'export DOTNET_ROOT="$HOME/.dotnet"' >> "$SHELL_RC"
        echo "$DOTNET_PATH_LINE" >> "$SHELL_RC"
        echo "[OK] Added .dotnet/tools to PATH in $SHELL_RC"
        echo "     Run 'source $SHELL_RC' or open a new terminal for it to take effect."
    fi
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
