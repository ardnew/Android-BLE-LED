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
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.ardnew.blixel.R;
import com.ardnew.blixel.activity.scan.ScanActivity;
import com.ardnew.blixel.bluetooth.scan.Result;

import java.util.List;

public class Scanner {

    public interface ScanListener {
        AppCompatActivity scanActivity();
        void onScanReady();
        void onScanStart();
        void onScanStop();
        void onScanResult(int callbackType, @NonNull Result result);
    }

    public static final long SCAN_DURATION_MS = 10000;

    private boolean isReady;
    private boolean isScanning;
    private long scanDuration;

    private final ScanListener scanListener;
    private final Handler scanHandler;

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothScanner;

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            Scanner.this.scanListener.scanActivity().runOnUiThread(
                () -> Scanner.this.scanListener.onScanResult(callbackType, new Result(result))
            );
        }
        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }
        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    public Scanner(ScanListener scanListener) throws NotSupportedException {

        this.isReady = false;
        this.isScanning = false;
        this.scanDuration = Scanner.SCAN_DURATION_MS;

        this.scanListener = scanListener;
        this.scanHandler = new Handler();

        if (this.scanListener.scanActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            this.bluetoothManager = (BluetoothManager)this.scanListener.scanActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        }

        if (null == this.bluetoothManager) {
            throw new NotSupportedException(this.scanListener.scanActivity());
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
            this.scanListener.onScanReady();
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

        this.scanDuration = (scanDuration > 0) ? scanDuration : Scanner.SCAN_DURATION_MS;
    }

    private void scanStart() {

        this.isScanning = true;

        this.scanListener.scanActivity().runOnUiThread(this.scanListener::onScanStart);
        this.bluetoothScanner.startScan(this.scanCallback);
    }

    private void scanStop() {

        this.isScanning = false;

        this.bluetoothScanner.stopScan(this.scanCallback);
        this.scanListener.scanActivity().runOnUiThread(this.scanListener::onScanStop);
    }

    public void enableBluetooth() {

        if ((null == this.bluetoothAdapter) || !this.bluetoothAdapter.isEnabled()) {
            this.scanListener.scanActivity().startActivityForResult(
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
                this.scanListener.scanActivity().getResources().getString(R.string.scan_permission_request)
        );
    }

    @SuppressWarnings("SameParameterValue")
    private void permitBluetoothScan(String permission, String message) {

        if (PackageManager.PERMISSION_GRANTED != this.scanListener.scanActivity().checkSelfPermission(permission)) {

            new AlertDialog.Builder(this.scanListener.scanActivity())
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.ok, null)
                    .setOnDismissListener(
                        dialog -> this.scanListener.scanActivity().requestPermissions(
                            new String[]{permission}, ScanActivity.REQUEST_BLUETOOTH_PERMISSION
                        ))
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

    static class NotSupportedException extends UnsupportedOperationException {

        NotSupportedException(AppCompatActivity context) {

            super(context.getResources().getString(R.string.exception_bluetooth_not_supported));
        }
    }
}
