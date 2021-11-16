package com.example.opensociety.connection.stun

import android.support.v4.media.session.PlaybackStateCompat.ErrorCode
import com.example.opensociety.db.Friend

abstract class Attribute {

    var length = 0

   /* Comprehension-required range (0x0000-0x7FFF):
    0x0000: (Reserved)
    0x0001: MAPPED-ADDRESS
    0x0002: (Reserved; was RESPONSE-ADDRESS)
    0x0003: (Reserved; was CHANGE-ADDRESS)
    0x0004: (Reserved; was SOURCE-ADDRESS)
    0x0005: (Reserved; was CHANGED-ADDRESS)
    0x0006: USERNAME
    0x0007: (Reserved; was PASSWORD)
    0x0008: MESSAGE-INTEGRITY
    0x0009: ERROR-CODE
    0x000A: UNKNOWN-ATTRIBUTES
    0x000B: (Reserved; was REFLECTED-FROM)
    0x0014: REALM
    0x0015: NONCE
    0x0020: XOR-MAPPED-ADDRESS
    Comprehension-optional range (0x8000-0xFFFF)
    0x8022: SOFTWARE
    0x8023: ALTERNATE-SERVER
    0x8028: FINGERPRINT*/

    enum class Type(val value: Int) {
        MAPPED_ADDRESS(0x0001),
        USERNAME(0x0006),
        PASSWORD(0x0007), //RESERVED
        MESSAGE_INTEGRITY(0x0008),
        ERROR_CODE(0x0009),
        UNKNOWN_ATTRIBUTE(0x000a),
        REALM(0x0014),
        NONCE(0x0015),
        XOR_MAPPED_ADDRES(0x0020);
        companion object {
            fun intToType(value: Int) =
                when (value){
                    MAPPED_ADDRESS.ordinal, USERNAME.ordinal, PASSWORD.ordinal ->
                        Attribute.Type.values()[value]
                    in MESSAGE_INTEGRITY.ordinal .. UNKNOWN_ATTRIBUTE.ordinal,
                    REALM.ordinal, NONCE.ordinal, XOR_MAPPED_ADDRES.ordinal ->
                        throw Exception("the attribute ${values()[value].toString()} " +
                                "is not supported yet")
                    else -> throw Exception("unknown attribute for ${value.toString(16)}")
                }
        }
    }
    var type: Type? = null

    fun constructor(type: Type?) {
        this.type = type
    }

    abstract fun getBytes(): ByteArray

    companion object{
        fun parseCommonHeader(data: ByteArray, shift:Int, length: Int): Attribute {
            try {
                val typeArray = Type.intToType(data.getUShortAt(shift).toInt())
                val lengthArray:Int = data.getUShortAt(shift + 2).toInt()
                if (lengthArray + 4 != length) {
                    throw Exception("Parsing error: to short data  got" +
                            " the length is $length but expected is ${lengthArray + 4}")
                }
                return when (typeArray) {
                    Attribute.Type.MAPPED_ADDRESS -> MappedAddress(data, shift + 4, lengthArray)
                    Type.USERNAME -> StringAttribute(data, shift + 4, lengthArray)
                    Type.PASSWORD -> StringAttribute(data, shift + 4, lengthArray)
                    //Type.MESSAGE_INTEGRITY -> MessageIntegrity.parse(data, shift + 4, lengthArray)
                    //Type.ERROR_CODE -> ErrorCode.parse(data, shift + 4, lengthArray)
                    //Type.UNKNOWN_ATTRIBUTE -> UnknownAttribute.parse(data, shift + 4, lengthArray)
                    else -> throw Exception("Unkown mandatory message attribute: $typeArray")
                }.also { it?.length = lengthArray }
            } catch (ue: Exception) {
                throw Exception("Parsing error: ${ue.message}")
            }
        }
    }
}