package be.kuleuven.cs.chikwadraat.socialfridge.dish;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import be.kuleuven.cs.chikwadraat.socialfridge.Application;
import be.kuleuven.cs.chikwadraat.socialfridge.R;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Dish;
import be.kuleuven.cs.chikwadraat.socialfridge.model.DishItem;
import be.kuleuven.cs.chikwadraat.socialfridge.util.AdapterUtils;


/**
 * A fragment displaying a dish's ingredients.
 */
public class DishIngredientsFragment extends Fragment {

    private static final String STATE_DISH = "dish";

    private ListView ingredientsList;
    private IngredientsListAdapter ingredientsAdapter;

    private Dish dish;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DishHeaderFragment.
     */
    public static DishIngredientsFragment newInstance() {
        DishIngredientsFragment fragment = new DishIngredientsFragment();
        return fragment;
    }

    public DishIngredientsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            // Restore
            dish = savedInstanceState.getParcelable(STATE_DISH);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dish_ingredients, container, false);

        ingredientsList = (ListView) view.findViewById(R.id.dish_ingredients);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ingredientsAdapter = new IngredientsListAdapter(getActivity());
        ingredientsList.setAdapter(ingredientsAdapter);

        updateDish();
    }

    public void setDish(Dish dish) {
        this.dish = dish;
        updateDish();
    }

    private void updateDish() {
        if (ingredientsAdapter == null) return;
        if (dish != null) {
            AdapterUtils.setAll(ingredientsAdapter, dish.getItems());
        } else {
            ingredientsAdapter.clear();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_DISH, dish);
    }

    public class IngredientsListAdapter extends ArrayAdapter<DishItem> {

        public IngredientsListAdapter(Context context) {
            super(context, R.layout.dish_ingredient_list_item);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            ViewHolder vh;
            if (v == null) {
                v = View.inflate(getContext(), R.layout.dish_ingredient_list_item, null);
                vh = new ViewHolder(v);
                v.setTag(vh);
            } else {
                vh = (ViewHolder) v.getTag();
            }

            DishItem item = getItem(position);
            vh.position = position;
            vh.pictureView.setImageUrl(item.getIngredient().getThumbnailURL(), Application.get().getImageLoader());
            vh.nameView.setText(item.getIngredient().getName());
            vh.quantityView.setText(item.getMeasure().toString());

            return v;
        }

        private class ViewHolder {
            TextView nameView;
            TextView quantityView;
            NetworkImageView pictureView;
            int position;

            private ViewHolder(View v) {
                nameView = (TextView) v.findViewById(R.id.ingredient_name);
                pictureView = (NetworkImageView) v.findViewById(R.id.ingredient_pic);
                quantityView = (TextView) v.findViewById(R.id.item_quantity);
            }
        }

    }

}
