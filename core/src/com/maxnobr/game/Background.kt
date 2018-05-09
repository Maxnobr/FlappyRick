package com.maxnobr.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.physics.box2d.World

class Background : GameObject  {

    private var atlas: TextureAtlas? = null
    private lateinit var bg0:Sprite
    private lateinit var bg1:Sprite
    private lateinit var bg2:Sprite
    private lateinit var bg3:Sprite

    private var speed3 = -1f
    private var speed2 = -4f
    private var speed1 = -7f

    override fun create(batch: SpriteBatch,camera: Camera,world: World) {
        atlas = TextureAtlas(Gdx.files.internal("TinyCave.atlas"))
        bg3 = Sprite(atlas?.findRegions("TinyCave")?.get(1))
        bg2 = Sprite(atlas?.findRegions("TinyCave")?.get(2))
        bg1 = Sprite(atlas?.findRegions("TinyCave")?.get(3))
        bg0 = Sprite(atlas?.findRegions("TinyCave")?.get(0))

        val scaleX = camera.viewportWidth/bg3.regionWidth.toFloat()
        val scaleY = camera.viewportHeight/bg3.regionHeight.toFloat()

        bg3.setOrigin(0f,0f)
        bg2.setOrigin(0f,0f)
        bg1.setOrigin(0f,0f)
        bg0.setOrigin(0f,0f)

        bg3.setScale(scaleX,scaleY)
        bg2.setScale(scaleX,scaleY)
        bg1.setScale(scaleX,scaleY)
        bg0.setScale(scaleX,scaleY)
    }

    override fun preRender(camera:Camera) {
        if(CthulhuGame.gameState == CthulhuGame.RUN)
        {
            bg3.translateX(Gdx.graphics.deltaTime * speed3)
            bg2.translateX(Gdx.graphics.deltaTime * speed2)
            bg1.translateX(Gdx.graphics.deltaTime * speed1)

            if (bg3.x < -camera.viewportWidth) bg3.x = 0f
            if (bg2.x < -camera.viewportWidth) bg2.x = 0f
            if (bg1.x < -camera.viewportWidth) bg1.x = 0f
        }
    }

    override fun render(batch: SpriteBatch,camera: Camera) {
        bg0.draw(batch)

        bg3.draw(batch)
        if(bg3.x < 0) {
            bg3.translateX(camera.viewportWidth-0.1f)
            bg3.draw(batch)
            bg3.translateX(-camera.viewportWidth+0.1f)
        }

        bg2.draw(batch)
        if(bg2.x < 0) {
            bg2.translateX(camera.viewportWidth-0.1f)
            bg2.draw(batch)
            bg2.translateX(-camera.viewportWidth+0.1f)
        }

        bg1.draw(batch)
        if(bg1.x < 0) {
            bg1.translateX(camera.viewportWidth-0.1f)
            bg1.draw(batch)
            bg1.translateX(-camera.viewportWidth+0.1f)
        }
    }

    override fun dispose() {
        atlas?.dispose()
    }

    override fun save(data: Persistence.GameData){}
    override fun load(data: Persistence.GameData){}
    override fun send(mPlayer: MultiPlayer){}
    override fun receive(msg: String){}
}