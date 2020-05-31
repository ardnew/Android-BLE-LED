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

package com.ardnew.blixel.activity.main;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
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
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.ardnew.blixel.R;
import com.ardnew.blixel.Utility;
import com.ardnew.blixel.activity.scan.ScanActivity;
import com.ardnew.blixel.bluetooth.Connection;
import com.ardnew.blixel.bluetooth.attribute.service.Callback;
import com.flask.colorpicker.OnColorChangedListener;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements OnColorChangedListener {

    private AppBarConfiguration appBarConfiguration;

    private ConnectionServiceDelegate connectionServiceDelegate;

    private boolean isConnectedToDevice = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.layout_main);

        Toolbar toolbar = this.findViewById(R.id.main_toolbar);
        this.setSupportActionBar(toolbar);

        DrawerLayout drawer = this.findViewById(R.id.main_drawer_layout);
        NavigationView navigationView = this.findViewById(R.id.main_nav_view);

        this.appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_devices, R.id.nav_config, R.id.nav_color, R.id.nav_effects)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, this.appBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        this.connectionServiceDelegate = ConnectionServiceDelegate.create(this);
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
                            BluetoothDevice device = Connection.Connect.device(intent);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.menu_toolbar_main, menu);

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
                        new Intent(this, ScanActivity.class), ScanActivity.REQUEST_DEVICE_SCAN
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

    @Override
    public boolean onSupportNavigateUp() {

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, this.appBarConfiguration) || super.onSupportNavigateUp();
    }

    @Override
    public void onColorChanged(int selectedColor) {

//        if (this.isConnectedToDevice) {
//            this.connectionServiceDelegate.transmitRgbLedCharPixel(0, this.pixelCount, selectedColor);
//        }
    }

    public void onGattDeviceConnected(@NonNull BluetoothGatt gatt) {

        this.isConnectedToDevice = true;

        this.connectionServiceDelegate.setConnectedDevice(gatt.getDevice());

        String connectionNotice =
                Utility.format(
                        this.getString(R.string.connected_toast_format),
                        gatt.getDevice().getAddress()
                );
        Toast.makeText(this, connectionNotice, Toast.LENGTH_SHORT).show();

        this.connectionServiceDelegate.discoverServices();
    }

    public void onGattDeviceDisconnected(@NonNull BluetoothGatt gatt) {

        String connectionNotice =
            Utility.format(
                    this.getString(R.string.disconnected_toast_format),
                    gatt.getDevice().getAddress()
            );
        Toast.makeText(this, connectionNotice, Toast.LENGTH_SHORT).show();

        this.isConnectedToDevice = false;

        if (this.connectionServiceDelegate.isReconnectingToDevice()) {
            BluetoothDevice device = this.connectionServiceDelegate.reconnectingDevice();
            this.connectionServiceDelegate.setConnectedDevice(null);
            this.connectionServiceDelegate.connectToDevice(device);
        } else {
            this.connectionServiceDelegate.setConnectedDevice(null);
        }
    }

    @SuppressWarnings("unused")
    public void onGattServicesDiscovered(@NonNull BluetoothGatt gatt) {

        this.connectionServiceDelegate.requestRgbLedCharStrip();
    }

    @SuppressWarnings("unused")
    static class ConnectionServiceDelegate implements ServiceConnection {

        private final MainActivity mainActivity;
        private final Intent serviceIntent;

        private Connection connection;
        private boolean isServiceBound;

        private BluetoothDevice connectedDevice;
        private BluetoothDevice reconnectingDevice;
        private BluetoothDevice lastConnectedDevice;

        static ConnectionServiceDelegate create(@NonNull MainActivity mainActivity) {

            return new ConnectionServiceDelegate(mainActivity);
        }

        ConnectionServiceDelegate(MainActivity mainActivity) {

            this.mainActivity = mainActivity;
            this.serviceIntent = Connection.Connect.init(this.mainActivity.getApplicationContext());

            this.connection = null;
            this.isServiceBound = false;

            this.connectedDevice = null;
            this.lastConnectedDevice = null;
            this.reconnectingDevice = null;

            this.mainActivity.startService(this.serviceIntent);
            this.mainActivity.bindService(this.serviceIntent, this, Context.BIND_IMPORTANT);
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            Connection.ServiceBinder binder = (Connection.ServiceBinder)service;
            this.connection = binder.getService();
            this.connection.setCallback(new Callback(this.mainActivity));
            this.isServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            this.connection = null;
            this.isServiceBound = false;
        }

        void onDestroy() {

            this.mainActivity.unbindService(this);
            this.mainActivity.stopService(this.serviceIntent);
        }

        BluetoothDevice connectedDevice() {

            return this.connectedDevice;
        }

        void setConnectedDevice(BluetoothDevice device) {

            this.connectedDevice = device;

            this.setLastConnectedDevice(device);
            this.setReconnectingDevice(null);
        }

        BluetoothDevice lastConnectedDevice() {

            return this.lastConnectedDevice;
        }

        private void setLastConnectedDevice(BluetoothDevice device) {

            this.lastConnectedDevice = device;
        }

        BluetoothDevice reconnectingDevice() {

            return this.reconnectingDevice;
        }

        private void setReconnectingDevice(BluetoothDevice device) {

            this.reconnectingDevice = device;
        }

        boolean isReconnectingToDevice() {

            return null != this.reconnectingDevice;
        }

        @SuppressWarnings("UnusedReturnValue")
        boolean connectToDevice(BluetoothDevice bluetoothDevice) {

            if (this.isServiceBound) {
                if (null != this.connectedDevice) {
                    this.setReconnectingDevice(bluetoothDevice);
                    return this.connection.disconnectFromDevice();
                } else {
                    return this.connection.connectToDevice(bluetoothDevice);
                }
            }
            return false;
        }

        @SuppressWarnings("UnusedReturnValue")
        boolean disconnectFromDevice() {

            if (this.isServiceBound && (null != this.connectedDevice)) {
                return this.connection.disconnectFromDevice();
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

        @SuppressWarnings("UnusedReturnValue")
        boolean requestRgbLedCharPixel() {

            if (this.isServiceBound && (null != this.connectedDevice)) {
                return this.connection.requestRgbLedCharPixel();
            }
            return false;
        }

        @SuppressWarnings("UnusedReturnValue")
        boolean transmitRgbLedCharPixel(int start, int length, int color) {

            if (this.isServiceBound && (null != this.connectedDevice)) {
                return this.connection.transmitRgbLedCharPixel(start, length, color);
            }
            return false;
        }

        @SuppressWarnings("UnusedReturnValue")
        boolean requestRgbLedCharStrip() {

            if (this.isServiceBound && (null != this.connectedDevice)) {
                return this.connection.requestRgbLedCharStrip();
            }
            return false;
        }

        @SuppressWarnings("UnusedReturnValue")
        boolean transmitRgbLedCharStrip(int count, int order, int type) {

            if (this.isServiceBound && (null != this.connectedDevice)) {
                return this.connection.transmitRgbLedCharStrip(count, order, type);
            }
            return false;
        }

        @SuppressWarnings("UnusedReturnValue")
        boolean requestRgbLedCharAnima() {

            if (this.isServiceBound && (null != this.connectedDevice)) {
                return this.connection.requestRgbLedCharAnima();
            }
            return false;
        }

        @SuppressWarnings("UnusedReturnValue")
        boolean transmitRgbLedCharAnima(int id, byte[] aniData) {

            if (this.isServiceBound && (null != this.connectedDevice)) {
                return this.connection.transmitRgbLedCharAnima(id, aniData);
            }
            return false;
        }

        @SuppressWarnings("UnusedReturnValue")
        boolean transmitRgbLedCharAnimaRainbow(int speed) {

            if (this.isServiceBound && (null != this.connectedDevice)) {
                return this.connection.transmitRgbLedCharAnimaRainbow(speed);
            }
            return false;
        }
    }
}
