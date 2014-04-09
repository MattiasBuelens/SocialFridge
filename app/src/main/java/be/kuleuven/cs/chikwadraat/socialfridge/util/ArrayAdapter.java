package be.kuleuven.cs.chikwadraat.socialfridge.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

import java.util.List;

/**
 * Created by Mattias on 29/03/2014.
 */
public class ArrayAdapter<T> extends android.widget.ArrayAdapter<T> {

    public ArrayAdapter(Context context, int resource) {
        super(context, resource);
    }

    public ArrayAdapter(Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
    }

    public ArrayAdapter(Context context, int resource, T[] objects) {
        super(context, resource, objects);
    }

    public ArrayAdapter(Context context, int resource, int textViewResourceId, T[] objects) {
        super(context, resource, textViewResourceId, objects);
    }

    public ArrayAdapter(Context context, int resource, List<T> objects) {
        super(context, resource, objects);
    }

    public ArrayAdapter(Context context, int resource, int textViewResourceId, List<T> objects) {
        super(context, resource, textViewResourceId, objects);
    }

    @TargetApi(11)
    public void setData(List<T> data) {
        clear();
        if (data != null) {
            //If the platform supports it, use addAll, otherwise add in loop
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                addAll(data);
            } else {
                for (T item : data) {
                    add(item);
                }
            }
        }
    }

}
