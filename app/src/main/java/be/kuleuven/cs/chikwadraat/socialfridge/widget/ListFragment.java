package be.kuleuven.cs.chikwadraat.socialfridge.widget;

import android.os.Bundle;
import android.view.View;

import be.kuleuven.cs.chikwadraat.socialfridge.R;

/**
 * Created by Mattias on 27/5/2014.
 */
public class ListFragment extends android.support.v4.app.ListFragment {

    protected static final int INTERNAL_PROGRESS_CONTAINER_ID = 0x00ff0002;
    protected static final int INTERNAL_LIST_CONTAINER_ID = 0x00ff0003;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Replace IDs with internal IDs from support library
        View progressContainer = view.findViewById(R.id.progress_container);
        progressContainer.setId(INTERNAL_PROGRESS_CONTAINER_ID);

        View listContainer = view.findViewById(R.id.list_container);
        listContainer.setId(INTERNAL_LIST_CONTAINER_ID);

        super.onViewCreated(view, savedInstanceState);
    }

}
