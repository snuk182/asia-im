package ua.snuk182.asia;

import android.app.Application;

/**
 * This custom Application class overrides standard JVM's uncaught exception handler 
 * to write possible exceptions to log file, if enabled in settings, along with console output.
 * 
 * @author Sergiy Plygun
 *
 */
public class AsiaApplication extends Application {

	@Override
    public void onCreate() {
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
        super.onCreate();
    }
}
