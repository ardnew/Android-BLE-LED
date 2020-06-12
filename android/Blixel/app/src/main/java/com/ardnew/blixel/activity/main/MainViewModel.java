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

import android.bluetooth.BluetoothGattService;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.ardnew.blixel.bluetooth.Connection;
import com.ardnew.blixel.bluetooth.Device;
import com.ardnew.blixel.bluetooth.gatt.GattCallback;
import com.ardnew.blixel.bluetooth.gatt.characteristic.NeopixelAnima;
import com.ardnew.blixel.bluetooth.gatt.characteristic.NeopixelColor;
import com.ardnew.blixel.bluetooth.gatt.characteristic.NeopixelStrip;

public class MainViewModel extends ViewModel implements ServiceConnection {

    public interface ForegroundRunner {
        void runInForeground(@NonNull Runnable action);
    }

    private static final boolean AUTO_CONNECT_DEFAULT = true;

    private static final String keyBooleanIsServiceBound  = "com.ardnew.blixel.activity.main.MainViewModel.isServiceBound";

    private static final String keyBooleanIsScanning      = "com.ardnew.blixel.activity.main.MainViewModel.isScanning";

    private static final String keyParcelConnection       = "com.ardnew.blixel.activity.main.MainViewModel.connection";
    private static final String keyBooleanAutoConnect     = "com.ardnew.blixel.activity.main.MainViewModel.autoConnect";
    private static final String keyParcelDevice           = "com.ardnew.blixel.activity.main.MainViewModel.device";
    private static final String keyParcelConnectedDevice  = "com.ardnew.blixel.activity.main.MainViewModel.connectedDevice";

    private static final String keyParcelNeopixelService  = "com.ardnew.blixel.activity.main.MainViewModel.neopixelService";
    private static final String keyParcelNeopixelColor    = "com.ardnew.blixel.activity.main.MainViewModel.neopixelColor";
    private static final String keyParcelNeopixelStrip    = "com.ardnew.blixel.activity.main.MainViewModel.neopixelStrip";
    private static final String keyParcelNeopixelAnima    = "com.ardnew.blixel.activity.main.MainViewModel.neopixelAnima";

    private static GattCallback gattCallback;
    private static ForegroundRunner foregroundRunner;

    private SavedStateHandle state;

    public MainViewModel(SavedStateHandle state) {

        this.state = state;

        if (null == MainViewModel.gattCallback) {
            MainViewModel.gattCallback = new GattCallback(this);
        }

        if (!this.state.contains(keyBooleanIsServiceBound)) {
            this.state.set(keyBooleanIsServiceBound, false);
        }

        if (!this.state.contains(keyBooleanIsScanning)) {
            this.state.set(keyBooleanIsScanning, false);
        }

        if (!this.state.contains(keyParcelConnection)) {
            this.state.set(keyParcelConnection, null);
        }
        if (!this.state.contains(keyBooleanAutoConnect)) {
            this.state.set(keyBooleanAutoConnect, MainViewModel.AUTO_CONNECT_DEFAULT);
        }
        if (!this.state.contains(keyParcelDevice)) {
            this.state.set(keyParcelDevice, null);
        }
        if (!this.state.contains(keyParcelConnectedDevice)) {
            this.state.set(keyParcelConnectedDevice, null);
        }

        if (!this.state.contains(keyParcelNeopixelService)) {
            this.state.set(keyParcelNeopixelService, null);
        }
        if (!this.state.contains(keyParcelNeopixelColor)) {
            this.state.set(keyParcelNeopixelColor, null);
        }
        if (!this.state.contains(keyParcelNeopixelStrip)) {
            this.state.set(keyParcelNeopixelStrip, null);
        }
        if (!this.state.contains(keyParcelNeopixelAnima)) {
            this.state.set(keyParcelNeopixelAnima, null);
        }
    }

    public static void setForegroundRunner(@NonNull ForegroundRunner foregroundRunner) {

        MainViewModel.foregroundRunner = foregroundRunner;
    }

