package com.maxnobr.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.maxnobr.game.level.LevelBorders
import java.util.*

class Cthulhu(private var game: CthulhuGame) :GameObject{

    private val FRAME_DURATION = .19f
    private val PUNCH_DURATION = .6f
    private lateinit var atlas: TextureAtlas

    private lateinit var bodySprite: Sprite
    private lateinit var wingSprite: Sprite
    private lateinit var armSprite: Sprite
    private lateinit var punchSprite: Sprite

    private var target = Vector2()
    private var originalTarget = Vector2()
    private var armLength = 17f

    private var isPunching = false

    private var wingOffset = Vector2(22f,-29f)
    private var armOffset = Vector2(51f,-12f)
    private var armpos = Vector2()


    private lateinit var bodyAnimation: Animation<TextureAtlas.AtlasRegion>
    private lateinit var wingAnimation: Animation<TextureAtlas.AtlasRegion>
    private lateinit var armAnimation: Animation<TextureAtlas.AtlasRegion>
    private lateinit var punchAnimation: Animation<TextureAtlas.AtlasRegion>

    private lateinit var punchBody:Body

    private var elapsed_time = 0f
    private var punch_time = 100f

    private var scaleX = .7f
    private var scaleY = .9f

    override fun create(batch: SpriteBatch, camera: Camera, world: World) {
        atlas = TextureAtlas(Gdx.files.internal("SashaCthulhu.atlas"))
        val bodyFrames = atlas.findRegions("Cthulhu_Body")
        bodyAnimation = Animation(FRAME_DURATION, bodyFrames, Animation.PlayMode.LOOP)

        val wingFrames = atlas.findRegions("Cthulhu_Wing")
        wingAnimation = Animation(FRAME_DURATION, wingFrames, Animation.PlayMode.LOOP)

        val armFrames = atlas.findRegions("Cthulhu_Arm")
        armAnimation = Animation(FRAME_DURATION, armFrames, Animation.PlayMode.LOOP)

        val punchFrames = atlas.findRegions("Cthulhu_Punch")
        punchAnimation = Animation(PUNCH_DURATION, punchFrames, Animation.PlayMode.NORMAL)

        bodySprite = Sprite(bodyFrames?.first())
        bodySprite.setOrigin(0f,0f)
        bodySprite.setScale(scaleX,scaleY)
        bodySprite.setPosition(-50f,0f)


        wingSprite = Sprite(wingFrames?.first())
        wingSprite.setOrigin(0f,0f)
        wingSprite.setScale(scaleX,scaleY)

        armSprite = Sprite(armFrames?.first())
        armSprite.setOrigin(0f,0f)
        armSprite.setScale(scaleX,scaleY)

        punchSprite = Sprite(punchFrames?.first())
        punchSprite.setScale(scaleX,scaleY)
        punchSprite.setOrigin(22f,43f)

        //physics body
        punchBody = world.createBody(BodyDef())

        val punchCircle = CircleShape()
        punchCircle.radius = 5f

        val punchFixDef = FixtureDef()
        punchFixDef.shape = punchCircle
        punchFixDef.filter.categoryBits = LevelBorders.CATEGORY_TRIGGERS
        punchFixDef.filter.maskBits = LevelBorders.MASK_TRIGGERS

        punchBody.createFixture(punchFixDef)
        punchBody.userData = "death"
        punchCircle.dispose()
        punchBody.isActive = false
    }

    override fun preRender(camera: Camera) {
        if(CthulhuGame.gameState == CthulhuGame.RUN) {
            elapsed_time += Gdx.graphics.deltaTime
            punch_time += Gdx.graphics.deltaTime
            if(isPunching)
                if (punchAnimation.isAnimationFinished(punch_time))
                    isPunching = false
                else {
                    aimPunch()
                    punchBody.setTransform(target, 0f)
                    punchBody.isActive = punchAnimation.getKeyFrameIndex(punch_time) == 2
                }
        }

        bodySprite.setRegion(bodyAnimation.getKeyFrame(elapsed_time))


        val vec = when(armAnimation.getKeyFrameIndex(elapsed_time)){
            0->Vector2(23f,43f)
            1->Vector2(21f,40f)
            2->Vector2(15f,45f)
            3->Vector2(25f,37f)
            4->Vector2(23f,39f)
            5->Vector2(18f,38f)
            6->Vector2(24f,35f)
            7->Vector2(28f,33f)
            else -> Vector2()
        }
        armSprite.setPosition(bodySprite.x+armOffset.x*scaleX,bodySprite.y+armOffset.y*scaleY)
        armpos.set(armSprite.x+vec.x,armSprite.y+vec.y)
        armSprite.setRegion(armAnimation.getKeyFrame(elapsed_time))

        punchSprite.setRegion(punchAnimation.getKeyFrame(punch_time))
        punchSprite.x = bodySprite.x+armOffset.x*scaleX-(1-scaleX)*punchSprite.originX+(vec.x-punchSprite.originX)
        punchSprite.y = bodySprite.y+armOffset.y*scaleY-(1-scaleY)*punchSprite.originY+(vec.y-punchSprite.originY)

        wingSprite.setRegion(wingAnimation.getKeyFrame(elapsed_time))
        wingSprite.setPosition(bodySprite.x+wingOffset.x*scaleX,bodySprite.y+wingOffset.y*scaleY)
    }

    override fun render(batch: SpriteBatch, camera: Camera) {
        bodySprite.draw(batch)

        if(!isPunching)
            armSprite.draw(batch)
        else
            punchSprite.draw(batch)
        wingSprite.draw(batch)
    }

    @Synchronized fun punch(pos:Vector2){
        originalTarget = Vector2(pos).sub(armpos).clamp(armLength*1.2f, Float.POSITIVE_INFINITY).add(armpos)
        val angle = Vector2(pos).sub(armpos).angle()
        if(!isPunching && ( angle < 100 || angle > 260)) {
            isPunching = true
            aimPunch()
            punch_time = 0f
        }
        if(CthulhuGame.gamer == CthulhuGame.ISMONSTER)
            game.multiPlayer.send(MultiPlayer.CTHULHU,"${pos.x} ${pos.y}")
    }

    private fun aimPunch() {
        target = Vector2(originalTarget).sub(armpos).setLength(armLength).add(armpos)
        val angle = Vector2(originalTarget).sub(armpos).angle()
        punchSprite.rotation = angle
        val inter = Interpolation.smooth.apply(punchSprite.rotation+360f,angle+360f,Math.min(punch_time/(PUNCH_DURATION*4),1f))-360f
        punchSprite.rotation = inter%360
    }

    override fun dispose() {
        atlas.dispose()
    }

    override fun save(data: Persistence.GameData){}
    override fun load(data: Persistence.GameData){}
    override fun send(mPlayer: MultiPlayer) {}
    override fun receive(msg: String) {
        if(CthulhuGame.gamer == CthulhuGame.ISPLAYER)
        {
            val words = ArrayDeque(msg.split(" ".toRegex()))
            punch(Vector2(words.pollFirst().toFloat(),words.pollFirst().toFloat()))
        }
    }
}