package be.kuleuven.cs.chikwadraat.socialfridge;

import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import java.util.List;

import be.kuleuven.cs.chikwadraat.socialfridge.loader.DishesLoader;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Dish;
import be.kuleuven.cs.chikwadraat.socialfridge.util.AdapterUtils;

/**
 * Dishes activity.
 */
public class DishesActivity extends ListActivity {

    private static final String TAG = "DishesActivity";
    private static final int LOADER_PARTIES = 1;

    private DishesArrayAdapter dishesArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_card_list);

        dishesArrayAdapter = new DishesArrayAdapter();
        setListAdapter(dishesArrayAdapter);

        getSupportLoaderManager().initLoader(LOADER_PARTIES, null, new DishesLoaderCallbacks());
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Dish dish = (Dish) l.getItemAtPosition(position);
        // TODO Link with View dish activity
        //Intent intent = new Intent(this, ViewDishActivity.class);
        //intent.putExtra(BasePartyActivity.EXTRA_DISH_ID, dish.getID());
        //startActivity(intent);
    }

    public class DishesArrayAdapter extends ArrayAdapter<Dish> {

        public DishesArrayAdapter() {
            super(DishesActivity.this, R.layout.dish_list_item);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            ViewHolder vh;
            if (v == null) {
                v = View.inflate(getContext(), R.layout.dish_list_item, null);
                vh = new ViewHolder(v);
                v.setTag(vh);
            } else {
                vh = (ViewHolder) v.getTag();
            }

            Dish dish = getItem(position);

            String nameText = dish.getName();
            String pictureUrl = dish.getThumbnailURL();

            vh.position = position;
            vh.nameView.setText(nameText);
            vh.pictureView.setImageUrl(pictureUrl, Application.get().getImageLoader());

            return v;
        }

        private class ViewHolder {
            TextView nameView;
            NetworkImageView pictureView;
            int position;

            private ViewHolder(View v) {
                nameView = (TextView) v.findViewById(R.id.dish_name);
                pictureView = (NetworkImageView) v.findViewById(R.id.dish_pic);
            }
        }

    }

    private class DishesLoaderCallbacks implements LoaderManager.LoaderCallbacks<List<Dish>> {

        @Override
        public Loader<List<Dish>> onCreateLoader(int id, Bundle args) {
            return new DishesLoader(DishesActivity.this);
        }

        @Override
        public void onLoadFinished(Loader<List<Dish>> loader, List<Dish> parties) {
            AdapterUtils.setAll(dishesArrayAdapter, parties);
        }

        @Override
        public void onLoaderReset(Loader<List<Dish>> loader) {
            dishesArrayAdapter.clear();
        }

    }

}
