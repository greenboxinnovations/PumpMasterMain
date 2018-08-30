package in.greenboxinnovations.android.pumpmaster;

import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class AddNewCustomer extends AppCompatActivity {

    private Button save;
    private EditText et_f_name,et_m_name,et_l_name,et_mobile,et_address,et_opening_balance;
    private ConstraintLayout constraintLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_customer);


        init();

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String f_name,m_name,l_name,mobile,address,opening_balance;
                f_name = String.valueOf(et_f_name.getText());
                m_name = String.valueOf(et_m_name.getText());
                l_name = String.valueOf(et_l_name.getText());
                mobile = String.valueOf(et_mobile.getText());
                address = String.valueOf(et_address.getText());
                opening_balance = String.valueOf(et_opening_balance.getText());

                if ((!f_name.equals(""))&&(!m_name.equals(""))&&(!l_name.equals(""))&&(!mobile.equals(""))&&(!address.equals(""))&&(!opening_balance.equals(""))){
                    Log.e("values",""+f_name+m_name+l_name+mobile+address+opening_balance);
                }else{
                    Snackbar.make(constraintLayout, "Please Input all values correctly", Snackbar.LENGTH_SHORT).show();
                }



            }
        });

    }

    private void init(){
        et_f_name = findViewById(R.id.et_f_name);
        et_m_name = findViewById(R.id.et_m_name);
        et_l_name = findViewById(R.id.et_l_name);
        et_mobile = findViewById(R.id.et_mobile);
        et_address = findViewById(R.id.et_address);
        et_opening_balance = findViewById(R.id.et_opening_balance);

        save = findViewById(R.id.btn_save_new_customer);

        constraintLayout = findViewById(R.id.add_new_customer_view);

    }
}
