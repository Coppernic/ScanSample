Barcode scanning
================

Context 
-----------

This application demonstrates how to scan a barcode on Coppernic device.

Prerequisites
-------------

CpcSystemServices from version 2.0.4 needs to be installed on device.

Trig a barcode
--------------

There are 2 ways to trig a barcode reading:
- Remap a physical button to barcode scan function
- Send an intent

### Remap a physical button to barcode reading

In the settings, go to remap key & shortcuts (may change on different devices), then remap a key to SCAN or Barcode Scan (device dependent).

### Send an intent

```java
Intent scanIntent = new Intent();
scanIntent.setPackage(SERVICE_PACKAGE_NAME);
scanIntent.setAction(INTENT_ACTION_SCAN);
scanIntent.putExtra(KEY_PACKAGE, this.getPackageName());
ComponentName info = this.startService(scanIntent);
if (info != null) {
    // OK
} else {
    // Error
}
```

where

```java
// With Barcode Manager App (C-One²)
private static final String SERVICE_PACKAGE_NAME = "fr.coppernic.features.barcode.conen"; //conen for C-One², idplatform for ID Platform, ...
// With CpcSystemServices
private static final String SERVICE_PACKAGE_NAME = "fr.coppernic.service.cfive"; //cfive for C-five, ceight for C-eight, cone for C-One
private static final String INTENT_ACTION_SCAN = "fr.coppernic.intent.action.SCAN";
private static final String KEY_PACKAGE = "package";
```

Get data read
-------------
Data read (and errors) are sent back with an intent. You need to declare a BroadcastReceiver to get it:

```java
private BroadcastReceiver scanResult = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION_SCAN_SUCCESS)) {
            String dataRead = intent.getExtras().getString(BARCODE_DATA);
        } else if (intent.getAction().equals(ACTION_SCAN_ERROR)) {
            int result = intent.getIntExtra(KEY_RESULT, CpcResult.RESULT.ERROR.ordinal());
            CpcResult.RESULT resultAsEnum = CpcResult.RESULT.values()[result];
            Toast.makeText(context, getString(R.string.scan_error, resultAsEnum.toString()), Toast.LENGTH_SHORT).show();
        }
    }
};
```

Where

```java
public final static String ACTION_SCAN_SUCCESS = "fr.coppernic.intent.scansuccess";
public final static String ACTION_SCAN_ERROR = "fr.coppernic.intent.scanfailed";
public final static String BARCODE_DATA = "BarcodeData";
```

This broadcast receiver needs to be registered:

```java
private void registerReceiver() {
    IntentFilter filter = new IntentFilter();
    filter.addAction(ACTION_SCAN_SUCCESS);
    filter.addAction(ACTION_SCAN_ERROR);
    registerReceiver(scanResult, filter);
}
```
For example in the onResume method of an Activity:

```java
@Override
protected void onResume() {
    super.onResume();
    registerReceiver();
}
```

And unregistered in the onPause for example:

```java
@Override
protected void onPause() {
     super.onPause();
     unregisterReceiver(scanResult);
}
```

Configure
---------

Barcode reader can be configured via Barcode Manager application. This application
is usually installed on devices. It is also available on [F-Droid](https://coppernic.github.io/coppernic/2018/02/13/F-Droid.html).

### General

* Scan sound : play a sound when scan is ended or not.
* Scan display : Diosplay the scanindicator on screen or not
* Continuous mode : Enable continuous mode or not (scan until button is released or scan until good read or timeout)
* Scan timeout : scan timeout
* Barcode service startup at boot: Enables/disables automatic barcode service start when device boot is finished.
* Display barcode notification: Displays a barcode notification in status bar or not.
* Keep barcode reader opened: Id checked, improves scan speed.
* Keyboard wedge: Sends data to input buffer in addition to intent.
* Keyboard fast wedge : Use fatser keyboard wedge, needs an additional keyboiard to be installed.
* Search and replace: Use regular expression to search and replace in data read.

### Barcode reader

### Parameters

Depends on devices.

# Symbologies

Allows user to enable/disable symbologies, to add suffix and prefix, mininmal and maximum length that can be read.

