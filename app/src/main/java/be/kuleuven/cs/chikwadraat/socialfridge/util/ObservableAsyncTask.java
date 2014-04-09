package be.kuleuven.cs.chikwadraat.socialfridge.util;

import android.os.AsyncTask;
import android.util.Log;

/**
 * Created by Mattias on 7/04/2014.
 */
public abstract class ObservableAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

    private static final String TAG = "ObservableAsyncTask";

    private Listener<Progress, Result> listener;

    private TaskState state = TaskState.READY;
    private Progress[] lastProgress;
    private Result result;
    private Exception exception;

    protected enum TaskState {
        READY,
        STARTED,
        PROGRESS,
        RESULT,
        ERROR
    }

    public interface Listener<Progress, Result> {

        void onResult(Result result);

        void onError(Exception exception);

        void onProgress(Progress... progress);

    }

    protected ObservableAsyncTask(Listener<Progress, Result> listener) {
        this.listener = listener;
    }

    @SuppressWarnings("unchecked")
    public void attach(Listener<Progress, Result> listener) {
        this.listener = listener;
        // Re-fire listener
        switch (state) {
            case PROGRESS:
                listener.onProgress(lastProgress);
                break;
            case RESULT:
                listener.onResult(result);
                break;
            case ERROR:
                listener.onError(exception);
                break;
        }
    }

    public void detach() {
        this.listener = null;
    }

    protected final void postProgress(Progress... progress) {
        this.state = TaskState.PROGRESS;
        this.lastProgress = progress;
        publishProgress(progress);
    }

    @Override
    protected final void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected final Result doInBackground(Params... params) {
        try {
            this.state = TaskState.STARTED;
            Result result = run(params);
            this.state = TaskState.RESULT;
            this.result = result;
            return result;
        } catch (Exception e) {
            this.state = TaskState.ERROR;
            this.exception = e;
            return null;
        }
    }

    protected abstract Result run(Params... params) throws Exception;

    @Override
    protected final void onProgressUpdate(Progress... progress) {
        if (listener == null) {
            Log.d(TAG, "onProgressUpdate() skipped -- no listener");
        } else {
            listener.onProgress(progress);
        }
    }

    @Override
    protected final void onPostExecute(Result result) {
        if (listener == null) {
            Log.d(TAG, "onPostExecute() skipped -- no listener");
        } else if (result != null) {
            listener.onResult(result);
        } else if (exception != null) {
            listener.onError(exception);
        }
    }

}
