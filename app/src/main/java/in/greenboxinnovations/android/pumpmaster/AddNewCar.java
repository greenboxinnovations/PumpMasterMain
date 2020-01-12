package in.greenboxinnovations.android.pumpmaster;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AddNewCar extends AppCompatActivity {

    private int cust_id = 0;
//    private String cust_name;

    private TextView tv_cust_name;
    private Button save;
    private EditText et_vehicle_no;
    private ConstraintLayout constraintLayout;
    private ProgressBar progressBar;
    boolean canClick = true;

    private RadioGroup radioGroup;
    private Boolean isReceipt = false, isPetrol = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_car);

        init();

        //noinspection -ConstantConditions
        if (getIntent().hasExtra("cust_id")) {
            cust_id = Objects.requireNonNull(getIntent().getExtras()).getInt("cust_id", -1);
        }


        if (getIntent().hasExtra("isReceipt")) {
            isReceipt = true;
        }

        if (getIntent().hasExtra("cust_name")) {
            tv_cust_name.setText((Objects.requireNonNull(getIntent().getExtras())).getString("cust_name"));
            tv_cust_name.setVisibility(View.VISIBLE);
        }

        Log.e("cust_id", "" + cust_id);


        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (canClick) {
                    String car_no;
                    String fuel_type = "";

                    car_no = String.valueOf(et_vehicle_no.getText());

                    switch (radioGroup.getCheckedRadioButtonId()) {
                        case R.id.petrol:
                            fuel_type = "petrol";
                            isPetrol = true;
                            break;
                        case R.id.diesel:
                            fuel_type = "diesel";
                            break;
                    }

                    if (!car_no.equals("")) {
                        String clean_car_no = car_no.replaceAll("[^A-Za-z0-9]","").toLowerCase();
                        Log.e("values", "" + clean_car_no + fuel_type);
                        postCustomerCar(clean_car_no, fuel_type);
                    } else {
                        Snackbar.make(constraintLayout, "Please Enter Valid Car Number", Snackbar.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void init() {
        et_vehicle_no = findViewById(R.id.et_vehicle_no);
        tv_cust_name = findViewById(R.id.tv_cust_name);
        progressBar = findViewById(R.id.progressBar_cc);
        radioGroup = findViewById(R.id.radio_fuel);
        radioGroup.check(R.id.petrol);
        save = findViewById(R.id.btn_save_new_car);
        constraintLayout = findViewById(R.id.layout_add_car);
    }

    private void postCustomerCar(final String car_no, String fuel_type) {

        progressBar.setVisibility(View.VISIBLE);
        canClick = false;

        String url = getResources().getString(R.string.url_add_customer_car);

        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("car_no_plate", car_no);
            jsonObj.put("car_fuel_type", fuel_type);
            jsonObj.put("cust_id", cust_id);
            jsonObj.put("car_brand", "unknown");
            jsonObj.put("car_sub_brand", "unknown");
            jsonObj.put("car_qr_code", "");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.e("credentials", jsonObj.toString());

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                url, jsonObj,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("Server add car response", response.toString());

                        // xml for slow networks
                        progressBar.setVisibility(View.INVISIBLE);
                        canClick = true;

                        try {
                            if (response.getBoolean("success")) {
                                Log.e("result", "success");
                                int car_id = response.getInt("car_id");
                                isPetrol = response.getBoolean("isPetrol");
                                if (isReceipt) {
                                    Intent intent = new Intent();
                                    intent.putExtra("car_id", car_id);
                                    intent.putExtra("car_no", car_no);
                                    intent.putExtra("isPetrol", isPetrol);
                                    setResult(RESULT_OK, intent);
                                    finish();
                                } else {
                                    finish();
                                }
                                //Snackbar.make(constraintLayout, "Customer Added Successfully", Snackbar.LENGTH_LONG).show();

                            } else {
                                int car_id = response.getInt("car_id");
                                isPetrol = response.getBoolean("isPetrol");
                                if (isReceipt) {
                                    Intent intent = new Intent();
                                    intent.putExtra("car_id", car_id);
                                    intent.putExtra("car_no", car_no);
                                    intent.putExtra("isPetrol", isPetrol);
                                    setResult(RESULT_OK, intent);
                                    finish();
                                } else {
                                    Snackbar.make(constraintLayout, response.getString("msg"), Snackbar.LENGTH_LONG).show();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volley Error", "Error: " + error.getMessage());
                Snackbar.make(constraintLayout, "Network Error", Snackbar.LENGTH_LONG).show();
                //xml for slow networks
                progressBar.setVisibility(View.INVISIBLE);
                canClick = true;
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
