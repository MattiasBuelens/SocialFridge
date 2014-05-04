package be.kuleuven.cs.chikwadraat.socialfridge.fridge;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import be.kuleuven.cs.chikwadraat.socialfridge.R;
import be.kuleuven.cs.chikwadraat.socialfridge.measuring.Unit;
import be.kuleuven.cs.chikwadraat.socialfridge.model.FridgeItem;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Measure;
import be.kuleuven.cs.chikwadraat.socialfridge.util.AdapterUtils;

/**
 * Fragment displaying the ingredients in a fridge.
 * Use the {@link IngredientsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class IngredientsFragment extends Fragment {

    private static final String TAG = "IngredientsFragment";

    private static final int LOADER_INGREDIENTS = 1;

    private ListView ingredientsList;
    private IngredientsListAdapter ingredientsAdapter;

    /**
     * Create a new candidates fragment.
     *
     * @return A new instance of fragment CandidatesFragment.
     */
    public static IngredientsFragment newInstance() {
        IngredientsFragment fragment = new IngredientsFragment();
        return fragment;
    }

    public IngredientsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fridge_ingredients, container, false);

        ingredientsList = (ListView) view.findViewById(R.id.fridge_ingredient_list);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ingredientsAdapter = new IngredientsListAdapter(getActivity());

        // TODO Remove dummy items
        List<FridgeItem> items = new ArrayList<FridgeItem>();
        items.add(new FridgeItem("Eggs", R.drawable.eggs, new Measure(6, Unit.PIECES)));
        AdapterUtils.setAll(ingredientsAdapter, items);

        ingredientsList.setAdapter(ingredientsAdapter);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public class IngredientsListAdapter extends ArrayAdapter<FridgeItem> {

        public IngredientsListAdapter(Context context) {
            super(context, R.layout.fridge_list_item);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            ViewHolder vh;
            if (v == null) {
                v = View.inflate(getContext(), R.layout.fridge_list_item, null);
                vh = new ViewHolder(v);
                v.setTag(vh);
            } else {
                vh = (ViewHolder) v.getTag();
            }

            FridgeItem item = getItem(position);

            String nameText = item.getName();
            Drawable picture = item.getPicture(getContext());
            String quantityText = item.getQuantity().toString();

            vh.position = position;
            vh.nameView.setText(nameText);
            vh.pictureView.setImageDrawable(picture);
            vh.quantityView.setText(quantityText);

            return v;
        }

        private class ViewHolder {
            TextView nameView;
            TextView quantityView;
            ImageView pictureView;
            int position;

            private ViewHolder(View v) {
                nameView = (TextView) v.findViewById(R.id.ingredient_name);
                pictureView = (ImageView) v.findViewById(R.id.ingredient_pic);
                quantityView = (TextView) v.findViewById(R.id.item_quantity);
            }
        }

    }

}
