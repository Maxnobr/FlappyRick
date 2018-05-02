package com.maxnobr.game.level

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.physics.box2d.Contact
import com.badlogic.gdx.physics.box2d.ContactListener
import com.maxnobr.game.CthulhuGame
import com.maxnobr.game.Saucer
import kotlin.experimental.or


class LevelBorders(val game:CthulhuGame, val world: World,camera: Camera){


    companion object {
        const val CATEGORY_PLAYER: Short = 0x0001  // 0000000000000001 in binary
        const val CATEGORY_LEVEL: Short = 0x0002 // 0000000000000010 in binary
        const val CATEGORY_TRIGGERS: Short = 0x0004 // 0000000000000100 in binary

        const val MASK_PLAYER: Short = -1
        const val MASK_LEVEL: Short = CATEGORY_PLAYER
        const val MASK_TRIGGERS: Short = CATEGORY_PLAYER
    }

    init{
        //ground
        val groundBodyDef = BodyDef()
        groundBodyDef.position.set(Vector2(camera.viewportWidth/2,-camera.viewportHeight/2))
        val groundBody = world.createBody(groundBodyDef)

        val groundBox = PolygonShape()
        groundBox.setAsBox(camera.viewportWidth, 2f)

        val fixtureDef = FixtureDef()
        fixtureDef.shape = groundBox
        fixtureDef.filter.categoryBits = CATEGORY_TRIGGERS
        fixtureDef.filter.maskBits = MASK_TRIGGERS

        groundBody.createFixture(fixtureDef)
        groundBody.userData = "death"
        groundBox.dispose()

        //cthulhu
        val cthulhuBodyDef = BodyDef()
        cthulhuBodyDef.position.set(Vector2(0f, camera.viewportHeight/2))
        val cthulhuBody = world.createBody(cthulhuBodyDef)

        val cthulhuBox = PolygonShape()
        cthulhuBox.setAsBox(camera.viewportWidth/5, camera.viewportHeight)

        val cthulhuFixDef = FixtureDef()
        cthulhuFixDef.shape = cthulhuBox
        cthulhuFixDef.filter.categoryBits = CATEGORY_TRIGGERS
        cthulhuFixDef.filter.maskBits = MASK_TRIGGERS

        cthulhuBody.createFixture(cthulhuFixDef)
        cthulhuBody.userData = "death"
        cthulhuBox.dispose()


        //ceiling
        val ceilingBodyDef = BodyDef()
        ceilingBodyDef.position.set(Vector2(camera.viewportWidth/2, camera.viewportHeight))
        val ceilingBody = world.createBody(ceilingBodyDef)

        val ceilingBox = PolygonShape()
        ceilingBox.setAsBox(camera.viewportWidth, 1f)

        val ceilingFixDef = FixtureDef()
        ceilingFixDef.shape = ceilingBox
        ceilingFixDef.filter.categoryBits = CATEGORY_LEVEL
        ceilingFixDef.filter.maskBits = MASK_LEVEL

        ceilingBody.createFixture(ceilingFixDef)
        ceilingBody.userData = "ceiling"
        ceilingBox.dispose()

        //winning
        val winningBodyDef = BodyDef()
        winningBodyDef.position.set(Vector2(camera.viewportWidth, camera.viewportHeight/2))
        val winningBody = world.createBody(winningBodyDef)

        val winningBox = PolygonShape()
        winningBox.setAsBox(1f, camera.viewportHeight)

        val winningFixDef = FixtureDef()
        winningFixDef.shape = ceilingBox
        winningFixDef.filter.categoryBits = CATEGORY_LEVEL
        winningFixDef.filter.maskBits = MASK_LEVEL

        winningBody.createFixture(winningFixDef)
        winningBody.userData = "winning"
        winningBox.dispose()

        world.setContactListener(GetHurtListener(game))
    }

    inner class GetHurtListener(val game: CthulhuGame) : ContactListener {

        override fun endContact(contact: Contact?) {
        }
        override fun beginContact(contact: Contact?) {
            if(contact?.fixtureA?.body?.userData == "death" || contact?.fixtureB?.body?.userData == "death")
                game.getHurt()
            if(contact?.fixtureA?.body?.userData == "winning" || contact?.fixtureB?.body?.userData == "winning")
                game.changeGameState(CthulhuGame.WINNIG)
        }
        override fun preSolve(contact: Contact?, oldManifold: Manifold?) {}
        override fun postSolve(contact: Contact?, impulse: ContactImpulse?) {}
    }
}