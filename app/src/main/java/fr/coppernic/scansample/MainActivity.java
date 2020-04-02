package fr.coppernic.scansample;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
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
    public static final String KEY_RESULT = "res";
    public static final String KEY_PACKAGE = "package";
    private static final String BARCODE_PERMISSION = "fr.coppernic.permission.BARCODE";
    private static final int BARCODE_SERVICE_CODE_PERMISSION = 41;
    private static final int BARCODE_CODE_PERMISSION = 42;
    private static final int BARCODE_SERVICE_TIMEOUT = 5000;
    private boolean isServiceRunning = true;
    private String packageName = "";
    private Button btnStartStop = null;
    private CountDownTimer cdtService = null;
    private boolean isConeV1 = false;

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
                //ask permission for Android 8 and upper
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
                //ask permission for Android 8 and upper
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    requestPermissions(new String[]{BARCODE_PERMISSION},
                            BARCODE_SERVICE_CODE_PERMISSION);
                } else {
                    startStopService();
                }
            }
        });
        AndroidInteractor androidInteractor = new AndroidInteractor(getApplicationContext());
        packageName = androidInteractor.loadPackage();
        if (packageName.contains("service")) {
            isConeV1 = true;
        }
        cdtService = new CountDownTimer(BARCODE_SERVICE_TIMEOUT, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                btnStartStop.setText(getString(R.string.service_waiting, millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                Toast.makeText(getApplicationContext(), getString(R.string.service_not_responding),
                        Toast.LENGTH_SHORT).show();
                if (isServiceRunning) {
                    btnStartStop.setText(R.string.stop_service);
                } else {
                    btnStartStop.setText(R.string.start_service);
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(serviceResult);
    }

    /**
     * No broadcast receiver for the C-OneV1 from his service
     */
    private BroadcastReceiver serviceResult = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {
                switch (intent.getAction()) {
                    case ACTION_SCAN_SUCCESS:
                        scanSuccess(intent);
                        break;
                    case ACTION_SCAN_ERROR:
                        scanError(intent);
                        break;
                    case INTENT_SERVICE_STARTED:
                        serviceStartStopped(false);
                        break;
                    case INTENT_SERVICE_STOPPED:
                        serviceStartStopped(true);
                        break;
                }
            }
        }
    };

    /**
     * Triggers a barcode scan
     **/
    private void startScan() {
        if (!packageName.isEmpty()) {
            Intent scanIntent = new Intent();
            // We need to set package to send an explicit intent.
            scanIntent.setPackage(packageName);
            // We want a scan
            scanIntent.setAction(INTENT_ACTION_SCAN);
            //We are telling who we are to barcode service.
            scanIntent.putExtra(KEY_PACKAGE, BuildConfig.APPLICATION_ID);
            ComponentName info = startService(scanIntent);
            if (info != null) {
                Timber.d("Scan Success with System Service");
            } else {
                Timber.e("Barcode service not found");
            }
        } else {
            Toast.makeText(getApplicationContext(), (R.string.service_install_error_message), Toast.LENGTH_LONG).show();
        }
    }

    private void startStopService() {
        if (!packageName.isEmpty()) {
            Intent startStopIntent = new Intent();
            startStopIntent.setPackage(packageName);
            //We are telling who we are to barcode service.
            startStopIntent.putExtra(KEY_PACKAGE, BuildConfig.APPLICATION_ID);
            if (isServiceRunning) {
                cdtService.start();
                startStopIntent.setAction(ACTION_SERVICE_STOP);
                if (isConeV1) {//to simulate ConeV2 display, no broadcast from v1 service
                    serviceStartStopped(true);
                }
            } else {
                cdtService.start();
                startStopIntent.setAction(ACTION_SERVICE_START);
                if (isConeV1) {//to simulate ConeV2 display, no broadcast from v1 service
                    serviceStartStopped(false);
                }
            }
            ComponentName info = startService(startStopIntent);
            if (info == null) {
                Timber.e("Barcode service in System Service not found");
            }
        }
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_SCAN_SUCCESS);
        filter.addAction(ACTION_SCAN_ERROR);
        filter.addAction(INTENT_SERVICE_STARTED);
        filter.addAction(INTENT_SERVICE_STOPPED);
        registerReceiver(serviceResult, filter);
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
                    startStopService();
                } else {
                    Toast.makeText(this, R.string.permission_required,
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void scanSuccess(Intent intent) {
        String dataRead = intent.getStringExtra(BARCODE_DATA);
        TextView tvBarcode = findViewById(R.id.tvBarcode);
        tvBarcode.setText(dataRead);
    }

    private void scanError(Intent intent) {
        int result = intent.getIntExtra(KEY_RESULT, RESULT.ERROR.ordinal());
        RESULT resultAsEnum = RESULT.values()[result];
        // In this case, cancelled means timeout
        if (resultAsEnum == RESULT.CANCELLED) {
            resultAsEnum = RESULT.TIMEOUT;
        }
        Toast.makeText(getApplicationContext(), getString(R.string.scan_error, resultAsEnum.toString()),
                Toast.LENGTH_SHORT)
                .show();
    }

    private void serviceStartStopped(boolean stop) {
        //declared here in case of start/stop service button spam
        Toast toast = Toast.makeText(getApplicationContext(),
                getString(R.string.service_started), Toast.LENGTH_SHORT);

        if (stop) {
            toast.setText(getString(R.string.service_stopped));
        }
        toast.show();
        isServiceRunning = !stop;
        cdtService.cancel();//don't go to onFinish
        if (isServiceRunning) {
            btnStartStop.setText(R.string.stop_service);
            Log.d("ScanSample", "Barcode service started");
        } else {
            btnStartStop.setText(R.string.start_service);
            Log.d("ScanSample", "Barcode service stopped");
        }
    }
}
