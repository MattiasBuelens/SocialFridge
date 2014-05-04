package be.kuleuven.cs.chikwadraat.socialfridge.fridge;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ListView;

import com.facebook.Session;
import com.google.android.gms.analytics.HitBuilders;

import be.kuleuven.cs.chikwadraat.socialfridge.Endpoints;
import be.kuleuven.cs.chikwadraat.socialfridge.ListActivity;
import be.kuleuven.cs.chikwadraat.socialfridge.R;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.Endpoint;
import be.kuleuven.cs.chikwadraat.socialfridge.model.FridgeItem;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Measure;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Party;
import be.kuleuven.cs.chikwadraat.socialfridge.party.BasePartyActivity;
import be.kuleuven.cs.chikwadraat.socialfridge.party.PlanPartyActivity;
import be.kuleuven.cs.chikwadraat.socialfridge.util.ObservableAsyncTask;

/**
 * Manage fridge activity.
 */
public class FridgeActivity extends ListActivity implements SearchView.OnQueryTextListener, SearchView.OnCloseListener,
        MeasureDialog.OnMeasureSetListener, ObservableAsyncTask.Listener<Void, Void>, View.OnClickListener {

    private static final String TAG = "FridgeActivity";

    private IngredientsFragment ingredientsFragment;
    private Button addIngredientsButton;

    private AddIngredientsTask task;

    private static final String STATE_QUERY = "fridge_search_query";
    private static final String STATE_EDITING_ITEM = "fridge_editing_item";

    private int editingItemPosition;

    private SearchView searchView;
    private String searchQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fridge_my_fridge);

        ingredientsFragment = (IngredientsFragment) getSupportFragmentManager().findFragmentById(R.id.ingredients_fragment);
        addIngredientsButton = (Button) findViewById(R.id.fridge_action_add_ingredients);

        addIngredientsButton.setOnClickListener(this);

        //TODO Re-attach to close invites task ?

        if (savedInstanceState != null) {
            searchQuery = savedInstanceState.getString(STATE_QUERY);
            editingItemPosition = savedInstanceState.getInt(STATE_EDITING_ITEM, -1);
        }

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

    @Override
    public void onResult(Void v) {
        // TODO WHAT TO DO HERE?
    }

    @Override
    public void onError(Exception exception) {
        Log.e(TAG, "Failed to go to add ingredients: " + exception.getMessage());
        hideProgressDialog();
        trackException(exception);

        // Handle regular exception
        handleException(exception);
    }

    @Override
    public void onProgress(Void... progress) {
        // do nothing
    }

    protected void searchIngredients(String query) {
        searchQuery = query;
        if (searchView != null && !TextUtils.equals(searchView.getQuery(), query)) {
            searchView.setQuery(searchQuery, false);
        }
        // TODO HOWTO itemsArrayAdapter.getFilter().filter(query);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fridge_action_add_ingredients:
                addIngredients();
                break;
        }
    }

    private void addIngredients() {
        if (task != null) return;

        task = new AddIngredientsTask(this);
        task.execute();
        showProgressDialog(R.string.fridge_add_ingredients_progress);
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
        fragment.setTitle(item.getName());
        fragment.setMeasure(item.getQuantity());
        fragment.show(ft, "dialog");
    }

    @Override
    public void onMeasureSet(Measure measure) {
        if (editingItemPosition >= 0) {
            FridgeItem item = (FridgeItem) getListView().getItemAtPosition(editingItemPosition);
            item.setQuantity(measure);
            //TODO itemsArrayAdapter.notifyDataSetChanged();
        }
        editingItemPosition = -1;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_QUERY, searchView.getQuery().toString());
        outState.putInt(STATE_EDITING_ITEM, editingItemPosition);
    }

    private static class AddIngredientsTask extends ObservableAsyncTask<Void, Void, Void> {

        private AddIngredientsTask(FridgeActivity activity) {
            super(activity);
        }

        @Override
        protected Void run(Void... unused) throws Exception {
            //TODO HOWTO return new Party(parties().closeInvites(partyID, Session.getActiveSession().getAccessToken()).execute());
            return null;
        }

    }

}
