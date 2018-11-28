package sachin.sdmp.com.assignment4;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

//This is the main Activity class of the Application
//It consists of 2 textviews, 2 Fragments and a button

public class MainActivity extends Activity {

    //Initialisation
    protected Button button;
    protected TextView player1TextView;
    protected TextView player2TextView;
    protected Player1 player1Fragment;
    protected Player2 player2Fragment;
    protected Handler  mainHandler;
    public int turns = 1;
    public int prevTurn = -1;
    protected Player1.Player1Thread player1Thread;
    protected Player2.Player2Thread player2Thread;
    protected Handler player1Handler;
    protected Handler player2Handler;
    //0 = no winner, 1 = player 1 won, 2 = player 2 won
    public int playerWon = 0;

    // Values to be used by handleMessage()
    public static final int SET_PLAYER1_SECRET_NUMBER = 0 ;
    public static final int SET_PLAYER2_SECRET_NUMBER= 1 ;

    //On Create Method
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("Main Activity:","On create method");
        //Setting up all initialisations
            setContentView(R.layout.mainlayout);
            button = (Button)findViewById(R.id.button);
            player1TextView =(TextView)findViewById(R.id.player1TextView);
            player2TextView =(TextView)findViewById(R.id.player2TextView);

        //Main Handler of UI Thread
            mainHandler = new Handler() {
            public void handleMessage(Message msg) {
                int what = msg.what ;
                switch (what) {
                    case SET_PLAYER1_SECRET_NUMBER:
                        player1TextView.setText("Player 1 Secret Number:"+msg.arg1);
                        break;
                    case SET_PLAYER2_SECRET_NUMBER:
                        player2TextView.setText("Player 2 Secret Number:"+msg.arg1);
                        break;
                }

            }
        };
            //Intialzing the fragments
              player1Fragment = (Player1) getFragmentManager().findFragmentById(R.id.player1Frag);
              player2Fragment = (Player2) getFragmentManager().findFragmentById(R.id.player2Frag);


        //On Button click
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                 // clear main handler message queue
                mainHandler.removeCallbacksAndMessages(null);
                //Kill player Looper
                killLoopers();
                // kill any  running player threads
                KillThreads();

                button.setText("Restart Game");

                // clear fragment views
                player1Fragment.clearViews();
                player2Fragment.clearViews();
                playerWon = 0;
                turns =1;
                prevTurn =-1;

                player1Fragment.initializeHandlerThread();
                player1Thread = player1Fragment.player1Thread;
                player1Handler =player1Thread.player1Handler;
                player1Thread.start();
                player2Fragment.initializeHandlerThread();
                player2Thread = player2Fragment.player2Thread;
                player2Handler =player2Thread.player2Handler;
                player2Thread.start();



                // get player one handler
                player1Handler = player1Thread.player1Handler;
                // if player one handler is not ready, wait.
                while (player1Handler == null) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    player1Handler = player1Thread.player1Handler;
                }


                player2Handler = player2Thread.player2Handler;
                // if player two handler is not ready, wait
                while (player2Handler == null) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    player2Handler = player2Thread.player2Handler;
                }
                //Generate and set secret number Fields
                Message player1Msg = player1Handler.obtainMessage(Player1.GENERATE_SECRET_NUMBER);
                player1Handler.sendMessage(player1Msg);

                Message player2Msg = player1Handler.obtainMessage(Player2.GENERATE_SECRET_NUMBER);
                player2Handler.sendMessage(player2Msg);
                // let both players wait 3s
                player1Handler.post(new Runnable() {
                    @Override
                    public void run() {
                       Utility.sleepThread();
                    }
                });
                player2Handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Utility.sleepThread();
                    }
                });
                runGame();
            }
        });


    }


    //manages the Game
    public void runGame() {

        Log.i("runGame():", "Turn = " + turns);
        // if no player has won yet
        if (playerWon == 0 && turns < 40) {
            // if both players played their turns
            if (turns == prevTurn + 2) {
                // get turn value for each player for total turns
                int playerTurn = turns - ((turns - 1) / 2);

                if (player1Handler == null) {
                    player1Thread = player1Fragment.player1Thread;
                    player1Handler = player1Thread.player1Handler;
                }
                if (player2Handler == null) {
                    player2Thread = player2Fragment.player2Thread;
                    player2Handler = player2Thread.player2Handler;
                }
                // send CONTINUE message to play next turn
                Message msgOne = player1Handler.obtainMessage(Utility.Constants.CONTINUE, 0, 0, playerTurn);
                player1Handler.sendMessage(msgOne);
                Message msgTwo = player2Handler.obtainMessage(Utility.Constants.CONTINUE, 0, 0, playerTurn);
                player2Handler.sendMessage(msgTwo);
                prevTurn = turns;
            }
        }
        else {
            if (playerWon == 1) {
                             Toast.makeText(MainActivity.this, Utility.Constants.PLAYER_ONE_WON, Toast.LENGTH_LONG).show();
            } else if (playerWon == 2) {
                Toast.makeText(MainActivity.this, Utility.Constants.PLAYER_TWO_WON, Toast.LENGTH_LONG).show();
            } else if (turns >= 40) {
                // turns limit reached, no winner
                Toast.makeText(MainActivity.this, Utility.Constants.NO_WINNER, Toast.LENGTH_LONG).show();
            }
            // kill threads
            KillThreads();
            // clear and stop loopers
            killLoopers();
        }
    }

    //Function to Kill Threads
    private void KillThreads() {
        if (player1Thread != null && player1Thread.isAlive()) {
            player1Thread.interrupt();
        }
        if (player2Thread != null && player2Thread.isAlive()) {

            player2Thread.interrupt();
        }

    }
    //Function to Kill Loopers
    private void killLoopers() {

        if ((player2Thread != null) && (player2Thread.isAlive())) {
            player2Handler.post(new Runnable() {
                @Override
                public void run() {

                    player2Handler.removeCallbacksAndMessages(null);
                    player2Handler.getLooper().quit();

                }
            });
        }

        if ((player1Thread != null) && (player1Thread.isAlive())) {
            Log.i("MainActivity:", "clearing message Queue");
            player1Handler.post(new Runnable() {
                @Override
                public void run() {
                    if (player1Handler != null) {
                        player1Handler.removeCallbacksAndMessages(null);
                        player1Handler.getLooper().quit();
                    }

                }
            });
        }

    }



















}
