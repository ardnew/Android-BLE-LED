//==============================================================================
//                                                                             =
// Blixel                                                                      =
// Copyright (c) 2020 ardnew [https://github.com/ardnew]                       =
//                                                                             =
//       Permission is hereby granted, free of charge, to any person           =
//       obtaining a copy of this software and associated                      =
//       documentation files (the "Software"), to deal in the                  =
//       Software without restriction, including without limitation            =
//       the rights to use, copy, modify, merge, publish, distribute,          =
//       sublicense, and/or sell copies of the Software, and to                =
//       permit persons to whom the Software is furnished to do so,            =
//       subject to the following conditions:                                  =
//                                                                             =
//       The above copyright notice and this permission notice shall           =
//       be included in all copies or substantial portions of the              =
//       Software.                                                             =
//                                                                             =
//       THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY             =
//       KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE            =
//       WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR               =
//       PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS            =
//       OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR              =
//       OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR            =
//       OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE             =
//       SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                =
//                                                                             =
//==============================================================================

package com.ardnew.blixel.bluetooth;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.ardnew.blixel.R;
import com.ardnew.blixel.activity.scan.ScanActivity;
import com.ardnew.blixel.bluetooth.scan.Callback;

public class Radio {

    public static final long SCAN_DURATION_MS = 10000;

    private boolean isReady;
    private boolean isScanning;
    private long scanDuration;

    private final ScanActivity scanActivity;
    private final Callback callback;
    private final Handler scanHandler;

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothScanner;

    public Radio(ScanActivity scanActivity) throws NotSupportedException {

        this.isReady = false;
        this.isScanning = false;
        this.scanDuration = Radio.SCAN_DURATION_MS;

        this.scanActivity = scanActivity;
        this.callback = new Callback(this.scanActivity);
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

    public void setIsScanning(boolean isScanning) {

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

        this.scanDuration = (scanDuration > 0) ? scanDuration : Radio.SCAN_DURATION_MS;
    }

    private void scanStart() {

        this.isScanning = true;

        this.scanActivity.runOnUiThread(this.scanActivity::onScanStart);
        this.bluetoothScanner.startScan(this.callback);
    }

    private void scanStop() {

        this.isScanning = false;

        this.bluetoothScanner.stopScan(this.callback);
        this.scanActivity.runOnUiThread(this.scanActivity::onScanStop);
    }

    public void enableBluetooth() {

        if ((null == this.bluetoothAdapter) || !this.bluetoothAdapter.isEnabled()) {
            this.scanActivity.startActivityForResult(
                    new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                    ScanActivity.REQUEST_BLUETOOTH_ENABLE
            );
        } else {
            this.onEnableBluetooth();
        }
    }

    public boolean onEnableBluetoothActivityResult(int resultCode) {
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

            PermissionDialogDismissal permissionDialogDismissal =
                    new PermissionDialogDismissal(this.scanActivity, new String[]{permission});

            new AlertDialog.Builder(this.scanActivity)
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.ok, null)
                    .setOnDismissListener(permissionDialogDismissal)
                    .create().show();

        } else {
            this.onPermitBluetoothScan();
        }
    }

    public boolean onPermitBluetoothScanPermissionsResult(int[] grantResults) {
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

    private static class PermissionDialogDismissal implements DialogInterface.OnDismissListener {

        private final ScanActivity scanActivity;
        private final String[] permission;
        private final int requestCode;

        PermissionDialogDismissal(@NonNull ScanActivity scanActivity, String[] permission) {

            this.scanActivity = scanActivity;
            this.permission = permission;
            this.requestCode = ScanActivity.REQUEST_BLUETOOTH_PERMISSION;
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
