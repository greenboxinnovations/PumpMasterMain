package in.greenboxinnovations.android.pumpmaster;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {

    private CoordinatorLayout coordinatorLayout;
    private TextView userName, password;
    private ProgressBar progress_bar;
    private Button login, retry;
    private LinearLayout login_layout;
    private RelativeLayout no_connection_layout;

    //shared pref variables
    private static final String APP_SHARED_PREFS = "prefs";
    private SharedPreferences sharedPrefs;
    private String imei;
    private int STORAGE_PERMISSION_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        init();
        // checkers
        networkChecker();

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

    @SuppressLint("HardwareIds")
    @Override
    protected void onResume() {
        super.onResume();
        if (sharedPrefs.getInt("user_id", -1) > 0) {
//            Intent i = new Intent(this, Splash.class);
//            startActivity(i);
//            finish();
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
        retry = findViewById(R.id.b_login_retry);
        login_layout = findViewById(R.id.login_layout);
        no_connection_layout = findViewById(R.id.no_connection_layout);

        userName = findViewById(R.id.et_Name);
        password = findViewById(R.id.et_Pass);
        login = findViewById(R.id.bLoginBtn);

        coordinatorLayout = findViewById(R.id.login_view);

        sharedPrefs = getApplicationContext().getSharedPreferences(APP_SHARED_PREFS, Context.MODE_PRIVATE);
    }

    private void logUser() {


        String url = getResources().getString(R.string.url_login);

        String name = userName.getText().toString();
        final String pass = password.getText().toString();
        if ((userName.getText().length() == 0) || (password.getText().length() == 0)) {
            //Toast.makeText(this, "Fill All Fields!", Toast.LENGTH_SHORT).show();
            Snackbar.make(coordinatorLayout, "Empty Fields!", Snackbar.LENGTH_SHORT).show();
        } else {

            // xml for slow networks
            login.setVisibility(View.GONE);
            progress_bar.setVisibility(View.VISIBLE);

            JSONObject jsonObj = new JSONObject();
            try {
                jsonObj.put("name", name);
                jsonObj.put("pass", pass);
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
                                    Snackbar.make(coordinatorLayout, "Access Granted.", Snackbar.LENGTH_SHORT).show();
                                    sharedPrefs.edit()
                                            .putInt("user_id",response.getInt("user_id"))
                                            .putInt("pump_id",response.getInt("pump_id"))
                                            .putString("user_name",response.getString("user_name"))
                                            .apply();

//                                    Intent i = new Intent(getApplicationContext(), Splash.class);
//                                    startActivity(i);
//                                    finish();
                                } else {
                                    Log.e("result", "fail");
                                    Snackbar.make(coordinatorLayout, "Access Denied Or Wrong User", Snackbar.LENGTH_SHORT).show();
                                    sharedPrefs.edit()
                                            .putInt("user_id",-1)
                                            .putInt("pump_id",-1)
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

    // checkers
    private void networkChecker() {
        if (isNetworkAvailable()) {
            login_layout.setVisibility(View.VISIBLE);
            no_connection_layout.setVisibility(View.GONE);
        } else {
            login_layout.setVisibility(View.GONE);
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
        if (result == PackageManager.PERMISSION_GRANTED){
            return true;
        }


        //If permission is not granted returning false
        return false;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, STORAGE_PERMISSION_CODE);
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
