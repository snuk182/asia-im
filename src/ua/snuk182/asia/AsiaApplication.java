package ua.snuk182.asia;

import android.app.Application;

public class AsiaApplication extends Application {

	@Override
    public void onCreate() {
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
        super.onCreate();
    }
}
