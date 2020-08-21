package in.greenboxinnovations.android.pumpmaster;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class OnlineCustomerAddCar extends AppCompatActivity {

    private TextView et_car_plate_no;
    private ConstraintLayout constraintLayout;
    private POJO_Transaction curTransPOJO;
    private boolean addButtonClick;
    private RadioGroup radioGroup;
    private String fuel_type, unassigned_qr, car_plate_no;

    private static final int SCAN_CAR_QR = 103;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_customer_add_car);

        init();

        curTransPOJO = (POJO_Transaction) getIntent().getSerializableExtra("curTransPOJO"); // Obtain data



    }

    private void init() {

        addButtonClick = false;

        et_car_plate_no = findViewById(R.id.et_online_cust_add_car_plate_no);
        constraintLayout = findViewById(R.id.cl_online_customer_add_car);


        radioGroup = findViewById(R.id.rg_car_fuel_type);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.rb_petrol:
                        fuel_type = "petrol";
                        break;
                    case R.id.rb_diesel:
                        fuel_type = "diesel";
                }

                Log.e("radiogroup", fuel_type);
            }
        });

        // cancel
        Button cancel = findViewById(R.id.b_online_cust_add_car_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonCancel();
            }
        });

        // add car
        Button b_add_car = findViewById(R.id.b_online_cust_add_car_add);
        b_add_car.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonAddCar();
            }
        });
    }

    private void buttonCancel() {
        finish();
    }

    private void buttonAddCar() {
        // assign to global var
        car_plate_no = et_car_plate_no.getText().toString();

        if (car_plate_no.equals("") || car_plate_no.equals("MH12")) {
            Snackbar.make(constraintLayout, "Invalid car number", BaseTransientBottomBar.LENGTH_SHORT).show();
        } else if (fuel_type == null) {
            Snackbar.make(constraintLayout, "Select Fuel Type", BaseTransientBottomBar.LENGTH_SHORT).show();
        } else {

            // qr is unassigned
            if (unassigned_qr == null) {
                Intent scan = new Intent(getApplicationContext(), Scan.class);
                scan.putExtra("title", "Assign Car QR Code");
                startActivityForResult(scan, SCAN_CAR_QR);
            }
            // qr is assigned
            // maybe network request failed
            // allow another request
            else {
                Log.e("network", unassigned_qr);
                serverAddCarPostRequest();
            }


//            if (curTransPOJO != null) {
//            String trans_qr = curTransPOJO.getCust_qr();
//            String trans_qr = "";
//            }
        }
    }

    private void serverAddCarPostRequest() {
        if (!addButtonClick) {
            addButtonClick = true;


            // sanitize vars
            if (paramsValid()) {

                JSONObject jsonObj = new JSONObject();
                try {

//                    jsonObj.put("trans_qr", curTransPOJO.getCust_qr());
                    jsonObj.put("trans_qr", "tcFE2u9DOm");
                    jsonObj.put("car_plate_no", car_plate_no);
                    jsonObj.put("car_fuel_type", fuel_type);
                    jsonObj.put("car_qr", unassigned_qr);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.e("post json", jsonObj.toString());

                JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                        AppConstants.POST_NEW_CAR, jsonObj,
                        new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {
                                Log.e("updateOTStatus", response.toString());
                                try {
                                    if (response.getBoolean("success")) {
                                        final AlertDialog.Builder builder =
                                                new AlertDialog.Builder(OnlineCustomerAddCar.this).
                                                        setTitle("Success").
                                                        setMessage("Car added Successfully").
                                                        setPositiveButton("DONE", new DialogInterface.OnClickListener() {
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
                                    } else {
                                        Log.e("result", "fail");
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                addButtonClick = false;
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
                                Snackbar.make(constraintLayout, message, Snackbar.LENGTH_SHORT).show();
                            } catch (UnsupportedEncodingException | JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        Log.e("MOVE_PEND_TO_COMPLETED", error.toString());
                        addButtonClick = false;
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
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SCAN_CAR_QR && resultCode == RESULT_OK) {
            if (data != null) {
                final Barcode barcode = data.getParcelableExtra("barcode");

                assert barcode != null;
                unassigned_qr = barcode.displayValue;
                serverAddCarPostRequest();
            }
        }
    }

    private boolean paramsValid() {

//        if (curTransPOJO == null) {
//            Log.e("paramsValid", "curTransPOJO null");
//            return false;
//        }
//
//        if (curTransPOJO.getCust_qr() == null) {
//            Log.e("paramsValid", "curTransPOJO.getCust_qr() null");
//            return false;
//        }

        if (car_plate_no == null) {
            Log.e("paramsValid", "car_plate_no null");
            return false;
        }

        if (car_plate_no.equals("") || car_plate_no.equals("MH12")) {
            Log.e("paramsValid", "car_plate_no invalid");
            return false;
        }

        if (fuel_type == null) {
            Log.e("paramsValid", "fuel_type null");
            return false;
        }

        if (unassigned_qr == null) {
            Log.e("paramsValid", "unassigned_qr null");
            return false;
        }

        return true;
    }
}