package com.example.opensociety.connection.stun




class StringAttribute(username: String? = null): Attribute() {
    var username: String? = username

    override fun getBytes(): ByteArray {
        var res = ByteArray(username?.length.let { if(it!! % 4 != 0) (it shr 2) shl 2 + 4 else it})
        System.arraycopy(username!!.toByteArray(Charsets.UTF_8), 0,
            res, 0, username!!.length)
        return res
    }

    constructor(data: ByteArray?, offset: Int, length: Int): this( String(data!!, offset, length))
}