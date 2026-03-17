"""
Decompiles .NET DLL files to C# source code using ilspycmd.

ilspycmd is the command-line version of ILSpy, powered by ICSharpCode.Decompiler.
It runs on .NET and works on Linux via the .NET SDK.
"""

import os
import subprocess
import tempfile
from pathlib import Path


def _find_ilspycmd() -> str | None:
    """Find the ilspycmd binary, checking PATH and common install locations."""
    # Check PATH first
    for name in ['ilspycmd']:
        try:
            result = subprocess.run(
                [name, '--version'],
                capture_output=True, text=True, timeout=10
            )
            if result.returncode == 0:
                return name
        except (FileNotFoundError, subprocess.TimeoutExpired):
            pass

    # Check ~/.dotnet/tools (default .NET global tool location)
    dotnet_tools_path = os.path.join(os.path.expanduser('~'), '.dotnet', 'tools', 'ilspycmd')
    if os.path.isfile(dotnet_tools_path):
        try:
            result = subprocess.run(
                [dotnet_tools_path, '--version'],
                capture_output=True, text=True, timeout=10
            )
            if result.returncode == 0:
                return dotnet_tools_path
        except (FileNotFoundError, subprocess.TimeoutExpired):
            pass

    return None


def check_ilspycmd() -> bool:
    """Check if ilspycmd is available."""
    return _find_ilspycmd() is not None


def decompile_dll(dll_path: str, output_dir: str) -> dict[str, str]:
    """
    Decompile a .NET DLL to C# source files.

    Args:
        dll_path: Path to the .dll file
        output_dir: Directory to write decompiled .cs files

    Returns:
        Dict mapping relative file paths to their C# source code
    """
    os.makedirs(output_dir, exist_ok=True)

    ilspy = _find_ilspycmd()
    if ilspy is None:
        raise RuntimeError("ilspycmd not found")

    result = subprocess.run(
        [ilspy, dll_path, '-p', '-o', output_dir],
        capture_output=True, text=True, timeout=120
    )

    if result.returncode != 0:
        raise RuntimeError(
            f"ilspycmd failed (exit {result.returncode}):\n"
            f"stdout: {result.stdout}\n"
            f"stderr: {result.stderr}"
        )

    # Collect all generated .cs files
    sources = {}
    output_path = Path(output_dir)
    for cs_file in sorted(output_path.rglob('*.cs')):
        rel = cs_file.relative_to(output_path)
        sources[str(rel)] = cs_file.read_text(encoding='utf-8', errors='replace')

    return sources


def decompile_dll_bytes(dll_bytes: bytes, dll_name: str) -> dict[str, str]:
    """
    Decompile a DLL from raw bytes.

    Args:
        dll_bytes: Raw DLL file contents
        dll_name: Name for the DLL file

    Returns:
        Dict mapping relative file paths to C# source code
    """
    with tempfile.TemporaryDirectory(prefix='tmod_decompile_') as tmpdir:
        dll_path = os.path.join(tmpdir, dll_name)
        with open(dll_path, 'wb') as f:
            f.write(dll_bytes)

        output_dir = os.path.join(tmpdir, 'output')
        return decompile_dll(dll_path, output_dir)
