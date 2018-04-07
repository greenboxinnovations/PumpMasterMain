package in.greenboxinnovations.android.pumpmaster;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class NewTransaction extends AppCompatActivity {

    private double p_rate = -1;
    private double d_rate = -1;
    private TextView fuel_type, fuel_rate;
    private EditText et_fuel_litres, et_fuel_rs;
    private FloatingActionButton b_new_transaction;
    private boolean isPetrol,complete = false;
    private CoordinatorLayout coordinatorLayout;
    boolean keyLock = false;
    private RelativeLayout rl_back;
    private int car_id, cust_id, user_id, pump_id, pump_code;
    private String shift;

    private File outputFile;
    private boolean isWiFiEnabled, click = false;

    private static final String APP_SHARED_PREFS = "prefs";
    private SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_transaction2);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        init();

        pump_code = Integer.valueOf(getIntent().getStringExtra("pump_code"));

        p_rate = Double.valueOf(sharedPrefs.getString("petrol_rate", "-1"));
        d_rate = Double.valueOf(sharedPrefs.getString("diesel_rate", "-1"));

        user_id = sharedPrefs.getInt("user_id", -1);
        pump_id = sharedPrefs.getInt("pump_id", -1);
        shift = sharedPrefs.getString("shift", "");

        try {
            JSONObject jsonObj = new JSONObject(getIntent().getStringExtra("jsonObject"));
            Log.e("tag", jsonObj.getString("cust_name"));
            isPetrol = jsonObj.getBoolean("isPetrol");
            car_id = jsonObj.getInt("car_id");
            cust_id = jsonObj.getInt("cust_id");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (isPetrol) {
            rl_back.setBackgroundColor(Color.parseColor("#0D9F56"));
            fuel_rate.setText(String.valueOf(p_rate));

        } else {
            rl_back.setBackgroundColor(Color.parseColor("#00AAE8"));
            fuel_type.setText("Diesel");
            fuel_rate.setText(String.valueOf(d_rate));
        }

        et_fuel_litres.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(7, 2)});
        et_fuel_rs.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(6, 2)});

        // text change listener
        et_fuel_litres.addTextChangedListener(new GenericTextWatcher(et_fuel_litres));
        et_fuel_rs.addTextChangedListener(new GenericTextWatcher(et_fuel_rs));


        b_new_transaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!click) {
                    click = true;
                    newTransaction();
                }


            }
        });
    }

    private void init() {

        MyGlobals myGlobals = new MyGlobals(getApplicationContext());
        isWiFiEnabled = myGlobals.isWiFiEnabled();

        sharedPrefs = getApplicationContext().getSharedPreferences(APP_SHARED_PREFS, Context.MODE_PRIVATE);

        coordinatorLayout = findViewById(R.id.cl_new_transaction);
        fuel_type = findViewById(R.id.tv_fuel_type);
        fuel_rate = findViewById(R.id.tv_fuel_rate);
        et_fuel_litres = findViewById(R.id.et_lit);
        et_fuel_rs = findViewById(R.id.et_rs);
        b_new_transaction = findViewById(R.id.b_new_transaction);
        rl_back = findViewById(R.id.rl_back);
    }

    private void clickPhoto() {
        if (isWiFiEnabled){

            String url = getResources().getString(R.string.url_main);

            url = url + "/exe/snap_photo.php";

            JSONObject jsonObj = new JSONObject();
            try {
                jsonObj.put("photo_type", "stop");
                jsonObj.put("car_id", car_id);
                jsonObj.put("pump_code",pump_code );

            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                    url, jsonObj,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            Log.e("new transaction resp", response.toString());
                            try {
                                if (response.getBoolean("success")) {
                                    //get photo url as response and display here
                                    String photo_url =  response.getString("photo_url");
                                    Log.e("photo_url", photo_url);
                                } else {

                                    Snackbar.make(coordinatorLayout, response.getString("message"), Snackbar.LENGTH_SHORT).show();
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

        }else{
            Snackbar.make(coordinatorLayout, "Please Enable Wifi", Snackbar.LENGTH_LONG).show();
        }


    }

//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        switch (requestCode) {
//
//            case 100:
//                if (resultCode == RESULT_OK) {
//                    //sendFile(outputFile);
//                    Log.e("photo", "send");
//
//                    Bitmap bitmap = decodeSampledBitmapFromResource(outputFile, 640, 480);
//
//                    try {
//                        FileOutputStream out = new FileOutputStream(outputFile);
//                        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, out);
//                        out.flush();
//                        out.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//
//                    if (!isMyServiceRunning(UploadService.class)) {
//                        Intent i = new Intent(getApplication(), UploadService.class);
//                        startService(i);
//                        finish();
//                    }
//                } else if (resultCode == RESULT_CANCELED) {
//                    clickPhoto();
//                } else {
//                    Log.e("photo result", "else");
//                }
//        }
//    }

    private void newTransaction() {

        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            assert imm != null;
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        String fuel_rs = et_fuel_litres.getText().toString();
        String fuel_lit = et_fuel_rs.getText().toString();

        if (fuel_lit.equals("") || fuel_rs.equals("")) {
            Snackbar.make(coordinatorLayout, "Empty Values Not Allowed", Snackbar.LENGTH_SHORT).show();
            click = false;
        } else {
            sendData(fuel_rs, fuel_lit);
        }
    }

    private void sendData(String fuel_rs, String fuel_lit) {
        if (isWiFiEnabled) {

            String url = getResources().getString(R.string.url_main);

            url = url + "/api/transactions/android";

            JSONObject jsonObj = new JSONObject();
            try {
                jsonObj.put("isPetrol", isPetrol);
                jsonObj.put("car_id", car_id);
                jsonObj.put("amount", fuel_rs);
                jsonObj.put("liters", fuel_lit);
                jsonObj.put("cust_id", cust_id);
                jsonObj.put("user_id", user_id);
                jsonObj.put("pump_code", pump_code);
                jsonObj.put("shift", shift);
                jsonObj.put("fuel_rate", fuel_rate);
                jsonObj.put("pump_id", pump_id);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                    url, jsonObj,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            Log.e("newTransaction response", response.toString());
                            try {
                                if (response.getBoolean("success")) {
                                    Snackbar.make(coordinatorLayout, "Transaction Added.", Snackbar.LENGTH_SHORT).show();
                                    clickPhoto();
                                } else {
                                    Log.e("result", "fail");
                                    Snackbar.make(coordinatorLayout, "Invalid Code", Snackbar.LENGTH_SHORT).show();
                                    click = false;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                click = false;
                            }
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("Volley Error", "Error: " + error.getMessage());
                    Snackbar.make(coordinatorLayout, "Network Error", Snackbar.LENGTH_LONG).show();
                    click = false;
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


    private class GenericTextWatcher implements TextWatcher {

        private View view;

        private GenericTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            String text = editable.toString();
            switch (view.getId()) {
                case R.id.et_lit:
                    if (!keyLock) {
                        keyLock = true;
                        if (!text.equals("")) {
                            double litVal = Double.parseDouble(text);
                            double pre_rsVal;
                            if (isPetrol) {
                                pre_rsVal = litVal * p_rate;
                            } else {
                                pre_rsVal = litVal * d_rate;
                            }
                            double rsVal = round(pre_rsVal, 2);
                            et_fuel_rs.setText(String.valueOf(rsVal));

                        } else {
                            et_fuel_rs.setText("");
                        }
                        keyLock = false;
                    }
                    break;

                case R.id.et_rs:
                    if (!keyLock) {
                        keyLock = true;
                        if (!text.equals("")) {
                            double rsVal = Double.parseDouble(text);
                            double pre_litVal;
                            if (isPetrol) {
                                pre_litVal = rsVal / p_rate;
                            } else {
                                pre_litVal = rsVal / d_rate;
                            }
                            double litVal = round(pre_litVal, 2);
                            et_fuel_litres.setText(String.valueOf(litVal));

                        } else {
                            et_fuel_litres.setText("");
                        }
                        keyLock = false;
                    }
                    break;
            }
        }
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }


    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static Bitmap decodeSampledBitmapFromResource(File file, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getPath(), options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(file.getPath(), options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

}
