package in.greenboxinnovations.android.pumpmaster;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class AddNewCar extends AppCompatActivity {

    private int cust_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_car);

        //noinspection ConstantConditions
        cust_id = getIntent().getExtras().getInt("cust_id",-1);

        Log.e("cust_id",""+cust_id);
    }
}
