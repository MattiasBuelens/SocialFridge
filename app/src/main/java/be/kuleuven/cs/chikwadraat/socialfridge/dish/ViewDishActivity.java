package be.kuleuven.cs.chikwadraat.socialfridge.dish;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import be.kuleuven.cs.chikwadraat.socialfridge.BaseActivity;
import be.kuleuven.cs.chikwadraat.socialfridge.R;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Dish;
import be.kuleuven.cs.chikwadraat.socialfridge.party.CreatePartyActivity;

/**
 * Activity to view a dish.
 */
public class ViewDishActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "ViewDishActivity";

    public static final String EXTRA_DISH = "dish_object";

    private DishHeaderFragment dishHeader;

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

        updateDish();
    }

    protected Dish getDish() {
        return dish;
    }

    private void updateDish() {
        dishHeader.setDish(getDish());
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

}


