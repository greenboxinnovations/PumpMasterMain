package in.greenboxinnovations.android.pumpmaster;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CarList extends AppCompatActivity implements AdapterCustomerList.gridListener{

    private String url;
    private boolean isWiFiEnabled;
    private CoordinatorLayout coordinatorLayout;

    private RecyclerView mRecyclerView;
    private LinearLayoutManager linearLayoutManager;
    private AdapterCustomerList mAdapter;

    private ArrayList<POJO_id_string> carList = new ArrayList<>();
    private AdapterCustomerList.gridListener mListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        init();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private void init(){
        url = getResources().getString(R.string.url_main) + "/api/customers/1";
        MyGlobals myGlobals = new MyGlobals(getApplicationContext());
        isWiFiEnabled = myGlobals.isWiFiEnabled();
        coordinatorLayout = findViewById(R.id.cl_new_transaction);

        mListener = this;


        mRecyclerView = findViewById(R.id.rv_customer_list);
        assert mRecyclerView != null;
        mAdapter = new AdapterCustomerList(carList, this, mListener);
        linearLayoutManager = new LinearLayoutManager(this);

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
                            carList.clear();
                            Log.e("resp", response.toString());

//                            try {
//                                for (int i = 0; i < response.length(); i++) {
//                                    // Get current json object
//                                    JSONObject customer = response.getJSONObject(i);
//
//                                    // Get the current student (json object) data
//                                    String cust_company = customer.getString("cust_company");
//                                    String cust_f_name = customer.getString("cust_f_name");
//                                    String cust_l_name = customer.getString("cust_l_name");
//                                    int cust_id = customer.getInt("cust_id");
//
//                                    String display_name = "";
//                                    if (cust_company.equals("")) {
//                                        display_name = cust_f_name + " " + cust_l_name;
//                                    } else {
//                                        display_name = cust_company;
//                                    }
////                                    Log.e("cust_names", display_name);
//
//
//                                    POJO_id_string pojo = new POJO_id_string();
////                                    pojo.setCust_id(cust_id);
//                                    pojo.setDisplay_name(display_name);
//                                    carList.add(pojo);
//
//                                }
////                                mAdapter.updateReceiptsList(customerList);
//                                mAdapter.notifyDataSetChanged();
//                                Log.e("size", "" + customerList.size());
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
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
    public void logStuff(int position) {

    }
}
