package com.example.opensociety.connection

import android.content.Context

import android.content.Intent




class ServiceCommandBuilder(context: Context) {
    var context = context
    val COMMAND = "command"
    val MESSAGE = "message"
    var message : String? = null
    var command : Command? = null

    enum class Command {
        START, STOP
    }

    constructor(intent: Intent, context:Context): this(context){
        message = intent.getStringExtra(MESSAGE)
        command = intent.getStringExtra(COMMAND)?.let {Command.valueOf(it)}
    }

    fun setMessage(message: String): ServiceCommandBuilder {
        this.message = message
        return this
    }

    fun setCommand(command: Command): ServiceCommandBuilder {
        this.command = command
        return this
    }

    fun build(): Intent {
        val intent = Intent(context, FoneClientService::class.java)
        if (command !== null) {
            intent.putExtra(COMMAND, command.toString())
        }
        if (message != null) {
            intent.putExtra(MESSAGE, message)
        }
        return intent
    }
}