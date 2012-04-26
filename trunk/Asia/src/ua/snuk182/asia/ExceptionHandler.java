package ua.snuk182.asia;

import ua.snuk182.asia.services.ServiceUtils;

/**
 * Overridden exception handler for storing stack traces in text log file.
 * 
 * @author SergiyP
 *
 */
public class ExceptionHandler implements Thread.UncaughtExceptionHandler {

    Thread.UncaughtExceptionHandler oldHandler;

    public ExceptionHandler() {
        oldHandler = Thread.getDefaultUncaughtExceptionHandler(); 
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        ServiceUtils.log(throwable);
        if(oldHandler != null) {
        	oldHandler.uncaughtException(thread, throwable); 
        }            
    }
}
