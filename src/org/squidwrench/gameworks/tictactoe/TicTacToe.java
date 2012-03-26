package org.squidwrench.gameworks.tictactoe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

/*
TiC Tac ToeJam
Squidwrench.org 2012
Brian Neugebauer, more names
Code for traditional game with 2 players, play vs computer, and ToeJam game 
*/

public class TicTacToe extends Activity implements SensorEventListener {
	private String playername = "X", startingplayer = "X";
	private int rows = 3, cols = 3, moves, moveslimit = rows * cols, xscore, oscore, tscore, toechosen;
	private int pointcount[] = new int[8]; //R1,R2,R3,C1,C2,C3,DD,DU = ways to win, 3 or -3 means a win
	private int squaremoves[] = new int[15]; //0 = first move, positive = O, negative = X, TL,TM,TR,ML,MM,MR,BL,BM,BR
	private boolean gameover = false, computeropponent = false, toe = false;
	private final int row1[] =  {R.id.TopLeft,		R.id.TopRight,		R.id.TopMiddle};
	private final int row2[] =  {R.id.MiddleMiddle,R.id.MiddleLeft,	R.id.MiddleRight};
	private final int row3[] =  {R.id.BottomLeft,	R.id.BottomRight,	R.id.BottomMiddle};
	private final int col1[] =  {R.id.TopLeft,		R.id.BottomLeft,	R.id.MiddleLeft};
	private final int col2[] =  {R.id.MiddleMiddle,R.id.TopMiddle,		R.id.BottomMiddle};
	private final int col3[] =  {R.id.TopRight,	R.id.BottomRight,	R.id.MiddleRight};
	private final int ddown[] = {R.id.MiddleMiddle,R.id.TopLeft,		R.id.BottomRight};
	private final int dup[] =   {R.id.MiddleMiddle,R.id.TopRight,		R.id.BottomLeft};
	private final int rcd[][] = {row1,row2,row3,col1,col2,col3,ddown,dup}; //rcd = rows, cols, diagonals
	private final int tls[] = {0,3,6}, tms[] = {0,4}, trs[] = {0,5,7}; //TLs = top left square, is used in pointcount(0,3,6)
	private final int mls[] = {1,3}, mms[] = {1,4,6,7}, mrs[] = {1,5};
	private final int bls[] = {2,3,7}, bms[] = {2,4}, brs[] = {2,5,6};
	private final int inTrio[][] = {tls,tms,trs,mls,mms,mrs,bls,bms,brs}; //way to lookup which pointcount values need updating
	private final int corners[] = {R.id.TopLeft,R.id.TopRight,R.id.BottomLeft,R.id.BottomRight};
	private final List<Integer> squares = new ArrayList<Integer>(Arrays.asList(R.id.TopLeft,R.id.TopMiddle,R.id.TopRight,R.id.MiddleLeft,R.id.MiddleMiddle,R.id.MiddleRight,R.id.BottomLeft,R.id.BottomMiddle,R.id.BottomRight)); //TL,TM,TR,ML,MM,MR,BL,BM,BR
	private List<Integer> squaresremaining = new ArrayList<Integer>(Arrays.asList(R.id.TopLeft,R.id.TopMiddle,R.id.TopRight,R.id.MiddleLeft,R.id.MiddleMiddle,R.id.MiddleRight,R.id.BottomLeft,R.id.BottomMiddle,R.id.BottomRight)); //TL,TM,TR,ML,MM,MR,BL,BM,BR
	private List<Integer> squarestaken = new ArrayList<Integer>(); //TL,TM,TR,ML,MM,MR,BL,BM,BR
	private SensorManager sensMgr;
	private Sensor accelerometer;
    private static SoundPool sounds;
    private static int xbeep, obeep, toebeep, gamewin, gametie, complaugh, maybe; 
    private int randsound[] = {0,0};
    private static boolean sound = true;
    private Random rand = new Random();
    private boolean computerTurn = false;
    private PopupWindow pwcredits;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);    
		sensMgr = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = sensMgr.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        setContentView(R.layout.main);
        
        //Load sounds 
        SharedPreferences settings = getSharedPreferences("PREF",0);
        sound = settings.getBoolean("sound", true);
        if (sound) {
        	loadSounds();
        }

        //Get Dimensions of screen and size buttons based on that
	    Display display = getWindowManager().getDefaultDisplay();
	    int dwidth = display.getWidth();
	    int dheight = display.getHeight();
	    int setsize;
	    if (dheight > dwidth) {
	    	//vertical
	    	setsize = dwidth/3;
	    }
	    else {
	    	//horizontal
	    	setsize = dheight/4;
	    }
	    ImageButton imagebutton;
	    for (Integer squareid : squares) {
	    	imagebutton = (ImageButton) findViewById(squareid);
    		imagebutton.getLayoutParams().height = setsize;
    		imagebutton.getLayoutParams().width = setsize;
	    	imagebutton.setTag(R.drawable.biggray);
	    }
	    
		showWhoseTurn();
		showScore();
		
		//Set up Vs Computer checkbox
		CheckBox cbAI =(CheckBox)findViewById(R.id.checkBoxAI);
	    cbAI.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
	    	public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
	    		if (arg1) {
	    			computeropponent = true;
	    			if (playername.equals("O"))
	       				computerMove();	
	    		}
	    		else {
	    			computeropponent = false;
	    		}
	    	}
	    });  
	    //Set up Toe checkbox
	    CheckBox cbtoe =(CheckBox)findViewById(R.id.checkBoxToe);
	    cbtoe.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
	    	public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
	    		CheckBox cb = (CheckBox) findViewById(R.id.checkBoxAI);
	    		if (arg1) {
	    			toe = true;
	    			if (cb.isChecked())
	    				cb.setChecked(false);
	    			cb.setClickable(false);
	    			cb.setTextColor(Color.GRAY);
	    		}
	    		else {
	    			toe = false;
    				cb.setClickable(true);
    				cb.setTextColor(Color.WHITE);
	    		}
	    		if (moves > 0)
	    			startOver();
	    	}
	    });     
    }
    
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
      //Save gamestate for orientation change or interruption by phonecall, etc
      savedInstanceState.putString("gmoves", Arrays.toString(squaremoves).replace("[", "").replace("]", "").replace(" ", ""));
      savedInstanceState.putBoolean("gameover", gameover);
      savedInstanceState.putString("playername", playername);
      savedInstanceState.putBoolean("vscomputer", computeropponent);
      savedInstanceState.putBoolean("toe", toe);
      savedInstanceState.putInt("xwins", xscore);
      savedInstanceState.putInt("owins", oscore);
      savedInstanceState.putInt("twins", tscore);   
      super.onSaveInstanceState(savedInstanceState);
    }
    
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
      super.onRestoreInstanceState(savedInstanceState);
      // Restore UI state from the savedInstanceState.
      loadGame(savedInstanceState.getString("gmoves"),savedInstanceState.getBoolean("gameover"),savedInstanceState.getString("playername"),savedInstanceState.getBoolean("vscomputer"),savedInstanceState.getBoolean("toe"),savedInstanceState.getInt("xwins"),savedInstanceState.getInt("owins"),savedInstanceState.getInt("twins"));
    }
    
    @Override
	protected void onResume() {
    	super.onResume();
    	sensMgr.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }
    
    
    @Override
	protected void onPause() {
    	super.onPause();
    	sensMgr.unregisterListener(this);
    }
    

	public void buttonsClickable(boolean clickable) {
		ImageButton button;
		for (Integer squareid : squares) {
			button = (ImageButton) findViewById(squareid);
			button.setClickable(clickable);
		}
	}
	
	
	public void claimSquare(View view) {
		//Player has clicked a square
		claimSquareProcess(view,false);
	}
	
	public void claimSquareProcess(View view, boolean fromComputer) {
		if (computerTurn) {
			if (!fromComputer) {
				return;
			}
		}
		if (gameover == true) {
			startOver();
		}
		else {
	    	ImageButton button = (ImageButton)view;
	    	if(button.getTag().equals((Integer) R.drawable.biggray)) {
	    		button.setClickable(false);
	    		int pvalue;
	    		int dvalue;
	    		if (toe) {
	    			//select random square that toe will step on after this move
	    			if  (moves == 2 || moves == 6 || moves == 10) {
	    				int randomsquare = rand.nextInt(squarestaken.size());
	    				toechosen = squarestaken.get(randomsquare);
	    				moveslimit += 2;
	    			}
	    		}
	    		if (playername.equals("X")) {
	    			pvalue = -1;
	    			dvalue = R.drawable.bigx;
	    		}
	    		else {
	    			pvalue = 1;
	    			dvalue = R.drawable.bigo;
	    		}
    			button.setBackgroundDrawable(getResources().getDrawable(dvalue));
    			button.setTag(dvalue);
    			
	    		int squareid = view.getId();
	    		
	    		for (Integer sqtrio : inTrio[squares.indexOf(squareid)])
	    			pointcount[sqtrio] += pvalue;
	    		
  	    	  	squaresremaining.remove(new Integer(squareid));
  	    	  	squarestaken.add(new Integer(squareid));
  	    	  	squaremoves[moves] = pvalue * squareid;
				moves += 1;				
				TextView tv = (TextView) findViewById(R.id.textView1);
				TextView tvs = (TextView) findViewById(R.id.textViewScore);
				if (moves > 4) {
		    		if (checkForWinCondition()) {
		    			buttonGlow(pvalue * 3);
		    			if (playername.equals("X")) {
		    				xscore += 1;
		    				playSound(gamewin);
		    			}
		    			else {
		    				oscore += 1; 
		    				if (computeropponent == true) {
		    					//playSound(complaugh);
		    					randomSound();
		    				}
		    				else
		    					playSound(gamewin);
		    			}
		    			tv.setText("Player " + playername + " WINS!");
		    			tvs.setText("Click Any Square To Start New Game");
		    			buttonsClickable(true);
		    			gameover = true;
		    			return;
		    		}
		    		else {
		    			if (moves == moveslimit) {
		    				playSound(gametie);
		    				tv.setText("Game Over: Tie");
		    				tscore += 1;
		    				tvs.setText("Click Any Square To Start New Game");
		    				buttonsClickable(true);
		    				gameover = true;
		    				return;
		    			}
		    		}
				}
	    		if (playername.equals("X"))
	    			playSound(xbeep);
	    		else
	    			playSound(obeep);
	    		playername = (playername.equals("X")) ? "O" : "X";
	    		showWhoseTurn();
	    		if (playername.equals("O") && computeropponent == true) {
	    			computerTurn = true;
	    			//delay
	    			Handler handler=new Handler();
	    			final Runnable r = new Runnable()
	    			{
	    			    public void run() 
	    			    {
	    			    	computerMove();
	    			    	computerTurn = false;
	    			    }
	    			};
	    			handler.postDelayed(r, 1000);
	    		}
	    		else if (toe) {	
	    			//The toe drops
	    			if (moves == 3 || moves == 7 || moves == 11)
	    				playToe();
	    			//The toe leaves
	    			else if (moves == 5 || moves == 9 || moves == 13) {	
	    				showScore();
	    				cleanToe();
	    			}
	    		} 
	    	}
    	}
    }
    
    public boolean checkForWinCondition() {
    	if (moves < (2 * rows - 1)) return false;    		
    	int check[] = pointcount.clone();
    	Arrays.sort(check);
    	if (check[0] == -3 || check[7] == 3)
    		return true;    		
    	return false;
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.replay, menu);
    	return true;
    }
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    		case R.id.startOver:
    			startOver();
    			return true;
    		case R.id.saveGame:
    			saveGame("SAVEGAME");
    			return true;
    		case R.id.loadGame:
    			SharedPreferences settings = getSharedPreferences("SAVEGAME", 0);        
       			loadGame(settings.getString("gmoves",""),settings.getBoolean("gameover",gameover),settings.getString("playername",playername),settings.getBoolean("vscomputer",computeropponent),settings.getBoolean("toe",toe),settings.getInt("xwins",xscore),settings.getInt("owins",oscore),settings.getInt("twins",tscore));
    			return true;
    		case R.id.clearScore:
    			clearScore();
    			return true;
    		case R.id.credits:
    			showCredits();
    			return true;
    		case R.id.toggleSound:
    			toggleSound();
    			return true;
    		default:
    			return super.onOptionsItemSelected(item);
    	}
    }

	private void startOver() {
		squaresremaining.clear();
		Collections.addAll(squaresremaining, R.id.TopLeft,R.id.TopMiddle,R.id.TopRight,R.id.MiddleLeft,R.id.MiddleMiddle,R.id.MiddleRight,R.id.BottomLeft,R.id.BottomMiddle,R.id.BottomRight);
		ImageButton button;
		for (Integer squareid : squaresremaining) {
			button = (ImageButton) findViewById(squareid);
			button.setBackgroundDrawable(getResources().getDrawable(R.drawable.biggray));
			button.setTag(R.drawable.biggray);
		}
		gameover = false;
		moves = 0;
		moveslimit = 9;
		Arrays.fill(pointcount, 0);
		Arrays.fill(squaremoves, 0);
		toechosen = 0;
		squarestaken.clear();
		startingplayer = (startingplayer.equals("X")) ? "O" : "X";
		playername = startingplayer;
		showWhoseTurn();
		showScore();
		buttonsClickable(true);
		//Player making the first turn alternates, if vs computer then make the first move
		if (startingplayer.equals("O") && computeropponent == true)
			computerMove();
	}

	public void showScore() {
		TextView tvs = (TextView) findViewById(R.id.textViewScore);
		tvs.setText("Score:   X:" + xscore + "   O:" + oscore + "   Tie:" + tscore);
	}
	
	public void showWhoseTurn() {
		TextView tv = (TextView) findViewById(R.id.textView1);
		tv.setText("Player " + playername + "'s turn");
	}
	
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		return;		
	}

	public void onSensorChanged(SensorEvent event) {
		if(event.values[0] > 5) {
		  startOver();
		}
		return;
	}
	
	public void randomMove() {
		//Dumb Computer AI
		int zeroCheck = squaresremaining.size();
		if (zeroCheck > 0) {
			int randomsquare = rand.nextInt(squaresremaining.size());
			ImageButton button = (ImageButton) findViewById(squaresremaining.get(randomsquare));
			//button.performClick();
			claimSquareProcess(button,true);
		}
	}
	
	public void smartMove() {
		ImageButton button;
		//if first move, grab the center square
		if (squaresremaining.size() == 9) {
  			button = (ImageButton) findViewById(R.id.MiddleMiddle);
  			claimSquareProcess(button,true);
  			return;
		}
		else {
			if (scanBoard(2)) return; //look for O about to win and complete
			if (scanBoard(-2)) return; //look for X about to win and block
			
			//take center if it's still available
			button = (ImageButton) findViewById(R.id.MiddleMiddle);
  			if (button.getTag().equals((Integer) R.drawable.biggray)) {
  				claimSquareProcess(button,true);
  				return;
  			}
  			
  			if (scanBoard(1)) return; //look for O with one and build on it
  			
  			//take corner if possible
  			for (int c = 0; c < 4; c++) { 
					button = (ImageButton) findViewById(corners[c]);
					if (button.getTag().equals((Integer) R.drawable.biggray)) {
						claimSquareProcess(button,true);
						return;
					}	
  			}
  			//might not ever get here
  			randomMove();
		}
	}
	
	public boolean scanBoard(int targetvalue) {
		//look for scenario and deal with it
		//target = 2: look for a way to win
		//target = -2: look for a way opponent can win and block
		//target = 1: look for a place to build on
		ImageButton button;
		for (int q = 0; q < 8; q++) { 
			if (pointcount[q] == targetvalue) {
				for (Integer squareid : rcd[q]) {
  					button = (ImageButton) findViewById(squareid);
  					if (button.getTag().equals((Integer) R.drawable.biggray)) {
  						claimSquareProcess(button,true);
  						return true;
  					}	
				}
			}
		}
		return false;
	}
	
	public void computerMove() {
		//alternate between easy mode and hard mode depending on who's winning
		if (oscore >= xscore) { 
			//dumb AI - random selection
			randomMove();
		}
		else { 
			//smart AI 
			smartMove();
		}
	}
	
	public void popup(String debug) {
		CharSequence text = debug;
		int duration = Toast.LENGTH_LONG;
		Toast toast = Toast.makeText(this, text, duration);
		toast.show();
	}
	
	public void popup(int debug) {
		CharSequence text = Integer.toString(debug);
		int duration = Toast.LENGTH_LONG;
		Toast toast = Toast.makeText(this, text, duration);
		toast.show();
	}
 	

	public void buttonGlow(int winner) {
		int glowcolor;
		if (winner == 3)
			glowcolor = R.drawable.bigooo;
		else
			glowcolor = R.drawable.bigxxx;
		ImageButton button;
		for (int p = 0; p < 8; p++) {
			if (pointcount[p] == winner) {
				for (Integer squareid : rcd[p]) {
					button = (ImageButton) findViewById(squareid);
					button.setBackgroundDrawable(getResources().getDrawable(glowcolor));
				}
			}
		}
	}

	public void saveGame(String preffilename) {		
	    SharedPreferences settings = getSharedPreferences(preffilename, 0);
	    SharedPreferences.Editor editor = settings.edit();
	    editor.putString("gmoves", Arrays.toString(squaremoves).replace("[", "").replace("]", "").replace(" ", ""));
		editor.putBoolean("gameover", gameover);
		editor.putString("playername", playername);
		editor.putBoolean("vscomputer", computeropponent);
	    editor.putBoolean("toe", toe);
	    editor.putInt("xwins", xscore);
	    editor.putInt("owins", oscore);
	    editor.putInt("twins", tscore);
	    editor.commit();
	}
	
	public static void playSound(int soundid) {
	    if (sound) 
	    	sounds.play(soundid, 1, 1, 1, 0, 1);
	}
	
	public void toggleSound() {		
		sound = !sound; 
		if (sound)
			loadSounds();
	    SharedPreferences settings = getSharedPreferences("PREF", 0);
	    SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean("sound", sound);
	    editor.commit();
	}
	
	public void randomSound() {
		//Random Computer Taunt
		if (sound) {
			int rsound = rand.nextInt(randsound.length);
			popup(rsound);
			popup(randsound[rsound]);
			playSound(randsound[rsound]);	
		}
	}
	
	public void loadSounds() {		
	    sounds = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
	    xbeep = sounds.load(this, R.raw.ttt_x, 1);
	    obeep = sounds.load(this, R.raw.ttt_o, 1);
	    toebeep = sounds.load(this, R.raw.toejam,1);
	    gamewin = sounds.load(this, R.raw.gamewin, 1);
	    gametie = sounds.load(this, R.raw.gametie, 1);
	    complaugh = sounds.load(this, R.raw.computerlaugh, 1);
	    maybe = sounds.load(this, R.raw.maybecj, 1);
	    randsound[0] = complaugh;
	    randsound[1] = maybe;
	}
	
		  
	public void loadGame(String lmoves, boolean lgameover, String lplayername, boolean lvscomputer, boolean ltoe, int lxwins, int lowins, int ltwins) { 
    	try{
    		//Loadgame plays through the recorded moves 
			if(lmoves == "") return; //First time program runs
			
			//disable sound
			boolean remsound = sound;
			sound = false;
	        	        
	        computeropponent = lvscomputer;
		    CheckBox cb = (CheckBox) findViewById(R.id.checkBoxAI);
			if (computeropponent) {
				if (!cb.isChecked()) {
					playername = "X";
					cb.setChecked(true);
				}
			}
			else {
				if (cb.isChecked())
					cb.setChecked(false);
			}
			
	        toe = ltoe;
		    CheckBox cbtoe = (CheckBox) findViewById(R.id.checkBoxToe);
			if (toe) {
				if (!cbtoe.isChecked()) {
					cbtoe.setChecked(true);
				}
			}
			else {
				if (cbtoe.isChecked())
					cbtoe.setChecked(false);
			}
			
			startOver();
			
			String token = ",";
	        int[] convertedIntArray = StringToArrayConverter.convertTokenizedStringToIntArray(lmoves, token);
	        
	        boolean toehold = toe;
	        boolean comphold = computeropponent;
	                
	        toe = false;
	        computeropponent = false;
	    
	        ImageButton button;
	        Integer mov;
	        for (int s = 0; s < 15; s++) {
	            mov = convertedIntArray[s];
	            if (mov == 0) 
	            	break;
	            if (mov < 0)
	            	playername = "X";
	            else
	            	playername = "O";
	            if (toehold && (moves == 5 || moves == 9 || moves == 13)) {
	            	cleanToe();
	            }
	            if (toehold && (moves == 3 || moves == 7 || moves == 11)) {
	                toechosen = mov;
		            moveslimit += 2;
		            playToe();
	            }
	            else {
	            	button = (ImageButton) findViewById(Math.abs(mov));
	    	        claimSquareProcess(button,true);
	            }
	        }
	        
	        sound = remsound;
	        toe = toehold;
	        computeropponent = comphold;
	        
	        xscore = Math.max(lxwins,xscore);
	        oscore = Math.max(lowins,oscore);
	        tscore = Math.max(ltwins,tscore);
	        showScore();
		
			gameover = lgameover;
			if (!gameover) {
				playername = lplayername;
				showWhoseTurn();
	    		if (playername.equals("O") && computeropponent == true) {
	    			computerMove();		
	    		}
			}

    	}catch(NullPointerException e){
    		startOver();
    	}
	}
	
	public void clearScore() {
		xscore = 0;
		oscore = 0;
		tscore = 0;
		showScore();
	}
	
	private void showCredits() {
		/*Show credits popup window when trigged from menu.*/
	    try {
	        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        //Inflate the view from a predefined XML layout
	        View layout = inflater.inflate(R.layout.credits,(ViewGroup) findViewById(R.id.popup_credits));
	        // create a 300px width and 470px height PopupWindow
	        pwcredits = new PopupWindow(layout, 300, 470, true);		 
	        // display the popup in the center
	        pwcredits.showAtLocation(layout, Gravity.CENTER, 0, 0);
	 
	        layout.setOnClickListener(dismiss_credits);
	 
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	 
	private OnClickListener dismiss_credits = new OnClickListener() {
		/*Click anywhere to dismiss Credits popup window*/
	    public void onClick(View v) {
	        pwcredits.dismiss();
	    }
	};
	
	public void cleanToe() {
		/*The Toe stays on the board until the next move is made.*/
		squaresremaining.add(new Integer(toechosen));
		squarestaken.remove(new Integer(toechosen));

    	ImageButton button = (ImageButton) findViewById(toechosen);
 		button.setBackgroundDrawable(getResources().getDrawable(R.drawable.biggray));
 		button.setTag(R.drawable.biggray);
 		button.setClickable(true);
 		playSound(toebeep);
	}

	public void playToe() {
		/*ToeJam is a mod of TicTacToe where a Toe drops every 3 moves to randomly squash one of the existing moves.
		If TTT is played correctly, there will always be a tie. ToeJam adds the random factor to make it a game you can win.
		The Toe occupies the square until it leaves, after the next move is made.*/
	 	ImageButton button = (ImageButton) findViewById(toechosen);
	 	int pvalue = 0;
		if (button.getTag().equals((Integer) R.drawable.bigx)) 
   			pvalue = 1;
   		else if (button.getTag().equals((Integer) R.drawable.bigo))
   			pvalue = -1;
		
		for (Integer sqtrio : inTrio[squares.indexOf(toechosen)])
			pointcount[sqtrio] += pvalue;
		
		squaremoves[moves] = toechosen;
		moves += 1;
		
    	button = (ImageButton) findViewById(toechosen);
    	button.setBackgroundDrawable(getResources().getDrawable(R.drawable.bigtoejam));
    	button.setTag(R.drawable.bigtoejam);
 		playSound(toebeep);
	    TextView tvs = (TextView) findViewById(R.id.textViewScore);
	    tvs.setText("Toe Jam!");
 	}
 }
 