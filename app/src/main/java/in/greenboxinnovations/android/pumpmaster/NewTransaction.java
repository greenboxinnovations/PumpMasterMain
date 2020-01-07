package in.greenboxinnovations.android.pumpmaster;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class NewTransaction extends AppCompatActivity {

    private static final String APP_SHARED_PREFS = "prefs";
    boolean keyLock = false;
    private double p_rate = -1;
    private double d_rate = -1;
    private TextView fuel_type, fuel_rate, tv_car_no_plate, tv_cust_name, tv_low_alert;
    private EditText et_fuel_litres, et_fuel_rs;
    private FloatingActionButton b_new_transaction;
    private boolean isPetrol, complete = false;
    private CoordinatorLayout coordinatorLayout;
    private RelativeLayout rl_back;
    private int car_id, cust_id, user_id, pump_id;
    private String shift, pump_code, cust_name, car_no, low_alert, receipt_number;
    private File outputFile;
    private boolean isWiFiEnabled, click = false;
    private SharedPreferences sharedPrefs;

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_transaction2);

        init();

//        enableDebugMode();

        pump_code = getIntent().getStringExtra("pump_code");

        Log.e("asd", "" + pump_code);

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
            cust_name = jsonObj.getString("cust_name");
            receipt_number = jsonObj.getString("receipt_number");
            car_no = jsonObj.getString("car_no");
            tv_car_no_plate.setText(car_no);
            tv_cust_name.setText(cust_name);
            Log.e("asd", "" + receipt_number);

            if (jsonObj.getBoolean("alert")) {
                tv_low_alert.setVisibility(View.VISIBLE);
                String alert = "Low Balance : " + jsonObj.getString("alert_value");
                tv_low_alert.setText(alert);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("errer", e.getMessage());
        }


//        tv_car_no_plate.setText(car_no);
//        tv_cust_name.setText(cust_name);


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
                    Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    if (vibe != null) {
                        vibe.vibrate(50);
                    }
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

        tv_car_no_plate = findViewById(R.id.tv_car_no_plate);
        tv_cust_name = findViewById(R.id.tv_cust_name);
        tv_low_alert = findViewById(R.id.tv_low_alert);

        et_fuel_rs = findViewById(R.id.et_rs);
        b_new_transaction = findViewById(R.id.b_new_transaction);
        rl_back = findViewById(R.id.rl_back);
    }

    private void clickPhoto() {
        if (isWiFiEnabled) {

            final String url1 = getResources().getString(R.string.url_main);

            final String url = url1 + "/exe/snap_photo.php";

            JSONObject jsonObj = new JSONObject();
            try {
                jsonObj.put("photo_type", "stop");
                jsonObj.put("car_id", car_id);
                jsonObj.put("pump_code", pump_code);

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
                                    String photo_url = response.getString("photo_url");

                                    String url_photo = url1 + "/" + photo_url;

                                    ImageView image = new ImageView(NewTransaction.this);

                                    Picasso.get().load(url_photo).into(image);

                                    final AlertDialog.Builder builder =
                                            new AlertDialog.Builder(NewTransaction.this).
                                                    setMessage("Final Photo").
                                                    setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                                            if (vibe != null) {
                                                                vibe.vibrate(50);
                                                            }
                                                            finish();
                                                        }
                                                    }).setCancelable(false).

                                                    setView(image);
                                    builder.create().show();

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
                public Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("charset", "utf-8");
                    return headers;
                }
            };
            MySingleton.getInstance(this.getApplicationContext()).addToRequestQueue(jsonObjReq);

        } else {
            Snackbar.make(coordinatorLayout, "Please Enable Wifi", Snackbar.LENGTH_LONG).show();
        }


    }

    private void newTransaction() {

        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            assert imm != null;
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        String fuel_rs = et_fuel_rs.getText().toString();

        String fuel_lit = et_fuel_litres.getText().toString();

        if (fuel_lit.equals("") || fuel_rs.equals("")) {
            Snackbar.make(coordinatorLayout, "Empty Values Not Allowed", Snackbar.LENGTH_SHORT).show();
            click = false;
        } else {
            try {
                Double maxValueAmount = Double.valueOf(fuel_rs);
                Double maxValueLit = Double.valueOf(fuel_lit);
                if ((maxValueAmount > 99999.99) || (maxValueLit > 999.99)) {
                    Snackbar.make(coordinatorLayout, "Amount has to be less than 99999.99 or lit less than 999.99", Snackbar.LENGTH_SHORT).show();
                    click = false;
                } else {
                    sendData(fuel_rs, fuel_lit);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Snackbar.make(coordinatorLayout, "Invalid Amount", Snackbar.LENGTH_SHORT).show();
                click = false;
            }


        }
    }

    private void sendData(String fuel_rs, String fuel_lit) {
        if (isWiFiEnabled) {

            String url = getResources().getString(R.string.url_main);

            url = url + "/api/transactions/android";

            JSONObject jsonObj = new JSONObject();
            try {
                jsonObj.put("isPetrol", isPetrol);
                if (isPetrol) {
                    jsonObj.put("fuel_rate", p_rate);
                } else {
                    jsonObj.put("fuel_rate", d_rate);
                }
                jsonObj.put("car_id", car_id);
                jsonObj.put("amount", fuel_rs);
                jsonObj.put("liters", fuel_lit);
                jsonObj.put("cust_id", cust_id);
                jsonObj.put("user_id", user_id);
                jsonObj.put("receipt_no", receipt_number);
                jsonObj.put("pump_code", pump_code);
                jsonObj.put("shift", shift);
                jsonObj.put("pump_id", pump_id);

            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("error", e.getMessage());
            }

            Log.e("Json Object", "obj" + jsonObj.toString());

            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                    url, jsonObj,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            Log.e("newTransaction response", response.toString());
                            try {
                                if (response.getBoolean("success")) {
                                    clickPhoto();
                                } else {
                                    Log.e("result", "fail");
                                    Snackbar.make(coordinatorLayout, response.getString("msg"), Snackbar.LENGTH_SHORT).show();
                                    click = false;
                                    if (response.getString("msg").equals("Duplicate Entry")) {
                                        final AlertDialog.Builder builder =
                                                new AlertDialog.Builder(NewTransaction.this).
                                                        setMessage("This Is A Duplicate Transaction").
                                                        setPositiveButton("Finish", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                                                if (vibe != null) {
                                                                    vibe.vibrate(50);
                                                                }
                                                                finish();
                                                            }
                                                        });
                                        builder.create().show();
                                    }
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

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
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

                            try {
                                double litVal = Double.parseDouble(text);
                                double pre_rsVal;
                                if (isPetrol) {
                                    pre_rsVal = litVal * p_rate;
                                } else {
                                    pre_rsVal = litVal * d_rate;
                                }
                                double rsVal = round(pre_rsVal, 2);
                                et_fuel_rs.setText(String.valueOf(rsVal));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }


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

                            try {
                                double rsVal = Double.parseDouble(text);
                                double pre_litVal;
                                if (isPetrol) {
                                    pre_litVal = rsVal / p_rate;
                                } else {
                                    pre_litVal = rsVal / d_rate;
                                }
                                double litVal = round(pre_litVal, 2);
                                et_fuel_litres.setText(String.valueOf(litVal));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        } else {
                            et_fuel_litres.setText("");
                        }
                        keyLock = false;
                    }
                    break;
            }
        }
    }

