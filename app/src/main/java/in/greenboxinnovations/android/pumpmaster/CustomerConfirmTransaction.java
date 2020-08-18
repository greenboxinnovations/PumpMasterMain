package in.greenboxinnovations.android.pumpmaster;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class CustomerConfirmTransaction extends AppCompatActivity {

    private POJO_Transaction curTransPOJO = null;
    private TextView tv_car_plate_no, tv_amount, tv_fuel_type;
    private Button b_confirm, b_cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_confirm_transaction);

        init();

        // receive POJO
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            curTransPOJO = (POJO_Transaction) getIntent().getSerializableExtra("curTransPOJO"); //Obtaining data
            assert curTransPOJO != null;
            Log.e("data", curTransPOJO.getCust_type());

            tv_amount.setText(String.valueOf((int) curTransPOJO.getAmount()));
            tv_fuel_type.setText(curTransPOJO.getFuel_type().toUpperCase());
        }
    }


    private void init() {
        tv_amount = findViewById(R.id.tv_cust_confirm_amount);
        tv_car_plate_no = findViewById(R.id.tv_cust_confirm_car_plate_no);
        tv_fuel_type = findViewById(R.id.tv_cust_confirm_fuel_type);

        b_confirm = findViewById(R.id.b_cust_confirm_start);
        b_cancel = findViewById(R.id.b_cust_confirm_cancel);

        b_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonCancel();
            }
        });

        b_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonConfirm();
            }
        });
    }

    private void buttonCancel() {
        finish();
    }

    private void buttonConfirm() {
        Intent data = new Intent();
        data.putExtra("confirm", true);
        setResult(RESULT_OK, data);
        finish();
    }


}