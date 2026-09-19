package org.cf0x.spicecompose.platform

@JsFun(
    """(bytesStr, filename) => {
        const a = document.createElement('a');
        a.href = URL.createObjectURL(new Blob([new Uint8Array(bytesStr.split(',').map(Number))], {type: 'image/jpeg'}));
        a.download = filename;
        a.click();
        URL.revokeObjectURL(a.href);
    }"""
)
private external fun jsSaveImage(bytesStr: String, filename: String)

actual fun saveImage(bytes: ByteArray, filename: String) {
    val jsBytes = bytes.joinToString(",") { it.toString() }
    jsSaveImage(jsBytes, filename)
}