//    public void setKeysBasic(String key) {
//        // [START crash_set_keys_basic]
//        Crashlytics.setString(key, "foo" /* string value */);
//
//        Crashlytics.setBool(key, true /* boolean value */);
//
//        Crashlytics.setDouble(key, 1.0 /* double value */);
//
//        Crashlytics.setFloat(key, 1.0f /* float value */);
//
//        Crashlytics.setInt(key, 1 /* int value */);
//        // [END crash_set_keys_basic]
//    }
//
//    public void resetKey() {
//        // [START crash_re_set_key]
//        Crashlytics.setInt("current_level", 3);
//        Crashlytics.setString("last_UI_action", "logged_in");
//        // [END crash_re_set_key]
//    }
//
//    public void logReportAndPrint() {
//        // [START crash_log_report_and_print]
//        Crashlytics.log(Log.DEBUG, "tag", "message");
//        // [END crash_log_report_and_print]
//    }
//
//    public void logReportOnly() {
//        // [START crash_log_report_only]
//        Crashlytics.log("message");
//        // [END crash_log_report_only]
//    }
//
//    public void enableAtRuntime() {
//        // [START crash_enable_at_runtime]
//        Fabric.with(this, new Crashlytics());
//        // [END crash_enable_at_runtime]
//    }
//
//    public void setUserId() {
//        // [START crash_set_user_id]
//        Crashlytics.setUserIdentifier("user123456789");
//        // [END crash_set_user_id]
//    }
//
//    public void methodThatThrows() throws Exception {
//        throw new Exception();
//    }
//
//    public void logCaughtEx() {
//        // [START crash_log_caught_ex]
//        try {
//            methodThatThrows();
//        } catch (Exception e) {
//            Crashlytics.logException(e);
//            // handle your exception here
//        }
//        // [END crash_log_caught_ex]
//    }
//
//    public void enableDebugMode() {
//        // [START crash_enable_debug_mode]
//        final Fabric fabric = new Fabric.Builder(this)
//                .kits(new Crashlytics())
//                .debuggable(true)  // Enables Crashlytics debugger
//                .build();
//        Fabric.with(fabric);
//        // [END crash_enable_debug_mode]
//    }
//
//    public void forceACrash() {
//        // [START crash_force_crash]
//        Button crashButton = new Button(this);
//        crashButton.setText("Crash!");
//        crashButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View view) {
//                Crashlytics.getInstance().crash(); // Force a crash
//            }
//        });
//
//        addContentView(crashButton, new ViewGroup.LayoutParams(
//                ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT));
//        // [END crash_force_crash]
//    }
}
