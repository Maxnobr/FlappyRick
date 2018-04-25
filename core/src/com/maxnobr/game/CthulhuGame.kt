package com.maxnobr.game

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector3
import kotlin.collections.LinkedHashMap

class CthulhuGame : ApplicationAdapter() {
    private lateinit var batch: SpriteBatch
    private var list = LinkedHashMap<String,GameObject>()
    lateinit var camera: OrthographicCamera
    private lateinit var introMsc:Music

    companion object {
        const val START = 0
        const val RUN = 1
        const val PAUSE = 2

        var gameState = -1
    }

    override fun create() {
        batch = SpriteBatch()

        introMsc = Gdx.audio.newMusic(Gdx.files.internal("intro8-Bit.mp3"))

        introMsc.setLooping(true)

        camera = OrthographicCamera()
        camera.setToOrtho(false,80F,48F)
        //viewport = ExtendViewport(80f, 48f, camera)

        list["background"] = Background()
        list["player"] = Saucer()
        list["gui"] = GUIHelper(this)
        list.forEach {it.value.create(batch,camera)}

        changeGameState(START)
        reset()
    }

    override fun render() {
        camera.update()
        batch.projectionMatrix = camera.combined
        Gdx.gl.glClearColor(0.57f, 0.77f, 0.85f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        /*
        if (Gdx.input.isTouched(0)) {
            val touchPos = Vector3()
            touchPos.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(),0f)
            camera.unproject(touchPos)
            (list["player"] as Saucer).setPosition(touchPos)
        }*/

        list.forEach { it.value.preRender(camera) }
        batch.begin()
        list.forEach { if(it.key != "gui")it.value.render(batch,camera) }
        batch.end()
        list["gui"]?.render(batch,camera)
    }

    fun changeGameState(state:Int)
    {
        gameState = state
        when(gameState)
        {
            START -> {
                introMsc.play()
                reset()
            }
            else -> introMsc.stop()
        }
    }

    override fun pause() {
        super.pause()
        if(gameState == RUN) changeGameState(PAUSE)
    }

    override fun resume() {
        super.resume()
        //if(gameState == RUN) changeGameState(PAUSE)
    }

    private fun reset() {
        (list["player"] as Saucer).setPosition(Vector3(camera.viewportWidth/2,camera.viewportHeight/2,0f))
    }

    override fun dispose() {
        batch.dispose()
        list.forEach { it.value.dispose() }
    }
}
