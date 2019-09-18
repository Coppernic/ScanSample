package fr.coppernic.scansample;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ComponentInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import fr.coppernic.sdk.core.Defines;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {
    public static final String ACTION_SCAN_SUCCESS = "fr.coppernic.intent.scansuccess";
    public static final String ACTION_SCAN_ERROR = "fr.coppernic.intent.scanfailed";
    public static final String INTENT_ACTION_SCAN = "fr.coppernic.intent.action.SCAN";
    public static final String BARCODE_DATA = "BarcodeData";
    public static final String BASE_NAME_SYSTEM_SERVICE= "fr.coppernic.service";
    public static final String BASE_NAME_BARCODE_MANAGER = "fr.coppernic.features.barcode";
    public final AndroidInteractor androidInteractor = new AndroidInteractor();

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
    }

    /**
     * Triggers a barcode scan
     **/

    private void startScan() {

        //BarcodeReader.ServiceManager.startScan(this);
        Context context = this.getApplicationContext();
        String packageNameSystemService = androidInteractor.isAppInstalled(context, BASE_NAME_SYSTEM_SERVICE);
        String packageNameBarcodeManager = androidInteractor.isAppInstalled(context, BASE_NAME_BARCODE_MANAGER);

        if (packageNameBarcodeManager != null) {
            Intent barcodeIntent = new Intent();
            barcodeIntent.setPackage(packageNameBarcodeManager);
            barcodeIntent.setAction(Defines.IntentDefines.INTENT_ACTION_SCAN);
            barcodeIntent.putExtra(Defines.Keys.KEY_PACKAGE, packageNameBarcodeManager);
            ComponentName info = context.startService(barcodeIntent);
            if (info !=null) {
                Timber.d( "Scan Success with Barcode Manager");
            } else {
                Timber.d( "Scan Error with Barcode Manager");
            }
            Timber.d("Barcode Manager is installed ");

        } else if (packageNameSystemService != null) {
            Intent scanIntent = new Intent();
            scanIntent.setPackage(packageNameSystemService);
            scanIntent.setAction(INTENT_ACTION_SCAN);
            ComponentName info = context.startService(scanIntent);
            if (info !=null) {
                Timber.d( "Scan Success with System Service");
            } else {
                Timber.d( "Scan Error with System Service");
            }
            Timber.d("System Service is installed");

        } else {
            Toast.makeText(context, (R.string.service_install_error_message), Toast.LENGTH_LONG).show();
        }
    }

    private BroadcastReceiver scanResult = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_SCAN_SUCCESS)) {
                String dataRead = intent.getExtras().getString(BARCODE_DATA);
                TextView tvBarcode = (TextView) findViewById(R.id.tvBarcode);
                tvBarcode.setText(dataRead);
            } else if (intent.getAction().equals(ACTION_SCAN_ERROR)) {
                Toast.makeText(context, (R.string.service_install_error_message), Toast.LENGTH_LONG).show();
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
