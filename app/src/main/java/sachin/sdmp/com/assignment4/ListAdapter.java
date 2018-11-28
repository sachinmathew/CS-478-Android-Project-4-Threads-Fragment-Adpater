package sachin.sdmp.com.assignment4;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

class ListAdapter extends BaseAdapter {

    private Context context;
    private List<Result> results;

    // create new list adapter and set results
    ListAdapter(Context c, List<Result> results) {
        context = c;
        this.results = new ArrayList<>();
        for (int i = results.size() - 1; i >= 0; i--) {
            this.results.add(results.get(i));
        }
    }


    @Override
    public int getCount() {
        return results.size();
    }

    @Override
    public Object getItem(int position) {
        return results.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItem;

        // recycle existing views
        if (convertView == null) {

            // get the layout inflater
            LayoutInflater inflater = LayoutInflater.from(context);
            listItem = inflater.inflate(R.layout.guesses_list_item, parent, false);
        } else {
            listItem = convertView;
        }

            TextView guessedNumber = (TextView) listItem.findViewById(R.id.guessed_number);
            TextView clue = (TextView) listItem.findViewById(R.id.hints);
            String guessedItem = "Turn: " + results.get(position).getTurn() + "\nGuessed number: "+ results.get(position).getGuess();
            guessedNumber.setText(guessedItem);
            String clueItem = "Correct positions: " + results.get(position).getRightPosition()+"\nWrong positions: "+ results.get(position).getWrongPosition();
            clue.setText(clueItem);
        return listItem;
    }
}
