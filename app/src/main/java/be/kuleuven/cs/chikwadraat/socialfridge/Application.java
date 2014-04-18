package be.kuleuven.cs.chikwadraat.socialfridge;

import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.StandardExceptionParser;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by Mattias on 10/04/2014.
 */
public class Application extends android.app.Application {

    private static Application instance;

    public static Application get() {
        return instance;
    }

    private final LruCache<String, Bitmap> bitmapCache = new LruCache<String, Bitmap>(20);
    private RequestQueue requestQueue;
    private ImageLoader imageLoader;

    private Tracker tracker;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        initVolley();
        initAnalytics();
    }

    private void initVolley() {
        requestQueue = Volley.newRequestQueue(this);
        ImageLoader.ImageCache imageCache = new ImageLoader.ImageCache() {
            @Override
            public void putBitmap(String key, Bitmap value) {
                bitmapCache.put(key, value);
            }

            @Override
            public Bitmap getBitmap(String key) {
                return bitmapCache.get(key);
            }
        };
        imageLoader = new ImageLoader(requestQueue, imageCache);
    }

    private void initAnalytics() {
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
        analytics.setDryRun(isDebug());
        tracker = analytics.newTracker(R.xml.app_tracker);
    }

    protected boolean isDebug() {
        return (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }

    public RequestQueue getRequestQueue() {
        return requestQueue;
    }

    public ImageLoader getImageLoader() {
        return imageLoader;
    }

    public Tracker getTracker() {
        return tracker;
    }

    public void trackException(Exception e) {
        getTracker().send(new HitBuilders.ExceptionBuilder()
                        .setDescription(new StandardExceptionParser(this, null)
                                .getDescription(Thread.currentThread().getName(), e))
                        .build()
        );
    }

}
