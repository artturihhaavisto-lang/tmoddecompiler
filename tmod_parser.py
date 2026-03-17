"""
.tmod file parser for Terraria tModLoader mods.

The .tmod format is a custom binary container. Structure:
  - 4 bytes: magic "TMOD"
  - string:  tModLoader version
  - 20 bytes: SHA1 hash of remaining data
  - 256 bytes: digital signature
  - uint32: length of remaining data
  - string: mod name
  - string: mod version
  - int32: file count
  - for each file: string path, int32 uncompressed size, int32 compressed size
  - raw file data in entry order

Strings use .NET's 7-bit encoded length prefix (LEB128-style).
Files may be DEFLATE-compressed (when compressed size != uncompressed size).
"""

import struct
import zlib
from dataclasses import dataclass
from pathlib import Path


@dataclass
class TModFileEntry:
    path: str
    uncompressed_size: int
    compressed_size: int
    data: bytes  # raw (possibly compressed) data


@dataclass
class TModFile:
    tmodloader_version: str
    mod_name: str
    mod_version: str
    files: list  # list of TModFileEntry


def _read_7bit_encoded_int(data: bytes, offset: int) -> tuple[int, int]:
    """Read a .NET-style 7-bit encoded integer. Returns (value, new_offset)."""
    result = 0
    shift = 0
    while True:
        byte = data[offset]
        offset += 1
        result |= (byte & 0x7F) << shift
        if (byte & 0x80) == 0:
            break
        shift += 7
    return result, offset


def _read_string(data: bytes, offset: int) -> tuple[str, int]:
    """Read a .NET BinaryReader-style length-prefixed string."""
    length, offset = _read_7bit_encoded_int(data, offset)
    s = data[offset:offset + length].decode('utf-8')
    return s, offset + length


def _read_uint32(data: bytes, offset: int) -> tuple[int, int]:
    val = struct.unpack_from('<I', data, offset)[0]
    return val, offset + 4


def _read_int32(data: bytes, offset: int) -> tuple[int, int]:
    val = struct.unpack_from('<i', data, offset)[0]
    return val, offset + 4


def parse_tmod(file_path: str) -> TModFile:
    """Parse a .tmod file and return its contents."""
    data = Path(file_path).read_bytes()
    offset = 0

    # Magic header
    magic = data[offset:offset + 4]
    offset += 4
    if magic != b'TMOD':
        raise ValueError(f"Not a valid .tmod file (magic: {magic!r}, expected b'TMOD')")

    # tModLoader version
    tml_version, offset = _read_string(data, offset)

    # SHA1 hash (20 bytes) — skip, we don't verify
    offset += 20

    # Signature (256 bytes) — skip
    offset += 256

    # Data length (uint32)
    _data_length, offset = _read_uint32(data, offset)

    # From here on is the "file data" section
    # Mod name and version
    mod_name, offset = _read_string(data, offset)
    mod_version, offset = _read_string(data, offset)

    # File count
    file_count, offset = _read_int32(data, offset)

    # Read file table (metadata only — path, sizes)
    entries = []
    for _ in range(file_count):
        path, offset = _read_string(data, offset)
        uncompressed_size, offset = _read_int32(data, offset)
        compressed_size, offset = _read_int32(data, offset)
        entries.append(TModFileEntry(
            path=path,
            uncompressed_size=uncompressed_size,
            compressed_size=compressed_size,
            data=b'',
        ))

    # Read actual file data in the same order
    for entry in entries:
        size = entry.compressed_size if entry.compressed_size > 0 else entry.uncompressed_size
        entry.data = data[offset:offset + size]
        offset += size

    return TModFile(
        tmodloader_version=tml_version,
        mod_name=mod_name,
        mod_version=mod_version,
        files=entries,
    )


def decompress_entry(entry: TModFileEntry) -> bytes:
    """Decompress a file entry if needed. Returns raw bytes."""
    if entry.compressed_size > 0 and entry.compressed_size != entry.uncompressed_size:
        return zlib.decompress(entry.data, -zlib.MAX_WBITS)
    return entry.data
