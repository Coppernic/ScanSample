package fr.coppernic.scansample;

import timber.log.Timber;

public class App extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }
}
