package be.kuleuven.cs.chikwadraat.socialfridge.widget;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * ListView which expands in height when asked to wrap its contents.
 *
 * Adapted from http://www.jayway.com/2012/10/04/how-to-make-the-height-of-a-gridview-wrap-its-content/
 */
public class ExpandableListView extends ListView {

    public ExpandableListView(Context context) {
        super(context);
    }

    public ExpandableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExpandableListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // WARNING: Very hackish and memory expensive!
        // The whole ListAdapter is traversed in the process
        if (getLayoutParams().height == LayoutParams.WRAP_CONTENT) {
            // Calculate entire height by providing a very large height hint.
            // View.MEASURED_SIZE_MASK represents the largest height possible.
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(ViewCompat.MEASURED_SIZE_MASK, MeasureSpec.AT_MOST);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

}