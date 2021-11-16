package com.example.opensociety.connection.stun

import android.util.Log
import java.lang.Exception

/*
  0               1               2               3
  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 |0 0|      STUN Message Type    |          Message Length       |
 +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 |                          Magic Cookie                         |
 +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 |                                                               |
 |                      Transaction ID (96 bits)                 |
 |                                                               |
 +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 */
class Message(type: MessageHeaderType? = null) {
    val TAG = "Message"
    companion object {
        val MAGIC_COOKIE:UInt = 0x2112A442.toUInt()
        val HEADER_SIZE = 20
        val ID_SIZE = 12
    }
    enum class MessageHeaderType(val i: Int) {
        BINDING_REQUEST(0x0001),
        BINDING_RESPONSE(0x0101),
        BINDING_ERROR_RESPONSE(0x0111),
        SHARED_SECRET_REQUEST(0x0002),
        SHARED_SECRET_RESPONSE(0x0102),
        SHARED_SECRET_ERROR_RESPONSE(0x0112)
    }
        var type: MessageHeaderType? = type
        var id = ByteArray(ID_SIZE)

        var attributes = hashMapOf<Attribute.Type, Attribute>()

        fun setTransactionID(id: ByteArray?) {
            System.arraycopy(id, 0, this.id, 0, ID_SIZE)
        }

        fun generateTransactionID() {
            for (i in id.indices) {
                id[i] = (Math.random() * 256).toInt().toByte()
            }
        }

        fun equalTransactionID(header: Message): Boolean {
            for(i in id.indices) {
                if(header.id[i] != id[i]) return false;
            }
           return true
        }

        fun addMessageAttribute(attri: Attribute) {
            attri.type?.let { attributes.put(it, attri) }
        }

        fun getMessageAttribute(type: Attribute.Type?): Attribute? {
            return attributes[type]
        }

        fun getBytes(): ByteArray {
            var length = HEADER_SIZE
            for(a in attributes) {
                length += a.value.length
            }
            val result = ByteArray(length.toInt())
            result.put(type!!.ordinal.toUShort(), 0)
            result.put((length - HEADER_SIZE).toUShort(), 2)
            result.put(MAGIC_COOKIE, 4)
            System.arraycopy(id, 0, result, 2, ID_SIZE)
            var offset = HEADER_SIZE
            for (a in attributes) {
                System.arraycopy(a.value.getBytes(), 0, result, offset, a.value.length)
                offset += a.value.length
            }
            return result
        }

        fun getLength(): Int {
            return getBytes().size
        }

        constructor(data: ByteArray):this() {
            Log.d(TAG, "create message from $data")
            if(data.getUIntAt(4) != MAGIC_COOKIE) throw Exception("this is not RFC 5389")
            val type = data.getUShortAt(0)
            var length = data.getUShortAt(2).toInt()
            System.arraycopy(data, 8, id, 0, 16)
            var offset = 24
            while (length > 0) {
                val ma = Attribute.parseCommonHeader(data, offset, length)
                addMessageAttribute(ma)
                length -= ma.length
                offset += ma.length
            }
        }
}