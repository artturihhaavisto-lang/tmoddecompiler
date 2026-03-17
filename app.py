"""
Terraria tModLoader Mod Decompiler — Web Application

A simple Flask web app that lets you upload a .tmod file, extracts all
embedded files, decompiles any .dll assemblies to C# source code, and
displays everything in the browser with syntax highlighting.
"""

import os
import tempfile
import shutil
from pathlib import Path

from flask import Flask, render_template, request, redirect, url_for, flash
from pygments import highlight
from pygments.lexers import CSharpLexer
from pygments.formatters import HtmlFormatter

from tmod_parser import parse_tmod, decompress_entry
from decompiler import decompile_dll_bytes, check_ilspycmd

app = Flask(__name__)
app.secret_key = os.urandom(24)
app.config['MAX_CONTENT_LENGTH'] = 200 * 1024 * 1024  # 200 MB max upload

CSHARP_LEXER = CSharpLexer()
HTML_FORMATTER = HtmlFormatter(linenos=True, cssclass='source')


def highlight_csharp(code: str) -> str:
    """Apply syntax highlighting to C# code."""
    return highlight(code, CSHARP_LEXER, HTML_FORMATTER)


@app.route('/')
def index():
    """Home page with file upload form."""
    ilspy_available = check_ilspycmd()
    return render_template('index.html', ilspy_available=ilspy_available)


@app.route('/upload', methods=['POST'])
def upload():
    """Handle .tmod file upload, parse, extract, and decompile."""
    if 'tmod_file' not in request.files:
        flash('No file selected.')
        return redirect(url_for('index'))

    file = request.files['tmod_file']
    if file.filename == '' or not file.filename.endswith('.tmod'):
        flash('Please upload a .tmod file.')
        return redirect(url_for('index'))

    # Save uploaded file to temp location
    tmpdir = tempfile.mkdtemp(prefix='tmod_upload_')
    try:
        upload_path = os.path.join(tmpdir, file.filename)
        file.save(upload_path)

        # Parse the .tmod file
        tmod = parse_tmod(upload_path)

        # Extract and categorize files
        extracted_files = []  # (path, size, category)
        dll_sources = {}      # dll_name -> {relative_path -> highlighted_code}
        raw_text_files = {}   # path -> content

        ilspy_ok = check_ilspycmd()

        for entry in tmod.files:
            decompressed = decompress_entry(entry)
            category = _categorize(entry.path)
            extracted_files.append((entry.path, len(decompressed), category))

            if entry.path.endswith('.dll') and ilspy_ok:
                # Decompile DLL to C# source
                try:
                    sources = decompile_dll_bytes(decompressed, os.path.basename(entry.path))
                    dll_sources[entry.path] = {
                        path: highlight_csharp(code)
                        for path, code in sources.items()
                    }
                except Exception as e:
                    dll_sources[entry.path] = {
                        'error.txt': f'<pre>Decompilation failed: {e}</pre>'
                    }
            elif _is_text_file(entry.path):
                try:
                    raw_text_files[entry.path] = decompressed.decode('utf-8', errors='replace')
                except Exception:
                    pass

        pygments_css = HTML_FORMATTER.get_style_defs('.source')

        return render_template(
            'results.html',
            mod_name=tmod.mod_name,
            mod_version=tmod.mod_version,
            tml_version=tmod.tmodloader_version,
            extracted_files=extracted_files,
            dll_sources=dll_sources,
            raw_text_files=raw_text_files,
            pygments_css=pygments_css,
            ilspy_available=ilspy_ok,
        )
    except ValueError as e:
        flash(f'Error parsing .tmod file: {e}')
        return redirect(url_for('index'))
    except Exception as e:
        flash(f'Unexpected error: {e}')
        return redirect(url_for('index'))
    finally:
        shutil.rmtree(tmpdir, ignore_errors=True)


def _categorize(path: str) -> str:
    """Categorize a file by its extension."""
    ext = Path(path).suffix.lower()
    categories = {
        '.dll': 'Assembly',
        '.pdb': 'Debug Symbols',
        '.png': 'Image',
        '.rawimg': 'Image (Raw)',
        '.ogg': 'Audio',
        '.mp3': 'Audio',
        '.wav': 'Audio',
        '.xnb': 'XNA Content',
        '.json': 'Data',
        '.txt': 'Text',
        '.xml': 'Data',
        '.fx': 'Shader',
        '.xps': 'Shader',
    }
    return categories.get(ext, 'Other')


def _is_text_file(path: str) -> bool:
    ext = Path(path).suffix.lower()
    return ext in {'.json', '.txt', '.xml', '.csv', '.cfg', '.ini', '.md', '.cs', '.fx'}


if __name__ == '__main__':
    print("=" * 60)
    print("  Terraria tModLoader Mod Decompiler")
    print("  Open http://localhost:5000 in your browser")
    print("=" * 60)
    app.run(debug=True, host='0.0.0.0', port=5000)
