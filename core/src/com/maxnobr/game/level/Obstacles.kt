package com.maxnobr.game.level

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.codeandweb.physicseditor.PhysicsShapeCache
import com.maxnobr.game.GameObject
import java.util.*

class Obstacles : GameObject {

    private var atlas: TextureAtlas? = null
    private lateinit var physicsBodies:PhysicsShapeCache
    private lateinit var prefab:ObstaclePrefab
    private val rnd = Random()

    private var gatePadding = 4f
    private var speed = -.3f

    private var queue:ArrayDeque<Gate> = ArrayDeque()

    override fun create(batch: SpriteBatch, camera: Camera, world: World) {
        atlas = TextureAtlas(Gdx.files.internal("Obstacles.atlas"))
        physicsBodies = PhysicsShapeCache("Obstacles.xml")
        prefab = ObstaclePrefab(atlas!!,physicsBodies)
        prefab.create(batch,camera,world)
        gatePadding = camera.viewportWidth/3
    }

    override fun preRender(camera: Camera) {
        queue.forEach {
            it.pos.x += speed
            it.preRender(camera)
        }

        if(queue.isEmpty()) {
            queue.add(Gate(Vector2(camera.viewportWidth+gatePadding,getNewY(camera.viewportHeight)),prefab))
        }
        else{
            if (queue.peekLast().pos.x < camera.viewportWidth)
                queue.add(Gate(Vector2(queue.peekLast().pos.x + gatePadding, getNewY(camera.viewportHeight)), prefab))

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
        prefab.dispose()
    }



    class Gate(var pos:Vector2, private val prefab: ObstaclePrefab): GameObject {

        private var gap = 7f

        private var bottomPos = Vector2(0f,0f)
        private var bottomName = "bottomV2"

        private var topPos = Vector2(0f,0f)
        private var topName = "topV2"

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

    }
}