package com.maxnobr.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas

class Background : GameObject  {

    private var atlas: TextureAtlas? = null
    private lateinit var bg1:Sprite
    private lateinit var bg2:Sprite
    private lateinit var bg3:Sprite

    private var speed3 = -1f
    private var speed2 = -4f
    private var speed1 = -7f

    override fun create(batch: SpriteBatch,camera: Camera) {
        atlas = TextureAtlas(Gdx.files.internal("Background.atlas"))
        bg3 = Sprite(atlas?.findRegion("bglevel3"))
        bg2 = Sprite(atlas?.findRegion("bglevel2"))
        bg1 = Sprite(atlas?.findRegion("bglevel1"))

        val scaleX = camera.viewportWidth/bg3.regionWidth.toFloat()
        val scaleY = camera.viewportHeight/bg3.regionHeight.toFloat()

        bg3.setOrigin(0f,0f)
        bg2.setOrigin(0f,0f)
        bg1.setOrigin(0f,0f)

        bg3.setScale(scaleX,scaleY)
        bg2.setScale(scaleX,scaleY)
        bg1.setScale(scaleX,scaleY)
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
        bg3.draw(batch)
        if(bg3.x < 0) {
            bg3.translateX(camera.viewportWidth)
            bg3.draw(batch)
            bg3.translateX(-camera.viewportWidth)
        }

        bg2.draw(batch)
        if(bg2.x < 0) {
            bg2.translateX(camera.viewportWidth)
            bg2.draw(batch)
            bg2.translateX(-camera.viewportWidth)
        }

        bg1.draw(batch)
        if(bg1.x < 0) {
            bg1.translateX(camera.viewportWidth)
            bg1.draw(batch)
            bg1.translateX(-camera.viewportWidth)
        }
    }

    override fun dispose() {
        atlas?.dispose()
    }
}