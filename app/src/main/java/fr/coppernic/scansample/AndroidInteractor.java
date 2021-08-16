package fr.coppernic.scansample;

import android.content.Context;
import android.content.pm.ApplicationInfo;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

class AndroidInteractor {
    private static final String BASE_NAME_SYSTEM_SERVICE = "fr.coppernic.service";
    private static final String BASE_NAME_BARCODE_MANAGER = "fr.coppernic.features.barcode";
    private final Context context;


    AndroidInteractor(Context ctx) {
        context = ctx;
    }

    String loadPackage() {
        final List<ApplicationInfo> appsInfo = context.getPackageManager().getInstalledApplications(0);
        final Set<String> packagesName = new TreeSet<>();
        
        for (final ApplicationInfo appInfo : appsInfo) {
            packagesName.add(appInfo.packageName);
        }

        for (final String packageName : packagesName) {
            if (packageName.startsWith(BASE_NAME_BARCODE_MANAGER) || packageName.startsWith(BASE_NAME_SYSTEM_SERVICE)) {
                return packageName;
            }
        }
        return "";
    }
}
