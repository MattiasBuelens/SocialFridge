package be.kuleuven.cs.chikwadraat.socialfridge.widget;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

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
        ViewGroup progressContainer = (ViewGroup) view.findViewById(R.id.progress_container);
        progressContainer.setId(INTERNAL_PROGRESS_CONTAINER_ID);

        ViewGroup listContainer = (ViewGroup) view.findViewById(R.id.list_container);
        listContainer.setId(INTERNAL_LIST_CONTAINER_ID);

        // Move to list container
        View emptyView = view.findViewById(android.R.id.empty);
        if (emptyView != null) {
            ViewGroup.LayoutParams lp = emptyView.getLayoutParams();
            if(emptyView.getParent() != null) {
                ((ViewGroup) emptyView.getParent()).removeView(emptyView);
            }
            listContainer.addView(emptyView, lp);
        }

        super.onViewCreated(view, savedInstanceState);
    }

}
