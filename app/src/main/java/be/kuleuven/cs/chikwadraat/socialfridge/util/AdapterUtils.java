package be.kuleuven.cs.chikwadraat.socialfridge.util;

import android.annotation.TargetApi;
import android.os.Build;
import android.widget.ArrayAdapter;

import java.util.List;

/**
 * Created by Mattias on 16/04/2014.
 */
public class AdapterUtils {

    @TargetApi(11)
    public static <T> void setAll(ArrayAdapter<T> adapter, List<T> items) {
        adapter.clear();
        if (items != null) {
            // If the platform supports it, use addAll, otherwise add in loop
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                adapter.addAll(items);
            } else {
                for (T item : items) {
                    adapter.add(item);
                }
            }
        }
    }

}
