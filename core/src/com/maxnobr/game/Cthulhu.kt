package com.maxnobr.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.physics.box2d.World

class Cthulhu :GameObject{
    private val FRAME_DURATION = .19f
    private lateinit var atlas: TextureAtlas
    private lateinit var sprite: Sprite

    private lateinit var flyingAnimation: Animation<TextureAtlas.AtlasRegion>

    private var elapsed_time = 0f

    private var scaleX = .7f
    private var scaleY = .9f

    override fun create(batch: SpriteBatch, camera: Camera, world: World) {
        atlas = TextureAtlas(Gdx.files.internal("NewNewCthulhu.atlas"))
        // Frames that compose the animation "running"
        val flyingFrames = atlas.findRegions("Cthulhu")
        flyingAnimation = Animation(FRAME_DURATION, flyingFrames, Animation.PlayMode.LOOP)

        val firstTexture = flyingFrames?.first()
        if(firstTexture != null) {
            sprite = Sprite(firstTexture)
            sprite.setOrigin(sprite.width/2,sprite.height/2)
            sprite.setScale(scaleX,scaleY)
            sprite.setCenter(-7f,camera.viewportHeight*0.6f)
        }
    }

    override fun preRender(camera: Camera) {
    }

    override fun render(batch: SpriteBatch, camera: Camera) {
        if(CthulhuGame.gameState == CthulhuGame.RUN) elapsed_time += Gdx.graphics.deltaTime
        sprite.setRegion(flyingAnimation.getKeyFrame(elapsed_time))
        sprite.draw(batch)
    }

    override fun dispose() {
        atlas.dispose()
    }

    override fun save(data: Persistence.GameData){}
    override fun load(data: Persistence.GameData){}
}