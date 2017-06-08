package fr.coppernic.scansample;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final String SERVICE_PACKAGE_NAME = "fr.coppernic.service.cfive";
    private static final String INTENT_ACTION_SCAN = "fr.coppernic.intent.action.SCAN";
    private static final String KEY_PACKAGE = "package";
    public final static String ACTION_SCAN_SUCCESS = "fr.coppernic.intent.scansuccess";
    public final static String ACTION_SCAN_ERROR = "fr.coppernic.intent.scanfailed";
    public final static String BARCODE_DATA = "BarcodeData";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startScan();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(scanResult);
    }

    /**
     * Triggers a barcode scan
     */
    private void startScan () {
        Intent scanIntent = new Intent();
        scanIntent.setPackage(SERVICE_PACKAGE_NAME);
        scanIntent.setAction(INTENT_ACTION_SCAN);
        scanIntent.putExtra(KEY_PACKAGE, this.getPackageName());
        ComponentName info = this.startService(scanIntent);
        if (info != null) {
            // OK
        } else {
            // Error
        }
    }

    private BroadcastReceiver scanResult = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_SCAN_SUCCESS)) {
                String dataRead = intent.getExtras().getString(BARCODE_DATA);
                TextView tvBarcode = (TextView)findViewById(R.id.tvBarcode);
                tvBarcode.setText(dataRead);
            } else if (intent.getAction().equals(ACTION_SCAN_ERROR)) {
                // Handle error
            }
        }
    };

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_SCAN_SUCCESS);
        filter.addAction(ACTION_SCAN_ERROR);
        registerReceiver(scanResult, filter);
    }

}
