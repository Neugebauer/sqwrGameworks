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
	static final int TWO_X_IN_A_ROW = -2, TWO_O_IN_A_ROW = 2, ONE_O_IN_A_ROW = 1, MINIMUM_MOVES_NEEDED_TO_WIN = 5, WAYS_TO_WIN = 8; 
	static final int X_WIN_POINT_SUM = -3, O_WIN_POINT_SUM = 3;
	static final int BLANK_SQUARE = R.drawable.biggray, X_SQUARE = R.drawable.bigx, O_SQUARE = R.drawable.bigo;
	static final int HIGHLIGHTED_X_SQUARE = R.drawable.bigxxx, HIGHLIGHTED_O_SQUARE = R.drawable.bigooo, TOE_SQUARE = R.drawable.bigtoejam;
	private String playerWhoseTurnItIs = "X", startingplayer = "X";
	private int moveCount, numberOfMovesAllowed = 9, xNumberOfWins, oNumberOfWins, NumberOfTies, toechosen;
	private int waysToWinPointCounter[] = new int[WAYS_TO_WIN]; //R1,R2,R3,C1,C2,C3,DD,DU = ways to win, 3 or -3 means a win
	private int movesHistory[] = new int[15]; //0 = first move, positive = O, negative = X, TL,TM,TR,ML,MM,MR,BL,BM,BR
	private boolean gameover = false, computeropponent = false, toe = false;
	private final int row1[] =  {R.id.TopLeft,		R.id.TopRight,		R.id.TopMiddle};
	private final int row2[] =  {R.id.MiddleMiddle,R.id.MiddleLeft,	R.id.MiddleRight};
	private final int row3[] =  {R.id.BottomLeft,	R.id.BottomRight,	R.id.BottomMiddle};
	private final int col1[] =  {R.id.TopLeft,		R.id.BottomLeft,	R.id.MiddleLeft};
	private final int col2[] =  {R.id.MiddleMiddle,R.id.TopMiddle,		R.id.BottomMiddle};
	private final int col3[] =  {R.id.TopRight,	R.id.BottomRight,	R.id.MiddleRight};
	private final int ddown[] = {R.id.MiddleMiddle,R.id.TopLeft,		R.id.BottomRight};
	private final int dup[] =   {R.id.MiddleMiddle,R.id.TopRight,		R.id.BottomLeft};
	private final int rowsColsDiagonals[][] = {row1,row2,row3,col1,col2,col3,ddown,dup}; 
	private final int topLeftSquarePartOfWaysToWin[] = {0,3,6}, topMiddleSquarePartOfWaysToWin[] = {0,4}, topRightSquarePartOfWaysToWin[] = {0,5,7}; //TLs = top left square, is used in waysToWinPointCounter(0,3,6)
	private final int middleLeftSquarePartOfWaysToWin[] = {1,3}, middleMiddleSquarePartOfWaysToWin[] = {1,4,6,7},middleRightSquarePartOfWaysToWin[] = {1,5};
	private final int bottomLeftSquarePartOfWaysToWin[] = {2,3,7}, bottomMiddleSquarePartOfWaysToWin[] = {2,4}, bottomRightSquarePartOfWaysToWin[] = {2,5,6};
	private final int inTrio[][] = {topLeftSquarePartOfWaysToWin,topMiddleSquarePartOfWaysToWin,topRightSquarePartOfWaysToWin,middleLeftSquarePartOfWaysToWin,middleMiddleSquarePartOfWaysToWin,middleRightSquarePartOfWaysToWin,bottomLeftSquarePartOfWaysToWin,bottomMiddleSquarePartOfWaysToWin,bottomRightSquarePartOfWaysToWin}; //way to lookup which waysToWinPointCounter values need updating
	private final int corners[] = {R.id.TopLeft,R.id.TopRight,R.id.BottomLeft,R.id.BottomRight};
	private final List<Integer> squares = new ArrayList<Integer>(Arrays.asList(R.id.TopLeft,R.id.TopMiddle,R.id.TopRight,R.id.MiddleLeft,R.id.MiddleMiddle,R.id.MiddleRight,R.id.BottomLeft,R.id.BottomMiddle,R.id.BottomRight)); //TL,TM,TR,ML,MM,MR,BL,BM,BR
	private List<Integer> squaresremaining = new ArrayList<Integer>(Arrays.asList(R.id.TopLeft,R.id.TopMiddle,R.id.TopRight,R.id.MiddleLeft,R.id.MiddleMiddle,R.id.MiddleRight,R.id.BottomLeft,R.id.BottomMiddle,R.id.BottomRight)); //TL,TM,TR,ML,MM,MR,BL,BM,BR
	private List<Integer> squarestaken = new ArrayList<Integer>(); //TL,TM,TR,ML,MM,MR,BL,BM,BR
	private SensorManager sensMgr;
	private Sensor accelerometer;
    private SoundPool sounds;
    private int xbeep, obeep, toebeep, gamewin, gametie, complaugh, maybe; 
    private int randsound[] = {0,0};
    private boolean userWantsSound = true;
    private Random rand = new Random();
    private boolean computerTurn = false;
    private PopupWindow pwCredits;
    private int displayWidth, displayHeight;
    
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);    
		sensMgr = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = sensMgr.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        setContentView(R.layout.main);
        
        //Load sounds 
        SharedPreferences settings = getSharedPreferences("PREF",0);
        userWantsSound = settings.getBoolean("sound", true);
        if (userWantsSound) {
        	loadSounds();
        }

        //Get Dimensions of screen and size buttons based on that
	    Display display = getWindowManager().getDefaultDisplay();
	    displayWidth = display.getWidth();
	    displayHeight = display.getHeight();
	    int squareSize;
	    if (displayHeight > displayWidth) {
	    	//vertical
	    	squareSize = displayWidth/3;
	    }
	    else {
	    	//horizontal
	    	squareSize = displayHeight/4;
	    }
	    ImageButton imagebutton;
	    for (Integer squareid : squares) {
	    	imagebutton = (ImageButton) findViewById(squareid);
    		imagebutton.getLayoutParams().height = squareSize;
    		imagebutton.getLayoutParams().width = squareSize;
	    	imagebutton.setTag(BLANK_SQUARE);
	    }
	    
		showWhoseTurn();
		showScore();
		
		//Set up Vs Computer checkbox
		CheckBox cbAI =(CheckBox)findViewById(R.id.checkBoxAI);
	    cbAI.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
	    	public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
	    		if (arg1) {
	    			computeropponent = true;
	    			if (playerWhoseTurnItIs.equals("O"))
	    				selectComputerOpponentIntelligenceLevel();	
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
	    		if (moveCount > 0)
	    			startOver();
	    	}
	    });     
    }
    
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
      //Save gamestate for orientation change or interruption by phonecall, etc
      savedInstanceState.putString("gmoves", Arrays.toString(movesHistory).replace("[", "").replace("]", "").replace(" ", ""));
      savedInstanceState.putBoolean("gameover", gameover);
      savedInstanceState.putString("playername", playerWhoseTurnItIs);
      savedInstanceState.putBoolean("vscomputer", computeropponent);
      savedInstanceState.putBoolean("toe", toe);
      savedInstanceState.putInt("xwins", xNumberOfWins);
      savedInstanceState.putInt("owins", oNumberOfWins);
      savedInstanceState.putInt("twins", NumberOfTies);   
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
	    	if(button.getTag().equals((Integer) BLANK_SQUARE)) {
	    		button.setClickable(false);
	    		int playerIdentifyingMultiplier;
	    		int drawableSelected;
	    		if (toe) {
	    			//select random square that toe will step on after this move
	    			if  (checkIfTimeToSelectToeTarget()) {
	    				int randomsquare = rand.nextInt(squarestaken.size());
	    				toechosen = squarestaken.get(randomsquare);
	    				numberOfMovesAllowed += 2;
	    			}
	    		}
	    		if (playerWhoseTurnItIs.equals("X")) {
	    			playerIdentifyingMultiplier = -1;
	    			drawableSelected = X_SQUARE;
	    		}
	    		else {
	    			playerIdentifyingMultiplier = 1;
	    			drawableSelected = O_SQUARE;
	    		}
    			button.setBackgroundDrawable(getResources().getDrawable(drawableSelected));
    			button.setTag(drawableSelected);
    			
	    		int squareid = view.getId();
	    		
	    		for (Integer sqtrio : inTrio[squares.indexOf(squareid)])
	    			waysToWinPointCounter[sqtrio] += playerIdentifyingMultiplier;
	    		
  	    	  	squaresremaining.remove(new Integer(squareid));
  	    	  	squarestaken.add(new Integer(squareid));
  	    	  	movesHistory[moveCount] = playerIdentifyingMultiplier * squareid;
				moveCount += 1;				
				TextView tvTurn = (TextView) findViewById(R.id.textViewTurn);
				TextView tvScore = (TextView) findViewById(R.id.textViewScore);
				if (moveCount >= MINIMUM_MOVES_NEEDED_TO_WIN) {
		    		if (checkForWinCondition()) {
		    			buttonGlow(playerIdentifyingMultiplier);
		    			int winPointSum = 0;
		    			if (playerWhoseTurnItIs.equals("X")) {
		    				xNumberOfWins += 1;
		    				playSound(gamewin);
		    			}
		    			else {
		    				oNumberOfWins += 1; 
		    				if (computeropponent == true) {
		    					randomComputerTauntSound();
		    				}
		    				else
		    					playSound(gamewin);
		    			}
		    			
		    			tvTurn.setText("Player " + playerWhoseTurnItIs + " WINS!");
		    			tvScore.setText("Click Any Square To Start New Game");
		    			buttonsClickable(true);
		    			gameover = true;
		    			return;
		    		}
		    		else {
		    			if (moveCount == numberOfMovesAllowed) {
		    				playSound(gametie);
		    				tvTurn.setText("Game Over: Tie");
		    				NumberOfTies += 1;
		    				tvScore.setText("Click Any Square To Start New Game");
		    				buttonsClickable(true);
		    				gameover = true;
		    				return;
		    			}
		    		}
				}
	    		if (playerWhoseTurnItIs.equals("X"))
	    			playSound(xbeep);
	    		else
	    			playSound(obeep);
	    		playerWhoseTurnItIs = (playerWhoseTurnItIs.equals("X")) ? "O" : "X";
	    		showWhoseTurn();
	    		if (playerWhoseTurnItIs.equals("O") && computeropponent == true) {
	    			computerTurn = true;
	    			//delay
	    			Handler handler=new Handler();
	    			final Runnable r = new Runnable()
	    			{
	    			    public void run() 
	    			    {
	    			    	selectComputerOpponentIntelligenceLevel();
	    			    	computerTurn = false;
	    			    }
	    			};
	    			handler.postDelayed(r, 1000);
	    		}
	    		else if (toe) {	
	    			//The toe drops
	    			if (checkIfTimeToPlaceToe())
	    				playToe();
	    			//The toe leaves
	    			else if (checkIfTimeToRemoveToe()) {	
	    				showScore();
	    				removeToe();
	    			}
	    		} 
	    	}
    	}
    }
    
    public boolean checkForWinCondition() {
    	if (moveCount < MINIMUM_MOVES_NEEDED_TO_WIN) return false;    		
    	int copyForSorting[] = waysToWinPointCounter.clone();
    	Arrays.sort(copyForSorting);
    	if (copyForSorting[0] == X_WIN_POINT_SUM || copyForSorting[7] == O_WIN_POINT_SUM)
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
       			loadGame(settings.getString("gmoves",""),settings.getBoolean("gameover",gameover),settings.getString("playername",playerWhoseTurnItIs),settings.getBoolean("vscomputer",computeropponent),settings.getBoolean("toe",toe),settings.getInt("xwins",xNumberOfWins),settings.getInt("owins",oNumberOfWins),settings.getInt("twins",NumberOfTies));
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
			button.setBackgroundDrawable(getResources().getDrawable(BLANK_SQUARE));
			button.setTag(BLANK_SQUARE);
		}
		gameover = false;
		moveCount = 0;
		numberOfMovesAllowed = 9;
		Arrays.fill(waysToWinPointCounter, 0);
		Arrays.fill(movesHistory, 0);
		toechosen = 0;
		squarestaken.clear();
		startingplayer = (startingplayer.equals("X")) ? "O" : "X";
		playerWhoseTurnItIs = startingplayer;
		showWhoseTurn();
		showScore();
		buttonsClickable(true);
		//Player making the first turn alternates, if vs computer then make the first move
		if (startingplayer.equals("O") && computeropponent == true)
			selectComputerOpponentIntelligenceLevel();
	}

	public void showScore() {
		TextView tvScore = (TextView) findViewById(R.id.textViewScore);
		tvScore.setText("Score:   X:" + xNumberOfWins + "   O:" + oNumberOfWins + "   Tie:" + NumberOfTies);
	}
	
	public void showWhoseTurn() {
		TextView tvTurn = (TextView) findViewById(R.id.textViewTurn);
		tvTurn.setText("Player " + playerWhoseTurnItIs + "'s turn");
	}
	
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		return;		
	}

	public void onSensorChanged(SensorEvent event) {
		if(event.values[0] > 8) {
		  startOver();
		}
		return;
	}
	
	public void randomComputerMove() {
		//Dumb Computer AI
		int zeroCheck = squaresremaining.size();
		if (zeroCheck > 0) {
			int randomsquare = rand.nextInt(squaresremaining.size());
			ImageButton button = (ImageButton) findViewById(squaresremaining.get(randomsquare));
			//button.performClick();
			claimSquareProcess(button,true);
		}
	}
	
	public void smartComputerMove() {
		ImageButton button;
		//if first move, grab the center square
		if (moveCount == 0) {
  			button = (ImageButton) findViewById(R.id.MiddleMiddle);
  			claimSquareProcess(button,true);
  			return;
		}
		else {
			if (findSmartComputerMove(TWO_O_IN_A_ROW)) return; //look for O about to win and complete
			if (findSmartComputerMove(TWO_X_IN_A_ROW)) return; //look for X about to win and block
			
			//take center if it's still available
			button = (ImageButton) findViewById(R.id.MiddleMiddle);
  			if (button.getTag().equals((Integer) BLANK_SQUARE)) {
  				claimSquareProcess(button,true);
  				return;
  			}
  			
  			if (findSmartComputerMove(ONE_O_IN_A_ROW)) return; //look for O with one and build on it
  			
  			//take corner if possible
  			for (int corner = 0; corner < 4; corner++) { 
					button = (ImageButton) findViewById(corners[corner]);
					if (button.getTag().equals((Integer) BLANK_SQUARE)) {
						claimSquareProcess(button,true);
						return;
					}	
  			}
  			//might not ever get here
  			randomComputerMove();
		}
	}
	
	public boolean findSmartComputerMove(int targetvalue) {
		//look for scenario and deal with it
		//target = 2: look for a way to win
		//target = -2: look for a way opponent can win and block
		//target = 1: look for a place to build on
		ImageButton button;
		for (int way = 0; way < WAYS_TO_WIN; way++) { 
			if (waysToWinPointCounter[way] == targetvalue) {
				for (Integer squareid : rowsColsDiagonals[way]) {
  					button = (ImageButton) findViewById(squareid);
  					if (button.getTag().equals((Integer) BLANK_SQUARE)) {
  						claimSquareProcess(button,true);
  						return true;
  					}	
				}
			}
		}
		return false;
	}
	
	public void selectComputerOpponentIntelligenceLevel() {
		//alternate between easy mode and hard mode depending on who's winning
		if (oNumberOfWins >= xNumberOfWins) { 
			//dumb AI - random selection
			randomComputerMove();
		}
		else { 
			//smart AI 
			smartComputerMove();
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
 	

	public void buttonGlow(int winnerIndicator) {
		int glowColor, winPointSum;
		if (winnerIndicator == 1) {
			glowColor = HIGHLIGHTED_O_SQUARE;
			winPointSum = O_WIN_POINT_SUM;
		}
		else {
			glowColor = HIGHLIGHTED_X_SQUARE;
			winPointSum = X_WIN_POINT_SUM;
		}
		ImageButton button;
		for (int way = 0; way < WAYS_TO_WIN; way++) {
			if (waysToWinPointCounter[way] == winPointSum) {
				for (Integer squareid : rowsColsDiagonals[way]) {
					button = (ImageButton) findViewById(squareid);
					button.setBackgroundDrawable(getResources().getDrawable(glowColor));
				}
			}
		}
	}

	public void saveGame(String preffilename) {		
	    SharedPreferences settings = getSharedPreferences(preffilename, 0);
	    SharedPreferences.Editor editor = settings.edit();
	    editor.putString("gmoves", Arrays.toString(movesHistory).replace("[", "").replace("]", "").replace(" ", ""));
		editor.putBoolean("gameover", gameover);
		editor.putString("playername", playerWhoseTurnItIs);
		editor.putBoolean("vscomputer", computeropponent);
	    editor.putBoolean("toe", toe);
	    editor.putInt("xwins", xNumberOfWins);
	    editor.putInt("owins", oNumberOfWins);
	    editor.putInt("twins", NumberOfTies);
	    editor.commit();
	}
	
	public void playSound(int soundid) {
	    if (userWantsSound) 
	    	sounds.play(soundid, 1, 1, 1, 0, 1);
	}
	
	public void toggleSound() {		
		userWantsSound = !userWantsSound; 
		if (userWantsSound)
			loadSounds();
	    SharedPreferences settings = getSharedPreferences("PREF", 0);
	    SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean("sound", userWantsSound);
	    editor.commit();
	}
	
	public void randomComputerTauntSound() {
		//Random Computer Taunt
		if (userWantsSound) {
			int rsound = rand.nextInt(randsound.length);
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
			boolean remsound = userWantsSound;
			userWantsSound = false;
	        	        
	        computeropponent = lvscomputer;
		    CheckBox cb = (CheckBox) findViewById(R.id.checkBoxAI);
			if (computeropponent) {
				if (!cb.isChecked()) {
					playerWhoseTurnItIs = "X";
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
	            	playerWhoseTurnItIs = "X";
	            else
	            	playerWhoseTurnItIs = "O";
	            if (toehold && (checkIfTimeToRemoveToe())) {
	            	removeToe();
	            }
	            if (toehold && (checkIfTimeToPlaceToe())) {
	                toechosen = mov;
		            numberOfMovesAllowed += 2;
		            playToe();
	            }
	            else {
	            	button = (ImageButton) findViewById(Math.abs(mov));
	    	        claimSquareProcess(button,true);
	            }
	        }
	        
	        userWantsSound = remsound;
	        toe = toehold;
	        computeropponent = comphold;
	        
	        xNumberOfWins = Math.max(lxwins,xNumberOfWins);
	        oNumberOfWins = Math.max(lowins,oNumberOfWins);
	        NumberOfTies = Math.max(ltwins,NumberOfTies);
	        showScore();
		
			gameover = lgameover;
			if (!gameover) {
				playerWhoseTurnItIs = lplayername;
				showWhoseTurn();
	    		if (playerWhoseTurnItIs.equals("O") && computeropponent == true) {
	    			selectComputerOpponentIntelligenceLevel();		
	    		}
			}

    	}catch(NullPointerException e){
    		startOver();
    	}
	}
	
	public void clearScore() {
		xNumberOfWins = 0;
		oNumberOfWins = 0;
		NumberOfTies = 0;
		showScore();
	}
	
	private void showCredits() {
		/*Show credits popup window when trigged from menu.*/
	    try {
	        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        //Inflate the view from a predefined XML layout
	        View layout = inflater.inflate(R.layout.credits,(ViewGroup) findViewById(R.id.popupCredits));
	        // create a 300px width and 470px height PopupWindow
	        int popupWidth = (int) (displayWidth * 0.6);
	        int popupHeight = (int) (displayHeight * 0.6);
	        pwCredits = new PopupWindow(layout, popupWidth, popupHeight, true);		 
	        // display the popup in the center
	        pwCredits.showAtLocation(layout, Gravity.CENTER, 0, 0);
	 
	        layout.setOnClickListener(dismissCredits);
	 
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	 
	private OnClickListener dismissCredits = new OnClickListener() {
		/*Click anywhere to dismiss Credits popup window*/
	    public void onClick(View v) {
	        pwCredits.dismiss();
	    }
	};
	
	public void removeToe() {
		/*The Toe stays on the board until the next move is made.*/
		squaresremaining.add(new Integer(toechosen));
		squarestaken.remove(new Integer(toechosen));

    	ImageButton button = (ImageButton) findViewById(toechosen);
 		button.setBackgroundDrawable(getResources().getDrawable(BLANK_SQUARE));
 		button.setTag(BLANK_SQUARE);
 		button.setClickable(true);
 		playSound(toebeep);
	}
	
	public boolean checkIfTimeToSelectToeTarget() {
		if (moveCount == 2 || moveCount == 6 || moveCount == 10)
			return true;
		return false;
	}

	public boolean checkIfTimeToPlaceToe() {
		if (moveCount == 3 || moveCount == 7 || moveCount == 11)
			return true;
		return false;
	}
	
	public boolean checkIfTimeToRemoveToe() {
		if (moveCount == 5 || moveCount == 9 || moveCount == 13)
			return true;
		return false;
	}
	
	public void playToe() {
		/*ToeJam is a mod of TicTacToe where a Toe drops every 3 moves to randomly squash one of the existing moves.
		If TTT is played correctly, there will always be a tie. ToeJam adds the random factor to make it a game you can win.
		The Toe occupies the square until it leaves, after the next move is made.*/
	 	ImageButton button = (ImageButton) findViewById(toechosen);
	 	int pvalue = 0;
		if (button.getTag().equals((Integer) X_SQUARE)) 
   			pvalue = 1;
   		else if (button.getTag().equals((Integer) O_SQUARE))
   			pvalue = -1;
		
		for (Integer sqtrio : inTrio[squares.indexOf(toechosen)])
			waysToWinPointCounter[sqtrio] += pvalue;
		
		movesHistory[moveCount] = toechosen;
		moveCount += 1;
		
    	button = (ImageButton) findViewById(toechosen);
    	button.setBackgroundDrawable(getResources().getDrawable(TOE_SQUARE));
    	button.setTag(TOE_SQUARE);
 		playSound(toebeep);
	    TextView tvScore = (TextView) findViewById(R.id.textViewScore);
	    tvScore.setText("Toe Jam!");
 	}
 }
 