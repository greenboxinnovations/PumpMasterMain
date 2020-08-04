package in.greenboxinnovations.android.pumpmaster;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    MyGlobals myGlobals;
    private boolean isWiFiEnabled;
    private CoordinatorLayout coordinatorLayout;
    private static final String APP_SHARED_PREFS = "prefs";
    private SharedPreferences sharedPrefs;
    private JSONObject jsonObject;
    private int car_id, receipt_number = 0;
    private String pump_code;
    private static final int SCAN_CAR = 100;
    private static final int SCAN_PUMP = 101;
    private static final int ADD_CAR = 102;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        myGlobals = new MyGlobals(getApplicationContext());
        isWiFiEnabled = myGlobals.isWiFiEnabled();

        coordinatorLayout = findViewById(R.id.activity_main_layout);
        sharedPrefs = getApplicationContext().getSharedPreferences(APP_SHARED_PREFS, Context.MODE_PRIVATE);


        init();
        car_id = 0;
        pump_code = null;
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                if (vibe != null) {
                    vibe.vibrate(50);
                }
                Intent scan = new Intent(getApplicationContext(), Scan.class);
                scan.putExtra("title", "Scan Car");
                startActivityForResult(scan, 100);
            }
        });
    }

    private void init() {

        TextView petrol_rate = findViewById(R.id.tv_petrol_rate);
        TextView diesel_rate = findViewById(R.id.tv_diesel_rate);
        TextView user_name = findViewById(R.id.tv_user_name);


        jsonObject = null;

        petrol_rate.setText(sharedPrefs.getString("petrol_rate", "00.00"));
        diesel_rate.setText(sharedPrefs.getString("diesel_rate", "00.00"));
        user_name.setText(sharedPrefs.getString("user_name", "error"));
    }

    @Override
    protected void onResume() {
        super.onResume();

        isWiFiEnabled = myGlobals.isWiFiEnabled();

        String date_login = sharedPrefs.getString("date", "");

        Date cDate = new Date();
        final String date = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(cDate);
        Log.e("date", date);


        if (!date.equals(date_login)) {
            sharedPrefs.edit().clear().apply();
            Intent i = new Intent(getApplicationContext(), Login.class);
            startActivity(i);
            finish();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SCAN_CAR && resultCode == RESULT_OK) {
            if (data != null) {
                final Barcode barcode = data.getParcelableExtra("barcode");
                assert barcode != null;
                String val = barcode.displayValue;
                Log.e("car_qr_code", "" + val);

                isOnlinecustomer(val);

                //moved to after isOnline check false
                //isCodeValid(val);
            }
        }
        if (requestCode == SCAN_PUMP && resultCode == RESULT_OK) {
            if (data != null) {
                final Barcode barcode = data.getParcelableExtra("barcode");

                assert barcode != null;
                String val = barcode.displayValue;
                Log.e("pump_qr_code", "" + val);
                pump_code = val;
                if ((val.equals("8FuAVN303E")) || (val.equals("4xzliayQPL")) || (val.equals("asdfg12345")) || (val.equals("12345asdfg"))) {
                    snapZeroPhoto(val);
                }
            }
        }

        if (requestCode == ADD_CAR && resultCode == RESULT_OK) {
            if (data != null) {
//                String val = data.getParcelableExtra("stuff");
                int car_id = data.getIntExtra("car_id", -1);
                String car_no = data.getStringExtra("car_no");
                Boolean isPetrol = data.getBooleanExtra("isPetrol", false);
                try {
                    jsonObject.put("car_id", car_id);
                    jsonObject.put("car_no", car_no);
                    jsonObject.put("isPetrol", isPetrol);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("ad", Objects.requireNonNull(e.getMessage()));
                }
                Log.e("json", jsonObject.toString());
                // scan pump now
                Intent scan = new Intent(getApplicationContext(), Scan.class);
                scan.putExtra("title", "Scan Pump");
                startActivityForResult(scan, SCAN_PUMP);
            }
        }
    }

    // Ensure the right menu is setup
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    // Start your settings activity when a menu item is selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.add_car_qr) {
            Intent i = new Intent(getApplicationContext(), AddQRCode.class);
            startActivity(i);
        }
        if (item.getItemId() == R.id.enter_receipt) {
            showDialog();
        }
        if (item.getItemId() == R.id.logout) {
            sharedPrefs.edit().clear().apply();
            Intent i = new Intent(getApplicationContext(), Login.class);
            startActivity(i);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDialog() {
        final EditText input = new EditText(this);

        input.setWidth(60);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);


        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Enter receipt no")
                .setView(input)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String text = input.getText().toString();
                        if (!text.equals("")) {
                            isReceiptInLocalDB(text);
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        input.setImeOptions(EditorInfo.IME_ACTION_DONE);
        dialog.show();
    }

    //online database check
    private void isOnlinecustomer(final String val) {
        if (isWiFiEnabled) {

            String url = AppConstants.SCAN_CUST_QR + "?qr=" + val;

            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                    url, null,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            Log.e("login response", response.toString());

                            try {

                                jsonObject = response;

                                //jsonobject comprises of all below items

                                String car_no = response.getString("car_no");
                                String cust_name = response.getString("cust_name");
                                String response_code = response.getString("trans_status");
                                int amount = response.getInt("amount");
                                String fuel_type = response.getString("'fuel_type'");
                                String car_qr_type = response.getString("car_qr_type");

                                if (response_code.equals("success")) {
                                    final AlertDialog.Builder builder =
                                            new AlertDialog.Builder(MainActivity.this).
                                                    setTitle(fuel_type).
                                                    setMessage(amount).
                                                    setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                                            if (vibe != null) {
                                                                vibe.vibrate(50);
                                                            }
                                                            Intent scan = new Intent(getApplicationContext(), Scan.class);
                                                            scan.putExtra("title", "Scan Pump");
                                                            startActivityForResult(scan, SCAN_PUMP);
                                                        }
                                                    });
                                    builder.create().show();
                                }


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

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
                            if (message.equals("QR Invalid")) {
                                isCodeValid(val);
                            }
                            Log.e("NetworkResponse", message);
                            Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_SHORT).show();
                        } catch (UnsupportedEncodingException | JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    Log.e("REF_VERIFY_URL", error.toString());
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

    //local network check
    private void isCodeValid(final String val) {
        if (isWiFiEnabled) {

            String url = getResources().getString(R.string.url_main);

            url = url + "/exe/check_qr.php";

            Log.e("login response", url);

            JSONObject jsonObj = new JSONObject();
            try {
                jsonObj.put("qr", val);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                    url, jsonObj,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            Log.e("login response", response.toString());
                            try {
                                if (response.getBoolean("success")) {

                                    jsonObject = response;
                                    car_id = response.getInt("car_id");

                                    final AlertDialog.Builder builder =
                                            new AlertDialog.Builder(MainActivity.this).
                                                    setMessage("Scan Pump Now").
                                                    setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                                            if (vibe != null) {
                                                                vibe.vibrate(50);
                                                            }
                                                            Intent scan = new Intent(getApplicationContext(), Scan.class);
                                                            scan.putExtra("title", "Scan Pump");
                                                            startActivityForResult(scan, SCAN_PUMP);
                                                        }
                                                    });
                                    builder.create().show();

                                } else {
                                    Log.e("result", "fail");
                                    Snackbar.make(coordinatorLayout, response.getString("msg"), Snackbar.LENGTH_SHORT).show();
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

    private void isReceiptValid(final String val) {
        if (isWiFiEnabled) {

            String url = getResources().getString(R.string.url_hosted);

            url = url + "/exe/check_receipt.php";

            Log.e("login response", url);

            final JSONObject jsonObj = new JSONObject();
            try {
                jsonObj.put("rnum", val);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                    url, jsonObj,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            Log.e("login response", response.toString());
                            try {
                                if (response.getBoolean("success")) {
                                    jsonObject = response;
                                    Log.e("json", jsonObject.toString());
                                    receipt_number = Integer.valueOf(val);

                                    final String cust_name = jsonObject.getString("cust_name");
                                    final int cust_id = jsonObject.getInt("cust_id");


                                    final AlertDialog.Builder builder =
                                            new AlertDialog.Builder(MainActivity.this).
                                                    setMessage(cust_name).
                                                    setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                                            if (vibe != null) {
                                                                vibe.vibrate(50);
                                                            }


                                                            Intent i = new Intent(getApplicationContext(), AddNewCar.class);
                                                            i.putExtra("isReceipt", true);
                                                            i.putExtra("cust_id", cust_id);
                                                            i.putExtra("cust_name", cust_name);
                                                            startActivityForResult(i, ADD_CAR);
                                                        }
                                                    }).
                                                    setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            dialog.dismiss();
                                                        }
                                                    }).setCancelable(false);
                                    builder.create().show();

                                } else {
                                    Log.e("result", "fail");
                                    Snackbar.make(coordinatorLayout, response.getString("msg"), Snackbar.LENGTH_SHORT).show();
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

    private void snapZeroPhoto(final String val) {
        if (isWiFiEnabled) {
            final String url1 = getResources().getString(R.string.url_main);
            final String url = url1 + "/exe/snap_photo.php";

            JSONObject jsonObj = new JSONObject();
            try {
                jsonObj.put("photo_type", "start");
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
                                    ImageView image = new ImageView(MainActivity.this);
                                    Picasso.get().load(url_photo).into(image);

                                    final AlertDialog.Builder builder =
                                            new AlertDialog.Builder(MainActivity.this).
                                                    setMessage("Zero Photo").
                                                    setPositiveButton("Start", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                                            if (vibe != null) {
                                                                vibe.vibrate(50);
                                                            }
                                                            Intent i = new Intent(getApplicationContext(), NewTransaction.class);
                                                            try {
                                                                jsonObject.put("receipt_number", receipt_number);
                                                            } catch (JSONException e) {
                                                                e.printStackTrace();
                                                            }
                                                            i.putExtra("jsonObject", jsonObject.toString());
                                                            i.putExtra("pump_code", val);
                                                            receipt_number = 0;
                                                            startActivity(i);
                                                        }
                                                    }).
                                                    setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            dialog.dismiss();
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


    private void isReceiptInLocalDB(final String val) {
        if (isWiFiEnabled) {

            String url = getResources().getString(R.string.url_main);

            url = url + "/exe/check_receipt_local.php";

            Log.e("login response", url);

            final JSONObject jsonObj = new JSONObject();
            try {
                jsonObj.put("rnum", val);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                    url, jsonObj,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            Log.e("login response", response.toString());
                            try {
                                if (response.getBoolean("success")) {
                                    isReceiptValid(val);
                                } else {
                                    Log.e("result", "fail");
                                    Snackbar.make(coordinatorLayout, response.getString("msg"), Snackbar.LENGTH_SHORT).show();
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
}
