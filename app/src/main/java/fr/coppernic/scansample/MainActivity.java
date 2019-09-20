package fr.coppernic.scansample;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import fr.coppernic.sdk.utils.core.CpcDefinitions;
import fr.coppernic.sdk.utils.core.CpcResult;
import fr.coppernic.sdk.utils.core.CpcResult.RESULT;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {
    public static final String ACTION_SCAN_SUCCESS = "fr.coppernic.intent.scansuccess";
    public static final String ACTION_SCAN_ERROR = "fr.coppernic.intent.scanfailed";
    public static final String INTENT_ACTION_SCAN = "fr.coppernic.intent.action.SCAN";
    public static final String BARCODE_DATA = "BarcodeData";
    public static final String BASE_NAME_SYSTEM_SERVICE = "fr.coppernic.service";
    public static final String BASE_NAME_BARCODE_MANAGER = "fr.coppernic.features.barcode";
    public static final String KEY_RESULT = "res";
    public static final String KEY_PACKAGE = "package";
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

    private BroadcastReceiver scanResult = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {
                if (intent.getAction().equals(ACTION_SCAN_SUCCESS)) {
                    String dataRead = intent.getStringExtra(BARCODE_DATA);
                    TextView tvBarcode = findViewById(R.id.tvBarcode);
                    tvBarcode.setText(dataRead);
                } else if (intent.getAction().equals(ACTION_SCAN_ERROR)) {
                    int result = intent.getIntExtra(KEY_RESULT, RESULT.ERROR.ordinal());
                    RESULT resultAsEnum = RESULT.values()[result];
                    // In this case, cancelled means timeout
                    if (resultAsEnum == RESULT.CANCELLED) {
                        resultAsEnum = RESULT.TIMEOUT;
                    }
                    Toast.makeText(context, getString(R.string.scan_error, resultAsEnum.toString()), Toast.LENGTH_SHORT)
                        .show();
                }
            }
        }
    };

    /**
     * Triggers a barcode scan
     **/

    private void startScan() {

        Context context = this.getApplicationContext();
        // We are checking first for BarcodeManager application. This application is installed on Coppernic's products
        // released from 2019. It is replacing barcode service hosted in CpcSystemServices application.
        String packageNameSystemServices = androidInteractor.isAppInstalled(context, BASE_NAME_SYSTEM_SERVICE);
        // We are checking then for CpcSystemServices application installed on older Coppernic's product. This
        // application has the same applicationId than CoreService which in installed on Coppernic's products
        // released from 2019. This is the reason why we are checking for BarcodeManager first. It is for avoiding
        // false positive.
        String packageNameBarcodeManager = androidInteractor.isAppInstalled(context, BASE_NAME_BARCODE_MANAGER);

        if (!packageNameBarcodeManager.isEmpty()) {
            Intent scanIntent = new Intent();
            // We need to set package to send an explicit intent.
            scanIntent.setPackage(packageNameBarcodeManager);
            // We want a scan
            scanIntent.setAction(INTENT_ACTION_SCAN);
            //We are telling who we are to barcode service.
            scanIntent.putExtra(KEY_PACKAGE, BuildConfig.APPLICATION_ID);
            ComponentName info = context.startService(scanIntent);
            if (info != null) {
                Timber.d("Scan Success with Barcode Manager");
            } else {
                Timber.e("Barcode service in Barcode Manager not found");
            }

        } else if (!packageNameSystemServices.isEmpty()) {
            Intent scanIntent = new Intent();
            // We need to set package to send an explicit intent.
            scanIntent.setPackage(packageNameSystemServices);
            // We want a scan
            scanIntent.setAction(INTENT_ACTION_SCAN);
            //We are telling who we are to barcode service.
            scanIntent.putExtra(KEY_PACKAGE, BuildConfig.APPLICATION_ID);
            ComponentName info = context.startService(scanIntent);
            if (info != null) {
                Timber.d("Scan Success with System Service");
            } else {
                Timber.e("Barcode service in System Service not found");
            }
        } else {
            Toast.makeText(context, (R.string.service_install_error_message), Toast.LENGTH_LONG).show();
        }
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_SCAN_SUCCESS);
        filter.addAction(ACTION_SCAN_ERROR);
        registerReceiver(scanResult, filter);
    }
}
