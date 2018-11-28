package sachin.sdmp.com.assignment4;


import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;


//Player2 strategy is simply generate random numbers

public class Player2 extends Fragment {
    //Intializations
    String  secretNumber;
    public Player2.Player2Thread player2Thread;
    private Result gResult;
    public List<Result> player2Results = new ArrayList<>();

    //Default Constructor
    public Player2(){
    }

    public void clearViews() {

        player2Results = new ArrayList<>();
        if (getView() != null) {
            ListView guessesList = (ListView) getView().findViewById(R.id.list_item);
            guessesList.setAdapter(new ListAdapter(getContext(), player2Results));
        }
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (player2Thread == null) {
            player2Thread= new Player2.Player2Thread("Player Two", ((MainActivity) getActivity()).mainHandler);
        }

    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        setRetainInstance(true);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_player, container, false);
    }

    // Values to be used by handleMessage()
    public static final int GENERATE_SECRET_NUMBER = 0 ;

    public void initializeHandlerThread() {
        player2Thread = new Player2.Player2Thread("Player Two", ((MainActivity) getActivity()).mainHandler);
    }


    //*******************************************************Player2 Thread***********************************************************

    public class Player2Thread extends HandlerThread {

        //handlers
        Handler player2Handler;
        Handler player1Handler;
        Handler mainHandler;
        //Constructor
        public Player2Thread(String name,Handler mainHandler) {
            super(name);
            this.mainHandler = mainHandler;
        }
        //Secret Number Generator
        public void generateSecretNumber(){
            secretNumber = Utility.generateRandomNumber(1);
            Message msg = mainHandler.obtainMessage(MainActivity.SET_PLAYER2_SECRET_NUMBER) ;
            msg.arg1 = 0;
            for(int i =0;i<secretNumber.length();i++){
                int temp = Character.getNumericValue(secretNumber.charAt(i));
                msg.arg1 = msg.arg1*10 + temp;
            }
            Log.i("Player 2","Sending secret Number to UI Thread");
            mainHandler.sendMessage(msg);
        }
        //Generates Guess
        String generateNextGuess() {
            Log.i("Thread 2: ", "generating next guess");
            String nextGuess= Utility.generateRandomNumber(1);
            return nextGuess;
        }

        @Override
        protected void onLooperPrepared() {
            super.onLooperPrepared();
            player2Handler = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    int what = msg.what ;
                    Player1.Player1Thread player1Thread = ((Player1) getFragmentManager().findFragmentById(R.id.player1Frag)).player1Thread;
                    switch (what) {
                        case GENERATE_SECRET_NUMBER:
                            Log.i("Thread 2 Looper","Generating secretNumber");
                            generateSecretNumber();
                            break;
                        case Utility.Constants.CONTINUE:
                            int postTurn = (Integer) msg.obj;
                            String nGuess = generateNextGuess();
                            String[] postMsgObjStrArray = {nGuess, String.valueOf(postTurn)};
                            // send next guess to player1
                            if (player1Thread != null && player1Thread.isAlive()) {
                                player1Handler = player1Thread.player1Handler;
                                Message guessMsg = player1Handler.obtainMessage(Utility.Constants.NEXT_GUESS, 0, 0, postMsgObjStrArray);
                                player1Handler.sendMessage(guessMsg);
                            }
                            break;
                        //Parse Guess of player1 and sends result back
                        case Utility.Constants.NEXT_GUESS:
                            String[] getMsgObjStrArray = (String[]) msg.obj;
                            String nextGuess = getMsgObjStrArray[0];
                            int getTurn = Integer.parseInt(getMsgObjStrArray[1]);
                            // get the guess results
                            Result result = Result.getGuessResults(getTurn, secretNumber, nextGuess);
                            // send guess results to player1
                            if (player1Thread != null && player1Thread.isAlive()) {
                                player1Handler = player1Thread.player1Handler;
                                Message resultMsg = player1Handler.obtainMessage(Utility.Constants.GUESS_RESULTS, 0, 0, result);
                                player1Handler.sendMessage(resultMsg);
                            }

                            break;
                        case Utility.Constants.GUESS_RESULTS:
                            gResult = (Result) msg.obj;
                            player2Results.add(gResult);
                            if (gResult.getRightPosition() == 4) {
                                mainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {

                                        ((MainActivity) getActivity()).playerWon = 2;

                                    }

                                });
                            }
                            // update the list view
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    ListView guesses_list = (ListView) getActivity().getFragmentManager()
                                            .findFragmentById(R.id.player2Frag).getView().findViewById(R.id.list_item);
                                    guesses_list.setAdapter(new ListAdapter(getContext(), player2Results));
                                    ((MainActivity) getActivity()).turns++;
                                    ((MainActivity) getActivity()).runGame();
                                }
                            });

                            //To delay after each turn
                            try {
                                Thread.sleep(1500);
                            } catch (InterruptedException e) {

                            }
                            break;
                    }

                }
            };
        }
    }




}
