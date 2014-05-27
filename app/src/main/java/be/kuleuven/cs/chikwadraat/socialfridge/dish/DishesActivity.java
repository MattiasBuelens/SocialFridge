package be.kuleuven.cs.chikwadraat.socialfridge.dish;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.facebook.Session;

import java.util.List;

import be.kuleuven.cs.chikwadraat.socialfridge.Application;
import be.kuleuven.cs.chikwadraat.socialfridge.ListActivity;
import be.kuleuven.cs.chikwadraat.socialfridge.R;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.User;
import be.kuleuven.cs.chikwadraat.socialfridge.loader.DishesLoader;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Dish;
import be.kuleuven.cs.chikwadraat.socialfridge.util.AdapterUtils;

/**
 * Dishes activity.
 */
public class DishesActivity extends ListActivity implements SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    private static final String TAG = "DishesActivity";
    private static final int LOADER_DISHES = 1;

    private DishesArrayAdapter dishesArrayAdapter;
    private DishesLoaderCallbacks loaderCallbacks = new DishesLoaderCallbacks();

    private static final String STATE_QUERY = "dishes_search_query";
    private SearchView searchView;
    private String searchQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_card_list);

        if (savedInstanceState != null) {
            searchQuery = savedInstanceState.getString(STATE_QUERY);
        }

        dishesArrayAdapter = new DishesArrayAdapter();
        setListAdapter(dishesArrayAdapter);
        setListShownNoAnimation(false);
    }

    @Override
    protected void onLoggedIn(Session session, User user) {
        super.onLoggedIn(session, user);
        getSupportLoaderManager().initLoader(LOADER_DISHES, null, loaderCallbacks);
    }

    @Override
    protected void onLoggedOut() {
        super.onLoggedOut();
        setListShown(false);
        getSupportLoaderManager().destroyLoader(LOADER_DISHES);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.dish_search, menu);

        MenuItem searchItem = menu.findItem(R.id.search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint(getString(R.string.dishes_search_hint));
        searchView.setOnQueryTextListener(this);
        searchView.setOnCloseListener(this);

        if (!TextUtils.isEmpty(searchQuery)) {
            String query = searchQuery;
            MenuItemCompat.expandActionView(searchItem);
            searchDishes(query);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onResume() {
        super.onResume();
        searchDishes(searchQuery);
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        searchDishes(newText);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        // When the search is "committed" by the user, then hide the keyboard so the user can
        // more easily browse the list of results.
        if (searchView != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
            }
            searchView.clearFocus();
        }
        return true;
    }

    @Override
    public boolean onClose() {
        searchDishes(null);
        return false;
    }

    protected void searchDishes(String query) {
        searchQuery = query;
        if (searchView != null && !TextUtils.equals(searchView.getQuery(), query)) {
            searchView.setQuery(searchQuery, false);
        }
        if (dishesArrayAdapter != null) {
            dishesArrayAdapter.getFilter().filter(query);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_QUERY, searchQuery);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Dish dish = (Dish) l.getItemAtPosition(position);
        // View dish
        Intent intent = new Intent(this, ViewDishActivity.class);
        intent.putExtra(ViewDishActivity.EXTRA_DISH, dish);
        startActivity(intent);
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
            setListShown(true);
        }

        @Override
        public void onLoaderReset(Loader<List<Dish>> loader) {
            dishesArrayAdapter.clear();
        }

    }

}
