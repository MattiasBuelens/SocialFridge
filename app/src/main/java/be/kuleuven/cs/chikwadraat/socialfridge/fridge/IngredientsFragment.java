package be.kuleuven.cs.chikwadraat.socialfridge.fridge;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import be.kuleuven.cs.chikwadraat.socialfridge.R;
import be.kuleuven.cs.chikwadraat.socialfridge.model.FridgeItem;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Measure;
import be.kuleuven.cs.chikwadraat.socialfridge.util.AdapterUtils;

/**
 * Fragment displaying a list of ingredients.
 * Use the {@link IngredientsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class IngredientsFragment extends ListFragment implements SearchView.OnQueryTextListener, SearchView.OnCloseListener,
        MeasureDialog.OnMeasureSetListener {

    private static final String TAG = "IngredientsFragment";

    private static final String STATE_QUERY = "ingredients_search_query";
    private static final String STATE_ITEMS = "ingredients_items";
    private static final String STATE_EDITING_ITEM = "ingredients_editing_item";

    private ArrayList<FridgeItem> ingredients = new ArrayList<FridgeItem>();
    private IngredientsListAdapter ingredientsAdapter;
    private int editingItemPosition;

    private SearchView searchView;
    private String searchQuery;

    private IngredientsListener listener;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            searchQuery = savedInstanceState.getString(STATE_QUERY);
            ingredients = savedInstanceState.getParcelableArrayList(STATE_ITEMS);
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

        ingredientsAdapter = new IngredientsListAdapter(getActivity(), ingredients);
        setListAdapter(ingredientsAdapter);
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
        ingredientsAdapter.getFilter().filter(query);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        FridgeItem item = (FridgeItem) l.getItemAtPosition(position);
        editingItemPosition = position;

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment current = getFragmentManager().findFragmentByTag("dialog");
        MeasureDialogFragment fragment;
        if (current != null) {
            fragment = (MeasureDialogFragment) current;
        } else {
            fragment = MeasureDialogFragment.newInstance();
        }
        fragment.setTitle(item.getName());
        fragment.setMeasure(item.getQuantity());
        fragment.setTargetFragment(this, 0);
        fragment.show(ft, "dialog");
    }

    @Override
    public void onMeasureSet(Measure measure) {
        if (editingItemPosition >= 0) {
            FridgeItem item = (FridgeItem) getListView().getItemAtPosition(editingItemPosition);
            item.setQuantity(measure);
            fireFridgeItemUpdated(item);
            ingredientsAdapter.notifyDataSetChanged();
        }
        editingItemPosition = -1;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_QUERY, searchView.getQuery().toString());
        outState.putParcelableArrayList(STATE_ITEMS, ingredients);
        outState.putInt(STATE_EDITING_ITEM, editingItemPosition);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (IngredientsListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement IngredientsListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public List<FridgeItem> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<FridgeItem> ingredients) {
        if (ingredientsAdapter != null) {
            AdapterUtils.setAll(ingredientsAdapter, ingredients);
        } else {
            this.ingredients = new ArrayList<FridgeItem>(ingredients);
        }
    }

    protected void fireFridgeItemUpdated(FridgeItem item) {
        if (listener != null) {
            listener.onFridgeItemUpdated(item);
        }
    }

    protected void fireFridgeItemRemoved(FridgeItem item) {
        if (listener != null) {
            listener.onFridgeItemRemoved(item);
        }
    }

    public interface IngredientsListener {

        public void onFridgeItemUpdated(FridgeItem item);

        public void onFridgeItemRemoved(FridgeItem item);

    }

    public class IngredientsListAdapter extends ArrayAdapter<FridgeItem> implements View.OnClickListener {

        public IngredientsListAdapter(Context context, List<FridgeItem> items) {
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
                vh = (ViewHolder) v.getTag();
            }

            FridgeItem item = getItem(position);

            String nameText = item.getName();
            Drawable picture = item.getPicture(getContext());
            String quantityText = item.getQuantity().toString();
            // TODO Add proper check if this is a fridge item
            boolean inFridge = true;

            vh.position = position;
            vh.nameView.setText(nameText);
            vh.pictureView.setImageDrawable(picture);
            vh.quantityView.setText(quantityText);
            vh.removeButton.setVisibility(inFridge ? View.VISIBLE : View.GONE);
            vh.removeButton.setOnClickListener(this);

            return v;
        }

        @Override
        public void onClick(View v) {
            ViewHolder vh = (ViewHolder) v.getTag();
            FridgeItem item = getItem(vh.position);
            switch (v.getId()) {
                case R.id.item_remove:
                    remove(item);
                    fireFridgeItemRemoved(item);
                    break;
            }
        }

        private class ViewHolder {
            TextView nameView;
            TextView quantityView;
            ImageView pictureView;
            ImageButton removeButton;
            int position;

            private ViewHolder(View v) {
                nameView = (TextView) v.findViewById(R.id.ingredient_name);
                pictureView = (ImageView) v.findViewById(R.id.ingredient_pic);
                quantityView = (TextView) v.findViewById(R.id.item_quantity);
                removeButton = (ImageButton) v.findViewById(R.id.item_remove);
            }
        }

    }

}
