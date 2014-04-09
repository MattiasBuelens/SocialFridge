package be.kuleuven.cs.chikwadraat.socialfridge;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import be.kuleuven.cs.chikwadraat.socialfridge.model.Dish;

/**
 * Dishes activity.
 */
public class DishesActivity extends ListActivity {

    private static final String TAG = "DishesActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        List<Dish> dishes = new ArrayList<Dish>();
        dishes.add(new Dish("Spaghetti Bolognaise", "Blablabla", R.drawable.spaghetti));

        setListAdapter(new DishesArrayAdapter(this, dishes));
    }

    public static class DishesArrayAdapter extends ArrayAdapter<Dish> {

        public DishesArrayAdapter(Context context, Dish[] dishes) {
            this(context, Arrays.asList(dishes));
        }

        public DishesArrayAdapter(Context context, List<Dish> dishes) {
            super(context, R.layout.dish_list_item, dishes);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = View.inflate(getContext(), R.layout.dish_list_item, null);
            }

            TextView nameView = (TextView) view.findViewById(R.id.text1);
            TextView descView = (TextView) view.findViewById(R.id.text2);
            ImageView imageView = (ImageView) view.findViewById(R.id.image);

            Dish dish = getItem(position);
            nameView.setText(dish.getName());
            descView.setText(dish.getDescription());
            imageView.setImageResource(dish.getImageResource());

            return view;
        }
    }
}
