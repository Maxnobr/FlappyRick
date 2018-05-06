package com.maxnobr.game

class MultiPlayer(var game:CthulhuGame,var bluetooth: Bluetooth){

    fun send(msg:String) {
        bluetooth.sendMessage(msg)
    }

    fun receive(msg:String){

    }
}