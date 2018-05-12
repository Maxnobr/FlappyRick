package com.maxnobr.game

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
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
import java.util.*

class CthulhuGame(var blue:Bluetooth) : ApplicationAdapter() {

    private lateinit var batch: SpriteBatch
    private var list = LinkedHashMap<String,GameObject>()
    private lateinit var camera: OrthographicCamera
    private lateinit var introMsc:Music
    private lateinit var gameMsc:Music
    var debug = true
    private var readyToTap = true

    var accumulator = 0f
    private lateinit var world: World
    private lateinit var debugRenderer: Box2DDebugRenderer

    lateinit var persistence: Persistence
    lateinit var multiPlayer: MultiPlayer

    companion object {
        const val START = 0
        const val RUN = 1
        const val WAITINGONRUN = 2
        const val PAUSE = 3
        const val GAMEOVER = 4
        const val WINNING = 5


        const val FILE = "files/data.txt"
        const val SINGLEGAMENAME = "local"

        var saveName = SINGLEGAMENAME
        var gameState = -1

        const val ISPLAYER = 1
        const val ISMONSTER = 2

        var gamer = ISPLAYER

        private const val STEP_TIME = 1f / 60f
        private const val VELOCITY_ITERATIONS = 6
        private const val POSITION_ITERATIONS = 2

        const val BACKGROUND = "background"
        const val OBSTACLES = "obstacles"
        const val PLAYER = "player"
        const val CTHULHU = "Cthulhu"
        const val GUI = "gui"

        const val CODE_CHANGESTATE = 0

        private var fromMulti = false
    }

    override fun create() {
        batch = SpriteBatch()

        Box2D.init()
        world = World(Vector2(0f, -10f), true)
        debugRenderer = Box2DDebugRenderer()

        introMsc = Gdx.audio.newMusic(Gdx.files.internal("intro8-Bit.mp3"))
        introMsc.setLooping(true)

        gameMsc = Gdx.audio.newMusic(Gdx.files.internal("angryJoe.mp3"))
        gameMsc.setLooping(true)

        camera = OrthographicCamera()
        camera.setToOrtho(false,80F,48F)

        list[BACKGROUND] = Background()
        list[OBSTACLES] = Obstacles(this)
        list[PLAYER] = Saucer(this)
        list[CTHULHU] = Cthulhu(this)
        list[GUI] = GUIHelper(this)
        list.forEach {it.value.create(batch,camera,world)}

        LevelBorders(this,world,camera)

        persistence = Persistence()
        multiPlayer = MultiPlayer(this,blue)
        //delete(SINGLEGAMENAME)

        changeGameState(START)

        //blue.logBlue("Starting Game !")
    }

    override fun render() {
        camera.update()
        batch.projectionMatrix = camera.combined
        Gdx.gl.glClearColor(0.57f, 0.77f, 0.85f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        if(gameState == RUN) list.forEach { it.value.preRender(camera)
            it.value.send(multiPlayer)}
        batch.begin()
        list.forEach { if(it.key != GUI) it.value.render(batch,camera)}
        batch.end()

        if(gameState == RUN) {
            stepWorld()
            if((Math.abs(Gdx.input.accelerometerX)+Math.abs(Gdx.input.accelerometerY)+Math.abs(Gdx.input.accelerometerZ)) > 60)
                changeGameState(PAUSE)
            if(Gdx.input.isTouched){
                if(readyToTap) {
                    readyToTap = false
                    when(gamer){
                        ISPLAYER -> (list[PLAYER] as Saucer).jump()
                        ISMONSTER -> {
                            val vec = camera.unproject(Vector3(Gdx.input.x.toFloat(),Gdx.input.y.toFloat(),0f))
                            (list[CTHULHU] as Cthulhu).punch(Vector2(vec.x,vec.y))
                        }
                    }
                }
            }
            else
                readyToTap = true
        }
        if(debug) {
            Gdx.gl.glClearColor(0f,0f,0f,.3f)
            //Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
            debugRenderer.render(world, camera.combined)
        }

        list[GUI]?.render(batch,camera)
    }

    @Synchronized fun changeGameState(state:Int) {

        while(persistence.processing)
            Thread.sleep(100)
        gameState = state
        if(!fromMulti)
            multiPlayer.send(MultiPlayer.GAME,"$CODE_CHANGESTATE $gameState")
        fromMulti = false
        when(gameState) {
            START -> {
                introMsc.play()
                gameMsc.stop()
                reset()
            }
            RUN->{
                gameMsc.play()
                introMsc.stop()
            }
            PAUSE -> {
                save(saveName)
            }
            WINNING, GAMEOVER -> {
                delete(saveName)
            }
        }
    }

    override fun pause() {
        super.pause()
        if(gameState == RUN) {
            changeGameState(PAUSE)
            save(saveName)
        }
    }

    private fun reset() {
        debug = false
        if (list.containsKey(OBSTACLES))
            (list[OBSTACLES] as Obstacles).reset()
        (list[PLAYER] as Saucer).setPosition(Vector3(camera.viewportWidth/2,camera.viewportHeight/2,0f))
        (list[PLAYER] as Saucer).reset()
    }

    fun getHurt() {
        (list[PLAYER] as Saucer).takeDamage(1,false)
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
        gameMsc.dispose()
        introMsc.dispose()
        list.forEach { it.value.dispose() }
    }

    fun save(saveName:String) {
        if(gamer == ISPLAYER)
            persistence.save(saveName,list)
    }

    fun load(saveName:String) {
        persistence.load(saveName,list)
    }

    private fun delete(saveName:String) {
        persistence.delete(saveName)
    }

    fun receive(toWhom:String,msg:String) {
        list[toWhom]?.receive(msg)
    }

    fun receive(msg:String) {
        val words = ArrayDeque(msg.split(" ".toRegex()))
        when(words.pollFirst().toInt()){
            CODE_CHANGESTATE -> {
                fromMulti = true
                changeGameState(words.pollFirst().toInt())}
        }
    }
}