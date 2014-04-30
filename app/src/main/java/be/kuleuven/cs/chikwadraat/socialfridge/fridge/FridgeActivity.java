package be.kuleuven.cs.chikwadraat.socialfridge.fridge;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import be.kuleuven.cs.chikwadraat.socialfridge.ListActivity;
import be.kuleuven.cs.chikwadraat.socialfridge.R;
import be.kuleuven.cs.chikwadraat.socialfridge.measuring.Unit;
import be.kuleuven.cs.chikwadraat.socialfridge.model.FridgeItem;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Measure;
import be.kuleuven.cs.chikwadraat.socialfridge.util.AdapterUtils;

/**
 * Manage fridge activity.
 */
public class FridgeActivity extends ListActivity implements SearchView.OnQueryTextListener, SearchView.OnCloseListener, MeasureDialog.OnMeasureSetListener {

    private static final String TAG = "FridgeActivity";

    private static final String STATE_QUERY = "fridge_search_query";
    private static final String STATE_EDITING_ITEM = "fridge_editing_item";

    private ItemsArrayAdapter itemsArrayAdapter;
    private int editingItemPosition;

    private SearchView searchView;
    private String searchQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_card_list);

        if (savedInstanceState != null) {
            searchQuery = savedInstanceState.getString(STATE_QUERY);
            editingItemPosition = savedInstanceState.getInt(STATE_EDITING_ITEM, -1);
        }

        itemsArrayAdapter = new ItemsArrayAdapter();

        // TODO Remove dummy items
        List<FridgeItem> items = new ArrayList<FridgeItem>();
        items.add(new FridgeItem("Eggs", R.drawable.eggs, new Measure(6, Unit.PIECES)));
        AdapterUtils.setAll(itemsArrayAdapter, items);

        setListAdapter(itemsArrayAdapter);

        handleIntent(getIntent());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.fridge_search, menu);

        MenuItem searchItem = menu.findItem(R.id.search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint(getString(R.string.fridge_search_hint));
        searchView.setOnQueryTextListener(this);
        searchView.setOnCloseListener(this);

        if (!TextUtils.isEmpty(searchQuery)) {
            String query = searchQuery;
            MenuItemCompat.expandActionView(searchItem);
            searchIngredients(query);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    protected void handleIntent(Intent intent) {
        if (intent == null) return;
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            searchIngredients(query);
        }
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        searchIngredients(newText);
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
        searchIngredients(null);
        return false;
    }

    protected void searchIngredients(String query) {
        searchQuery = query;
        if (searchView != null && !TextUtils.equals(searchView.getQuery(), query)) {
            searchView.setQuery(searchQuery, false);
        }
        itemsArrayAdapter.getFilter().filter(query);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_QUERY, searchView.getQuery().toString());
        outState.putInt(STATE_EDITING_ITEM, editingItemPosition);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        FridgeItem item = (FridgeItem) l.getItemAtPosition(position);
        editingItemPosition = position;

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment current = getSupportFragmentManager().findFragmentByTag("dialog");
        MeasureDialogFragment fragment;
        if (current != null) {
            fragment = (MeasureDialogFragment) current;
        } else {
            fragment = MeasureDialogFragment.newInstance();
        }
        fragment.setMeasure(item.getQuantity());
        fragment.show(ft, "dialog");
    }

    @Override
    public void onMeasureSet(Measure measure) {
        if (editingItemPosition >= 0) {
            FridgeItem item = (FridgeItem) getListView().getItemAtPosition(editingItemPosition);
            item.setQuantity(measure);
            itemsArrayAdapter.notifyDataSetChanged();
        }
        editingItemPosition = -1;
    }

    public class ItemsArrayAdapter extends ArrayAdapter<FridgeItem> {

        public ItemsArrayAdapter() {
            super(FridgeActivity.this, R.layout.fridge_list_item);
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
