package com.example.opensociety.connection.stun


fun ByteArray.getUShortAt(idx: Int) =
    (((this[idx].toUInt() and 0xFFu) shl 8) or (this[idx + 1].toUInt() and 0xFFu)).toUShort()

fun ByteArray.getUIntAt(idx: Int) =
    ((this[idx].toUInt() and 0xFFu) shl 24) or
            ((this[idx + 1].toUInt() and 0xFFu) shl 16) or
            ((this[idx + 2].toUInt() and 0xFFu) shl 8) or
            (this[idx + 3].toUInt() and 0xFFu)

fun ByteArray.put(data: UInt, idx: Int) {
    this[idx] = (data shr 24 and 0xFFu).toByte()
    this[idx + 1] = (data shr 16 and 0xFFu).toByte()
    this[idx + 2] = (data shr 8 and 0xFFu).toByte()
    this[idx + 3] = (data and 0xFFu).toByte()
}

fun ByteArray.put(data: UShort, idx: Int) {
    this[idx + 2] = (data.toUInt() shr 8 and 0xFFu).toByte()
    this[idx + 3] = (data and 0xFFu).toByte()
}