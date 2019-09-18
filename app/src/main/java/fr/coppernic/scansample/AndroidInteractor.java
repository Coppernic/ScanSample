package fr.coppernic.scansample;

import android.content.Context;
import android.content.pm.ApplicationInfo;

import java.util.List;

import io.reactivex.annotations.Nullable;

class AndroidInteractor {

    String regex = "^([A-Za-z]{1}[A-Za-z\\d_]*\\.)+[A-Za-z][A-Za-z\\d_]*$";

    @Nullable
    String isAppInstalled(final Context context, final String packageName) {
        final List<ApplicationInfo> appsInfo = context.getPackageManager().getInstalledApplications(0);
        for (final ApplicationInfo appInfo : appsInfo) {
            if (appInfo.packageName.contains(packageName) && appInfo.packageName.matches(regex)) {
                return appInfo.packageName;
            }
        }
        return null;
    }
}
