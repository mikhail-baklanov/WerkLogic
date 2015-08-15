package ru.werklogic.werklogic.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

import ru.werklogic.werklogic.R;
import ru.werklogic.werklogic.dm.SensorState;
import ru.werklogic.werklogic.dm.WerkLogicApplication;
import ru.werklogic.werklogic.service.WerkLogicService;

public class Utils {
    private static final String TAG = Utils.class.getName();

    public static void showConfirmDialog(Context context, String title, String message, String yesButtonText, String noButtonText, final Runnable onYesClick) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setMessage(message)
                .setCancelable(true)
                .setPositiveButton(yesButtonText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        onYesClick.run();
                    }
                })
                .setNegativeButton(noButtonText, null);
        if (title != null)
            builder.setTitle(title);
        builder.show();
    }

    public static void showConfirmDialog(Context context, String title, String message, String yesButtonText, String noButtonText, final Runnable onYesClick, final Runnable onClose) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setMessage(message)
                .setCancelable(true)
                .setPositiveButton(yesButtonText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        onYesClick.run();
                    }
                })
                .setNegativeButton(noButtonText, null)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                         @Override
                                         public void onCancel(DialogInterface dialog) {
                                             onClose.run();
                                         }
                                     }
                );
        if (title != null)
            builder.setTitle(title);
        builder.show();
    }

    public static boolean detectWiFi(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifi != null && mWifi.isConnected();
    }

    public static boolean detect3G(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mMobile = connManager
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        return mMobile != null && mMobile.isConnected();
    }

    public static boolean inRoaming(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager != null && telephonyManager.isNetworkRoaming();
    }

    public static String getStackTrace(Throwable e) {
        StringWriter stackTrace = new StringWriter();
        e.printStackTrace(new PrintWriter(stackTrace));
        return stackTrace.toString();
    }

    public static void startService(Context c) {
        log("Запуск сервиса");
        Intent intent = new Intent(c, WerkLogicService.class);
        c.startService(intent);
    }

    public static void stopService(Context c) {
        log("Останов сервиса");
        Intent intent = new Intent(c, WerkLogicService.class);
        c.stopService(intent);
    }


    public static WerkLogicApplication getApplication(Activity a) {
        return (WerkLogicApplication) a.getApplication();
    }

    public static WerkLogicApplication getApplication(Service a) {
        return (WerkLogicApplication) a.getApplication();
    }

    public static void createAppDirs() {
        File dir = new File(getAppDir());
        dir.mkdirs();
        dir = new File(getConfigsDir());
        dir.mkdirs();
        dir = new File(getEventsDir());
        dir.mkdirs();
    }

    public static String getEventsDir() {
        return getAppDir() + "/events";
    }

    public static String getSensorEventsFileName(String sensorGuid) {
        return sensorGuid + ".txt";
    }

    public static String getConfigsDir() {
        return getAppDir() + "/configs";
    }

    public static String getConfigFileName(String checkSum) {
        return checkSum + ".dat";
    }

    public static String getAppDir() {
        String dirName = Environment.getExternalStorageDirectory() + "/werklogic";
        return dirName;
    }

    public static void log(String message) {
        if (logWriter != null)
            logWriter.write(message + "\n");
    }

    public static String bytes2str(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            if (i > 0)
                sb.append(", ");
            sb.append(bytes[i] + " ('" + (char) bytes[i] + "')");
        }
        return sb.toString();
    }

    public static void updateSensorStateView(View v, SensorState.State state) {
        View v1 = v.findViewById(R.id.btnChangeActive);
        if (v1 != null) {
            if (SensorState.State.INIT_ACTIVE.equals(state) ||
                    SensorState.State.INIT_NOT_ACTIVE.equals(state)) {
                v1.setBackgroundResource(
                        SensorState.State.INIT_ACTIVE.equals(state) ? R.drawable.swi_on : R.drawable.swi_off);
            } else {
                v1.setVisibility(View.GONE);
            }
        }
        switch (state) {
            case NOT_INIT:
                v.findViewById(R.id.sensor_state).setBackgroundResource(R.drawable.disconnect);
                break;
            case IN_INIT_PROCESS:
                v.findViewById(R.id.sensor_state).setBackgroundResource(R.drawable.connect_creating);
                break;
            case STEP1:
                v.findViewById(R.id.sensor_state).setBackgroundResource(R.drawable.step1);
                break;
            case STEP2:
                v.findViewById(R.id.sensor_state).setBackgroundResource(R.drawable.step2);
                break;
            case INIT_NOT_ACTIVE:
//                            v.setBackgroundColor(getResources().getColor(R.color.sensor_normal_color));
                break;
            case INIT_ACTIVE:
//                            v.findViewById(R.id.sensor_state).setBackgroundResource(R.drawable.alert);
//                            v.setBackgroundColor(getResources().getColor(R.color.sensor_alert_color));
                break;
        }

    }

    public static String genGuid() {
        String guid = UUID.randomUUID().toString();
        return guid;
    }

    public static Date getDay(Date d) {
        Calendar c = GregorianCalendar.getInstance();
        c.setTime(d);
        Calendar day = GregorianCalendar.getInstance();
        day.clear();
        day.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        return day.getTime();
    }

    public static String genCheckSum() {
        long cs = (long) (Math.random() * 0x100000000L);
        byte b1 = (byte) (cs & 0xFF);
        byte b2 = (byte) ((cs >> 8) & 0xFF);
        byte b3 = (byte) ((cs >> 16) & 0xFF);
        byte b4 = (byte) ((cs >> 24) & 0xFF);
//        return "ABCD1234";
        return ru.werklogic.werklogic.protocol.utils.Utils.byte2hex(b1) +
                ru.werklogic.werklogic.protocol.utils.Utils.byte2hex(b2) +
                ru.werklogic.werklogic.protocol.utils.Utils.byte2hex(b3) +
                ru.werklogic.werklogic.protocol.utils.Utils.byte2hex(b4);
    }

    public static interface LogWriter {
        void write(String message);
    }

    private static LogWriter logWriter = new FileLogWriter();

    public static class FileLogWriter implements LogWriter {

        @Override
        public void write(String message) {
            try {
                Log.i(Utils.class.getSimpleName(), message);
                File log = new File(getAppDir(), "log.txt");
                Writer out = new BufferedWriter(new FileWriter(log, true));
                out.write(message);
                out.close();
            } catch (Exception e) {
                Log.e(TAG, "Error creating log file", e);
            }
        }
    }

    public static void vibrate(Context context) {
        Vibrator v = (Vibrator) context
                .getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null)
            v.vibrate(50);
    }


}
