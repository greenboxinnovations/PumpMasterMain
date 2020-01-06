package in.greenboxinnovations.android.pumpmaster;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;


public class TestActivity extends AppCompatActivity {

    private CoordinatorLayout coordinatorLayout;
    private int minBattLevel = 5;

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        init();


        if (batteryOK(minBattLevel)) {
            if (isWiFiEnabled()) {
                pingServer();
            } else {
                showSnackBar("Please enable WiFi and try again");
            }
        } else {
            showSnackBar("Insufficient Battery Level");
        }

        loadPhoto("http://192.168.1.105/pump_master/uploads/2018-04-07/648_pump.jpeg");
    }

    private void init() {
        coordinatorLayout = findViewById(R.id.cl_test);
        imageView = findViewById(R.id.im_test);
    }


    private void showSnackBar(String msg) {
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, msg, Snackbar.LENGTH_LONG);
        snackbar.show();
    }


    private boolean isWiFiEnabled() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connManager != null;
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifi.isConnected();
    }


    private void pingServer() {
        String url = getResources().getString(R.string.url_main);
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        showSnackBar("Success");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                showSnackBar("Server Error");
            }
        });
        MySingleton.getInstance(this.getApplicationContext()).addToRequestQueue(stringRequest);
    }


    private boolean batteryOK(int level) {
        BatteryManager bm = (BatteryManager) getSystemService(BATTERY_SERVICE);
        assert bm != null;
        int batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);

        Log.e("batt", "" + batLevel);
        return batLevel < level;
    }


    private void loadPhoto(String url) {
//        Picasso.get()
//                .load(url)
//                .placeholder(R.drawable.user_placeholder)
//                .error(R.drawable.user_placeholder_error)
//                .into(imageView);
        Picasso.get()
                .load(url)
                .into(imageView);
    }

}
