package be.kuleuven.cs.chikwadraat.socialfridge;

import android.content.pm.ApplicationInfo;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.StandardExceptionParser;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by Mattias on 10/04/2014.
 */
public class Application extends android.app.Application {

    private Tracker tracker;

    protected boolean isDebug() {
        return (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }

    public synchronized Tracker getTracker() {
        if (tracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            analytics.setDryRun(isDebug());
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
