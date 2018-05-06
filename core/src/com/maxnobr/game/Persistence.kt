package com.maxnobr.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2
import java.util.*
import kotlin.collections.HashMap
import kotlin.concurrent.thread

class Persistence {

    private var file = Gdx.files.local(CthulhuGame.FILE)
    var saves = HashMap<String,GameData>()
    var processing = true
    var hasData = false

    init {
        if(file.exists())
            thread {
                Gdx.app.log("CRUD","Reading File")
                Gdx.app.log("CRUD",file.readString())
                val lines = ArrayDeque(file.readString().split("\n".toRegex()))

                var gamesSaved = 0
                try {
                    gamesSaved  = lines.pollFirst().toInt()
                }
                catch (e : NumberFormatException) { }
                hasData = gamesSaved > 0

                for(i in 1..gamesSaved){
                    var data:GameData? = null
                    try {
                        data = GameData(lines.pollFirst())
                        data.read(lines)
                        saves[data.saveName] = data
                    }
                    catch (e:NumberFormatException) {
                        Gdx.app.log("CRUD","corrupted data: '${data?.saveName}' not loaded")
                    }
                }
                Gdx.app.log("CRUD","finished reading data !")
                processing = false
            }
        else {
            Gdx.app.log("CRUD", "file DOESN'T EXIST")
            processing = false
        }

    }

    fun save(saveName:String,list:LinkedHashMap<String,GameObject>) {
        if(saves[saveName] == null)
            saves[saveName] = GameData(saveName)
        Gdx.app.log("CRUD","saving data to GameData")
        list.forEach { it.value.save(saves[saveName]!!)}
        Gdx.app.log("CRUD","finished saving data to GameData")
        writeAll()
    }

    private fun writeAll(){
        thread {
            processing = true
            Gdx.app.log("CRUD","writing data")

            file.writeString("${saves.size}\n",false)

            saves.forEach {
                it.value.write()
                file.writeString("\n",true)
            }
            hasData = saves.size > 0
            processing = false
            Gdx.app.log("CRUD","finished writing data")
            Gdx.app.log("CRUD","data : \n${file.readString()}")
        }
    }

    fun load(saveName:String,list:LinkedHashMap<String,GameObject>) {
        Gdx.app.log("CRUD","trying to load $saveName")
        if(saves[saveName] != null) {
            list.forEach { it.value.load(saves[saveName]!!) }
            Gdx.app.log("CRUD","loaded GameData")

        }
        else
            Gdx.app.log("CRUD","no data here..")

    }

    fun delete(saveName:String) {
        saves.remove(saveName)
        writeAll()
    }

    inner class GameData(var saveName:String){

        var playerPosition = Vector2()
        var playerHealth = 0

        var playerInterVec = Vector2()
        var playerInvisibilityBlink = 1f
        var playerBlinkTimer = 0f
        var playerIsBlinking = false
        var playerElapsed_time = 0f

        var playerBodyIsActive = true
        var playerBodyVelocity = Vector2()

        var playerIsInvinsible = false

        var queue:ArrayDeque<GateData> = ArrayDeque()

        fun saveGate(pos:Vector2,gap:Float,nameBot:String,nameTop:String) {
            queue.add(GateData(pos,gap,nameBot,nameTop))
        }

        inner class GateData(var pos:Vector2,var gap:Float,var nameBot:String,var nameTop:String)

        fun read(lines:ArrayDeque<String>) {
            val line =  ArrayDeque(lines.pollFirst().split(" ".toRegex()))
            playerPosition = Vector2(line.pollFirst().toFloat(),line.pollFirst().toFloat())
            playerHealth = line.pollFirst().toInt()

            playerInterVec = Vector2(line.pollFirst().toFloat(),line.pollFirst().toFloat())
            playerInvisibilityBlink = line.pollFirst().toFloat()
            playerBlinkTimer = line.pollFirst().toFloat()
            playerIsBlinking = line.pollFirst()!!.toBoolean()
            playerElapsed_time = line.pollFirst().toFloat()

            playerBodyIsActive = line.pollFirst()!!.toBoolean()
            playerBodyVelocity = Vector2(line.pollFirst().toFloat(),line.pollFirst().toFloat())

            playerIsInvinsible = line.pollFirst()!!.toBoolean()


            queue.clear()
            for (i in 1..lines.pollFirst().toInt()) {
                val words = ArrayDeque(lines.pollFirst().split(" ".toRegex()))
                queue.add(GateData(Vector2(
                        words.pollFirst().toFloat(),
                        words.pollFirst().toFloat()),
                        words.pollFirst().toFloat(),
                        words.pollFirst().toString(),
                        words.pollFirst().toString()))
            }
        }

        fun write(){
            file.writeString("$saveName\n",true)
            file.writeString("${playerPosition.x} ${playerPosition.y} ",true)
            file.writeString("$playerHealth ",true)

            file.writeString("${playerInterVec.x} ${playerInterVec.y} ",true)
            file.writeString("$playerInvisibilityBlink ",true)
            file.writeString("$playerBlinkTimer ",true)
            file.writeString("$playerIsBlinking ",true)
            file.writeString("$playerElapsed_time ",true)

            file.writeString("$playerBodyIsActive ",true)
            file.writeString("${playerBodyVelocity.x} ${playerBodyVelocity.y} ",true)

            file.writeString("$playerIsInvinsible\n",true)

            file.writeString("${queue.size}\n",true)

            queue.forEach {
                file.writeString("${it.pos.x} ${it.pos.y} ",true)
                file.writeString("${it.gap} ",true)
                file.writeString("${it.nameBot} ",true)
                file.writeString("${it.nameTop}",true)
                file.writeString("\n",true)
            }
        }
    }
}