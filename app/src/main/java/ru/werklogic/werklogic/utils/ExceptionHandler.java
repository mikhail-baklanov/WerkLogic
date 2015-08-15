package ru.werklogic.werklogic.utils;

import android.app.Activity;
import android.os.Build;

public class ExceptionHandler implements
        Thread.UncaughtExceptionHandler {
    private static final String LINE_SEPARATOR = "\n";
    private static final String TAG = ExceptionHandler.class.getName();

    public ExceptionHandler() {
    }

    @Override
    public void uncaughtException(Thread thread, Throwable exception) {
        Utils.log(getLogUncaughtException(exception));
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);
    }

    private static String getLogUncaughtException(Throwable exception) {
        StringBuilder errorReport = new StringBuilder();
        errorReport.append("************ CAUSE OF ERROR ************\n\n");
        errorReport.append(Utils.getStackTrace(exception));

        errorReport.append("\n************ DEVICE INFORMATION ***********\n");
        errorReport.append("Brand: ");
        errorReport.append(Build.BRAND);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Device: ");
        errorReport.append(Build.DEVICE);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Model: ");
        errorReport.append(Build.MODEL);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Id: ");
        errorReport.append(Build.ID);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Product: ");
        errorReport.append(Build.PRODUCT);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("\n************ FIRMWARE ************\n");
        errorReport.append("SDK: ");
        errorReport.append(Build.VERSION.SDK);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Release: ");
        errorReport.append(Build.VERSION.RELEASE);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Incremental: ");
        errorReport.append(Build.VERSION.INCREMENTAL);
        errorReport.append(LINE_SEPARATOR);

        return errorReport.toString();
    }
}