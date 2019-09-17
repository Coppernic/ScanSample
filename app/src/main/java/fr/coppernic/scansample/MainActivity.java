package fr.coppernic.scansample;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.system.Os;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import fr.coppernic.sdk.core.Defines;
import fr.coppernic.sdk.utils.helpers.OsHelper;
import timber.log.Timber;

//import fr.coppernic.sdk.barcode.BarcodeReader;


public class MainActivity extends AppCompatActivity {

    public final static String ACTION_SCAN_SUCCESS = "fr.coppernic.intent.scansuccess";
    public final static String ACTION_SCAN_ERROR = "fr.coppernic.intent.scanfailed";
    public final static String BARCODE_DATA = "BarcodeData";
    public static final String INTENT_ACTION_SCAN = "fr.coppernic.intent.action.SCAN";

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
    }

    /**
     * Triggers a barcode scan
     */

    private void startScan() {

        //BarcodeReader.ServiceManager.startScan(this);

        Context context = this.getApplicationContext();
        PackageManager pm = context.getPackageManager();
        String packageName = OsHelper.getSystemServicePackage(context);
        boolean isSystemServiceInstall = isPackageInstalled("fr.coppernic.service.cone", pm);
        boolean isBarcodeServiceInstall = isPackageInstalled("fr.coppernic.service.conen", pm);
        Log.d("tag", packageName);

        if (isSystemServiceInstall) {
            Log.d("tag", packageName);
            Intent scanIntent = new Intent();
            scanIntent.setPackage(packageName);
            scanIntent.setAction(INTENT_ACTION_SCAN);
            context.startService(scanIntent);
            Log.d("tag", "System Service is install");
        } else if (isBarcodeServiceInstall) {
            Intent barcodeIntent = getBaseIntent(context);
            barcodeIntent.setPackage(packageName);
            barcodeIntent.setAction(Defines.IntentDefines.INTENT_ACTION_SCAN);
            barcodeIntent.putExtra(Defines.Keys.KEY_PACKAGE, packageName);
            context.startService(barcodeIntent);
            Log.d("tag", "Barcode Manager is install");
        } else {
            if (OsHelper.isConeV2()) {
                Toast.makeText(context,"Barcode Service is required", Toast.LENGTH_LONG).show();
            } else if (OsHelper.isCone()) {
                Toast.makeText(context,"CPC System Service is required", Toast.LENGTH_LONG).show();
            }
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
                // Handle error
            }
        }
    };

    private boolean isPackageInstalled(String packageName, PackageManager packageManager) {
        try {
            packageManager.getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private static Intent getBaseIntent(@NonNull Context context) {
        Intent intent = new Intent();
        String SERVICE_PACKAGE = "fr.coppernic.features.barcode";
        String SERVICE_CLASS = "fr.coppernic.features.barcode.service.BarcodeService";
        String pack = OsHelper.getSystemServicePackage(context, SERVICE_PACKAGE);
        intent.setPackage(pack);
        intent.setComponent(new ComponentName(pack, SERVICE_CLASS));
        return intent;
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_SCAN_SUCCESS);
        filter.addAction(ACTION_SCAN_ERROR);
        registerReceiver(scanResult, filter);
    }
}
