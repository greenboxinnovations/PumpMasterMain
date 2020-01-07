package in.greenboxinnovations.android.pumpmaster;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CarList extends AppCompatActivity implements AdapterCustomerList.gridListener {

    private String url;

    private CoordinatorLayout coordinatorLayout;

    private AdapterCustomerList mAdapter;

    private ArrayList<POJO_id_string> carList = new ArrayList<>();
    private int cust_id = -1;
    private int car_id = -1;
    private String cust_name = "";
    private static final int SCAN_QR_CODE_INTENT = 107;
    private ProgressBar progressBar;
    private boolean inProcess = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_list);

        getBundle(savedInstanceState);
        init();


    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        getData();
    }

    private void init() {
        url = getResources().getString(R.string.url_hosted) + "/api/cars/1";
        coordinatorLayout = findViewById(R.id.cl_car_list);
        progressBar = findViewById(R.id.pb_loading);

        AdapterCustomerList.gridListener mListener = this;

        RecyclerView mRecyclerView = findViewById(R.id.rv_car_list);
        assert mRecyclerView != null;
        mAdapter = new AdapterCustomerList(carList, this, mListener);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

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
                cust_name = extras.getString("cust_name");

            }
        } else {
            cust_id = (int) savedInstanceState.getSerializable("cust_id");
            cust_name = (String) savedInstanceState.getSerializable("cust_name");
        }

        Log.e("tag", " " + cust_id);
    }


    private void getData() {
        String url_local = url + "/" + cust_id;

        Log.e("url", url_local);


        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url_local,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        carList.clear();

                        POJO_id_string pojo1 = new POJO_id_string();
                        pojo1.setCust_id(-99);
                        pojo1.setDisplay_name("ADD NEW CAR");
                        carList.add(pojo1);
                        mAdapter.notifyDataSetChanged();

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


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SCAN_QR_CODE_INTENT && resultCode == RESULT_OK) {
            if (data != null) {
                final Barcode barcode = data.getParcelableExtra("barcode");
                String val = barcode.displayValue;
                Log.e("car_qr_code", "" + val);

                if (!inProcess) {
                    postCode(val);
                }
            }
        }
    }


    @Override
    public void listClick(int position) {

        car_id = carList.get(position).getCust_id();

        if (car_id == -99) {
            Intent i = new Intent(getApplicationContext(), AddNewCar.class);
            i.putExtra("cust_id", cust_id);
            i.putExtra("cust_name", cust_name);
            startActivity(i);
        } else {
            Intent scan = new Intent(getApplicationContext(), Scan.class);
            scan.putExtra("title", "Scan QR Code");
            startActivityForResult(scan, SCAN_QR_CODE_INTENT);
        }
    }


    private void showDialog(String msg) {

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(msg)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .create();
        dialog.show();

    }


    private void postCode(String qr_code) {

        inProcess = true;

        progressBar.setVisibility(View.VISIBLE);

        String url = getResources().getString(R.string.url_hosted);

        url = url + "/exe/post_qr_code.php";

        Date cDate = new Date();
        final String date = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(cDate);
        Log.e("date", date);


        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("cust_id", cust_id);
            jsonObj.put("car_id", car_id);
            jsonObj.put("qr_code", qr_code);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.e("post qr details", jsonObj.toString());


        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                url, jsonObj,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("post code response", response.toString());

                        progressBar.setVisibility(View.INVISIBLE);


                        try {
                            if (response.getBoolean("success")) {
                                Log.e("result", "success");

                                showDialog(response.getString("msg"));


                            } else {
                                Snackbar.make(coordinatorLayout, response.getString("msg"), Snackbar.LENGTH_SHORT).show();

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        inProcess = false;
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volley Error", "Error: " + error.getMessage());
                Snackbar.make(coordinatorLayout, "Network Error", Snackbar.LENGTH_LONG).show();
                progressBar.setVisibility(View.INVISIBLE);
                inProcess = false;
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
