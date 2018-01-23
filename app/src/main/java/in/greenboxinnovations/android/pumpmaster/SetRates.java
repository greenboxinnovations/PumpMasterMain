package in.greenboxinnovations.android.pumpmaster;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SetRates extends AppCompatActivity {

    private EditText petrol_et;
    private EditText diesel_et;
    private Button b_setRates;
    private CoordinatorLayout coordinatorLayout;
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_rates);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        init();

        // limit decimal input range
        petrol_et.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(4, 2)});
        diesel_et.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(4, 2)});

        b_setRates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setRates();
            }
        });
    }

    private void init() {
        url = getResources().getString(R.string.url_base) + "/transactions/rates";
        coordinatorLayout = findViewById(R.id.cl_set_rates);
        petrol_et = findViewById(R.id.et_petrol);
        diesel_et = findViewById(R.id.et_diesel);
        b_setRates = findViewById(R.id.b_setrates);
    }


    private void setRates() {

        // hide keyboard on submit
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            assert imm != null;
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        // check if empty
        String p_string = petrol_et.getText().toString();
        String d_string = diesel_et.getText().toString();
        if (p_string.matches("") || d_string.matches("")) {
            Snackbar.make(coordinatorLayout, "Please Enter Both Rates", Snackbar.LENGTH_SHORT).show();
        } else {
            // check if number is too large
            double p_rate = Double.parseDouble(p_string);
            double d_rate = Double.parseDouble(d_string);

            Log.e("r", "" + p_rate);
            Log.e("r", "" + d_rate);

            if ((p_rate < 1 || p_rate > 140) || (d_rate < 1 || d_rate > 140)) {
                Snackbar.make(coordinatorLayout, "Invalid Rate", Snackbar.LENGTH_SHORT).show();
            } else {
                // jsonObj
                JSONObject jsonObj = new JSONObject();
                try {
                    jsonObj.put("petrol", p_rate);
                    jsonObj.put("diesel", d_rate);
                    jsonObj.put("pump_id", "1");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // volley
                JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                        url, jsonObj,
                        new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {
                                Log.e("login response", response.toString());

                                try {
                                    if (response.getBoolean("success")) {
                                        Log.e("result", "success");
//                                        Snackbar.make(coordinatorLayout, "Access Granted.", Snackbar.LENGTH_SHORT).show();
//                                        sharedPrefs.edit()
//                                                .putInt("user_id", response.getInt("user_id"))
//                                                .putInt("pump_id", response.getInt("pump_id"))
//                                                .putString("user_name", response.getString("user_name"))
//                                                .apply();

//                                    Intent i = new Intent(getApplicationContext(), Splash.class);
//                                    startActivity(i);
//                                    finish();
                                    } else {
                                        Log.e("result", "fail");
                                        String msg = response.getString("msg");
                                        Snackbar.make(coordinatorLayout, msg, Snackbar.LENGTH_SHORT).show();
//                                        sharedPrefs.edit()
//                                                .putInt("user_id", -1)
//                                                .putInt("pump_id", -1)
//                                                .apply();
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
            }
        }
    }


    public class DecimalDigitsInputFilter implements InputFilter {

        Pattern mPattern;

        public DecimalDigitsInputFilter(int digitsBeforeZero, int digitsAfterZero) {
            mPattern = Pattern.compile("[0-9]{0," + (digitsBeforeZero - 1) + "}+((\\.[0-9]{0," + (digitsAfterZero - 1) + "})?)||(\\.)?");
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

            Matcher matcher = mPattern.matcher(dest);
            if (!matcher.matches())
                return "";
            return null;
        }

    }

}
