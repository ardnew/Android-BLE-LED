/*-----------------------------------------------------------------------------
 - Nitelite                                                                   -
 - Copyright (c) 2020 andrew                                                  -
 -                                                                            -
 - Permission is hereby granted, free of charge, to any person obtaining a copy
 - of this software and associated documentation files (the "Software"), to deal
 - in the Software without restriction, including without limitation the rights
 - to use, copy, modify, merge, publish, distribute, sublicense, and/or sell  -
 - copies of the Software, and to permit persons to whom the Software is      -
 - furnished to do so, subject to the following conditions:                   -
 -                                                                            -
 - The above copyright notice and this permission notice shall be included in all
 - copies or substantial portions of the Software.                            -
 -                                                                            -
 - THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR -
 - IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,   -
 - FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE-
 - AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER     -
 - LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 - OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 - SOFTWARE.                                                                  -
 -                                                                            -
 -----------------------------------------------------------------------------*/

package com.ardnew.nitelite;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanRecord;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

class BluetoothRadio {

    // activity request code, used to identify origin in onActivityResult
    static final int REQUEST_BLUETOOTH_ENABLE = 1;
    static final int REQUEST_BLUETOOTH_PERMISSION = 1;
    static final long SCAN_DURATION_MS = 10000;

    private boolean isReady;
    private boolean isScanning;
    private long scanDuration;

    private final ScanActivity scanActivity;
    private final ScanCallback scanCallback;
    private final Handler scanHandler;

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothScanner;

    BluetoothRadio(ScanActivity scanActivity) throws NotSupportedException {

        this.isReady = false;
        this.isScanning = false;
        this.scanDuration = BluetoothRadio.SCAN_DURATION_MS;

        this.scanActivity = scanActivity;
        this.scanCallback = new ScanCallback(this.scanActivity);
        this.scanHandler = new Handler();

        if (this.scanActivity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            this.bluetoothManager = (BluetoothManager) this.scanActivity.getSystemService(Context.BLUETOOTH_SERVICE);
        }

        if (null == this.bluetoothManager) {
            throw new NotSupportedException(
                    this.scanActivity
            );
        }

        this.bluetoothAdapter = this.bluetoothManager.getAdapter();
        this.bluetoothScanner = this.bluetoothAdapter.getBluetoothLeScanner();
    }

    @SuppressWarnings("WeakerAccess")
    public boolean isReady() {

        return this.isReady;
    }

    @SuppressWarnings("SameParameterValue")
    private void setIsReady(boolean isReady) {

        boolean readyChange = isReady != this.isReady;

        // isReady means the adapter has been initialized and we have sufficient permissions to use
        // it for scanning and communications.
        this.isReady = isReady;

        // notify the activity if radio state changed from not-ready to ready
        if (this.isReady && readyChange) {
            this.scanActivity.onBluetoothRadioReady();
        }
    }

    @SuppressWarnings("unused")
    public boolean isScanning() {

        return this.isReady() && this.isScanning;
    }

    void setIsScanning(boolean isScanning) {

        if (this.isReady()) {
            if (isScanning != this.isScanning) {
                if (isScanning) {
                    this.scanHandler.postDelayed(
                            () -> this.setIsScanning(false),
                            this.scanDuration()
                    );
                    this.scanHandler.post(this::scanStart);
                } else {
                    this.scanHandler.post(this::scanStop);
                }
            }
        }
    }

    @SuppressWarnings("WeakerAccess")
    long scanDuration() {

        return this.scanDuration;
    }

    @SuppressWarnings("unused")
    void setScanDuration(long scanDuration) {

        this.scanDuration = (scanDuration > 0) ? scanDuration : BluetoothRadio.SCAN_DURATION_MS;
    }

    private void scanStart() {

        this.isScanning = true;

        AsyncTask.execute(
                () -> {
                    this.scanActivity.runOnUiThread(this.scanActivity::onScanStart);
                    this.bluetoothScanner.startScan(this.scanCallback);
                }
        );
    }

    private void scanStop() {

        this.isScanning = false;

        AsyncTask.execute(
                () -> {
                    this.bluetoothScanner.stopScan(this.scanCallback);
                    this.scanActivity.runOnUiThread(this.scanActivity::onScanStop);
                }
        );
    }

    void enableBluetooth() {

        if ((null == this.bluetoothAdapter) || !this.bluetoothAdapter.isEnabled()) {
            this.scanActivity.startActivityForResult(
                    new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                    BluetoothRadio.REQUEST_BLUETOOTH_ENABLE
            );
        } else {
            this.onEnableBluetooth();
        }
    }

