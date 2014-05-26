package be.kuleuven.cs.chikwadraat.socialfridge.dish;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import be.kuleuven.cs.chikwadraat.socialfridge.Application;
import be.kuleuven.cs.chikwadraat.socialfridge.R;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Dish;


/**
 * A header fragment displaying a dish's name and picture.
 */
public class DishHeaderFragment extends Fragment {

    private static final String STATE_DISH = "dish";

    private NetworkImageView imageView;
    private TextView nameView;

    private Dish dish;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DishHeaderFragment.
     */
    public static DishHeaderFragment newInstance() {
        DishHeaderFragment fragment = new DishHeaderFragment();
        return fragment;
    }

    public DishHeaderFragment() {
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
        View view = inflater.inflate(R.layout.fragment_dish_header, container, false);

        imageView = (NetworkImageView) view.findViewById(R.id.dish_detail_photo);
        nameView = (TextView) view.findViewById(R.id.dish_detail_label);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        updateDish();
    }

    public void setDish(Dish dish) {
        this.dish = dish;
        updateDish();
    }

    private void updateDish() {
        if (dish != null) {
            imageView.setImageUrl(dish.getPictureURL(), Application.get().getImageLoader());
            nameView.setText(dish.getName());
        } else {
            imageView.setImageUrl("", Application.get().getImageLoader());
            nameView.setText("");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_DISH, dish);
    }

}
