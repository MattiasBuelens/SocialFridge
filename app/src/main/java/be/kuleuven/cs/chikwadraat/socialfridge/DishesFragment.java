package be.kuleuven.cs.chikwadraat.socialfridge;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Login fragment.
 */
public class DishesFragment extends ListFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        List<Dish> dishes = new ArrayList<Dish>();
        dishes.add(new Dish("Spaghetti Bolognaise", "Blablabla", R.drawable.spaghetti));

        ArrayAdapter<Dish> adapter = new ArrayAdapter<Dish>(getActivity(), R.layout.dish_list_item, dishes);
        setListAdapter(adapter);

        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
