package be.kuleuven.cs.chikwadraat.socialfridge.fridge;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;
import java.util.List;

import be.kuleuven.cs.chikwadraat.socialfridge.Application;
import be.kuleuven.cs.chikwadraat.socialfridge.R;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Ingredient;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Measure;
import be.kuleuven.cs.chikwadraat.socialfridge.util.AdapterUtils;

/**
 * Fragment displaying a list of ingredients.
 */
public abstract class AbstractFridgeFragment<T extends Parcelable> extends ListFragment implements SearchView.OnQueryTextListener, SearchView.OnCloseListener,
        MeasureDialog.OnMeasureSetListener {

    private static final String TAG = "AbstractFridgeFragment";

    private static final String STATE_QUERY = "fridge_search_query";
    private static final String STATE_ITEMS = "fridge_items";
    private static final String STATE_EDITING_ITEM = "fridge_editing_item";

    private ArrayList<T> items = new ArrayList<T>();
    private ItemsListAdapter itemsAdapter;
    private int editingItemPosition;

    private SearchView searchView;
    private String searchQuery;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            searchQuery = savedInstanceState.getString(STATE_QUERY);
            items = savedInstanceState.getParcelableArrayList(STATE_ITEMS);
            editingItemPosition = savedInstanceState.getInt(STATE_EDITING_ITEM, -1);
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.simple_card_list, container);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        itemsAdapter = new ItemsListAdapter(getActivity(), items);
        setListAdapter(itemsAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

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
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
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
        itemsAdapter.getFilter().filter(query);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        T item = itemsAdapter.getItem(position);
        editingItemPosition = position;

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment current = getFragmentManager().findFragmentByTag("dialog");
        MeasureDialogFragment fragment;
        if (current != null) {
            fragment = (MeasureDialogFragment) current;
        } else {
            fragment = MeasureDialogFragment.newInstance();
        }

        Ingredient ingredient = getIngredient(item);
        Measure measure = getMeasure(item);
        fragment.setTitle(ingredient.getName());
        fragment.setMeasure(measure);
        fragment.setTargetFragment(this, 0);
        fragment.show(ft, "dialog");
    }

    @Override
    public void onMeasureSet(Measure measure) {
        if (editingItemPosition >= 0) {
            T item = itemsAdapter.getItem(editingItemPosition);
            onItemUpdated(item, measure);
            itemsAdapter.notifyDataSetChanged();
        }
        editingItemPosition = -1;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_QUERY, searchView.getQuery().toString());
        outState.putParcelableArrayList(STATE_ITEMS, items);
        outState.putInt(STATE_EDITING_ITEM, editingItemPosition);
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        if (itemsAdapter != null) {
            AdapterUtils.setAll(itemsAdapter, items);
        } else {
            this.items = new ArrayList<T>(items);
        }
    }

    protected abstract Ingredient getIngredient(T item);

    protected abstract boolean showMeasure(T item);

    protected abstract Measure getMeasure(T item);

    protected abstract boolean allowRemove(T item);

    protected abstract void onItemUpdated(T item, Measure measure);

    protected abstract void onItemRemoved(T item);

    public class ItemsListAdapter extends ArrayAdapter<T> implements View.OnClickListener {

        public ItemsListAdapter(Context context, List<T> items) {
            super(context, R.layout.fridge_list_item, items);
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
                vh = getViewHolder(v);
            }

            T item = getItem(position);
            Ingredient ingredient = getIngredient(item);
            Measure measure = showMeasure(item) ? getMeasure(item) : null;

            String nameText = ingredient.getName();
            String pictureURL = ingredient.getThumbnailURL();
            String quantityText = measure == null ? "" : measure.toString();

            vh.position = position;
            vh.nameView.setText(nameText);
            vh.pictureView.setImageUrl(pictureURL, Application.get().getImageLoader());
            vh.quantityView.setText(quantityText);
            vh.removeButton.setVisibility(allowRemove(item) ? View.VISIBLE : View.GONE);
            vh.removeButton.setOnClickListener(this);

            return v;
        }

        @Override
        public void onClick(View v) {
            ViewHolder vh = getViewHolder(v);
            T item = getItem(vh.position);
            switch (v.getId()) {
                case R.id.item_remove:
                    remove(item);
                    onItemRemoved(item);
                    break;
            }
        }

        @SuppressWarnings("unchecked")
        private ViewHolder getViewHolder(View v) {
            return (ViewHolder) v.getTag();
        }

        private class ViewHolder {
            TextView nameView;
            TextView quantityView;
            NetworkImageView pictureView;
            ImageButton removeButton;
            int position;

            private ViewHolder(View v) {
                nameView = (TextView) v.findViewById(R.id.ingredient_name);
                pictureView = (NetworkImageView) v.findViewById(R.id.ingredient_pic);
                quantityView = (TextView) v.findViewById(R.id.item_quantity);
                removeButton = (ImageButton) v.findViewById(R.id.item_remove);
            }
        }

    }

}
