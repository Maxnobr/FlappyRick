package com.maxnobr.game

interface Bluetooth {

    fun logBlue(msg: String)
    fun setMulti(multi:MultiPlayer)
    fun canBlue():Boolean
    fun sendMessage(message: String)
    fun receiveBtn(id : Int)
    fun stop()
}