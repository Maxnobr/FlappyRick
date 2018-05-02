package com.maxnobr.game

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.physics.box2d.World

interface GameObject {
    fun create(batch:SpriteBatch,camera:Camera,world: World)
    fun preRender(camera:Camera)
    fun render(batch:SpriteBatch,camera:Camera)
    fun dispose()
    fun save(data: Persistence.GameData)
    fun load(data: Persistence.GameData)
}