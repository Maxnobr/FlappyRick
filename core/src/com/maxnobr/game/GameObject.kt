package com.maxnobr.game

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.g2d.SpriteBatch

interface GameObject {
    fun create(camera:Camera)
    fun preRender(camera:Camera)
    fun render(batch:SpriteBatch,camera:Camera)
    fun dispose()
}