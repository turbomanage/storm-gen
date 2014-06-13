package com.turbomanage.storm.sample.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

public abstract class AbstractAsyncTaskLoader<T> extends AsyncTaskLoader<T> {
	
	private T mResult;

	public AbstractAsyncTaskLoader(Context context) {
		super(context);
	}
	
	@Override
	protected void onStartLoading() {
		if (mResult != null) {
			// If we currently have a result available, deliver it
            // immediately.
			deliverResult(mResult);
		}
		
		if (takeContentChanged() || mResult == null) {
			// If the data has changed since the last time it was loaded
            // or is not currently available, start a load.
			forceLoad();
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

        // At this point we can release the resources associated with the result
        // if needed.
        onReleaseResources(result);
    }

	@Override
	protected void onReset() {
		super.onReset();
		stopLoading();
		mResult = null;
	}
	
	@Override
	public void deliverResult(T data) {
		if (isReset()) {
            // An async query came in while the loader is stopped.  We
            // don't need the result.
            if (data != null) {
                onReleaseResources(data);
            }
        }
		
		// Release resources associated with the previous value
        if (mResult != null) {
            onReleaseResources(mResult);
        }
		
		mResult = data;
		
		if (isStarted()) {
			// If the Loader is currently started, we can immediately
            // deliver its results.
			super.deliverResult(data);
		}
	}
	
	/**
     * Helper function to take care of releasing resources associated
     * with an actively loaded data set. Override it if necessary.
     */
    protected void onReleaseResources(T resource) {
        // For a simple objects there is nothing to do.
        // For something like a Cursor, we would close it here.
    }
}
