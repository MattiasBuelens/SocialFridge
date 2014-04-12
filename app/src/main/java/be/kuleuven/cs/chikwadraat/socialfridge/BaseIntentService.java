package be.kuleuven.cs.chikwadraat.socialfridge;

import android.app.IntentService;

import com.google.android.gms.analytics.Tracker;

/**
 * Base intent service.
 */
public abstract class BaseIntentService extends IntentService {

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public BaseIntentService(String name) {
        super(name);
    }

    public Tracker getTracker() {
        return ((Application) getApplication()).getTracker();
    }

    public void trackException(Exception e) {
        ((Application) getApplication()).trackException(e);
    }

}
