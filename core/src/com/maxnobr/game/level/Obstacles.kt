package com.maxnobr.game.level

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.World
import com.codeandweb.physicseditor.PhysicsShapeCache
import com.maxnobr.game.CthulhuGame
import com.maxnobr.game.GameObject
import com.maxnobr.game.MultiPlayer
import com.maxnobr.game.Persistence
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque

class Obstacles(private var game: CthulhuGame) : GameObject {
    private var atlas: TextureAtlas? = null
    private lateinit var physicsBodies:PhysicsShapeCache
    private lateinit var prefab:ObstaclePrefab
    private val rnd = Random()

    private var gatePadding = 25f
    private var speed = -.3f
    private val bottomName = "bottomV2"
    private val topName = "topV2"
    private var gap = 7f


    //private var updateMsg = ""
    //private var screenHeight = 480f

    //private var queue:ArrayDeque<Gate> = ArrayDeque()
    private var queue:ConcurrentLinkedDeque<Gate> = ConcurrentLinkedDeque()

    override fun create(batch: SpriteBatch, camera: Camera, world: World) {
        atlas = TextureAtlas(Gdx.files.internal("Obstacles.atlas"))
        physicsBodies = PhysicsShapeCache("Obstacles.xml")
        prefab = ObstaclePrefab(atlas!!,physicsBodies,world)
        //gatePadding = camera.viewportWidth/3
        //gap = camera.viewportHeight/gap
        //screenHeight = camera.viewportHeight
    }

    override fun preRender(camera: Camera) {
        queue.forEach {
            it.pos.x += speed
            it.preRender(camera)
        }

        //readUpdateMsg()
        if(queue.isEmpty()) {
            if(CthulhuGame.gamer == CthulhuGame.ISPLAYER)
                createGate(Vector2(camera.viewportWidth+gatePadding,getNewY(camera.viewportHeight)),gap,bottomName,topName)
        }
        else{
            if (queue.peekLast().pos.x < camera.viewportWidth && CthulhuGame.gamer == CthulhuGame.ISPLAYER)
                createGate(Vector2(queue.peekLast().pos.x + gatePadding, getNewY(camera.viewportHeight)),gap,bottomName,topName)

            if (queue.peekFirst().pos.x < -20) {
                queue.pollFirst().dispose()
            }
        }
    }

    fun reset() {
        queue.forEach { it.dispose() }
        queue.clear()
    }

    private fun getNewY(height:Float) : Float {
        return rnd.nextFloat()*height/10*7+height/10*2
    }

    override fun render(batch: SpriteBatch, camera: Camera) {
        queue.forEach { it.render(batch,camera) }
    }

    override fun dispose() {
        atlas?.dispose()
        physicsBodies.dispose()
    }

    @Synchronized private fun createGate(pos:Vector2, gap:Float, bottomName:String, topName:String){
            queue.add(Gate(pos, gap, bottomName, topName, prefab))
        if(CthulhuGame.gamer == CthulhuGame.ISPLAYER)
            game.multiPlayer.send(MultiPlayer.LEVEL, "${pos.x} ${pos.y} $gap $bottomName $topName")
    }

    override fun save(data: Persistence.GameData){
        data.queue.clear()
        queue.forEach{ it.save(data) }
    }
    override fun load(data: Persistence.GameData){
        queue.clear()
        data.queue.forEach {
            createGate(it.pos,it.gap,it.nameBot,it.nameTop)
        }
    }

    override fun send(mPlayer: MultiPlayer) {}
    override fun receive(msg: String) {
        if(CthulhuGame.gamer == CthulhuGame.ISMONSTER){
            val words = ArrayDeque(msg.split(" ".toRegex()))
            createGate(Vector2(words.pollFirst().toFloat(), words.pollFirst().toFloat()), words.pollFirst().toFloat(), words.pollFirst(), words.pollFirst())
            //updateMsg = msg
        }
    }

    /*private fun readUpdateMsg(){
        if(updateMsg.isNotBlank()) {
            val words = ArrayDeque(updateMsg.split(" ".toRegex()))
            createGate(Vector2(words.pollFirst().toFloat(), words.pollFirst().toFloat()), words.pollFirst().toFloat(), words.pollFirst(), words.pollFirst())
            updateMsg = ""
        }
    }*/

    class Gate(var pos:Vector2,private var gap:Float, private val bottomName:String, private val topName:String, private val prefab: ObstaclePrefab): GameObject {

        private var bottomPos = Vector2(0f,0f)
        private var topPos = Vector2(0f,0f)

        private var bottomBody: Body
        private var topBody: Body

        init {
            bottomPos.y =  pos.y - gap/2
            topPos.y =  pos.y + gap/2

            bottomBody = prefab.findBody(bottomName)
            topBody = prefab.findBody(topName)
        }

        override fun create(batch: SpriteBatch, camera: Camera, world: World) {
        }

        override fun preRender(camera: Camera) {
            bottomPos.x = pos.x
            topPos.x = pos.x
        }

        override fun render(batch: SpriteBatch, camera: Camera) {
            prefab.draw(bottomName,bottomPos,bottomBody,batch,false)
            prefab.draw(topName,topPos,topBody,batch,true)
        }

        override fun dispose() {
            prefab.loseBody(bottomName,bottomBody)
            prefab.loseBody(topName,topBody)
        }

        override fun save(data: Persistence.GameData){
            data.saveGate(pos,gap,bottomName,topName)
        }
        override fun load(data: Persistence.GameData){}

        override fun send(mPlayer: MultiPlayer) {}

        override fun receive(msg: String) {}
    }
}