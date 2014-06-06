package be.kuleuven.cs.chikwadraat.socialfridge.dish;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import be.kuleuven.cs.chikwadraat.socialfridge.Application;
import be.kuleuven.cs.chikwadraat.socialfridge.BaseActivity;
import be.kuleuven.cs.chikwadraat.socialfridge.R;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Dish;
import be.kuleuven.cs.chikwadraat.socialfridge.model.DishItem;
import be.kuleuven.cs.chikwadraat.socialfridge.party.CreatePartyActivity;
import be.kuleuven.cs.chikwadraat.socialfridge.util.AdapterUtils;

/**
 * Activity to view a dish.
 */
public class ViewDishActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "ViewDishActivity";

    public static final String EXTRA_DISH = "dish_object";

    private DishHeaderFragment dishHeader;
    private ListView ingredientsList;
    private IngredientsListAdapter ingredientsAdapter;

    private Dish dish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dish_view);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_DISH)) {
            dish = intent.getParcelableExtra(EXTRA_DISH);
        } else {
            throw new IllegalArgumentException("Missing required dish in intent");
        }

        dishHeader = (DishHeaderFragment) getSupportFragmentManager().findFragmentById(R.id.dish_header);
        ingredientsList = (ListView) findViewById(R.id.dish_ingredients);

        findViewById(R.id.dish_action_create_party).setOnClickListener(this);

        ingredientsAdapter = new IngredientsListAdapter(this);
        ingredientsList.setAdapter(ingredientsAdapter);

        updateDish();
    }

    protected Dish getDish() {
        return dish;
    }

    private void updateDish() {
        dishHeader.setDish(getDish());
        AdapterUtils.setAll(ingredientsAdapter, dish.getItems());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dish_action_create_party:
                createParty();
                break;
        }
    }

    private void createParty() {
        Intent intent = new Intent(this, CreatePartyActivity.class);
        intent.putExtra(CreatePartyActivity.EXTRA_DISH, getDish());
        startActivity(intent);
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


