package be.kuleuven.cs.chikwadraat.socialfridge.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import be.kuleuven.cs.chikwadraat.socialfridge.Application;

/**
 * Base class for async task loaders.
 * Based on sample code from http://developer.android.com/reference/android/content/AsyncTaskLoader.html
 */
public abstract class BaseLoader<T> extends AsyncTaskLoader<T> {

    private T result;

    public BaseLoader(Context context) {
        super(context);
    }

    protected T getResult() {
        return result;
    }

    @Override
    protected void onStartLoading() {
        if (result != null) {
            // If we currently have a result available,
            // deliver it immediately.
            deliverResult(result);
        }

        // Start watching for changes
        startObserving();

        // Has something interesting changed?
        if (takeContentChanged() || result == null || needForceLoad()) {
            // If the data has changed since the last time it was loaded
            // or is not currently available, start a load.
            forceLoad();
        }
    }

    @Override
    public void deliverResult(T result) {
        if (isReset()) {
            // An async query came in while the loader is stopped.
            // We don't need the result.
            if (result != null) {
                releaseResources(result);
            }
            return;
        }

        T oldResult = this.result;
        this.result = result;

        if (isStarted()) {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(result);
        }

        // At this point we can release the resources associated with
        // 'oldResult' if needed; now that the new result is delivered we
        // know that it is no longer in use.
        if (oldResult != null) {
            releaseResources(oldResult);
        }

    }

    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    @Override
    public void onCanceled(T result) {
        super.onCanceled(result);

        // At this point we can release the resources
        // associated with 'result' if needed.
        releaseResources(result);
    }


    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        // At this point we can release the resources
        // associated with 'result' if needed.
        if (result != null) {
            releaseResources(result);
            result = null;
        }

        // Stop monitoring for changes.
        stopObserving();
    }

    /**
     * Start observing the data source for changes.
     * Call {@link #onContentChanged()} from the observer when a change is detected.
     */
    protected void startObserving() {
    }

    /**
     * Stop observing the data source for changes.
     */
    protected void stopObserving() {
    }

    /**
     * Check if a force load is needed.
     */
    protected boolean needForceLoad() {
        return false;
    }

    /**
     * Release the resources associated with the given result object.
     *
     * @param result The result object to release.
     */
    protected void releaseResources(T result) {
    }

    protected void trackException(Exception e) {
        Application.get().trackException(e);
    }

}
