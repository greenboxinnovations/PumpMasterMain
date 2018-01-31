package in.greenboxinnovations.android.pumpmaster;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class NewTransaction extends AppCompatActivity {

    private double p_rate = 10;
    private double d_rate = 20;
    private TextView fuel_type, fuel_rate;
    private EditText et_fuel_litres, et_fuel_rs;
    private FloatingActionButton b_new_transaction;
    private boolean isPetrol = false;
    private CoordinatorLayout coordinatorLayout;
    boolean keyLock = false;
    private RelativeLayout rl_back;
    private int car_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_transaction2);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        init();


        try {
            JSONObject jsonObj = new JSONObject(getIntent().getStringExtra("jsonObject"));
            Log.e("tag", jsonObj.getString("cust_name"));
            isPetrol = jsonObj.getBoolean("isPetrol");
            car_id   = jsonObj.getInt("car_id");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (isPetrol) {
            rl_back.setBackgroundColor(Color.parseColor("#0D9F56"));

        } else {
            rl_back.setBackgroundColor(Color.parseColor("#00AAE8"));
            fuel_type.setText("Diesel");
        }

        et_fuel_litres.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(7, 2)});
        et_fuel_rs.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(6, 2)});

        // text change listener
        et_fuel_litres.addTextChangedListener(new GenericTextWatcher(et_fuel_litres));
        et_fuel_rs.addTextChangedListener(new GenericTextWatcher(et_fuel_rs));


        b_new_transaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                newTransaction();
                clickPhoto();
            }
        });
    }

    private void init() {
        coordinatorLayout = findViewById(R.id.cl_new_transaction);
        fuel_type = findViewById(R.id.tv_fuel_type);
        fuel_rate = findViewById(R.id.tv_fuel_rate);
        et_fuel_litres = findViewById(R.id.et_lit);
        et_fuel_rs = findViewById(R.id.et_rs);
        b_new_transaction = findViewById(R.id.b_new_transaction);
        rl_back = findViewById(R.id.rl_back);
    }

    private void clickPhoto() {
        // this.current_photo = type;

        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + "/pump_master");
        String type = "pump";
        String filename = car_id+"_"+type+".png";
        File outputFile = new File(dir, filename);


        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        Uri photoURI = FileProvider.getUriForFile(getApplicationContext(), "in.greenboxinnovations.android.pumpmaster.provider", outputFile);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(intent, 100);

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case 100:
                if (resultCode == RESULT_OK) {
                    //sendFile(outputFile);
                    Log.e("photo", "send");

                    if (!isMyServiceRunning(UploadService.class)) {
                        Intent i = new Intent(getApplication(), UploadService.class);
                        startService(i);
                    }
//
//                  confirmPhoto(current_photo);

                } else if (resultCode == RESULT_CANCELED) {
                    // User cancelled the image capture
                    Log.e("photo result", "cancelled");
                } else {
                    Log.e("photo result", "else");
                }
        }
    }

    private void newTransaction() {

        // hide keyboard on submit
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            assert imm != null;
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }


        String fuel_rs = et_fuel_litres.getText().toString();
        String fuel_lit = et_fuel_rs.getText().toString();

        if (fuel_lit.equals("") || fuel_rs.equals("")) {
            Snackbar.make(coordinatorLayout, "Empty Values Not Allowed", Snackbar.LENGTH_SHORT).show();
        } else {
            Log.e("da", "valid");
        }
    }


    private class GenericTextWatcher implements TextWatcher {

        private View view;

        private GenericTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            String text = editable.toString();
            switch (view.getId()) {
                case R.id.et_lit:
                    if (!keyLock) {
                        keyLock = true;
                        if (!text.equals("")) {
                            double litVal = Double.parseDouble(text);
                            double pre_rsVal;
                            if (isPetrol) {
                                pre_rsVal = litVal * p_rate;
                            } else {
                                pre_rsVal = litVal * d_rate;
                            }
                            double rsVal = round(pre_rsVal, 2);
                            et_fuel_rs.setText(String.valueOf(rsVal));

                        } else {
                            et_fuel_rs.setText("");
                        }
                        keyLock = false;
                    }
                    break;

                case R.id.et_rs:
                    if (!keyLock) {
                        keyLock = true;
                        if (!text.equals("")) {
                            double rsVal = Double.parseDouble(text);
                            double pre_litVal;
                            if (isPetrol) {
                                pre_litVal = rsVal / p_rate;
                            } else {
                                pre_litVal = rsVal / d_rate;
                            }
                            double litVal = round(pre_litVal, 2);
                            et_fuel_litres.setText(String.valueOf(litVal));

                        } else {
                            et_fuel_litres.setText("");
                        }
                        keyLock = false;
                    }
                    break;
            }
        }
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }


    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
