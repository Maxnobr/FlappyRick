package com.maxnobr.game;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class AndroidLauncher extends AndroidApplication {
    private AndroidBluetooth blue;

    @Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();

		blue = new AndroidBluetooth(this);
        CthulhuGame game = new CthulhuGame(blue);
		initialize(game, config);

        blue.onCreate();
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        blue.logBlue("onActivityResult in launcher req: "+requestCode+" result: "+resultCode+" data: "+data);
        //if(data != null)
            blue.onActivityResult(requestCode,resultCode,data);
        //else
        //    blue.logBlue("no DATA onActivityResult in launcher");
    }

    @Override
    public void onStart() {
        super.onStart();
        blue.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        blue.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        blue.onResume();
    }
}
