package co.yishun.library.datacenter;

import android.os.AsyncTask;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by carlos on 2/5/16.
 */
public abstract class DoubleAsyncTask<Params, Progress, Result extends Discardable> {
    boolean post = false;
    private AsyncTask<Params, Progress, Result> mOptionalTask = new AsyncTask<Params, Progress, Result>() {
        @SafeVarargs
        @Override
        protected final Result doInBackground(Params... params) {
            return doOptionalInBackground(params);
        }

        @Override
        protected void onPostExecute(Result result) {
            onOptionPostExecute(result);
            if (!result.needDiscard()) {
                if (!post) {
                    DoubleAsyncTask.this.onPostExecute(result);
                }
                // abandon result
            } else if (mNecessaryTask.getStatus() == AsyncTask.Status.FINISHED && !post) {
                onPostExecute(null);
            }
        }
    };
    private AsyncTask<Params, Progress, Result> mNecessaryTask = new AsyncTask<Params, Progress, Result>() {
        @SafeVarargs
        @Override
        protected final Result doInBackground(Params... params) {
            return doNecessaryInBackground(params);
        }

        @Override
        protected void onPostExecute(Result result) {

            onNecessaryPostExecute(result);
            if (!result.needDiscard()) {
                post = true;
                DoubleAsyncTask.this.onPostExecute(result);
                mOptionalTask.cancel(true);
            } else if (mOptionalTask.getStatus() == AsyncTask.Status.FINISHED) {
                post = true;
                onPostExecute(null);

            }

        }
    };

    protected void onOptionPostExecute(Result result) {

    }

    protected void onNecessaryPostExecute(Result result) {

    }

    /**
     * This method will be called twice at most and once at least.
     *
     * If necessary loading or optional return, it will be called if its {@link Result} not null. If
     * both necessary loading succeeded and optional loading return null {@link Result}, it will be
     * called with null {@link Result} at last.
     */
    protected abstract void onPostExecute(Result result);

    protected abstract Result doOptionalInBackground(Params... params);

    protected abstract Result doNecessaryInBackground(Params... params);


    public Status getStatus() {
        AsyncTask.Status a = mOptionalTask.getStatus();
        AsyncTask.Status b = mNecessaryTask.getStatus();

        if (a == AsyncTask.Status.PENDING && b == AsyncTask.Status.PENDING) {
            return Status.PENDING;
        } else if (b == AsyncTask.Status.FINISHED) {
            return Status.FINISHED;
        } else if (a == AsyncTask.Status.FINISHED) {
            return Status.CACHE_FINISHED;
        } else {
            return Status.RUNNING;
        }
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        return mNecessaryTask.cancel(mayInterruptIfRunning) && mOptionalTask.cancel(mayInterruptIfRunning);
    }

    public DoubleAsyncTask<Params, Progress, Result> execute(Params... params) {
        mOptionalTask.execute(params);
        mNecessaryTask.execute(params);
        return this;
    }

    public DoubleAsyncTask<Params, Progress, Result> executeOnExecutor(Executor exec, Params... params) {
        mOptionalTask.executeOnExecutor(exec, params);
        mNecessaryTask.executeOnExecutor(exec, params);
        return this;
    }

    public boolean isCancelled() {
        return mOptionalTask.isCancelled() && mNecessaryTask.isCancelled();
    }

    public Result optionalGet() throws InterruptedException, ExecutionException {
        return mOptionalTask.get();
    }

    public Result necessaryGet() throws InterruptedException, ExecutionException {
        return mNecessaryTask.get();
    }

    public Result optionalGet(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return mOptionalTask.get(timeout, unit);
    }

    public Result necessaryGet(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return mNecessaryTask.get(timeout, unit);
    }

    public enum Status {
        /**
         * Indicates that the task has not been executed yet.
         */
        PENDING,

        /**
         * Indicates that the task is running.
         */
        RUNNING,

        /**
         * Indicates that the optional load has finished.
         */
        CACHE_FINISHED,

        /**
         * Indicates that {@link AsyncTask#onPostExecute} has finished.
         */
        FINISHED,
    }
}
