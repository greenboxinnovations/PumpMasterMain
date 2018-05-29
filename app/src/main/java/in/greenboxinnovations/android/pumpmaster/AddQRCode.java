package in.greenboxinnovations.android.pumpmaster;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AddQRCode extends AppCompatActivity implements AdapterCustomerList.gridListener {

    private String url;
    private boolean isWiFiEnabled;
    private CoordinatorLayout coordinatorLayout;

    private AdapterCustomerList mAdapter;

    private ArrayList<POJO_id_string> customerList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_qrcode);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        init();
        getData();

    }

    private void init() {
        url = getResources().getString(R.string.url_main) + "/api/customers/1";
        MyGlobals myGlobals = new MyGlobals(getApplicationContext());
        isWiFiEnabled = myGlobals.isWiFiEnabled();
        coordinatorLayout = findViewById(R.id.cl_add_qr_code);

        AdapterCustomerList.gridListener mListener = this;


        RecyclerView mRecyclerView = findViewById(R.id.rv_customer_list);
        assert mRecyclerView != null;
//        mRecyclerView.setHasFixedSize(true);
        mAdapter = new AdapterCustomerList(customerList, this, mListener);
//        gridLayoutManager = new GridLayoutManager(this, 2);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }


    private void getData() {
        if (isWiFiEnabled) {

            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                    Request.Method.GET,
                    url,
                    null,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            customerList.clear();
                            Log.e("resp", response.toString());

                            try {
                                for (int i = 0; i < response.length(); i++) {
                                    // Get current json object
                                    JSONObject customer = response.getJSONObject(i);

                                    // Get the current student (json object) data
                                    String cust_company = customer.getString("cust_company");
                                    String cust_f_name = customer.getString("cust_f_name");
                                    String cust_l_name = customer.getString("cust_l_name");
                                    int cust_id = customer.getInt("cust_id");

                                    String display_name;
                                    if (cust_company.equals("")) {
                                        display_name = cust_f_name + " " + cust_l_name;
                                    } else {
                                        display_name = cust_company;
                                    }
//                                    Log.e("cust_names", display_name);


                                    POJO_id_string pojo = new POJO_id_string();
                                    pojo.setCust_id(cust_id);
                                    pojo.setDisplay_name(display_name);
                                    customerList.add(pojo);

                                }
//                                mAdapter.updateReceiptsList(customerList);
                                mAdapter.notifyDataSetChanged();
                                Log.e("size", "" + customerList.size());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // Do something when error occurred
                            Snackbar.make(
                                    coordinatorLayout,
                                    "Error fetching JSON",
                                    Snackbar.LENGTH_LONG
                            ).show();
                        }
                    }
            );
            MySingleton.getInstance(this.getApplicationContext()).addToRequestQueue(jsonArrayRequest);
        }
    }

    @Override
    public void listClick(int position) {
        Intent i = new Intent(getApplicationContext(), CarList.class);
        int cust_id = customerList.get(position).getCust_id();
//        Log.e("cust", "" + cust_id);
        i.putExtra("cust_id", cust_id);
        startActivity(i);
    }
}
