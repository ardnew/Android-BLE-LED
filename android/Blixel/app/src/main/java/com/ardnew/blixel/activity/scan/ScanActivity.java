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

package com.ardnew.blixel.activity.scan;

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

import com.ardnew.blixel.R;
import com.ardnew.blixel.Utility;
import com.ardnew.blixel.bluetooth.Connection;
import com.ardnew.blixel.bluetooth.Device;
import com.ardnew.blixel.bluetooth.Radio;
import com.ardnew.blixel.bluetooth.scan.Result;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

@SuppressWarnings("unused")
public class ScanActivity extends AppCompatActivity {

    // activity request code, used to identify origin in onActivityResult
    public static final int REQUEST_BLUETOOTH_ENABLE = 0xB00E;
    public static final int REQUEST_BLUETOOTH_PERMISSION = 0xB00B;

    public static final int REQUEST_DEVICE_SCAN = 0xD00D;
    public static final int SCAN_RESULT_OK = 0xB0;
    public static final int SCAN_RESULT_ERROR = 0xB1;
    public static final int SCAN_RESULT_CONNECT = 0xB2;

    private SearchView searchBar = null;
    private ScanAdapter scanAdapter = null;
    private FloatingActionButton refreshButton = null;
    private Snackbar scanningSnackBar = null;
    private Radio radio = null;

    public SearchView searchBar() {

        return this.searchBar;
    }

    public ScanAdapter scanAdapter() {

        return this.scanAdapter;
    }

    public FloatingActionButton refreshButton() {

        return this.refreshButton;
    }

    public Snackbar scanningSnackBar() {

        return this.scanningSnackBar;
    }

    public Radio radio() {

        return this.radio;
    }

    @Override
    protected void onStart() {

        super.onStart();

        if (null != this.radio) {
            this.radio.enableBluetooth();
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

        if (null != this.radio) {
            this.radio.setIsScanning(false);
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

        this.setContentView(R.layout.layout_scan);

        Toolbar toolBar = this.findViewById(R.id.scan_toolbar);
        this.setSupportActionBar(toolBar);

        ActionBar actionBar = this.getSupportActionBar();
        if (null != actionBar) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        this.searchBar = this.findViewById(R.id.scan_filter_search_view);
        this.searchBar.setOnClickListener(
                v -> ScanActivity.this.searchBar.onActionViewExpanded()
        );
        this.searchBar.setOnFocusChangeListener(
                (v, hasFocus) -> {
                    if (!hasFocus) {
                        Utility.dismissKeyboard(ScanActivity.this, v);
                    }
                }
        );

        this.scanAdapter = new ScanAdapter(this);

        RecyclerView scanView = this.findViewById(R.id.scan_recycler_view);
        scanView.setLayoutManager(new LinearLayoutManager(this));
        scanView.setAdapter(this.scanAdapter);

        this.refreshButton = this.findViewById(R.id.scan_refresh_fab);
        this.refreshButton.setOnClickListener(
                v -> ScanActivity.this.radio.setIsScanning(true)
        );

        this.scanningSnackBar = Snackbar.make(this.refreshButton, R.string.scan_begin_text, (int) Radio.SCAN_DURATION_MS);
        this.scanningSnackBar.setBackgroundTint(this.getColor(R.color.color_accent_interactive));
        this.scanningSnackBar.setTextColor(this.getColor(R.color.color_primary_dark));
        this.scanningSnackBar.setAction("Stop",
                v -> ScanActivity.this.radio.setIsScanning(false)
        );

        try {
            this.radio = new Radio(this);
        } catch (Exception ex) {
            this.displayFatalAlert("Failed to initialize", ex.getMessage());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        //noinspection SwitchStatementWithTooFewBranches
        switch (requestCode) {

            case ScanActivity.REQUEST_BLUETOOTH_ENABLE:
                if (!this.radio.onEnableBluetoothActivityResult(resultCode)) {
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

            case ScanActivity.REQUEST_BLUETOOTH_PERMISSION:
                if (!this.radio.onPermitBluetoothScanPermissionsResult(grantResults)) {
                    displayFatalAlert("Permission denied", this.getString(R.string.fatal_alert_bluetooth_scan_not_permitted));
                }
                break;
        }
    }

    public void onBluetoothRadioReady() {

        this.radio.setIsScanning(true);
    }

    public void onScanStart() {

        this.scanAdapter.clear();
        this.refreshButton.setVisibility(View.GONE);
        this.scanningSnackBar.show();
    }

    public void onScanStop() {

        this.refreshButton.setVisibility(View.VISIBLE);
        this.scanningSnackBar.dismiss();
    }

    public void onScanResult(@NonNull Result result) {

        this.scanAdapter.add(result);
    }

    public void onConnectButtonClick(@NonNull Device device) {

        this.radio.setIsScanning(false);
        Intent connectionIntent = Connection.Connect.init(this.getApplicationContext(), device.device());
        this.setResult(ScanActivity.SCAN_RESULT_CONNECT, connectionIntent);
        this.finishAndRemoveTask();
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

            this.activity.radio.setIsScanning(false);
            this.activity.finishAndRemoveTask();
        }
    }
}
