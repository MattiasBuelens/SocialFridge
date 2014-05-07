package be.kuleuven.cs.chikwadraat.socialfridge.util;

/**
 * Created by Mattias on 7/05/2014.
 */
public abstract class TransformedObservableAsyncTaskListener<Progress, Original, Transformed> implements ObservableAsyncTask.Listener<Progress, Original> {

    private final ObservableAsyncTask.Listener<Progress, Transformed> listener;

    protected TransformedObservableAsyncTaskListener(ObservableAsyncTask.Listener<Progress, Transformed> listener) {
        this.listener = listener;
    }

    @Override
    public void onResult(Original result) {
        listener.onResult(transformResult(result));
    }

    @Override
    public void onError(Exception exception) {
        listener.onError(exception);
    }

    @Override
    public void onProgress(Progress... progress) {
        listener.onProgress(progress);
    }

    protected abstract Transformed transformResult(Original result);

}
