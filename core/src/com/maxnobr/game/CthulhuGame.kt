package com.maxnobr.game

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.box2d.Box2D
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.World
import com.maxnobr.game.level.LevelBorders
import com.maxnobr.game.level.Obstacles

class CthulhuGame : ApplicationAdapter() {
    private lateinit var batch: SpriteBatch
    private var list = LinkedHashMap<String,GameObject>()
    private lateinit var camera: OrthographicCamera
    private lateinit var introMsc:Music
    var debug = true
    private var readyToJump = true

    companion object {
        const val START = 0
        const val RUN = 1
        const val PAUSE = 2
        const val GAMEOVER = 3
        const val WINNIG = 4

        var gameState = -1

        private const val STEP_TIME = 1f / 60f
        private const val VELOCITY_ITERATIONS = 6
        private const val POSITION_ITERATIONS = 2
    }
    var accumulator = 0f
    private lateinit var world: World
    private lateinit var debugRenderer: Box2DDebugRenderer

    override fun create() {
        batch = SpriteBatch()


        val file= Gdx.files.local("files/myFile.txt")
        file.writeString("firstLine\n", false)
        file.writeString("SecondLine",true)

        val lines = file.readString().split("\n".toRegex())
        Gdx.app.log("CRUD","loaded message is :'$lines'")

        Box2D.init()
        world = World(Vector2(0f, -10f), true)
        debugRenderer = Box2DDebugRenderer()

        introMsc = Gdx.audio.newMusic(Gdx.files.internal("intro8-Bit.mp3"))
        introMsc.setLooping(true)

        camera = OrthographicCamera()
        camera.setToOrtho(false,80F,48F)

        list["background"] = Background()
        list["obstacles"] = Obstacles()
        list["player"] = Saucer(this)
        list["Cthulhu"] = Cthulhu()
        list["gui"] = GUIHelper(this)
        list.forEach {it.value.create(batch,camera,world)}

        LevelBorders(this,world,camera)

        changeGameState(START)
    }

    override fun render() {
        camera.update()
        batch.projectionMatrix = camera.combined
        Gdx.gl.glClearColor(0.57f, 0.77f, 0.85f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        if(gameState == RUN) list.forEach { it.value.preRender(camera) }
        batch.begin()
        list.forEach { if(it.key != "gui")it.value.render(batch,camera) }
        batch.end()

        if(gameState == RUN) {
            stepWorld()
            if(Gdx.input.isTouched){
                if(readyToJump) {
                    (list["player"] as Saucer).jump()
                    readyToJump = false
                }
            }
            else
                readyToJump = true
        }
        if(debug) {
            Gdx.gl.glClearColor(0f,0f,0f,1f)
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
            debugRenderer.render(world, camera.combined)
        }

        list["gui"]?.render(batch,camera)
    }

    fun changeGameState(state:Int) {
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
        //if(gameState == RUN) changeGameState(RUN)
    }

    private fun reset() {
        debug = false
        (list["obstacles"] as Obstacles).reset()
        (list["player"] as Saucer).setPosition(Vector3(camera.viewportWidth/2,camera.viewportHeight/2,0f))
        (list["player"] as Saucer).reset()
    }

    fun getHurt(){
        (list["player"] as Saucer).takeDamage(1)
    }

    private fun stepWorld() {
        val delta = Gdx.graphics.deltaTime

        accumulator += Math.min(delta, 0.25f)

        if (accumulator >= STEP_TIME) {
            accumulator -= STEP_TIME

            world.step(STEP_TIME, VELOCITY_ITERATIONS, POSITION_ITERATIONS)
        }
    }

    override fun dispose() {
        batch.dispose()
        world.dispose()
        debugRenderer.dispose()
        list.forEach { it.value.dispose() }
    }
}