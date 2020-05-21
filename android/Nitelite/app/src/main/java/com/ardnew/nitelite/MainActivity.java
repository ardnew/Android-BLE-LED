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

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.ardnew.nitelite.bluetooth.ConnectIntent;
import com.ardnew.nitelite.bluetooth.Connection;
import com.ardnew.nitelite.bluetooth.GattHandler;
import com.google.android.material.tabs.TabLayout;

import top.defaults.colorpicker.ColorPickerView;

public class MainActivity extends AppCompatActivity {

    private ConnectionServiceDelegate connectionServiceDelegate;

    private ScrollView scrollView;
    private TabPager tabPager;

    private boolean isConnectedToDevice = false;
    private boolean isAutoUpdateEnabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.activity_main);

        Toolbar toolBar = this.findViewById(R.id.main_toolbar);
        this.setSupportActionBar(toolBar);

        this.connectionServiceDelegate = ConnectionServiceDelegate.create(this);

        this.scrollView = this.findViewById(R.id.main_scroll_view);
        this.tabPager = this.findViewById(R.id.main_tab_pager);

        TabLayout tabLayout = this.findViewById(R.id.main_tab_layout);
        MainAdapter mainAdapter = new MainAdapter(this, tabLayout, tabLayout.getTabCount());
        tabPager.setAdapter(mainAdapter);
        //viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        this.connectionServiceDelegate.onDestroy();
    }

    @SuppressWarnings("EmptyMethod")
    @Override
    protected void onStart() {

        super.onStart();
    }

     @SuppressWarnings("EmptyMethod")
    @Override
    protected void onStop() {

        super.onStop();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        View focusedView = this.getCurrentFocus();
        if (null != focusedView) {
            Utility.dismissKeyboard(this, focusedView);
            focusedView.clearFocus();
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {

        super.onActivityResult(requestCode, resultCode, intent);

        //noinspection SwitchStatementWithTooFewBranches
        switch (requestCode) {

            case ScanActivity.REQUEST_DEVICE_SCAN:

                switch (resultCode) {

                    case ScanActivity.SCAN_RESULT_CONNECT:
                        if (null != intent) {
                            BluetoothDevice device = ConnectIntent.getDevice(intent);
                            if (null != device) {
                                this.connectionServiceDelegate.connectToDevice(device);
                            }
                        }
                        break;

                    case ScanActivity.SCAN_RESULT_OK:
                    case ScanActivity.SCAN_RESULT_ERROR:
                    default:
                        break;
                }
                break;
        }
    }

    public void onGattDeviceConnected(@NonNull BluetoothGatt gatt) {

        this.isConnectedToDevice = true;

        String connectionNotice =
                Utility.format(
                        this.getString(R.string.connected_toast_format),
                        this.connectionServiceDelegate.connectedDevice.getAddress()
                );
        Toast.makeText(this, connectionNotice, Toast.LENGTH_SHORT).show();

        ColorPickerView colorPickerView = this.findViewById(R.id.main_color_picker);
        if (null != colorPickerView) {
            colorPickerView.subscribe(this.connectionServiceDelegate.connection());
        }

        this.connectionServiceDelegate.discoverServices();
    }

    public void onGattDeviceDisconnected(@NonNull BluetoothGatt gatt) {

        this.isConnectedToDevice = false;

        String disconnectionNotice =
                Utility.format(
                        this.getString(R.string.disconnected_toast_format),
                        this.connectionServiceDelegate.lastDeviceAddress()
                );
        Toast.makeText(this, disconnectionNotice, Toast.LENGTH_SHORT).show();

        ColorPickerView colorPickerView = this.findViewById(R.id.main_color_picker);
        if (null != colorPickerView) {
            colorPickerView.unsubscribe(this.connectionServiceDelegate.connection());
        }
    }

    public void onGattServicesDiscovered(@NonNull BluetoothGatt gatt) {

        Log.d("services", gatt.getServices().toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.toolbar_main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem disconnectMenuItem = menu.findItem(R.id.disconnect_menu_item);
        disconnectMenuItem.setVisible(this.isConnectedToDevice);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.scan_menu_item:
                this.startActivityForResult(
                        new Intent(this, ScanActivity.class),
                        ScanActivity.REQUEST_DEVICE_SCAN
                );
                return true;

            case R.id.disconnect_menu_item:
                if (this.isConnectedToDevice) {
                    this.connectionServiceDelegate.disconnectFromDevice();
                }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public ScrollView scrollView() {

        return this.scrollView;
    }

    public TabPager tabPager() {

        return this.tabPager;
    }

    static class ConnectionServiceDelegate implements ServiceConnection {

        private final MainActivity mainActivity;
        private final Intent serviceIntent;

        private Connection connection;
        private boolean isServiceBound;
        private BluetoothDevice connectedDevice;
        private String lastDeviceAddress;

        static ConnectionServiceDelegate create(@NonNull MainActivity mainActivity) {

            return new ConnectionServiceDelegate(mainActivity);
        }

        ConnectionServiceDelegate(MainActivity mainActivity) {

            this.mainActivity = mainActivity;
            this.connection = null;
            this.isServiceBound = false;
            this.connectedDevice = null;
            this.lastDeviceAddress = null;

            this.serviceIntent = ConnectIntent.initConnection(this.mainActivity.getApplicationContext());
            this.mainActivity.startService(this.serviceIntent);
            this.mainActivity.bindService(this.serviceIntent, this, Context.BIND_IMPORTANT);
        }

        void onDestroy() {

            this.mainActivity.unbindService(this);
            this.mainActivity.stopService(this.serviceIntent);
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            Connection.ServiceBinder binder = (Connection.ServiceBinder)service;
            this.connection = binder.getService();
            this.connection.setGattHandler(new GattHandler(this.mainActivity));
            this.isServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            this.isServiceBound = false;
        }

        @SuppressWarnings("UnusedReturnValue")
        boolean connectToDevice(BluetoothDevice bluetoothDevice) {

            if (this.isServiceBound) {
                if ((null == this.connectedDevice) || this.disconnectFromDevice()) {
                    if (this.connection.connectToDevice(bluetoothDevice)) {
                        this.connectedDevice = bluetoothDevice;
                        this.lastDeviceAddress = bluetoothDevice.getAddress();
                        return true;
                    }
                }
            }
            return false;
        }

        boolean disconnectFromDevice() {

            if (this.isServiceBound && (null != this.connectedDevice)) {
                if (this.connection.disconnectFromDevice()) {
                    this.connectedDevice = null;
                    return true;
                }
            }
            return false;
        }

        @SuppressWarnings("UnusedReturnValue")
        boolean discoverServices() {

            if (this.isServiceBound && (null != this.connectedDevice)) {
                return this.connection.discoverServices();
            }
            return false;
        }

        Connection connection() {

            return this.connection;
        }

        String lastDeviceAddress() {

            return this.lastDeviceAddress;
        }
    }

}
