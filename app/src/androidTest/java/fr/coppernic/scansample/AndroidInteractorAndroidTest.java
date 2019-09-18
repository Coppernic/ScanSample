package fr.coppernic.scansample;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class AndroidInteractorAndroidTest {

    private Context context;
    private AndroidInteractor androidInteractor;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getContext();
        androidInteractor = new AndroidInteractor();
    }

    @Test
    public void isAppInstalledTest() {
        assertEquals(null, androidInteractor.isAppInstalled(context, "fr.coppernic.service"));
        assertEquals("fr.coppernic.feature.barcode.conen", androidInteractor.isAppInstalled(context, "fr.coppernic.feature.barcode"));
    }
}