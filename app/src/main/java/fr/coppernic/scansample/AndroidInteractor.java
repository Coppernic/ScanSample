package fr.coppernic.scansample;

import android.content.Context;
import android.content.pm.ApplicationInfo;

import java.util.List;

class AndroidInteractor {
    private static final String BASE_NAME_SYSTEM_SERVICE = "fr.coppernic.service";
    private static final String BASE_NAME_BARCODE_MANAGER = "fr.coppernic.features.barcode";
    private Context context;


    AndroidInteractor(Context ctx) {
        context = ctx;
    }

    String loadPackage() {
        final List<ApplicationInfo> appsInfo = context.getPackageManager().getInstalledApplications(0);
        for (final ApplicationInfo appInfo : appsInfo) {
            if (appInfo.packageName.startsWith(BASE_NAME_BARCODE_MANAGER) || appInfo.packageName.startsWith(BASE_NAME_SYSTEM_SERVICE)) {
                return appInfo.packageName;
            }
        }
        return "";
    }
}
