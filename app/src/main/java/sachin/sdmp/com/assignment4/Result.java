package sachin.sdmp.com.assignment4;


class Result {

    // player turn
    private int turn;
    // guessed number
    private String guess;
    // right positions
    private int rightPosition;
    // wrong positions
    private int wrongPosition;

    private Result(int turn, String guess, int rightPos, int wrongPos) {
        this.turn = turn;
        this.guess = guess;
        this.rightPosition = rightPos;
        this.wrongPosition = wrongPos;
    }


    static Result getGuessResults(int turn, String secretNumber, String nextGuess) {

        int rPos = 0;
        int wPos = 0;
        int[] numbers = new int[10];
        for (int i = 0; i < secretNumber.length(); i++) {
            int s = Character.getNumericValue(secretNumber.charAt(i));
            int g = Character.getNumericValue(nextGuess.charAt(i));
            if (s == g) rPos++;
            else {
                if (numbers[s] < 0) wPos++;
                if (numbers[g] > 0) wPos++;
                numbers[s]++;
                numbers[g]--;
            }
        }
        return new Result(turn, nextGuess, rPos, wPos);

    }

    int getTurn() {
        return turn;
    }


    String getGuess() {
        return guess;
    }


    int getRightPosition() {
        return rightPosition;
    }


    int getWrongPosition() {
        return wrongPosition;
    }

}
