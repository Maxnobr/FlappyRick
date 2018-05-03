package com.maxnobr.game;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class AndroidLauncher extends AndroidApplication {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();

		com.badlogic.gdx.ApplicationListener game = new CthulhuGame();
		initialize(game, config);
	}
}

interface GameInterface extends com.badlogic.gdx.ApplicationListener{
}
