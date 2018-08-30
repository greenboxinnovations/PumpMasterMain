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

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AddNewCustomer extends AppCompatActivity {

    private Button save;
    private EditText et_f_name,et_m_name,et_l_name,et_mobile,et_address,et_opening_balance;
    private ConstraintLayout constraintLayout;
    private ProgressBar progressBar;
    boolean canClick = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_customer);

        init();

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(canClick){
                    String f_name,m_name,l_name,mobile,address,opening_balance;
                    f_name = String.valueOf(et_f_name.getText());
                    m_name = String.valueOf(et_m_name.getText());
                    l_name = String.valueOf(et_l_name.getText());
                    mobile = String.valueOf(et_mobile.getText());
                    address = String.valueOf(et_address.getText());
                    opening_balance = String.valueOf(et_opening_balance.getText());

                    if ((!f_name.equals(""))&&(!m_name.equals(""))&&(!l_name.equals(""))&&(mobile.length() == 10)&&(!address.equals(""))&&(Integer.valueOf(opening_balance) > 0)){
                        Log.e("values",""+f_name+m_name+l_name+mobile+address+opening_balance);
                        postCustomer(f_name, m_name, l_name, mobile, address, opening_balance);
                    }else{
                        Snackbar.make(constraintLayout, "Please Input all values correctly", Snackbar.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }

    private void init(){
        et_f_name = findViewById(R.id.et_f_name);
        et_m_name = findViewById(R.id.et_m_name);
        et_l_name = findViewById(R.id.et_l_name);
        et_mobile = findViewById(R.id.et_mobile);
        et_address = findViewById(R.id.et_address);
        et_opening_balance = findViewById(R.id.et_opening_balance);
        progressBar = findViewById(R.id.progressBar);

        save = findViewById(R.id.btn_save_new_customer);
        constraintLayout = findViewById(R.id.add_new_customer_view);
    }


    private void postCustomer(String f_name,String m_name,String l_name,String mobile,String address,String opening_balance) {

        progressBar.setVisibility(View.VISIBLE);
        canClick = false;

        String url = getResources().getString(R.string.url_add_customer);

        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("cust_f_name", f_name);
            jsonObj.put("cust_m_name", m_name);
            jsonObj.put("cust_l_name", l_name);
            jsonObj.put("cust_ph_no", mobile);
            jsonObj.put("cust_address", address);
            jsonObj.put("cust_balance", opening_balance);
            jsonObj.put("cust_id", JSONObject.NULL);
            jsonObj.put("cust_post_paid", "N");
            jsonObj.put("cust_company", "");
            jsonObj.put("cust_gst", "");
            jsonObj.put("cust_service", "0");
            jsonObj.put("cust_type", "new");

            jsonObj.put("cust_outstanding", "0");
            jsonObj.put("cust_credit_limit", "0");
            jsonObj.put("cust_deposit", "0");
            jsonObj.put("cust_pump_id", "1");


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
