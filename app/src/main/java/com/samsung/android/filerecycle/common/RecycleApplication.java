package com.samsung.android.filerecycle.common;

import android.app.Application;

/**
 * Created by samsung on 2017/6/14.
 */

public class RecycleApplication extends Application {
    private static RecycleApplication application;

    public static RecycleApplication getApplication() {
        if (application == null) {
            application = new RecycleApplication();
        }
        return application;
    }
}
