package be.kuleuven.cs.chikwadraat.socialfridge.dish;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import be.kuleuven.cs.chikwadraat.socialfridge.R;


/**
 * A header fragment displaying a dish's name and picture.
 */
public class DishHeaderFragment extends Fragment {

    private ImageView imageView;
    private TextView nameView;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dish_header, container, false);

        imageView = (ImageView) view.findViewById(R.id.dish_detail_photo);
        nameView = (TextView) view.findViewById(R.id.dish_detail_label);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // TODO Dummy
        imageView.setImageResource(R.drawable.detail_spaghetti);
        nameView.setText("Spaghetti Bolognese");
    }

    // TODO Getter/setter for dish?

}
