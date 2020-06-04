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
import android.content.SharedPreferences;
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
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.ardnew.blixel.R;
import com.ardnew.blixel.Utility;
import com.ardnew.blixel.activity.main.ui.config.ConfigFragment;
import com.ardnew.blixel.activity.scan.ScanActivity;
import com.ardnew.blixel.bluetooth.Connection;
import com.ardnew.blixel.bluetooth.attribute.characteristic.Neopixel;
import com.ardnew.blixel.bluetooth.attribute.characteristic.NeopixelAnima;
import com.ardnew.blixel.bluetooth.attribute.characteristic.NeopixelColor;
import com.ardnew.blixel.bluetooth.attribute.characteristic.NeopixelStrip;
import com.ardnew.blixel.bluetooth.attribute.service.Callback;
import com.flask.colorpicker.OnColorChangedListener;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements OnColorChangedListener {

    private SharedPreferences sharedPreferences;

    private NavController navController;
    private AppBarConfiguration appBarConfiguration;

    private ConnectionServiceDelegate connectionServiceDelegate;

    private boolean isConnectedToDevice = false;

    private NeopixelStrip neopixelStrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.layout_main);

        Toolbar toolbar = this.findViewById(R.id.main_toolbar);
        this.setSupportActionBar(toolbar);

        DrawerLayout drawer = this.findViewById(R.id.main_drawer_layout);
        NavigationView navigationView = this.findViewById(R.id.main_nav_view);

        this.appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_color, R.id.nav_effects, R.id.nav_motion, R.id.nav_devices, R.id.nav_config)
                .setDrawerLayout(drawer)
                .build();

        Fragment navHostFragment = this.getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment instanceof NavHostFragment) {
            this.navController = ((NavHostFragment)navHostFragment).getNavController();
            NavigationUI.setupActionBarWithNavController(this, this.navController, this.appBarConfiguration);
            NavigationUI.setupWithNavController(navigationView, this.navController);
        }

        this.connectionServiceDelegate = ConnectionServiceDelegate.create(this);

        PreferenceManager.setDefaultValues(this, R.xml.device_config, false);
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
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

        if (null != this.navController) {
            return NavigationUI.navigateUp(this.navController, this.appBarConfiguration) || super.onSupportNavigateUp();
        }
        return super.onSupportNavigateUp();
    }

    @Override
    public void onColorChanged(int selectedColor) {

        if (this.isConnectedToDevice && (null != this.neopixelStrip)) {
            this.connectionServiceDelegate.transmitRgbLedCharColor(0, this.neopixelStrip.count(), selectedColor);
        }
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

        this.updatePreference(ConfigFragment.PREF_DEVICE_KEY, gatt.getDevice().getAddress());
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

        this.updatePreference(ConfigFragment.PREF_DEVICE_KEY, null);
    }

    @SuppressWarnings("unused")
    public void onGattServicesDiscovered(@NonNull BluetoothGatt gatt) {

        this.connectionServiceDelegate.requestRgbLedCharStrip();
    }

    public void onRgbLedCharStripUpdate(@NonNull BluetoothGatt gatt, NeopixelStrip strip, Neopixel.Observation observation) {

        if (observation.isReadSuccess()) {
            this.setNeopixelStrip(strip);
        }
    }

    public void onRgbLedCharColorUpdate(@NonNull BluetoothGatt gatt, NeopixelColor color, Neopixel.Observation observation) {


    }

    public void onRgbLedCharAnimaUpdate(@NonNull BluetoothGatt gatt, NeopixelAnima anima, Neopixel.Observation observation) {


    }

    private void setNeopixelStrip(@NonNull NeopixelStrip neopixelStrip) {

        this.neopixelStrip = neopixelStrip;

        this.updatePreference(ConfigFragment.PREF_STRIP_TYPE_KEY, neopixelStrip.type().value());
        this.updatePreference(ConfigFragment.PREF_COLOR_ORDER_KEY, neopixelStrip.order().value());
        this.updatePreference(ConfigFragment.PREF_STRIP_LENGTH_KEY, neopixelStrip.count());
    }

    public void updatePreference(@NonNull String key, Object value) {

        SharedPreferences.Editor editor = this.sharedPreferences.edit();
        switch (key) {
            case ConfigFragment.PREF_DEVICE_KEY:
                editor.putString(key, (String)value);
                break;
            case ConfigFragment.PREF_STRIP_TYPE_KEY:
            case ConfigFragment.PREF_COLOR_ORDER_KEY:
            case ConfigFragment.PREF_STRIP_LENGTH_KEY:
                editor.putString(key, Utility.format("%d", value));
                break;
        }
        editor.apply();
    }

    public static String readAddressFromPreferences(@NonNull SharedPreferences sharedPreferences) {

        return sharedPreferences.getString(ConfigFragment.PREF_DEVICE_KEY, null);
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

        private boolean isTryingToConnectToDevice;
        private boolean isTryingToDisconnectToDevice;

        static ConnectionServiceDelegate create(@NonNull MainActivity mainActivity) {

            return new ConnectionServiceDelegate(mainActivity);
        }

        ConnectionServiceDelegate(MainActivity mainActivity) {

            this.mainActivity = mainActivity;
            this.serviceIntent = Connection.Connect.init(this.mainActivity.getApplicationContext());

            this.connection = null;
            this.isServiceBound = false;

            this.connectedDevice = null;
            this.reconnectingDevice = null;
            this.lastConnectedDevice = null;

            this.isTryingToConnectToDevice = false;
            this.isTryingToDisconnectToDevice = false;

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

        void setIsTryingToConnectToDevice(boolean isTryingToConnectToDevice) {

            this.isTryingToConnectToDevice = isTryingToConnectToDevice;
            if (isTryingToConnectToDevice) {
                this.isTryingToDisconnectToDevice = false;
            }
        }

        void setIsTryingToDisconnectToDevice(boolean isTryingToDisconnectToDevice) {

            this.isTryingToDisconnectToDevice = isTryingToDisconnectToDevice;
            if (isTryingToDisconnectToDevice) {
                this.isTryingToConnectToDevice = false;
            }
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
        boolean requestRgbLedCharColor() {

            if (this.isServiceBound && (null != this.connectedDevice)) {
                return this.connection.requestRgbLedCharColor();
            }
            return false;
        }

        @SuppressWarnings("UnusedReturnValue")
        boolean transmitRgbLedCharColor(int start, int length, int color) {

            if (this.isServiceBound && (null != this.connectedDevice)) {
                return this.connection.transmitRgbLedCharColor(start, length, color);
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
