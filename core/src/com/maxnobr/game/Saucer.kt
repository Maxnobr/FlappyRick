package com.maxnobr.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.g2d.*
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3

class Saucer : GameObject {

    // Time while each frame keeps on screen
    private val FRAME_DURATION = .4f

    // Atlas with the definition of the frames "charset.atlas"
    private var atlas: TextureAtlas? = null

    // Frame that must be rendered at each time
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

    // Auxiliar variables to know where to draw the picture to center it on the screen
    private var originX: Float = 0f
    private var originY: Float = 0f
    private var width = 0f
    private var height = 0f

    private var scaleX = .5f
    private var scaleY = .5f

    private var health = 4

    fun takeDamage(dam:Int)
    {
        health -= dam
        if(health <1)
            elapsed_time = 0f
    }

    fun jump()
    {
        sprite.translateY(2f)
    }

    private fun setPosition(pos:Vector2) {
        sprite.setCenter(pos.x,pos.y)
        //originX = pos.x-width/2
        //originY = pos.y - height/2
    }

    fun setPosition(pos: Vector3) {
        setPosition(Vector2(pos.x,pos.y))
    }

    override fun create(batch: SpriteBatch,camera: Camera) {
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
            //originX = 0f//(Gdx.graphics.width.toFloat() - firstTexture.regionWidth) / 2
            //originY = 0f//(Gdx.graphics.height.toFloat() - firstTexture.regionHeight) / 2
            //width = firstTexture.regionWidth.toFloat() *scaleX
            //height = firstTexture.regionHeight.toFloat() *scaleY
        }
    }

    override fun preRender(camera:Camera) {
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