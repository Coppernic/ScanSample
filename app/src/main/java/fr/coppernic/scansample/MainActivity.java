package fr.coppernic.scansample;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import fr.coppernic.sdk.utils.core.CpcResult.RESULT;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    public static final String ACTION_SERVICE_START = "fr.coppernic.intent.action.start.barcode.service";
    public static final String ACTION_SERVICE_STOP = "fr.coppernic.intent.action.stop.barcode.service";
    public static final String INTENT_SERVICE_STARTED = "fr.coppernic.intent.barcode.service.STARTED";
    public static final String INTENT_SERVICE_STOPPED = "fr.coppernic.intent.barcode.service.STOPPED";
    public static final String ACTION_SCAN_SUCCESS = "fr.coppernic.intent.scansuccess";
    public static final String ACTION_SCAN_ERROR = "fr.coppernic.intent.scanfailed";
    public static final String INTENT_ACTION_SCAN = "fr.coppernic.intent.action.SCAN";
    public static final String BARCODE_DATA = "BarcodeData";
    public static final String BASE_NAME_SYSTEM_SERVICE = "fr.coppernic.service";
    public static final String BASE_NAME_BARCODE_MANAGER = "fr.coppernic.features.barcode";
    public static final String KEY_RESULT = "res";
    public static final String KEY_PACKAGE = "package";
    private static final String BARCODE_PERMISSION = "fr.coppernic.permission.BARCODE";
    private static final int BARCODE_SERVICE_CODE_PERMISSION = 41;
    private static final int BARCODE_CODE_PERMISSION = 42;
    public final AndroidInteractor androidInteractor = new AndroidInteractor();
    private boolean isServiceRunning = true;
    private String packageNameBarcodeManager = "";
    private Button btnStartStop = null;


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
                //ask permission for Android 7 and upper
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    requestPermissions(new String[]{BARCODE_PERMISSION}, BARCODE_CODE_PERMISSION);
                } else {
                    startScan();
                }
            }
        });
        btnStartStop = findViewById(R.id.btnStartStop);
        btnStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ask permission for Android 7 and upper
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    requestPermissions(new String[]{BARCODE_PERMISSION},
                            BARCODE_SERVICE_CODE_PERMISSION);
                } else {
                    startStop();
                }
            }
        });
        // We are checking then for CpcSystemServices application installed on older Coppernic's product. This
        // application has the same applicationId than CoreService which in installed on Coppernic's products
        // released from 2019. This is the reason why we are checking for BarcodeManager first. It is for avoiding
        // false positive.
        packageNameBarcodeManager = androidInteractor.isAppInstalled(getApplicationContext(),
                BASE_NAME_BARCODE_MANAGER);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(onOffResult);
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

    private BroadcastReceiver onOffResult = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {
                if (intent.getAction().equals(INTENT_SERVICE_STARTED)) {
                    Toast.makeText(getApplicationContext(), getString(R.string.service_started),
                            Toast.LENGTH_LONG).show();
                    Log.d("ScanSample", "Barcode service started");
                } else if (intent.getAction().equals(INTENT_SERVICE_STOPPED)) {
                    Toast.makeText(getApplicationContext(), getString(R.string.service_stopped),
                            Toast.LENGTH_LONG).show();
                    Log.d("ScanSample", "Barcode service stopped");
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

    private void startStop() {
        if (!packageNameBarcodeManager.isEmpty()) {
            Intent startStopIntent = new Intent();
            startStopIntent.setPackage(packageNameBarcodeManager);
            //We are telling who we are to barcode service.
            startStopIntent.putExtra(KEY_PACKAGE, BuildConfig.APPLICATION_ID);
            if (isServiceRunning) {
                isServiceRunning = false;
                btnStartStop.setText(R.string.start_service);
                startStopIntent.setAction(ACTION_SERVICE_STOP);
            } else {
                isServiceRunning = true;
                btnStartStop.setText(R.string.stop_service);
                startStopIntent.setAction(ACTION_SERVICE_START);
            }
            ComponentName info = getApplicationContext().startService(startStopIntent);
            if (info == null) {
                Timber.e("Barcode service in System Service not found");
            }
        }
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_SCAN_SUCCESS);
        filter.addAction(ACTION_SCAN_ERROR);
        registerReceiver(scanResult, filter);
        //
        IntentFilter filterOnOff = new IntentFilter();
        filterOnOff.addAction(INTENT_SERVICE_STARTED);
        filterOnOff.addAction(INTENT_SERVICE_STOPPED);
        registerReceiver(onOffResult, filterOnOff);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case BARCODE_CODE_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startScan();
                } else {
                    Toast.makeText(this, R.string.permission_required,
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case BARCODE_SERVICE_CODE_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startStop();
                } else {
                    Toast.makeText(this, R.string.permission_required,
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
