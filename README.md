# tMod Decompiler

A simple web-based tool that decompiles Terraria tModLoader mods (`.tmod` files) into readable C# source code. Runs on Linux.

## What It Does

1. **Upload** a `.tmod` file through your browser (drag & drop or browse)
2. **Extracts** all embedded files (DLLs, images, audio, data files, etc.)
3. **Decompiles** `.dll` assemblies into C# source code using ILSpy
4. **Displays** the decompiled code with syntax highlighting, organized by file

## Quick Start

```bash
# 1. Run the setup script (installs everything)
./setup.sh

# 2. Start the app
./venv/bin/python app.py

# 3. Open in your browser
#    http://localhost:5000
```

## Requirements

- **Python 3.10+** (pre-installed on Kubuntu 25.10)
- **Flask** and **Pygments** (installed automatically by setup.sh)
- **.NET SDK 8.0+** (optional, needed for DLL decompilation)
  - Install with: `sudo apt install dotnet-sdk-8.0`
  - Then: `dotnet tool install -g ilspycmd`

Without the .NET SDK, the app still works — it extracts and lists all files from the `.tmod`, but won't decompile DLLs to C# source.

## How .tmod Files Work

`.tmod` is a custom binary format used by tModLoader. It contains:
- A header with magic bytes (`TMOD`), version info, and a SHA1 hash
- A file table listing all embedded files with their sizes
- The actual file data (usually DEFLATE-compressed)

Mods typically contain `.dll` assemblies (the compiled mod code), images, sounds, and data files.

## For Personal Use Only

This tool is intended for personal learning and research. Respect mod authors' work and licenses.
