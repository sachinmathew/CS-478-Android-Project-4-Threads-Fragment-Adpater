package sachin.sdmp.com.assignment4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//Genearl Utility Class
public class Utility {
    static void sleepThread(){
        try {
                Thread.sleep(3000);
            }
            catch (InterruptedException e) {
                    e.printStackTrace(); }
    }

    // generate 4 didgit random number
    static String generateRandomNumber(int digit) {
        List<Integer> numbers = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            numbers.add(i);
        }
        Collections.shuffle(numbers);
        String secretNumber = "";
        for (int i = 0; i < 4; i++) {
            secretNumber += numbers.get(i).toString();
        }
        return secretNumber;
    }

    // List of Constants
    static class Constants {

        static final int CONTINUE = 1;
        static final int NEXT_GUESS = 2;
        static final int GUESS_RESULTS = 3;
        static final String PLAYER_ONE_WON = "Player 1 won!";
        static final String PLAYER_TWO_WON = "Player 2 won!";
        static final String NO_WINNER = "Game Over! No winner!";
    }



}

