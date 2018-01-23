package in.greenboxinnovations.android.pumpmaster;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.vision.barcode.Barcode;

import junit.framework.Test;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                Intent scan = new Intent(getApplicationContext(), Scan.class);
                startActivityForResult(scan, 100);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100 && resultCode == RESULT_OK) {
            if (data != null) {
                final Barcode barcode = data.getParcelableExtra("barcode");
                String val = barcode.displayValue;
//                Log.e("code", "" + val);
                Toast.makeText(getApplicationContext(), "Code is " + val, Toast.LENGTH_SHORT).show();
//                if (db.isCodeValid(val)) {
//
//                    Intent newTransaction = new Intent(getApplicationContext(), NewTransaction.class);
//                    newTransaction.putExtra("qr", val);
//                    startActivity(newTransaction);
//                } else {
//                    Toast.makeText(getApplicationContext(), "Invalid Code" + val, Toast.LENGTH_SHORT).show();
//                }
            }
        }
    }

    // Ensure the right menu is setup
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    // Start your settings activity when a menu item is selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.enterReceipt) {
            Intent settingsIntent = new Intent(this, Test.class);
            startActivity(settingsIntent);
        }
        return super.onOptionsItemSelected(item);
    }



}
