package com.maxnobr.game.level

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.codeandweb.physicseditor.PhysicsShapeCache
import com.maxnobr.game.GameObject
import com.maxnobr.game.Persistence
import java.util.*

class Obstacles : GameObject {

    private var atlas: TextureAtlas? = null
    private lateinit var physicsBodies:PhysicsShapeCache
    private lateinit var prefab:ObstaclePrefab
    private val rnd = Random()

    private var gatePadding = 4f
    private var speed = -.3f
    private val bottomName = "bottomV2"
    private val topName = "topV2"
    private var gap = 7f

    private var queue:ArrayDeque<Gate> = ArrayDeque()

    override fun create(batch: SpriteBatch, camera: Camera, world: World) {
        atlas = TextureAtlas(Gdx.files.internal("Obstacles.atlas"))
        physicsBodies = PhysicsShapeCache("Obstacles.xml")
        prefab = ObstaclePrefab(atlas!!,physicsBodies,world)
        gatePadding = camera.viewportWidth/3
        gap = camera.viewportHeight/7
    }

    override fun preRender(camera: Camera) {
        queue.forEach {
            it.pos.x += speed
            it.preRender(camera)
        }

        if(queue.isEmpty()) {
            queue.add(Gate(Vector2(camera.viewportWidth+gatePadding,getNewY(camera.viewportHeight)),gap,bottomName,topName,prefab))
        }
        else{
            if (queue.peekLast().pos.x < camera.viewportWidth)
                queue.add(Gate(Vector2(queue.peekLast().pos.x + gatePadding, getNewY(camera.viewportHeight)),gap,bottomName,topName, prefab))

            if (queue.peekFirst().pos.x < -20) {
                queue.peekFirst().dispose()
                queue.removeFirst()
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

    override fun save(data: Persistence.GameData){
        data.queue.clear()
        queue.forEach{ it.save(data) }
    }
    override fun load(data: Persistence.GameData){
        queue.clear()
        data.queue.forEach {
            queue.add(Gate(it.pos,it.gap,it.nameBot,it.nameTop,prefab))
        }
    }

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
        override fun load(data: Persistence.GameData){
        }
    }
}