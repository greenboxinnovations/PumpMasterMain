package in.greenboxinnovations.android.pumpmaster;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class UploadService extends Service implements AysncInterface{

    private String TAG, url_main;

    MyGlobals myGlobals;
    boolean isWiFiEnabled;
    private boolean hasFiles = false;
    private boolean isProcessingPhotos = false;
    private final Handler handler = new Handler();
    private File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + "/pump_master");

    public UploadService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        Log.e("onCreate is called", "service");
        super.onCreate();
        TAG = getClass().getSimpleName();
        url_main = getResources().getString(R.string.url_main);

        myGlobals = new MyGlobals(getApplicationContext());
        isWiFiEnabled = myGlobals.isWiFiEnabled();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");

        checkData();

        return START_STICKY;
    }

    // Main
    private void checkData() {

        if (isWiFiEnabled) {

            if (!isProcessingPhotos) {                      // Non blocking , prevent double sending
                scanFiles(folder);
            }
            if (!hasFiles) {     // All data is uploaded stop service
                Log.e(TAG, "nothing found");
                handler.removeMessages(0);
                stop();

            } else {                            // Ops Or Photos still not uploaded
                retry();
            }
        } else {                                // No network retry
            retry();
        }
    }

    private void scanFiles(File f) {
        //create folder before

        File[] files = f.listFiles();

        if (files != null) {
            Log.e("no of files", "" + files.length);

            int counter = 0;

            for (File inFile : files) {
                if (inFile.isFile()) {
                    counter++;
                    sendFile(inFile);
                    Log.e("file found ", "name " + inFile.getName());
                }
            }
            hasFiles = counter > 0;
        }
    }

    private void sendFile(File file) {

        isProcessingPhotos = true;
        try {

            String url_photos = url_main + "/exe/save_android_photos.php";
            URL connectURL = new URL(url_photos);
            HttpFileUpload httpFileUpload = new HttpFileUpload(connectURL, file, this);
            httpFileUpload.execute();

        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
    }

    // Helpers
    private void retry() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkData();
            }
        }, 10 * 1000);
    }

    // Overrides
    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeMessages(0);
        stop();
        Log.e(TAG, "onDestroy");
    }

    private void stop() {
        stopSelf();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.e(TAG, "onTaskRemoved");
    }

    // Interface
    @Override
    public void uploadSuccess() {
        isProcessingPhotos = false;
    }

    @Override
    public void asyncError() {
        isProcessingPhotos = false;
    }
}
