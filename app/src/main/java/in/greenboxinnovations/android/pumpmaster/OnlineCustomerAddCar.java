package in.greenboxinnovations.android.pumpmaster;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

public class OnlineCustomerAddCar extends AppCompatActivity {

    TextView et_car_plate_no;
    ConstraintLayout constraintLayout;
    POJO_Transaction curTransPOJO;
    boolean addButtonClick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_customer_add_car);

        init();

        curTransPOJO = (POJO_Transaction) getIntent().getSerializableExtra("curTransPOJO"); // Obtain data
    }

    private void init() {

        addButtonClick = false;

        et_car_plate_no = findViewById(R.id.et_online_cust_add_car_plate_no);
        constraintLayout = findViewById(R.id.cl_online_customer_add_car);


        // cancel
        Button cancel = findViewById(R.id.b_online_cust_add_car_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonCancel();
            }
        });

        // add car
        Button b_add_car = findViewById(R.id.b_online_cust_add_car_add);
        b_add_car.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonAddCar();
            }
        });
    }

    private void buttonCancel() {
        finish();
    }

    private void buttonAddCar() {
        String curVal = et_car_plate_no.getText().toString();

        if (curVal.equals("") || curVal.equals("MH12")) {
            Snackbar.make(constraintLayout, "Invalid car number", BaseTransientBottomBar.LENGTH_SHORT).show();
        } else {


            if (curTransPOJO != null) {
                String trans_qr = curTransPOJO.getCust_qr();

                serverAddCarPostRequest(trans_qr, curVal);
            }
        }
    }

    private void serverAddCarPostRequest(String trans_qr, String car_plate_no) {
        if (!addButtonClick) {
            addButtonClick = true;
        }
    }
}