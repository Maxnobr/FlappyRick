package com.maxnobr.game.level

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.World
import com.codeandweb.physicseditor.PhysicsShapeCache
import com.maxnobr.game.GameObject
import java.util.*

class ObstaclePrefab(private var atlas: TextureAtlas,private var physicsBodies: PhysicsShapeCache) : GameObject {

    private var sprites = HashMap<String,Sprite>()
    private var bodies = HashMap<String,ArrayDeque<Body>>()
    private lateinit var world:World

    val scaleX = 0.7f
    val scaleY = 0.7f

    override fun create(batch: SpriteBatch, camera: Camera, prmWorld: World) {

        world = prmWorld
        sprites["bottomV2"] = Sprite(atlas.findRegion("bottomV2"))
        sprites["topV2"] = Sprite(atlas.findRegion("topV2"))

        sprites.iterator().forEach {
            it.value.setPosition(-100f,-100f)
            it.value.setOrigin(0f,0f)
            it.value.setScale(scaleX,scaleY)
        }
        bodies["bottomV2"] = ArrayDeque()
        bodies["topV2"] = ArrayDeque()
    }

    override fun preRender(camera: Camera) {

    }

    override fun render(batch: SpriteBatch, camera: Camera) {
    }

    fun draw(str:String,pos: Vector2,body: Body,batch: SpriteBatch,isTop:Boolean) {

        sprites[str]?.setCenterX(pos.x)
        sprites[str]?.y = pos.y
        if(!isTop)
            sprites[str]?.translateY(-sprites[str]!!.height*scaleY)

        body.setTransform(sprites[str]!!.x,sprites[str]!!.y,0f)
        sprites[str]?.draw(batch)
    }

    fun findBody(name:String) : Body{

        if(bodies[name]!!.isEmpty()) {
            bodies[name]?.add(physicsBodies.createBody(name, world, sprites[name]!!.scaleX, sprites[name]!!.scaleY))
            bodies[name]?.peekFirst()!!.setTransform(sprites[name]!!.originX, sprites[name]!!.originY, sprites[name]!!.rotation)
        }

        return bodies[name]!!.poll()
    }

    fun loseBody(name:String,body: Body) {
        bodies[name]?.add(body)
    }

    override fun dispose() {
    }
}