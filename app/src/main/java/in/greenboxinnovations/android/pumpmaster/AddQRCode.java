package in.greenboxinnovations.android.pumpmaster;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AddQRCode extends AppCompatActivity implements AdapterCustomerList.gridListener {

    private String url;

    private CoordinatorLayout coordinatorLayout;

    private AdapterCustomerList mAdapter;

    private ArrayList<POJO_id_string> customerList = new ArrayList<>();

    private Button retry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_qrcode);

        init();

        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getData();
            }
        });
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        getData();
    }

    private void init() {
        url = getResources().getString(R.string.url_hosted) + "/api/customers/1";
        coordinatorLayout = findViewById(R.id.cl_add_qr_code);

        AdapterCustomerList.gridListener mListener = this;

        retry = findViewById(R.id.b_retry_customer_list);

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


        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        customerList.clear();
                        retry.setVisibility(View.GONE);

                        POJO_id_string pojo1 = new POJO_id_string();
                        pojo1.setCust_id(-99);
                        pojo1.setDisplay_name("ADD NEW CUSTOMER");
                        customerList.add(pojo1);
                        mAdapter.notifyDataSetChanged();


//                        Log.e("resp", response.toString());

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
                        retry.setVisibility(View.VISIBLE);
                        Log.e("r", error.toString());
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
    public void listClick(int position) {
        int cust_id = customerList.get(position).getCust_id();
        if (cust_id == -99) {
            Intent i = new Intent(getApplicationContext(), AddNewCustomer.class);
            startActivity(i);
        } else {
            Intent i = new Intent(getApplicationContext(), CarList.class);
            String cust_name = customerList.get(position).getDisplay_name();
            i.putExtra("cust_id", cust_id);
            i.putExtra("cust_name", cust_name);
            startActivity(i);
        }
    }
}
