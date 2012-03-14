package org.squidwrench.gameworks.tictactoe;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
/*
Squidwrench.org
Brian Neugebauer 2012
Show splash image and play jingle
*/
public class Splash extends Activity{

	MediaPlayer splashMusic;
	@Override
	protected void onCreate(Bundle TTTTime) {
		super.onCreate(TTTTime);
		setContentView(R.layout.splash);
		splashMusic = MediaPlayer.create(Splash.this, R.raw.splashjam);
		splashMusic.start();
		Thread timer = new Thread(){
			@Override
			public void run(){
				try{
					sleep(3000);
				} catch(InterruptedException e) {
					e.printStackTrace();
				} finally {
					Intent openStartingPoint = new Intent("org.squidwrench.gameworks.tictactoe.TICTACTOE");
					startActivity(openStartingPoint);
				}
			}
		};
		timer.start();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		splashMusic.release();
		finish();
	}
}
