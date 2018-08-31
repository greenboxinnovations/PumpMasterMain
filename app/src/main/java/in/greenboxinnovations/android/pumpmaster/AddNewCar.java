package in.greenboxinnovations.android.pumpmaster;

import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AddNewCar extends AppCompatActivity {

    private int cust_id;

    private Button save;
    private EditText et_vehicle_no;
    private ConstraintLayout constraintLayout;
    private ProgressBar progressBar;
    boolean canClick = true;

    private RadioGroup radioGroup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_car);

        //noinspection ConstantConditions
        cust_id = getIntent().getExtras().getInt("cust_id",-1);

        Log.e("cust_id",""+cust_id);

        init();

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(canClick){
                    String car_no;
                    String fuel_type = "";

                    car_no = String.valueOf(et_vehicle_no.getText());

                    switch (radioGroup.getCheckedRadioButtonId()){
                        case R.id.petrol:
                            fuel_type = "petrol";
                            break;
                        case R.id.diesel:
                            fuel_type = "diesel";
                            break;
                    }

                    if (!car_no.equals("")){
                        Log.e("values",""+car_no+fuel_type);
                        postCustomerCar(car_no, fuel_type);
                    }else{
                        Snackbar.make(constraintLayout, "Please Input all values correctly", Snackbar.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void init(){
        et_vehicle_no = findViewById(R.id.et_vehicle_no);

        progressBar = findViewById(R.id.progressBar_cc);
        radioGroup = findViewById(R.id.radio_fuel);
        radioGroup.check(R.id.petrol);
        save = findViewById(R.id.btn_save_new_car);
        constraintLayout = findViewById(R.id.layout_add_car);
    }

    private void postCustomerCar(String car_no, String fuel_type) {

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
                        Log.e("login response", response.toString());

                        // xml for slow networks
                        progressBar.setVisibility(View.INVISIBLE);
                        canClick = true;

                        try {
                            if (response.getBoolean("success")) {
                                Log.e("result", "success");
                                //Snackbar.make(constraintLayout, "Customer Added Successfully", Snackbar.LENGTH_LONG).show();
                                finish();
                            } else {
                                Log.e("result", "fail");
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
