package in.greenboxinnovations.android.pumpmaster;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
    private String shift, pump_code, cust_name, car_no, low_alert, receipt_number, cust_type, cust_qr;
    private File outputFile;
    private boolean isWiFiEnabled, click = false;
    private SharedPreferences sharedPrefs;

    private POJO_Transaction curTransPOJO;

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_transaction2);

        init();

        boolean isCreditCustomer = getBundleData();

        setPetrolDieselUI();

        if (isCreditCustomer) {
            setInputTextWatchers();
        }

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

    private boolean getBundleData() {

        curTransPOJO = (POJO_Transaction) getIntent().getSerializableExtra("curTransPOJO"); // Obtain data

        boolean isCredit = false;

        pump_code = getIntent().getStringExtra("pump_code");

        Log.e("asd", "" + pump_code);

        p_rate = Double.parseDouble(Objects.requireNonNull(sharedPrefs.getString("petrol_rate", "-1")));
        d_rate = Double.parseDouble(Objects.requireNonNull(sharedPrefs.getString("diesel_rate", "-1")));

        user_id = sharedPrefs.getInt("user_id", -1);
        pump_id = sharedPrefs.getInt("pump_id", -1);
        shift = sharedPrefs.getString("shift", "");


        if (curTransPOJO != null) {
            // customer type is online
            // fill in details
            if (curTransPOJO.getCust_type().equals("online")) {
                tv_car_no_plate.setVisibility(View.INVISIBLE);
                tv_cust_name.setVisibility(View.INVISIBLE);

                // auto set amounts and litres
                double doubleAmount = curTransPOJO.getAmount();
                double pre_litVal;
                et_fuel_rs.setText(String.valueOf(doubleAmount));

                if (curTransPOJO.getFuel_type().equals("petrol")) {
                    isPetrol = true;
                    pre_litVal = doubleAmount / p_rate;
                } else {
                    isPetrol = false;
                    pre_litVal = doubleAmount / d_rate;
                }
                double litVal = round(pre_litVal, 2);
                et_fuel_litres.setText(String.valueOf(litVal));

                // make edit texts uneditable
                disableEditText(et_fuel_rs);
                disableEditText(et_fuel_litres);

                // init blank vars
//                car_id = 0;
//                cust_id = 0;
//                cust_name = "";
//                receipt_number = "";
//                car_no = "";

                //cust_type = jsonObj.getString("cust_type");
                //cust_qr = jsonObj.getString("cust_qr_code");
            }
            // customer is a credit customer
            else {
                try {
                    JSONObject jsonObj = new JSONObject(Objects.requireNonNull(getIntent().getStringExtra("jsonObject")));
                    Log.e("jsonObj", jsonObj.toString());

                    // check if jsonObj keys exist
                    // evaluate if online/credit customer

                    // customer is online customer
                    if (jsonObj.has("cust_qr_code")) {


                    }
                    // customer is credit customer
                    else {

                        isCredit = true;

                        isPetrol = jsonObj.getBoolean("isPetrol");
                        car_id = jsonObj.getInt("car_id");
                        cust_id = jsonObj.getInt("cust_id");
                        cust_name = jsonObj.getString("cust_name");
                        receipt_number = jsonObj.getString("receipt_number");
                        car_no = jsonObj.getString("car_no");

                        cust_type = jsonObj.getString("cust_type");
                        cust_qr = jsonObj.getString("cust_qr_code");

                        tv_car_no_plate.setText(car_no);
                        tv_cust_name.setText(cust_name);
                        //Log.e("asd", "" + receipt_number);
                        //Log.e("tag", jsonObj.getString("cust_name"));

                        if (jsonObj.getBoolean("alert")) {
                            tv_low_alert.setVisibility(View.VISIBLE);
                            String alert = "Low Balance : " + jsonObj.getString("alert_value");
                            tv_low_alert.setText(alert);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("errer", Objects.requireNonNull(e.getMessage()));
                }
            }
        }


        return isCredit;
    }

    private void setPetrolDieselUI() {
        if (isPetrol) {
            rl_back.setBackgroundColor(Color.parseColor("#0D9F56"));
            fuel_rate.setText(String.valueOf(p_rate));

        } else {
            rl_back.setBackgroundColor(Color.parseColor("#00AAE8"));
            fuel_type.setText("Diesel");
            fuel_rate.setText(String.valueOf(d_rate));
        }

    }

    private void setInputTextWatchers() {
        et_fuel_litres.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(7, 2)});
        et_fuel_rs.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(6, 2)});

        // text change listener
        et_fuel_litres.addTextChangedListener(new GenericTextWatcher(et_fuel_litres));
        et_fuel_rs.addTextChangedListener(new GenericTextWatcher(et_fuel_rs));
    }

    private void clickPhoto() {
        if (isWiFiEnabled) {

            final String url1 = getResources().getString(R.string.url_main);

            final String url = url1 + "/exe/snap_photo.php";

            Log.e("new transaction url", url);

            JSONObject jsonObj = new JSONObject();
            try {
                jsonObj.put("photo_type", "stop");
                jsonObj.put("car_id", car_id);
                jsonObj.put("pump_qr_code", pump_code);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.e("new-trans post", jsonObj.toString());

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

                                                            // if qr type is trans qr, then show add online add car activity
                                                            if (curTransPOJO.getCust_qr_type() == null) {
                                                                Intent i = new Intent(getApplicationContext(), OnlineCustomerAddCar.class);
                                                                i.putExtra("curTransPOJO", curTransPOJO);
                                                                startActivity(i);
                                                                finish();
                                                            } else {
                                                                finish();
                                                            }
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
                double maxValueAmount = Double.parseDouble(fuel_rs);
                double maxValueLit = Double.parseDouble(fuel_lit);
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
//                jsonObj.put("isPetrol", isPetrol);
//                if (isPetrol) {
//                    jsonObj.put("fuel_rate", p_rate);
//                } else {
//                    jsonObj.put("fuel_rate", d_rate);
//                }
//                jsonObj.put("car_id", car_id);
//                jsonObj.put("amount", fuel_rs);
//                jsonObj.put("liters", fuel_lit);
//                jsonObj.put("cust_id", cust_id);
//                jsonObj.put("user_id", user_id);
//                jsonObj.put("receipt_no", receipt_number);
//                jsonObj.put("pump_qr_code", pump_code);
//                jsonObj.put("shift", shift);
//                jsonObj.put("pump_id", pump_id);
//                jsonObj.put("cust_type", cust_type);

//                jsonObj.put("isPetrol", isPetrol);
                jsonObj.put("fuel_type", curTransPOJO.getFuel_type());
                if (curTransPOJO.getFuel_type().equals("petrol")) {
                    jsonObj.put("fuel_rate", p_rate);
                } else {
                    jsonObj.put("fuel_rate", d_rate);
                }
                jsonObj.put("car_id", curTransPOJO.getCar_id());
                jsonObj.put("amount", fuel_rs);
                jsonObj.put("liters", fuel_lit);
                jsonObj.put("cust_id", curTransPOJO.getCust_id());
                jsonObj.put("user_id", user_id);
                jsonObj.put("receipt_no", curTransPOJO.getReceipt_number());
                jsonObj.put("pump_qr_code", curTransPOJO.getPump_qr());
                jsonObj.put("shift", shift);
                jsonObj.put("pump_id", pump_id);
                jsonObj.put("cust_type", curTransPOJO.getCust_type());

            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("error123", Objects.requireNonNull(e.getMessage()));
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

                                    // update online to move pending to completed transactions
//                                    if (cust_type.equals("online")) {
//                                        updateOnlineCustomerTransStatus();
//                                    }
                                    if (curTransPOJO.getCust_type().equals("online")) {
                                        updateOnlineCustomerTransStatus();
                                    }

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
                public Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("charset", "utf-8");
                    return headers;
                }
            };
            MySingleton.getInstance(this.getApplicationContext()).addToRequestQueue(jsonObjReq);
        }
    }


    private void updateOnlineCustomerTransStatus() {

        Log.e("updateCustTrans", AppConstants.MOVE_PEND_TO_COMPLETED);

        JSONObject jsonObj = new JSONObject();
        try {
//            jsonObj.put("qr", cust_qr);
//            jsonObj.put("liters", et_fuel_litres.getText().toString());
//
//            if ((isPetrol)) {
//                jsonObj.put("rate", p_rate);
//            } else {
//                jsonObj.put("rate", d_rate);
//            }
//            jsonObj.put("shift", shift);
//            jsonObj.put("attendant_id", user_id);

            jsonObj.put("qr", curTransPOJO.getCust_qr());
            jsonObj.put("liters", et_fuel_litres.getText().toString());

            if ((curTransPOJO.getFuel_type().equals("petrol"))) {
                jsonObj.put("rate", p_rate);
            } else {
                jsonObj.put("rate", d_rate);
            }
            jsonObj.put("shift", shift);
            jsonObj.put("attendant_id", user_id);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.e("postobj", jsonObj.toString());

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                AppConstants.MOVE_PEND_TO_COMPLETED, jsonObj,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("updateOTStatus", response.toString());
                        try {
                            if (response.getBoolean("success")) {
//                                clickPhoto();
                            } else {
                                Log.e("result", "fail");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        click = false;
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null && networkResponse.statusCode == 409) {
                    // HTTP Status Code: 409 Client error
                    try {
                        String jsonString = new String(networkResponse.data, HttpHeaderParser.parseCharset(networkResponse.headers));
                        JSONObject obj = new JSONObject(jsonString);
                        String message = obj.getString("message");
                        Log.e("NetworkResponse", message);
                        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_SHORT).show();
                    } catch (UnsupportedEncodingException | JSONException e) {
                        e.printStackTrace();
                    }
                }

                Log.e("MOVE_PEND_TO_COMPLETED", error.toString());
                click = false;
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
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }

    private void disableEditText(EditText editText) {
        editText.setFocusable(false);
        editText.setEnabled(false);
        editText.setCursorVisible(false);
        editText.setKeyListener(null);
        editText.setBackgroundColor(Color.TRANSPARENT);
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
}
