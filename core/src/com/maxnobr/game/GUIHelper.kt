package com.maxnobr.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.maxnobr.game.CthulhuGame.Companion.PAUSE
import com.maxnobr.game.CthulhuGame.Companion.RUN
import com.maxnobr.game.CthulhuGame.Companion.START
import com.maxnobr.game.CthulhuGame.Companion.gameState


class GUIHelper(var game: CthulhuGame):GameObject {
    private lateinit var stage:Stage
    private lateinit var mySkin:Skin
    private lateinit var splashScreen: Texture
    private var screenState = -1

    //private var scaleX = 1f
    //private var scaleY = 1f

    override fun create(batch: SpriteBatch,camera: Camera) {
        //stage = Stage(ExtendViewport(camera.viewportWidth,camera.viewportHeight,camera),batch)
        //stage = Stage(ExtendViewport(Gdx.graphics.width.toFloat(),Gdx.graphics.height.toFloat(),camera),batch)
        stage = Stage(ScreenViewport())
        Gdx.input.inputProcessor = stage

        mySkin = Skin(Gdx.files.internal("pixthulhu/skin/pixthulhu-ui.json"))

        splashScreen = Texture(Gdx.files.internal(("introSprite.jpg")))
    }

    override fun preRender(camera: Camera) {
    }

    override fun render(batch: SpriteBatch, camera: Camera) {

        when(gameState) {
            START -> {
                if(screenState != gameState) getStartScreen()
                Gdx.gl.glClearColor(.3f, .8f, .3f, 1f)
                Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
            }
            PAUSE ->{
                if(screenState != gameState) getPauseScreen()
                Gdx.gl.glClearColor(1f, 1f, 1f, .5f)
            }
            RUN ->{
                if(screenState != gameState) getRunScreen()
            }
        }
        stage.act()
        stage.draw()
    }

    private fun getStartScreen() {
        stage.clear()
        Gdx.input.inputProcessor = stage
        val Help_Guides = 6
        val row_height = stage.height / Help_Guides
        val col_width = stage.width / Help_Guides

        val image = Image(splashScreen)
        image.setSize(stage.width,stage.height)
        image.setPosition(0f,0f)
        stage.addActor(image)


        val title = Label("Flappy Rick", mySkin, "title")
        title.setSize(stage.width, (row_height * 2))
        title.setPosition(0f, (Gdx.graphics.height - row_height*1.5f))
        title.setAlignment(Align.center)
        stage.addActor(title)


        // Start Button
        val button = TextButton("Start Game", mySkin, "default")
        button.setSize(col_width, row_height)
        button.setPosition((stage.width-col_width)/2, row_height)
        button.label.setAlignment(Align.center)
        button.addListener(object : InputListener() {
            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                game.changeGameState(RUN)
            }
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
               return true
            }
        })
        stage.addActor(button)
    }

    private fun getPauseScreen() {
        stage.clear()
        val Help_Guides = 6
        val row_height = stage.height / Help_Guides
        val col_width = stage.width / Help_Guides

        val title = Label("PAUSED", mySkin, "title")
        title.setSize(stage.width, (row_height * 2))
        title.setPosition(0f, (Gdx.graphics.height - row_height * 2))
        title.setAlignment(Align.center)
        stage.addActor(title)

        // Continue Button
        val button = TextButton("Continue", mySkin, "default")
        button.setSize(col_width*2, row_height)
        button.setPosition((stage.width-col_width*2)/2, (stage.height)/2)
        button.setColor(button.color.r,button.color.g,button.color.b,button.color.a/2)
        button.label.setAlignment(Align.center)
        button.addListener(object : InputListener() {
            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                game.changeGameState(RUN)
            }
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                return true
            }
        })
        stage.addActor(button)

        //Quit Button
        val button2 = TextButton("Quit", mySkin, "default")
        button2.setSize(col_width*2, row_height)
        button2.setPosition((stage.width-col_width*2)/2, (stage.height - row_height*3)/2)
        button2.setColor(button2.color.r,button2.color.g,button2.color.b,button2.color.a/2)
        button2.label.setAlignment(Align.center)
        button2.addListener(object : InputListener() {
            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                game.changeGameState(START)
            }
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                return true
            }
        })
        stage.addActor(button2)
    }

    private fun getRunScreen() {
        stage.clear()
        val side = stage.height / 10

        val button = TextButton("| |", mySkin, "default")
        button.setSize(side, side)
        button.setPosition(stage.width-side, stage.height - side)
        button.setColor(button.color.r,button.color.g,button.color.b,button.color.a/2)
        button.addListener(object : InputListener() {
            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                game.changeGameState(PAUSE)
            }
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                return true
            }
        })
        stage.addActor(button)
    }

    override fun dispose() {
        stage.dispose()
    }
}