package com.example.byunghwa.newsapp.interfaces;

/**
 * Created by SAMSUNG on 11/21/2017.
 */

/**
 * This is a useful callback mechanism so we can abstract our AsyncTaskLoader out into separate, re-usable
 * and testable classes yet still retain a hook back into the calling activity. Basically, it'll make classes
 * cleaner and easier to unit test.
 *
 */
public interface OnRefreshStarted {
    /**
     * Invoked when the AsyncTaskLoader has started its execution.
     *
     */
    public void onTaskStarted();
}