    boolean onEnableBluetoothActivityResult(int resultCode) {
        // should be called from a FragmentActivity.onActivityResult() event handler

        if (0 != resultCode) {
            this.onEnableBluetooth();
            return true;
        }
        return false;
    }

    private void onEnableBluetooth() {

        this.permitBluetoothScan(
                Manifest.permission.ACCESS_FINE_LOCATION,
                this.scanActivity.getResources().getString(R.string.scan_permission_request)
        );
    }

    @SuppressWarnings("SameParameterValue")
    private void permitBluetoothScan(String permission, String message) {

        if (PackageManager.PERMISSION_GRANTED != this.scanActivity.checkSelfPermission(permission)) {

            PermissionRequestEventListener requestEventListener =
                    new PermissionRequestEventListener(this.scanActivity, new String[]{permission});

            new AlertDialog.Builder(this.scanActivity)
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok, null)
                    .setOnDismissListener(requestEventListener)
                    .create().show();

        } else {
            this.onPermitBluetoothScan();
        }
    }

    boolean onPermitBluetoothScanPermissionsResult(int[] grantResults) {
        // should be called from a FragmentActivity.onRequestPermissionsResult() event handler

        if ((grantResults.length > 0) && (PackageManager.PERMISSION_GRANTED == grantResults[0])) {
            this.onPermitBluetoothScan();
            return true;
        }
        return false;
    }

    private void onPermitBluetoothScan() {

        this.setIsReady(true);
    }

    private static class ScanCallback extends android.bluetooth.le.ScanCallback {

        private final ScanActivity scanActivity;

        ScanCallback(@NonNull ScanActivity scanActivity) {

            this.scanActivity = scanActivity;
        }

        @Override
        public void onScanResult(int callbackType, android.bluetooth.le.ScanResult result) {

            super.onScanResult(callbackType, result);
            this.scanActivity.runOnUiThread(
                    () -> this.scanActivity.onScanResult(new ScanResult(result))
            );
        }

        @Override
        public void onScanFailed(int errorCode) {

            super.onScanFailed(errorCode);
        }

        @Override
        public void onBatchScanResults(List<android.bluetooth.le.ScanResult> results) {

            super.onBatchScanResults(results);
        }
    }

    static class ScanResult {

        private final android.bluetooth.le.ScanResult scanResult;

        ScanResult(@NonNull android.bluetooth.le.ScanResult scanResult) {

            this.scanResult = scanResult;
        }

        android.bluetooth.le.ScanResult content() {

            return this.scanResult;
        }

        BluetoothDevice device() {

            return this.scanResult.getDevice();
        }

        ScanRecord scanRecord() {

            return this.scanResult.getScanRecord();
        }

        String name() {

            BluetoothDevice device = this.scanResult.getDevice();
            String name = null;

            if (null != device) {
                name = device.getName();
                if ((null == name) || (0 == name.trim().length())) {
                    ScanRecord scanRecord = this.scanResult.getScanRecord();
                    if (null != scanRecord) {
                        name = this.scanResult.getScanRecord().getDeviceName();
                    }
                }
            }

            if (null == name) {
                name = PeripheralDevice.INVALID_NAME;
            }

            return name;
        }

        String address() {

            BluetoothDevice device = this.scanResult.getDevice();
            String address = null;

            if (null != device) {
                address = device.getAddress();
            }

            if (null == address) {
                address = PeripheralDevice.INVALID_ADDRESS;
            }

            return address;
        }

        int rssi() {

            return this.scanResult.getRssi();
        }
    }

    private static class PermissionRequestEventListener implements DialogInterface.OnDismissListener {

        private final ScanActivity scanActivity;
        private final String[] permission;
        private final int requestCode;

        PermissionRequestEventListener(@NonNull ScanActivity scanActivity, String[] permission) {

            this.scanActivity = scanActivity;
            this.permission = permission;
            this.requestCode = BluetoothRadio.REQUEST_BLUETOOTH_PERMISSION;
        }

        @Override
        public void onDismiss(DialogInterface dialog) {

            this.scanActivity.requestPermissions(this.permission, this.requestCode);
        }
    }

    static class NotSupportedException extends UnsupportedOperationException {

        NotSupportedException(AppCompatActivity context) {

            super(context.getResources().getString(R.string.exception_bluetooth_not_supported));
        }
    }

}
