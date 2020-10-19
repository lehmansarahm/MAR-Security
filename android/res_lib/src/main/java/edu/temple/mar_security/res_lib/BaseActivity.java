package edu.temple.mar_security.res_lib;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import edu.temple.mar_security.res_lib.utils.StorageUtil;

import static edu.temple.mar_security.res_lib.utils.Constants.LOG_TAG;
import static edu.temple.mar_security.res_lib.utils.Constants.PERMISSION_REQUESTS;

public abstract class BaseActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback {

    protected boolean arePermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                return false;
            }
        }
        return true;
    }

    protected void getRuntimePermissions() {
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                allNeededPermissions.add(permission);
            }
        }

        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
        }
    }

    protected abstract void moveForward();

    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------


    private int timerDelay = 0, timerPeriod = 1000;
    private Timer serviceTimer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // run stat collection task as background service every five seconds
        final Handler handler = new Handler();

        serviceTimer = new Timer();
        serviceTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        Log.i(LOG_TAG, "Local service heartbeat");
                        // launchService(FileIoOpsService.class);
                        // launchService(MLOpsService.class);
                        // launchService(ProcStatsService.class);
                    }
                });
            }
        }, timerDelay, timerPeriod);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        serviceTimer.cancel();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(LOG_TAG, "Permission granted!");
        if (arePermissionsGranted()) moveForward();
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------

    private static List<String> fileIoEvents = new ArrayList<>();
    private static List<String> mlEvents = new ArrayList<>();

    public static void logFileIoEvent(String filename, String operation, String dataSize) {
        String eventString = (StorageUtil.getTimestamp() + "," + filename + "," + operation + "," + dataSize);
        Log.i(LOG_TAG, "Logging File I/O event: " + eventString);
        fileIoEvents.add(eventString);
    }

    public static void logMLEvent(String descriptor) {
        String eventString = (StorageUtil.getTimestamp() + "," + descriptor);
        Log.i(LOG_TAG, "Logging ML event: " + eventString);
        mlEvents.add(eventString);
    }

    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------

    private String[] getRequiredPermissions() {
        try {
            PackageInfo info = this.getPackageManager().getPackageInfo(this.getPackageName(),
                    PackageManager.GET_PERMISSIONS);
            String[] ps = info.requestedPermissions;
            if (ps != null && ps.length > 0) return ps;
            else return new String[0];
        } catch (Exception e) {
            return new String[0];
        }
    }

    private static boolean isPermissionGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            Log.i(LOG_TAG, "Permission granted: " + permission);
            return true;
        }

        Log.i(LOG_TAG, "Permission NOT granted: " + permission);
        return false;
    }

    /* private void launchService(Class service) {
        Intent serviceIntent = new Intent(BaseActivity.this, service);
        serviceIntent.putExtra(EXTRA_KEY_PROC_ID, android.os.Process.myPid());

        if (service.equals(FileIoOpsService.class)) {
            serviceIntent.putExtra(EXTRA_KEY_EVENT_LIST,
                    fileIoEvents.toArray(new String[fileIoEvents.size()]));
            fileIoEvents.clear();
        } else if (service.equals(MLOpsService.class)) {
            serviceIntent.putExtra(EXTRA_KEY_EVENT_LIST,
                    mlEvents.toArray(new String[mlEvents.size()]));
            mlEvents.clear();
        }

        startService(serviceIntent);
    } */

}