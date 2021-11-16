package com.example.opensociety.connection.stun


class MappedAddress(): Attribute() {
    val TAG = "MappedAddress"
    var port:UShort = 0u
    var address: ByteArray? = null //= emptyArray<Int>()
    var family:Byte = 0
    /*
         0                   1                   2                   3
          0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
         +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
         |0 0 0 0 0 0 0 0|     Family    |              Port             |
         +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
         |                                                               |
         |              Address (32 bits or 128 bits)                    |
         |                                                               |
         +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 */
    companion object {
        val FAMILY_IPv4: Byte = 0x01
        val FAMILY_IPv6: Byte = 0x02
    }
    constructor(data: ByteArray, shift:Int, length: Int):this() {
        val family:Byte = data[shift + 1]
        val portArray = data.getUShortAt(shift + 2)
        when(family) {
            FAMILY_IPv4 -> IP4pars(data, shift + 4, length - 4)
            FAMILY_IPv6 -> IP6pars(data, shift + 4, length - 4)
        }
    }

    fun IP4pars(data: ByteArray, shift: Int, length: Int) {
        if (length < 4) {
            throw Exception("Data array too short")
        }
        address = data.copyOfRange(shift, shift + 4)
    }

    fun IP6pars(data: ByteArray, shift: Int, length: Int) {
        if (length < 16) {
            throw Exception("Data array too short")
        }
        address = data.copyOfRange(shift, shift+16)
    }

    override fun getBytes(): ByteArray {
        var res = ByteArray(4 + address!!.size)
        res[0] = 0
        res[1] = family
        res[2] = ((address!!.size shr 8) and 0xFF).toByte()
        res[3] = (address!!.size and 0xFF).toByte()
        System.arraycopy(address, 0, res, 4, address!!.size)
        return res
    }

    fun addressToString() = when(family) {
        FAMILY_IPv4 -> addressIP4ToString()
        FAMILY_IPv6 -> addressIP6ToString()
        else -> address.toString()
    }

    fun addressIP4ToString() = "${address!![3]}.${address!![2]}.${address!![1]}.${address!![0]}"
    fun addressIP6ToString() = address.toString()
    override fun toString() ="${addressToString()}:$port"
}

