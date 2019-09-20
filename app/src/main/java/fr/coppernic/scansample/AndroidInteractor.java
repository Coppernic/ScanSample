package fr.coppernic.scansample;

import android.content.Context;
import android.content.pm.ApplicationInfo;

import java.util.List;

class AndroidInteractor {

    String isAppInstalled(final Context context, final String packageName) {
        final List<ApplicationInfo> appsInfo = context.getPackageManager().getInstalledApplications(0);
        for (final ApplicationInfo appInfo : appsInfo) {
            if (appInfo.packageName.startsWith(packageName)) {
                return appInfo.packageName;
            }
        }
        return "";
    }
}

