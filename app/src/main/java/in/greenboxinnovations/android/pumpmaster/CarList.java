package in.greenboxinnovations.android.pumpmaster;

import android.content.Intent;
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
import com.google.android.gms.vision.barcode.Barcode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CarList extends AppCompatActivity implements AdapterCustomerList.gridListener {

    private String url;
    private boolean isWiFiEnabled;
    private CoordinatorLayout coordinatorLayout;

    private RecyclerView mRecyclerView;
    private LinearLayoutManager linearLayoutManager;
    private AdapterCustomerList mAdapter;

    private ArrayList<POJO_id_string> carList = new ArrayList<>();
    private AdapterCustomerList.gridListener mListener;
    private int cust_id = -1;
    private static final int SCAN_QR_CODE_INTENT = 107;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getBundle(savedInstanceState);
        init();
        getData();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private void init() {
        url = getResources().getString(R.string.url_main) + "/api/cars/1";
        MyGlobals myGlobals = new MyGlobals(getApplicationContext());
        isWiFiEnabled = myGlobals.isWiFiEnabled();
        coordinatorLayout = findViewById(R.id.cl_car_list);

        mListener = this;


        mRecyclerView = findViewById(R.id.rv_car_list);
        assert mRecyclerView != null;
        mAdapter = new AdapterCustomerList(carList, this, mListener);
        linearLayoutManager = new LinearLayoutManager(this);

        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void getBundle(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                cust_id = -1;
            } else {
                cust_id = extras.getInt("cust_id");
            }
        } else {
            cust_id = (int) savedInstanceState.getSerializable("cust_id");
        }

        Log.e("tag", " " + cust_id);
    }


    private void getData() {
        String url_local = url + "/" + cust_id;
        Log.e("url", url_local);
        if (isWiFiEnabled) {

            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                    Request.Method.GET,
                    url_local,
                    null,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            carList.clear();
                            Log.e("resp", response.toString());

                            try {
                                for (int i = 0; i < response.length(); i++) {
                                    // Get current json object
                                    JSONObject car = response.getJSONObject(i);
                                    String car_no_plate = car.getString("car_no_plate");
                                    int car_id = car.getInt("car_id");

                                    POJO_id_string pojo = new POJO_id_string();
                                    pojo.setCust_id(car_id);
                                    pojo.setDisplay_name(car_no_plate);
                                    carList.add(pojo);

                                }
//                                mAdapter.updateReceiptsList(customerList);
                                mAdapter.notifyDataSetChanged();
                                Log.e("size", "" + carList.size());
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SCAN_QR_CODE_INTENT && resultCode == RESULT_OK) {
            if (data != null) {
                final Barcode barcode = data.getParcelableExtra("barcode");
                String val = barcode.displayValue;
                Log.e("car_qr_code", "" + val);
            }
        }
    }


    @Override
    public void listClick(int position) {
        Intent scan = new Intent(getApplicationContext(), Scan.class);
        scan.putExtra("title", "Scan Car");
        startActivityForResult(scan, SCAN_QR_CODE_INTENT);
    }
}
