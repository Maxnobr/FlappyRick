package com.maxnobr.game

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.physics.box2d.World

class Cthulhu :GameObject{
    private val FRAME_DURATION = .4f
    private lateinit var atlas: TextureAtlas

    override fun create(batch: SpriteBatch, camera: Camera, world: World) {

    }

    override fun preRender(camera: Camera) {
    }

    override fun render(batch: SpriteBatch, camera: Camera) {
    }

    override fun dispose() {
    }
}