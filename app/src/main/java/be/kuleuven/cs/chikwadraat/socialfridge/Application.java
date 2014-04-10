package be.kuleuven.cs.chikwadraat.socialfridge;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.StandardExceptionParser;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by Mattias on 10/04/2014.
 */
public class Application extends android.app.Application {

    private Tracker tracker;

    public synchronized Tracker getTracker() {
        if (tracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            tracker = analytics.newTracker(R.xml.app_tracker);
        }
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
