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

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

public class ScanActivity extends AppCompatActivity {

    public static final int REQUEST_DEVICE_SCAN = 0xD00D;
    public static final int SCAN_RESULT_OK = 0xB0;
    public static final int SCAN_RESULT_ERROR = 0xB1;

    SearchView searchBar = null;
    @SuppressWarnings("WeakerAccess")
    DeviceScanAdapter scanAdapter = null;
    @SuppressWarnings("WeakerAccess")
    FloatingActionButton refreshButton = null;
    @SuppressWarnings("WeakerAccess")
    Snackbar scanningSnackBar = null;
    @SuppressWarnings("WeakerAccess")
    BluetoothRadio bluetoothRadio = null;

    @Override
    protected void onStart() {

        super.onStart();

        if (null != this.bluetoothRadio) {
            this.bluetoothRadio.enableBluetooth();
        }
    }

    @SuppressWarnings("EmptyMethod")
    @Override
    protected void onResume() {

        super.onResume();
    }

    @SuppressWarnings("EmptyMethod")
    @Override
    protected void onPause() {

        super.onPause();
    }

    @Override
    protected void onStop() {

        super.onStop();

        if (null != this.bluetoothRadio) {
            this.bluetoothRadio.setIsScanning(false);
        }
    }

    @SuppressWarnings("EmptyMethod")
    @Override
    protected void onRestart() {

        super.onRestart();
    }

    @SuppressWarnings("EmptyMethod")
    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        this.setResult(ScanActivity.SCAN_RESULT_OK);

        this.setContentView(R.layout.activity_scan);

        Toolbar toolBar = this.findViewById(R.id.scan_toolbar);
        this.setSupportActionBar(toolBar);

        ActionBar actionBar = this.getSupportActionBar();
        if (null != actionBar) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        SearchBarEventListener searchBarEventListener = new SearchBarEventListener(this);
        this.searchBar = this.findViewById(R.id.scan_filter_search_view);
        this.searchBar.setOnClickListener(searchBarEventListener);
        this.searchBar.setOnFocusChangeListener(searchBarEventListener);

        this.scanAdapter = new DeviceScanAdapter(this);

        RecyclerView scanView = this.findViewById(R.id.scan_recycler_view);
        scanView.setLayoutManager(new LinearLayoutManager(this));
        scanView.setAdapter(this.scanAdapter);

        RefreshButtonEventListener refreshButtonEventListener = new RefreshButtonEventListener(this);
        this.refreshButton = this.findViewById(R.id.scan_refresh_fab);
        this.refreshButton.setOnClickListener(refreshButtonEventListener);

        ScanningSnackBarEventListener scanningSnackBarEventListener = new ScanningSnackBarEventListener(this);
        this.scanningSnackBar = Snackbar.make(this.refreshButton, R.string.scan_begin_text, (int) BluetoothRadio.SCAN_DURATION_MS);
        this.scanningSnackBar.setAction("Stop", scanningSnackBarEventListener);

        try {
            this.bluetoothRadio = new BluetoothRadio(this);
        } catch (Exception ex) {
            this.displayFatalAlert("Failed to initialize", ex.getMessage());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        //noinspection SwitchStatementWithTooFewBranches
        switch (requestCode) {

            case BluetoothRadio.REQUEST_BLUETOOTH_ENABLE:
                if (!this.bluetoothRadio.onEnableBluetoothActivityResult(resultCode)) {
                    displayFatalAlert("Failed to initialize", this.getString(R.string.fatal_alert_bluetooth_not_enabled));
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //noinspection SwitchStatementWithTooFewBranches
        switch (requestCode) {

            case BluetoothRadio.REQUEST_BLUETOOTH_PERMISSION:
                if (!this.bluetoothRadio.onPermitBluetoothScanPermissionsResult(grantResults)) {
                    displayFatalAlert("Permission denied", this.getString(R.string.fatal_alert_bluetooth_scan_not_permitted));
                }
                break;
        }
    }

    void onBluetoothRadioReady() {

        this.bluetoothRadio.setIsScanning(true);
    }

    void onScanStart() {

        this.scanAdapter.clear();
        this.refreshButton.setVisibility(View.GONE);
        this.scanningSnackBar.show();
    }

    void onScanStop() {

        this.refreshButton.setVisibility(View.VISIBLE);
        this.scanningSnackBar.dismiss();
    }

    void onScanResult(@NonNull BluetoothRadio.DeviceScanResult result) {

        this.scanAdapter.add(result);
    }

    private void displayFatalAlert(String title, String message) {

        this.displayFatalAlert(title, message, ScanActivity.SCAN_RESULT_ERROR);
    }

    @SuppressWarnings("SameParameterValue")
    private void displayFatalAlert(String title, String message, int result) {

        this.setResult(result);

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setNeutralButton(
                        this.getString(R.string.fatal_alert_button_text),
                        new FatalEventListener(this)
                )
                .create().show();
    }

    static class FatalEventListener implements DialogInterface.OnClickListener {

        private final ScanActivity activity;

        FatalEventListener(@NonNull ScanActivity activity) {

            this.activity = activity;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {

            this.activity.finishAndRemoveTask();
        }
    }

    static class SearchBarEventListener implements View.OnClickListener, View.OnFocusChangeListener {

        private final ScanActivity activity;

        SearchBarEventListener(@NonNull ScanActivity activity) {

            this.activity = activity;
        }

        @Override
        public void onClick(View view) {

            this.activity.searchBar.onActionViewExpanded();
        }

        @Override
        public void onFocusChange(View view, boolean hasFocus) {

            if (!hasFocus) {
                Utility.dismissKeyboard(this.activity, view);
            }
        }
    }

    static class RefreshButtonEventListener implements View.OnClickListener {

        private final ScanActivity activity;

        RefreshButtonEventListener(@NonNull ScanActivity activity) {

            this.activity = activity;
        }

        @Override
        public void onClick(View view) {

            this.activity.bluetoothRadio.setIsScanning(true);
        }
    }

    static class ScanningSnackBarEventListener implements View.OnClickListener {

        private final ScanActivity activity;

        ScanningSnackBarEventListener(@NonNull ScanActivity activity) {

            this.activity = activity;
        }

        @Override
        public void onClick(View view) {

            this.activity.bluetoothRadio.setIsScanning(false);
        }
    }

}
