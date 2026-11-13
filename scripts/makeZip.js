import fs from 'node:fs';
import path from 'node:path';
import zlib from 'node:zlib';
import { fileURLToPath } from 'node:url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const rootDir = path.resolve(__dirname, '..');
const outputFile = path.resolve(rootDir, '..', 'agrosense.zip');

function crc32(buf) {
  let table = crc32.table;
  if (!table) {
    table = crc32.table = new Uint32Array(256);
    for (let i = 0; i < 256; i++) {
      let c = i;
      for (let k = 0; k < 8; k++) {
        c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1;
      }
      table[i] = c >>> 0;
    }
  }
  let crc = 0xffffffff;
  for (let i = 0; i < buf.length; i++) {
    crc = table[(crc ^ buf[i]) & 0xff] ^ (crc >>> 8);
  }
  return (crc ^ 0xffffffff) >>> 0;
}

function getAllFiles(dir, base = '') {
  let results = [];
  const list = fs.readdirSync(dir);
  for (const file of list) {
    if (file === 'node_modules' || file === '.git') continue;
    const filePath = path.join(dir, file);
    const relPath = path.posix.join(base, file);
    const stat = fs.statSync(filePath);
    if (stat.isDirectory()) {
      results = results.concat(getAllFiles(filePath, relPath));
    } else {
      results.push({ filePath, relPath });
    }
  }
  return results;
}

export function createZip() {
  const files = getAllFiles(rootDir);
  const localHeaders = [];
  const centralDirs = [];
  let offset = 0;

  for (const { filePath, relPath } of files) {
    const data = fs.readFileSync(filePath);
    const compressed = zlib.deflateRawSync(data);
    const crc = crc32(data);
    const nameBuf = Buffer.from(relPath, 'utf8');

    // Local file header (30 bytes + name)
    const localHeader = Buffer.alloc(30 + nameBuf.length);
    localHeader.writeUInt32LE(0x04034b50, 0); // Signature
    localHeader.writeUInt16LE(20, 4);          // Version needed
    localHeader.writeUInt16LE(0, 6);           // Flags
    localHeader.writeUInt16LE(8, 8);           // Compression: Deflate
    localHeader.writeUInt16LE(0, 10);          // Mod time
    localHeader.writeUInt16LE(0, 12);          // Mod date
    localHeader.writeUInt32LE(crc, 14);        // CRC-32
    localHeader.writeUInt32LE(compressed.length, 18); // Compressed size
    localHeader.writeUInt32LE(data.length, 22);       // Uncompressed size
    localHeader.writeUInt16LE(nameBuf.length, 26);    // Filename length
    localHeader.writeUInt16LE(0, 28);                 // Extra field length
    nameBuf.copy(localHeader, 30);

    localHeaders.push(localHeader, compressed);

    // Central directory header (46 bytes + name)
    const centralHeader = Buffer.alloc(46 + nameBuf.length);
    centralHeader.writeUInt32LE(0x02014b50, 0); // Signature
    centralHeader.writeUInt16LE(20, 4);          // Version made by
    centralHeader.writeUInt16LE(20, 6);          // Version needed
    centralHeader.writeUInt16LE(0, 8);           // Flags
    centralHeader.writeUInt16LE(8, 10);          // Compression: Deflate
    centralHeader.writeUInt16LE(0, 12);          // Mod time
    centralHeader.writeUInt16LE(0, 14);          // Mod date
    centralHeader.writeUInt32LE(crc, 16);        // CRC-32
    centralHeader.writeUInt32LE(compressed.length, 20); // Compressed size
    centralHeader.writeUInt32LE(data.length, 24);       // Uncompressed size
    centralHeader.writeUInt16LE(nameBuf.length, 28);    // Filename length
    centralHeader.writeUInt16LE(0, 30);                 // Extra length
    centralHeader.writeUInt16LE(0, 32);                 // Comment length
    centralHeader.writeUInt16LE(0, 34);                 // Disk number start
    centralHeader.writeUInt16LE(0, 36);                 // Internal file attributes
    centralHeader.writeUInt32LE(0, 38);                 // External file attributes
    centralHeader.writeUInt32LE(offset, 42);            // Relative offset of local header
    nameBuf.copy(centralHeader, 46);

    centralDirs.push(centralHeader);
    offset += localHeader.length + compressed.length;
  }

  const centralDirBuffer = Buffer.concat(centralDirs);
  const localHeadersBuffer = Buffer.concat(localHeaders);

  // End of Central Directory Record (22 bytes)
  const eocd = Buffer.alloc(22);
  eocd.writeUInt32LE(0x06054b50, 0);                 // Signature
  eocd.writeUInt16LE(0, 4);                          // Number of this disk
  eocd.writeUInt16LE(0, 6);                          // Disk where central directory starts
  eocd.writeUInt16LE(files.length, 8);               // Number of central directory records on this disk
  eocd.writeUInt16LE(files.length, 10);              // Total number of central directory records
  eocd.writeUInt32LE(centralDirBuffer.length, 12);   // Size of central directory
  eocd.writeUInt32LE(localHeadersBuffer.length, 16); // Offset of start of central directory
  eocd.writeUInt16LE(0, 20);                         // Comment length

  const fullZip = Buffer.concat([localHeadersBuffer, centralDirBuffer, eocd]);
  fs.writeFileSync(outputFile, fullZip);
  console.log(`[ZIP Generator] Archivo ZIP creado exitosamente en: ${outputFile} (${fullZip.length} bytes)`);
}

createZip();
