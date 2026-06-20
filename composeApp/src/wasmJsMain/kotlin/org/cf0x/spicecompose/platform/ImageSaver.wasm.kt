package org.cf0x.spicecompose.platform

actual fun saveImage(bytes: ByteArray, filename: String) {
    // Trigger browser download via JS blob.
    val jsBytes = bytes.toList().joinToString(",") { it.toString() }
    js(
        """
        const a = document.createElement('a');
        a.href = URL.createObjectURL(new Blob([new Uint8Array([$jsBytes])], {type: 'image/jpeg'}));
        a.download = '$filename';
        a.click();
        URL.revokeObjectURL(a.href);
        """
    )
}