    public void runInForeground(@NonNull Runnable action) {

        if (null != MainViewModel.foregroundRunner) {
            MainViewModel.foregroundRunner.runInForeground(action);
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {

        this.state.set(keyBooleanIsServiceBound, true);

        this.state.set(keyParcelConnection, ((Connection.ServiceBinder)service).getService());
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

        this.state.set(keyBooleanIsServiceBound, true);
    }

    @Override
    public void onBindingDied(ComponentName name) {

        this.state.set(keyBooleanIsServiceBound, true);
    }

    @Override
    public void onNullBinding(ComponentName name) {

        this.state.set(keyBooleanIsServiceBound, true);
    }


    public LiveData<Boolean> isScanning() {

        return this.state.getLiveData(keyBooleanIsScanning);
    }

    public boolean getIsScanning() {

        Boolean isScanning = this.state.get(keyBooleanIsScanning);
        if (null == isScanning) {
            isScanning = false;
        }
        return isScanning;
    }

    public void setIsScanning(boolean isScanning) {

        this.state.set(keyBooleanIsScanning, isScanning);
    }

    public LiveData<Connection> connection() {

        return this.state.getLiveData(keyParcelConnection);
    }

    public Connection getConnection() {

        return this.state.get(keyParcelConnection);
    }

    public LiveData<Boolean> autoConnect() {

        return this.state.getLiveData(keyBooleanAutoConnect);
    }

    public boolean getAutoConnect() {

        Boolean autoConnect = this.state.get(keyBooleanAutoConnect);
        if (null == autoConnect) {
            autoConnect = MainViewModel.AUTO_CONNECT_DEFAULT;
        }
        return autoConnect;
    }

    public void setAutoConnect(boolean autoConnect) {

        this.state.set(keyBooleanAutoConnect, autoConnect);
    }

    public LiveData<Device> device() {

        return this.state.getLiveData(keyParcelDevice);
    }

    public Device getDevice() {

        return this.state.get(keyParcelDevice);
    }

    public void setDevice(Device device) {

        this.state.set(keyParcelDevice, device);

        if (null != device) {
            MainViewModel.gattCallback.connect(device);
        } else {
            MainViewModel.gattCallback.disconnect(false);
        }
    }

    public LiveData<Device> connectedDevice() {

        return this.state.getLiveData(keyParcelConnectedDevice);
    }

    public Device getConnectedDevice() {

        return this.state.get(keyParcelConnectedDevice);
    }

    public void setConnectedDevice(Device device) {

        this.state.set(keyParcelConnectedDevice, device);
    }

    public LiveData<BluetoothGattService> neopixelService() {

        return this.state.getLiveData(keyParcelNeopixelService);
    }

    public BluetoothGattService getNeopixelService() {

        return this.state.get(keyParcelNeopixelService);
    }

    public void setNeopixelService(@NonNull BluetoothGattService neopixelService) {

        this.state.set(keyParcelNeopixelService, neopixelService);
    }

    public LiveData<NeopixelStrip> neopixelStrip() {

        return this.state.getLiveData(keyParcelNeopixelStrip);
    }

    public NeopixelStrip getNeopixelStrip() {

        return this.state.get(keyParcelNeopixelStrip);
    }

    public void setNeopixelStrip(@NonNull NeopixelStrip neopixelStrip) {

        this.state.set(keyParcelNeopixelStrip, neopixelStrip);
    }

    public LiveData<NeopixelColor> neopixelColor() {

        return this.state.getLiveData(keyParcelNeopixelColor);
    }

    public NeopixelColor getNeopixelColor() {

        return this.state.get(keyParcelNeopixelColor);
    }

    public void setNeopixelColor(@NonNull NeopixelColor neopixelColor) {

        this.state.set(keyParcelNeopixelColor, neopixelColor);
    }

    public LiveData<NeopixelAnima> neopixelAnima() {

        return this.state.getLiveData(keyParcelNeopixelAnima);
    }

    public NeopixelAnima getNeopixelAnima() {

        return this.state.get(keyParcelNeopixelAnima);
    }

    public void setNeopixelAnima(@NonNull NeopixelAnima neopixelAnima) {

        this.state.set(keyParcelNeopixelAnima, neopixelAnima);
    }

    public void updateNeopixelStrip(final int count, final int order, final int type) {

        final NeopixelStrip neopixelStrip = this.getNeopixelStrip();
        if ((null != neopixelStrip) && neopixelStrip.isValid()) {
            neopixelStrip.setData(count, order, type);
            MainViewModel.gattCallback.transmit(neopixelStrip);
        }
    }

    public void updateNeopixelColor(final int start, final int length, final int color, final int alpha, final int bright) {

        final NeopixelColor neopixelColor = this.getNeopixelColor();
        if ((null != neopixelColor) && neopixelColor.isValid()) {
            neopixelColor.setData(start, length, color, alpha, bright);
            MainViewModel.gattCallback.transmit(neopixelColor);
        }
    }

    public void updateNeopixelColor(final int length, final int color, final int alpha, final int bright) {

        final int start  = NeopixelColor.DEFAULT_START;

        this.updateNeopixelColor(start, length, color, alpha, bright);
    }

    public void updateNeopixelColor(final int color, final int alpha, final int bright) {

        int length = 0;

        // use strip length by default
        final NeopixelStrip neopixelStrip = this.getNeopixelStrip();
        if ((null != neopixelStrip) && neopixelStrip.isValid()) {
            length = neopixelStrip.count();
        }

        this.updateNeopixelColor(length, color, alpha, bright);
    }

    public void updateNeopixelColor(final int color) {

        int alpha  = NeopixelColor.DEFAULT_ALPHA;
        int bright = NeopixelColor.DEFAULT_BRIGHT;

        // use color alpha/bright by default
        final NeopixelColor neopixelColor = this.getNeopixelColor();
        if ((null != neopixelColor) && neopixelColor.isValid()) {
            alpha  = neopixelColor.alpha();
            bright = neopixelColor.bright();
        }

        this.updateNeopixelColor(color, alpha, bright);
    }

    public void updateNeopixelAnima(final int id, final byte[] aniData) {

        final NeopixelAnima neopixelAnima = this.getNeopixelAnima();
        if ((null != neopixelAnima) && neopixelAnima.isValid()) {
            neopixelAnima.setMode(id, aniData);
            MainViewModel.gattCallback.transmit(neopixelAnima);
        }
    }

    public void updateNeopixelAnimaWheel(int speed) {

        final NeopixelAnima neopixelAnima = this.getNeopixelAnima();
        if ((null != neopixelAnima) && neopixelAnima.isValid()) {
            neopixelAnima.setModeWheel(speed);
            MainViewModel.gattCallback.transmit(neopixelAnima);
        }
    }

    public void updateNeopixelAnimaChase(int speed, int length, int color1, int color2) {

        final NeopixelAnima neopixelAnima = this.getNeopixelAnima();
        if ((null != neopixelAnima) && neopixelAnima.isValid()) {
            neopixelAnima.setModeChase(speed, length, color1, color2);
            MainViewModel.gattCallback.transmit(neopixelAnima);
        }
    }

    public void updateNeopixelAnimaFade(int speed, int length, int color1, int color2) {

        final NeopixelAnima neopixelAnima = this.getNeopixelAnima();
        if ((null != neopixelAnima) && neopixelAnima.isValid()) {
            neopixelAnima.setModeFade(speed, length, color1, color2);
            MainViewModel.gattCallback.transmit(neopixelAnima);
        }
    }
}
