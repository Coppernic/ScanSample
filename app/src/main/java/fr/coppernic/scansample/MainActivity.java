package fr.coppernic.scansample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import fr.coppernic.sdk.barcode.BarcodeReader;

public class MainActivity extends AppCompatActivity {

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
    protected void onStart() {
        super.onStart();
        registerReceiver();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(scanResult);
        stopScan();
    }

    /**
     * Triggers a barcode scan
     */

    private void startScan () {
        BarcodeReader.ServiceManager.startScan(this);
    }

    private void stopScan() {
        BarcodeReader.ServiceManager.stopScan(this);
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
