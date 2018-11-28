package sachin.sdmp.com.assignment4;


import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 */

//Player 1 startergy is to first generate all 4 digit numbers and
//then prune out all digits which is not possible by guesses provided by player2
public class Player1 extends Fragment {
    //Initialisations
    String  secretNumber;
    public Player1Thread player1Thread;
    private Result gResult;
    public List<Result> player1Results;
    //Constructor
    public Player1(){
    }
    //clears current buffer and fragment views
    public void clearViews() {
        player1Results = new ArrayList<>();
        if (getView() != null) {
            ListView guessesList = (ListView) getView().findViewById(R.id.list_item);
            guessesList.setAdapter(new ListAdapter(getContext(), player1Results));
            gResult = null;
        }
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
        player1Thread = new Player1Thread("Player One", ((MainActivity) getActivity()).mainHandler);
    }


    //*******************************************************Player1 Thread***********************************************************

    public class Player1Thread extends HandlerThread{
        //Handlers
       Handler player1Handler;
       Handler player2Handler;
       Handler mainHandler;
        private Set<String> allNumbers;
        public Player1Thread(String name,Handler mainHandler) {
                super(name);
                this.mainHandler = mainHandler;
                generateAllNumbers();
            }
        //generates Secret Number
            public void generateSecretNumber(){
                //Calls Utility.genrate Random Number function to generate 4 digit random number
                secretNumber = Utility.generateRandomNumber(1);

                Message msg = mainHandler.obtainMessage(MainActivity.SET_PLAYER1_SECRET_NUMBER) ;
                msg.arg1 = 0;
                //converting number in string format to integer
                for(int i =0;i<secretNumber.length();i++){
                    int temp = Character.getNumericValue(secretNumber.charAt(i));
                    msg.arg1 = msg.arg1*10 + temp;
                }
                Log.i("Player 1","Sending secret Number to UI Thread");
                mainHandler.sendMessage(msg);
            }
        //genrates all possible unique 4 digit number
        public void generateAllNumbers() {
            allNumbers = new HashSet<>();
            Set<String> numbersToRemove = new HashSet<>();
            //All possible 4 digits are formed
            for (int i = 100; i < 1000; i++) {
                allNumbers.add("0" + String.valueOf(i));
            }
            for (int i = 1000; i < 10000; i++) {
                allNumbers.add(String.valueOf(i));
            }
            //pruning of non unique 4 digit number
            for (String number : allNumbers) {
                Set<Character> uniqueDigits;
                uniqueDigits = new HashSet<>();
                for (int i = 0; i < 4; i++) {

                    uniqueDigits.add(number.charAt(i));

                }
                if (uniqueDigits.size() != 4) {
                    numbersToRemove.add(number);
                }
            }

            allNumbers.removeAll(numbersToRemove);
        }
        //Generates Next guess
        String generateNextGuess(Result prevResult, int turn) {
            Set<String> numbersToRemove = new HashSet<>();
            String nextGuess;
            if (prevResult == null) {
                nextGuess = Utility.generateRandomNumber(1);
            } else {
                String prevGuess = prevResult.getGuess();
                int prevRightPos = prevResult.getRightPosition();
                int prevWrongPos = prevResult.getWrongPosition();
                for (String possibleNumber : allNumbers) {
                    Result gResult = Result.getGuessResults(turn, prevGuess, possibleNumber);
                    if ((gResult.getRightPosition() != prevRightPos) || (gResult.getWrongPosition() != prevWrongPos)) {
                        numbersToRemove.add(possibleNumber);
                    }
                }
                allNumbers.removeAll(numbersToRemove);
                nextGuess = allNumbers.iterator().next();
            }
            return nextGuess;
            
        }

        @Override
        protected void onLooperPrepared() {
            super.onLooperPrepared();
            player1Handler = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    Player2.Player2Thread player2Thread = ((Player2) getFragmentManager().findFragmentById(R.id.player2Frag)).player2Thread;
                    int what = msg.what ;
                    switch (what) {
                        //Generates Secret Number
                        case GENERATE_SECRET_NUMBER:
                            Log.i("Thread 1 Looper","Generating secretNumber");
                            generateSecretNumber();
                            break;
                        case Utility.Constants.CONTINUE:

                            int postTurn = (Integer) msg.obj;
                            String nGuess = generateNextGuess(gResult, postTurn);
                            // build message object containing next guess and turn
                            String[] postMsgObjStrArray = {nGuess, String.valueOf(postTurn)};

                            // send next guess to player two
                            if (player2Thread != null && player2Thread.isAlive()) {
                                player2Handler = player2Thread.player2Handler;
                                Message guessMsg = player2Handler.obtainMessage(Utility.Constants.NEXT_GUESS, 0, 0, postMsgObjStrArray);
                                player2Handler.sendMessage(guessMsg);
                            }
                            break;
                        // process player two's next guess for player one's secret number
                        case Utility.Constants.NEXT_GUESS:
                             String[] getMsgObjStrArray = (String[]) msg.obj;
                            // get next guess from message object
                            String nextGuess = getMsgObjStrArray[0];
                            // get turn from message object
                            int getTurn = Integer.parseInt(getMsgObjStrArray[1]);
                            Result result = Result.getGuessResults(getTurn, secretNumber, nextGuess);
                            // send guess result to player2
                            if (player2Thread != null && player2Thread.isAlive()) {
                                player2Handler = player2Thread.player2Handler;
                                Message resultMsg = player2Handler.obtainMessage(Utility.Constants.GUESS_RESULTS, 0, 0, result);
                                player2Handler.sendMessage(resultMsg);
                            }
                            break;
                            //Stroring Guess Results and checking Guess Values
                        case Utility.Constants.GUESS_RESULTS:
                            gResult = (Result) msg.obj;
                            player1Results.add(gResult);
                            // if player one got all right positions, set playerWon as 1
                            if (gResult.getRightPosition() == 4) {
                                mainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {

                                        ((MainActivity) getActivity()).playerWon = 1;

                                    }

                                });

                            }
                            // update the list view with new appended list
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    ListView guesses_list = (ListView) getActivity().getFragmentManager().findFragmentById(R.id.player1Frag).getView().findViewById(R.id.list_item);
                                    guesses_list.setAdapter(new ListAdapter(getContext(), player1Results));
                                    ((MainActivity) getActivity()).turns++;
                                    ((MainActivity) getActivity()).runGame();
                                }
                            });

                            // delay after each turn
                            try {
                                Thread.sleep(1600);
                            } catch (InterruptedException e) {
                            }
                            break;
                            }

                }
            };
        }
    }




}
