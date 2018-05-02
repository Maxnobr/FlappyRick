package com.maxnobr.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.*
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.World
import com.codeandweb.physicseditor.PhysicsShapeCache
import com.maxnobr.game.level.LevelBorders

class Saucer(private val game: CthulhuGame) : GameObject {

    private var interVec = Vector2()
    private val FRAME_DURATION = .4f
    private val invisibilityBlinkMax = 1f
    private var invisibilityBlink = 1f
    private var blinkTimer = 0f
    private var isBlinking = false
    private val invisibilityLength = 5f
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
    private var isInvinsible = false

    fun takeDamage(dam:Int){
        if(!isInvinsible) {
            isInvinsible = true
            health -= dam
            elapsed_time = 0f
            body.isActive = false
            interVec = body.position
            invisibilityBlink = invisibilityBlinkMax
            blinkTimer = 0f
        }
    }

    fun reset() {
        body.linearVelocity = Vector2()
        health = 4
    }

    fun jump() {
        if(!isInvinsible)
            body.linearVelocity = Vector2(0f,jump)
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
        body.fixtureList.forEach { it.filterData.categoryBits = LevelBorders.CATEGORY_PLAYER; it.filterData.maskBits = LevelBorders.MASK_PLAYER}
        body.userData = "player"
    }

    override fun preRender(camera:Camera) {
        //if(CthulhuGame.gameState == CthulhuGame.RUN) body.linearVelocity = Vector2(body.linearVelocity.x,body.linearVelocity.y - 1)

        if((CthulhuGame.gameState == CthulhuGame.RUN) and isInvinsible) {
            body.setTransform(interVec.interpolate(
                    Vector2((camera.viewportWidth - sprite.width * scaleX) / 2,
                            (camera.viewportHeight - sprite.height * scaleY) / 2),
                    Math.min(1f, elapsed_time / invisibilityLength), Interpolation.linear), 0f)

            if(health > 0){
                blinkTimer += Gdx.graphics.deltaTime
                if (blinkTimer  > invisibilityBlink) {
                    invisibilityBlink = invisibilityBlinkMax/(Math.max(Math.min(1f,elapsed_time/invisibilityLength),.1f)*10)
                    blinkTimer = 0f
                    isBlinking = !isBlinking
                }
                val a = if (isBlinking) .3f else .8f
                sprite.setColor(sprite.color.r,sprite.color.g,sprite.color.b,a)
            }

            if(elapsed_time > invisibilityLength) {
                sprite.setColor(sprite.color.r,sprite.color.g,sprite.color.b,1f)
                isInvinsible = false
                body.isActive = true
                body.linearVelocity = Vector2()
            }
        }
        sprite.setPosition(body.position.x,body.position.y)
        sprite.rotation = Math.toDegrees(body.angle.toDouble()).toFloat()
    }

    override fun render(batch: SpriteBatch,camera:Camera) {
        if(CthulhuGame.gameState == CthulhuGame.RUN) {
            elapsed_time += Gdx.graphics.deltaTime
        }

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

        if(CthulhuGame.gameState == CthulhuGame.RUN && health <= 0 && crashAnimation?.isAnimationFinished(elapsed_time)!!)
            game.changeGameState(CthulhuGame.GAMEOVER)
        //batch.draw(currentFrame, originX, originY, width, height)
    }

    override fun dispose() {
        atlas?.dispose()
    }

    override fun save(data: Persistence.GameData){
        data.playerPosition = body.position
        data.playerHealth = health

        data.playerInterVec = interVec
        data.playerInvisibilityBlink = invisibilityBlink
        data.playerBlinkTimer = blinkTimer
        data.playerIsBlinking = isBlinking
        data.playerElapsed_time = elapsed_time

        data.playerBodyIsActive = body.isActive
        data.playerBodyVelocity = body.linearVelocity

        data.playerIsInvinsible = isInvinsible
    }

    override fun load(data: Persistence.GameData){
        body.setTransform(data.playerPosition,0f)
        health = data.playerHealth

        interVec = data.playerInterVec
        invisibilityBlink = data.playerInvisibilityBlink
        blinkTimer = data.playerBlinkTimer
        isBlinking = data.playerIsBlinking
        elapsed_time = data.playerElapsed_time

        body.isActive = data.playerBodyIsActive
        body.linearVelocity = data.playerBodyVelocity

        isInvinsible = data.playerIsInvinsible
    }
}