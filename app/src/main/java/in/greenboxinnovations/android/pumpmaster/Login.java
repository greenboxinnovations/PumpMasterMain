package in.greenboxinnovations.android.pumpmaster;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Login extends AppCompatActivity {

    MyGlobals myGlobals;
    private CoordinatorLayout coordinatorLayout;
    private TextView userName, password;
    private String userNameVal, passwordVal;
    private ProgressBar progress_bar;
    private Button login, retry;
    private LinearLayout login_layout;
    private RelativeLayout no_connection_layout;

    private boolean isWiFiEnabled;

    //shared pref variables
    private static final String APP_SHARED_PREFS = "prefs";
    private SharedPreferences sharedPrefs;
    private String imei;
    private int STORAGE_PERMISSION_CODE = 101;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        myGlobals = new MyGlobals(getApplicationContext());
        isWiFiEnabled = myGlobals.isWiFiEnabled();

        init();
        // checkers
        networkChecker();

        checkPhotoDir();

        // login
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                if (vibe != null) {
                    vibe.vibrate(50);
                }
                logUser();

//                Snackbar.make(coordinatorLayout, "Empty Fields!", Snackbar.LENGTH_SHORT).show();
            }
        });

        // network retry
        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                networkChecker();
            }
        });

    }

    // Ensure the right menu is setup
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login_menu, menu);
        return true;
    }


    // Start your settings activity when a menu item is selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.add_car_qr_new) {
            showDialog_pass(0);
        }
        if (item.getItemId() == R.id.add_new_rate) {
            showDialog_pass(1);
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("HardwareIds")
    @Override
    protected void onResume() {
        super.onResume();

        isWiFiEnabled = myGlobals.isWiFiEnabled();
        if (!isWiFiEnabled) {
            Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (vibe != null) {
                vibe.vibrate(50);
            }
            Snackbar.make(coordinatorLayout, "Please Enable Wifi", Snackbar.LENGTH_LONG).show();
            login_layout.setVisibility(View.INVISIBLE);
            no_connection_layout.setVisibility(View.VISIBLE);
        } else {
            login_layout.setVisibility(View.VISIBLE);
            no_connection_layout.setVisibility(View.INVISIBLE);
        }

        if (!sharedPrefs.getString("shift", "").equals("")) {
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            finish();
        }

        if (!isReadPhoneStateAllowed()) {
            requestPermission();
        } else {
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            if (telephonyManager != null) {
                imei = telephonyManager.getDeviceId();
            }
            Log.e("login", imei);
        }
    }

    private void init() {

        progress_bar = findViewById(R.id.pb_login);
        retry = findViewById(R.id.b_wifi_retry);
        login_layout = findViewById(R.id.login_layout);
        no_connection_layout = findViewById(R.id.no_connection_layout);

        userName = findViewById(R.id.et_Name);
        password = findViewById(R.id.et_Pass);
        login = findViewById(R.id.bLoginBtn);

        coordinatorLayout = findViewById(R.id.login_view);

        sharedPrefs = getApplicationContext().getSharedPreferences(APP_SHARED_PREFS, Context.MODE_PRIVATE);
    }


    private void checkPhotoDir() {
        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + "/pump_master");
        Log.e("h", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString());
        if (folder.exists()) {
            Log.e("rag", "exists");
        } else {
            if (folder.mkdirs()) {
                Log.e("rag", "pump_master dir created");
            }
        }
    }

    private void redirect() {
        Intent i = new Intent(getApplicationContext(), SetRates.class);
        i.putExtra("userNameVal", userNameVal);
        i.putExtra("passwordVal", passwordVal);
        i.putExtra("imei", imei);
        startActivity(i);
        finish();
    }


    private void logUser() {


        String url = getResources().getString(R.string.url_main);

        url = url + "/exe/login_and.php";

        Date cDate = new Date();
        final String date = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(cDate);
        Log.e("date", date);

        userNameVal = userName.getText().toString();
        passwordVal = password.getText().toString();
        if ((userName.getText().length() == 0) || (password.getText().length() == 0)) {
            //Toast.makeText(this, "Fill All Fields!", Toast.LENGTH_SHORT).show();
            View view = Login.this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                assert imm != null;
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                view.clearFocus();
            }
            Snackbar.make(coordinatorLayout, "Empty Fields!", Snackbar.LENGTH_SHORT).show();
        } else {

            // xml for slow networks
            login.setVisibility(View.GONE);
            progress_bar.setVisibility(View.VISIBLE);

            JSONObject jsonObj = new JSONObject();
            try {
                jsonObj.put("name", userNameVal);
                jsonObj.put("pass", passwordVal);
                jsonObj.put("imei", imei);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.e("credentials", jsonObj.toString());

            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                    url, jsonObj,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            Log.e("login response", response.toString());

                            // xml for slow networks
                            login.setVisibility(View.VISIBLE);
                            progress_bar.setVisibility(View.GONE);

                            try {
                                if (response.getBoolean("success")) {
                                    Log.e("result", "success");
                                    if ((response.getString("date").equals(date)) && (response.getBoolean("rate_set"))) {

                                        Snackbar.make(coordinatorLayout, "Access Granted.", Snackbar.LENGTH_SHORT).show();
                                        sharedPrefs.edit()
                                                .putString("date", date)
                                                .putInt("user_id", response.getInt("user_id"))
                                                .putInt("pump_id", response.getInt("pump_id"))
                                                .putString("petrol_rate", response.getString("petrol_rate"))
                                                .putString("diesel_rate", response.getString("diesel_rate"))
                                                .putString("user_name", response.getString("user_name"))
                                                .apply();
                                        showDialog();
                                    } else {
                                        redirect();
                                    }

                                } else {
                                    Log.e("result", "fail");
                                    // hide the keyboard
                                    View view = Login.this.getCurrentFocus();
                                    if (view != null) {
                                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                        assert imm != null;
                                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                        view.clearFocus();
                                    }


                                    Snackbar.make(coordinatorLayout, "Access Denied Or Wrong User", Snackbar.LENGTH_SHORT).show();
                                    sharedPrefs.edit()
                                            .putInt("user_id", -1)
                                            .putInt("pump_id", -1)
                                            .apply();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("Volley Error", "Error: " + error.getMessage());
                    Snackbar.make(coordinatorLayout, "Network Error", Snackbar.LENGTH_LONG).show();
                    //xml for slow networks
                    login.setVisibility(View.VISIBLE);
                    progress_bar.setVisibility(View.GONE);
                }
            }) {

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("charset", "utf-8");
                    return headers;
                }
            };
            MySingleton.getInstance(this.getApplicationContext()).addToRequestQueue(jsonObjReq);

        }
    }

    private void showDialog() {
        final EditText input = new EditText(this);


        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Select Shift")
                .setPositiveButton("Shift B", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        next("b");
                    }
                })
                .setNegativeButton("Shift A", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        next("a");
                    }
                })
                .create();
        input.setImeOptions(EditorInfo.IME_ACTION_DONE);
        dialog.show();

    }

    private void showDialog_pass(final int i) {

        final EditText input = new EditText(this);

        input.setWidth(60);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);


        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Enter Password")
                .setView(input)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int val = Integer.valueOf(String.valueOf(input.getText()));
                        if (val == 124578){
                            if (i == 0){
                                Intent i = new Intent(getApplicationContext(), AddQRCode.class);
                                startActivity(i);
                            }else if(i == 1){
                                Intent i = new Intent(getApplicationContext(), SetRates.class);
                                startActivity(i);
                            }

                        }else{
                            Snackbar.make(coordinatorLayout, "Access Denied", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        input.setImeOptions(EditorInfo.IME_ACTION_DONE);
        dialog.show();

    }

    private void next(String shift) {
        sharedPrefs.edit().putString("shift", shift).apply();
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);
        finish();
    }

    // checkers
    private void networkChecker() {
        isWiFiEnabled = myGlobals.isWiFiEnabled();
        if (isNetworkAvailable() && isWiFiEnabled) {
            login_layout.setVisibility(View.VISIBLE);
            no_connection_layout.setVisibility(View.INVISIBLE);
        } else {
            login_layout.setVisibility(View.INVISIBLE);
            no_connection_layout.setVisibility(View.VISIBLE);
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = null;
        if (connectivityManager != null) {
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        }

        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    private boolean isReadPhoneStateAllowed() {
        //Getting the permission status
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);

        //If permission is granted returning true
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        }


        //If permission is not granted returning false
        return false;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }

    //This method will be called when the user will tap on allow or deny
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //Checking the request code of our request
        if (requestCode == STORAGE_PERMISSION_CODE) {

            //If permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                //Displaying a toast
                Toast.makeText(this, "You Granted the permission", Toast.LENGTH_LONG).show();

            } else {
                //Displaying another toast if permission is not granted
                Toast.makeText(this, "Oops you just denied the permission", Toast.LENGTH_LONG).show();

            }
        }
    }


}
