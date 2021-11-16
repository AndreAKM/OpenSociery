package com.example.opensociety.connection.stun

import android.util.Log
import java.net.*


class StunClient(sourceAddress: InetAddress, sourcePort: Int) {
    val TAG = "StunClient"
    val port = 3478
    private val timeoutInitValue = 300
    val sourceAddress = sourceAddress
    val sourcePort = sourcePort
    var stunServerID = 0

    fun getAddress (): Pair<InetAddress?, Int>? {
        var timeSinceFirstTransmission = 0
        var socket: DatagramSocket? = null
        for (i in stunServerID until StunServerList.list.size) {
            var timeout: Int = timeoutInitValue

            while(timeout < timeoutInitValue shl 4) {
                try {
                    // Test 1 including response
                    var (stunServer, stunServerPort) = StunServerList.list[i]
                    Log.d(TAG, "server id $i: " +
                            "Binding Request sent to ($stunServer:$stunServerPort)" +
                            " timeout: $timeout")
                    socket = DatagramSocket(InetSocketAddress(sourceAddress, sourcePort))
                    socket.setReuseAddress(true)
                    socket.connect(InetAddress.getByName(stunServer), stunServerPort)
                    socket.setSoTimeout(timeout)
                    val sendMH = Message(Message.MessageHeaderType.BINDING_REQUEST)
                    sendMH.generateTransactionID()
                    val data: ByteArray = sendMH.getBytes()
                    val send = DatagramPacket(data, data.size)
                    socket.send(send)
                    var receiveMH = Message()
                    while (!receiveMH.equalTransactionID(sendMH)) {
                        val receive = DatagramPacket(ByteArray(200), 200)
                        socket.receive(receive)
                        receiveMH = Message(receive.getData())
                    }
                    val ma =
                        receiveMH.getMessageAttribute(Attribute.Type.MAPPED_ADDRESS) as MappedAddress
                    stunServerID = i
                    return Pair(
                        when (ma.family) {
                            MappedAddress.FAMILY_IPv4 -> Inet4Address.getByAddress(ma.address)
                            MappedAddress.FAMILY_IPv6 -> Inet6Address.getByAddress(ma.address)
                            else -> sourceAddress
                        }, ma.port.toInt()
                    )
                } catch (ste: SocketTimeoutException) {
                    if (timeSinceFirstTransmission < 7900) {
                        Log.d(TAG, "Test 1: Socket timeout while receiving the response.")
                        timeout = timeout shl 1
                        socket?.close()
                    } else {
                        // node is not capable of udp communication
                        Log.d(
                            TAG, "Test 1: Socket timeout while receiving the response. " +
                                    "Maximum retry limit exceed. Give up."
                        )
                    }
                }
                /*catch(e: Exception) {
                    e.message?.let { Log.d(TAG, "catched exception: $it") }
                }*/
            }
        }
        return null
    }
}