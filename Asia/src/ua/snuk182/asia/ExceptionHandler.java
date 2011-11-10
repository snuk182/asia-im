package ua.snuk182.asia;

import ua.snuk182.asia.services.ServiceUtils;

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
