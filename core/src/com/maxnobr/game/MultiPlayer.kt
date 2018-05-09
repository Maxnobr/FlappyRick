package com.maxnobr.game

import com.badlogic.gdx.Gdx
import java.util.*

class MultiPlayer(var game:CthulhuGame, private var bluetooth: Bluetooth){

    companion object {
        const val STATE_NONE = 0       // we're doing nothing
        const val STATE_LISTEN = 1     // now listening for incoming connections
        const val STATE_CONNECTING = 2 // now initiating an outgoing connection
        const val STATE_CONNECTED = 3  // now connected to a remote device

        var connectedName:String? = ""
        var myName:String? = ""
        var state = STATE_NONE

        const val SAUCER = 0
        const val CTHULHU = 1
        const val LEVEL = 2
        const val GAME = 3


        private const val TAG = "MultiPlayer"
    }

    init{
        bluetooth.setMulti(this)
    }

    fun isConnected():Boolean{
        return state == STATE_CONNECTED
    }

    fun handshake(deviceName:String){
        connectedName = deviceName
        if(CthulhuGame.gameState == CthulhuGame.START) {
            CthulhuGame.saveName = connectedName!!
            game.receive(CthulhuGame.GUI,"")
            //game.changeGameState(CthulhuGame.RUN)
        }
        Gdx.app.log(TAG,"HANDSHAKE got name = '$connectedName")
    }

    fun changeStatus(status:Int){
        val oldStatus = state
        state = status
        if(CthulhuGame.gameState == CthulhuGame.RUN && oldStatus == STATE_CONNECTED && status == STATE_NONE) {
            game.changeGameState(CthulhuGame.START)
            CthulhuGame.saveName = CthulhuGame.SINGLEGAMENAME
            CthulhuGame.gamer = CthulhuGame.ISPLAYER
        }
    }

    fun send(code:Int,msg:String) {
        bluetooth.sendMessage("$code\n$msg\n")
    }

    fun send(code:Int,msg:Int) {
        send(code,msg.toString())
    }

    fun receive(msg:String){
        //Gdx.app.log(TAG,"received: $msg")
        try {
            val lines = ArrayDeque(msg.split("\n".toRegex()))
            when (lines.pollFirst().toInt()) {
                SAUCER -> game.receive(CthulhuGame.PLAYER, lines.pollFirst())
                CTHULHU -> game.receive(CthulhuGame.CTHULHU, lines.pollFirst())
                LEVEL -> game.receive(CthulhuGame.OBSTACLES, lines.pollFirst())
                GAME -> game.receive(lines.pollFirst())
            }
        }
        catch (e:Exception){
            Gdx.app.log("MultiPlayer","CORRUPTED DATA NOT PARSED")
        }
    }
}