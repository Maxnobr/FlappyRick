package com.maxnobr.game

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.viewport.ExtendViewport

class CthulhuGame : ApplicationAdapter() {
    private lateinit var batch: SpriteBatch
    private val list = mutableMapOf<String,GameObject>()

    lateinit var camera: OrthographicCamera
    lateinit var viewport: ExtendViewport

    var ready = true

    override fun create() {
        batch = SpriteBatch()

        camera = OrthographicCamera()
        camera.setToOrtho(false,80F,48F)
        //viewport = ExtendViewport(80f, 48f, camera)

        Gdx.app.log("tag","creating game!")
        list["background"] = Background()
        list["player"] = Saucer()
        //list.plus(Pair("player",Saucer()))
        list.forEach {it.value.create(camera)}
    }

    override fun render() {
        camera.update()
        batch.projectionMatrix = camera.combined

        Gdx.gl.glClearColor(0.57f, 0.77f, 0.85f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        //(list["player"] as Saucer).SetPosition(Vector2(viewport.worldWidth/2,viewport.worldHeight/2))


        //(list["player"] as Saucer).setPosition(Vector2(camera.viewportWidth/2,camera.viewportHeight/2))
        if (Gdx.input.isTouched(0)) {
            val touchPos = Vector3()
            touchPos.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(),0f)
            camera.unproject(touchPos)
            (list["player"] as Saucer).setPosition(touchPos)
        }

        if (Gdx.input.isTouched(1)) {
            if(ready)
            {
                (list["player"] as Saucer).takeDamage(1)
                ready = false
            }
        }
        else
            ready = true


        list.forEach { it.value.preRender(camera) }
        batch.begin()
        list.forEach { it.value.render(batch,camera) }
        batch.end()
    }

    override fun dispose() {
        batch.dispose()
        list.forEach { it.value.dispose() }
    }
}
