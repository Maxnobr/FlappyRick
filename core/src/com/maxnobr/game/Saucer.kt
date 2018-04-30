package com.maxnobr.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.g2d.*
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.World
import com.codeandweb.physicseditor.PhysicsShapeCache

class Saucer : GameObject {

    private val FRAME_DURATION = .4f
    private var atlas: TextureAtlas? = null

    private lateinit var currentFrame: TextureRegion
    private lateinit var sprite: Sprite

    // Running animation
    private var flyingAnimation: Animation<TextureAtlas.AtlasRegion>? = null
    private var dam1Animation: Animation<TextureAtlas.AtlasRegion>? = null
    private var dam2Animation: Animation<TextureAtlas.AtlasRegion>? = null
    private var dam3Animation: Animation<TextureAtlas.AtlasRegion>? = null
    private var crashAnimation: Animation<TextureAtlas.AtlasRegion>? = null

    // Elapsed time
    private var elapsed_time = 0f

    private var scaleX = .2f
    private var scaleY = .2f

    private var health = 4

    private lateinit var physicsBodies:PhysicsShapeCache
    private lateinit var body: Body
    private var gravityScale = 20f
    private val jump = 60f

    fun takeDamage(dam:Int)
    {
        health -= dam
        if(health <1)
            elapsed_time = 0f
    }

    fun reset()
    {
        body.linearVelocity = Vector2()
    }

    fun jump()
    {
        //body.applyLinearImpulse(0f, 1000f,0f, 0f, true)
        body.linearVelocity = Vector2(0f,jump)
        //sprite.translateY(2f)
    }


    private fun setPosition(pos:Vector2) {
        body.setTransform(pos.x - sprite.width/2,pos.y - sprite.height/2,0f)
    }

    fun setPosition(pos: Vector3) {
        setPosition(Vector2(pos.x,pos.y))
    }

    override fun create(batch: SpriteBatch,camera: Camera,world:World) {
        // Frames loading from "charset.atlas"
        //Gdx.app.log("tag","creating Saucer")
        atlas = TextureAtlas(Gdx.files.internal("Saucer.atlas"))
        // Frames that compose the animation "running"
        val flyingFrames = atlas?.findRegions("Saucer")
        val dam1Frames = atlas?.findRegions("SaucerDam1")
        val dam2Frames = atlas?.findRegions("SaucerDam2")
        val dam3Frames = atlas?.findRegions("SaucerDam3")
        val crashFrames = atlas?.findRegions("SaucerCrash")

        // Building the animation
        flyingAnimation = Animation(FRAME_DURATION, flyingFrames, PlayMode.LOOP)
        dam1Animation = Animation(FRAME_DURATION, dam1Frames, PlayMode.LOOP)
        dam2Animation = Animation(FRAME_DURATION, dam2Frames, PlayMode.LOOP)
        dam3Animation = Animation(FRAME_DURATION, dam3Frames, PlayMode.LOOP)
        crashAnimation = Animation(FRAME_DURATION, crashFrames, PlayMode.NORMAL)

        // Calculates the x and y position to center the image
        val firstTexture = flyingFrames?.first()
        if(firstTexture != null) {
            sprite = Sprite(firstTexture)
            sprite.setOrigin(0f,0f)
            sprite.setScale(scaleX,scaleY)
        }

        physicsBodies = PhysicsShapeCache("SaucerPhysics.xml")
        body = physicsBodies.createBody("Saucer_0", world, sprite.scaleX,sprite.scaleY)
        body.setTransform(sprite.originX,sprite.originY,sprite.rotation)
        body.gravityScale = gravityScale
    }

    override fun preRender(camera:Camera) {
        //if(CthulhuGame.gameState == CthulhuGame.RUN) body.linearVelocity = Vector2(body.linearVelocity.x,body.linearVelocity.y - 1)
        sprite.setPosition(body.position.x,body.position.y)
        sprite.rotation = Math.toDegrees(body.angle.toDouble()).toFloat()
    }

    override fun render(batch: SpriteBatch,camera:Camera) {
        //Gdx.app.log("tag","rendering Saucer")
        // Elapsed time
        if(CthulhuGame.gameState == CthulhuGame.RUN) elapsed_time += Gdx.graphics.deltaTime
        //elapsed_time += Gdx.graphics.deltaTime

        // Getting the frame which must be rendered
        currentFrame = when(health)
        {
            4 -> flyingAnimation?.getKeyFrame(elapsed_time)!!
            3 -> dam1Animation?.getKeyFrame(elapsed_time)!!
            2 -> dam2Animation?.getKeyFrame(elapsed_time)!!
            1 -> dam3Animation?.getKeyFrame(elapsed_time)!!
            else -> crashAnimation?.getKeyFrame(elapsed_time)!!
        }
        //currentFrame = flyingAnimation?.getKeyFrame(elapsed_time)!!

        // Drawing the frame
        //Gdx.app.log("tag","originX :$originX, originY :$originY")
        sprite.setRegion(currentFrame)
        sprite.draw(batch)
        //batch.draw(currentFrame, originX, originY, width, height)
    }
    override fun dispose() {
        atlas?.dispose()
    }
}