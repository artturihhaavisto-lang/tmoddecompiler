# CLAUDE.md

## Project Overview

tMod Decompiler is a Flask web app that decompiles Terraria tModLoader mods (`.tmod` files) into readable C# source code. Users upload a `.tmod` file via the browser, and the app extracts all embedded files and decompiles `.dll` assemblies using ILSpy.

## Tech Stack

- **Python 3.10+** with Flask web framework
- **Pygments** for C# syntax highlighting
- **.NET SDK 8.0** + **ilspycmd** for DLL-to-C# decompilation
- Jinja2 templates with inline CSS (dark theme)
- No database, no JavaScript framework — pure server-rendered HTML

## Project Structure

```
app.py              # Flask web app — routes, upload handling, highlighting
tmod_parser.py      # Binary parser for .tmod format (TMOD magic, LEB128 strings, DEFLATE)
decompiler.py       # ilspycmd wrapper — finds binary, runs decompilation, collects .cs output
setup.sh            # One-command setup: Python venv, pip deps, .NET SDK, ilspycmd
requirements.txt    # Flask, Pygments
templates/
  index.html        # Upload page with drag-and-drop
  results.html      # Results page with sidebar nav + syntax-highlighted source
```

## Key Architecture Decisions

- **Graceful degradation**: The app works without .NET/ilspycmd installed — it extracts and lists files but skips decompilation. `check_ilspycmd()` gates decompilation paths.
- **ilspycmd discovery**: `_find_ilspycmd()` in `decompiler.py` checks both PATH and `~/.dotnet/tools/ilspycmd` directly, since the dotnet tools directory may not be on PATH.
- **Temp file cleanup**: Uploads and decompilation output use `tempfile` and are cleaned up in `finally` blocks.
- **Binary parsing**: `tmod_parser.py` reads the .tmod binary format directly using struct unpacking and .NET-style 7-bit encoded integers (LEB128).

## Development Commands

```bash
# Setup (installs Python venv, pip deps, .NET SDK, ilspycmd)
./setup.sh

# Run the dev server
./venv/bin/python app.py
# App runs at http://localhost:5000

# Quick syntax check
./venv/bin/python -c "from app import app; print('OK')"
```

## Conventions

- **No test suite** currently exists. Validate changes by importing modules and running the app.
- **No linter/formatter** configured. Keep code style consistent with existing files (standard Python, type hints on function signatures).
- **Templates use inline `<style>`** — no external CSS files or build step.
- **Max upload size**: 200 MB (set in `app.py`).
- Strings in the .tmod format use .NET's 7-bit encoded length prefix — see `_read_7bit_encoded_int()` in `tmod_parser.py`.

## Common Pitfalls

- `ilspycmd` may not be on PATH even after installation. Always use `_find_ilspycmd()` rather than hardcoding `'ilspycmd'`.
- `.tmod` files use DEFLATE compression (raw, no zlib header) — decompress with `zlib.decompress(data, -zlib.MAX_WBITS)`.
- `compressed_size == 0` or `compressed_size == uncompressed_size` means the entry is stored uncompressed.
