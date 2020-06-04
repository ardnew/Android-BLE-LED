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

package com.ardnew.blixel.bluetooth.attribute.service;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.ParcelUuid;

import androidx.annotation.NonNull;

import com.ardnew.blixel.activity.main.MainActivity;
import com.ardnew.blixel.bluetooth.attribute.characteristic.Neopixel;
import com.ardnew.blixel.bluetooth.attribute.characteristic.NeopixelAnima;
import com.ardnew.blixel.bluetooth.attribute.characteristic.NeopixelColor;
import com.ardnew.blixel.bluetooth.attribute.characteristic.NeopixelStrip;

import java.util.Observable;
import java.util.UUID;

public class Callback extends BluetoothGattCallback {

    private static final UUID rgbLedServiceUuid = ParcelUuid.fromString("3f1d00c0-632f-4e53-9a14-437dd54bcccb").getUuid();

    private final MainActivity mainActivity;

    private BluetoothGattService neopixelService;
    private NeopixelColor neopixelColor;
    private NeopixelStrip neopixelStrip;
    private NeopixelAnima neopixelAnima;

    public Callback(@NonNull MainActivity mainActivity) {

        super();

        this.mainActivity = mainActivity;

        this.neopixelService = null;
        this.neopixelColor = null;
        this.neopixelStrip = null;
        this.neopixelAnima = null;
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

        Runnable runOnMain = null;

        if (newState == BluetoothProfile.STATE_CONNECTED) {
            runOnMain = () -> this.mainActivity.onGattDeviceConnected(gatt);
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            runOnMain = () -> this.mainActivity.onGattDeviceDisconnected(gatt);
        }

        if (null != runOnMain) {
            this.mainActivity.runOnUiThread(runOnMain);
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {

        Runnable runOnMain = null;

        if (status == BluetoothGatt.GATT_SUCCESS) {

            this.neopixelService = gatt.getService(Callback.rgbLedServiceUuid);
            if (null != this.neopixelService) {

                this.neopixelStrip = new NeopixelStrip(this.neopixelService,
                    (Observable o, Object arg) -> {
                        if ((o instanceof NeopixelStrip) && (arg instanceof Neopixel.Observation)) {
                            this.mainActivity.onRgbLedCharStripUpdate(gatt, (NeopixelStrip)o, (Neopixel.Observation)arg);
                        }
                    }
                );

                this.neopixelColor = new NeopixelColor(this.neopixelService,
                    (Observable o, Object arg) -> {
                        if ((o instanceof NeopixelColor) && (arg instanceof Neopixel.Observation)) {
                            this.mainActivity.onRgbLedCharColorUpdate(gatt, (NeopixelColor)o, (Neopixel.Observation)arg);
                        }
                    }
                );

                this.neopixelAnima = new NeopixelAnima(this.neopixelService,
                    (Observable o, Object arg) -> {
                        if ((o instanceof NeopixelAnima) && (arg instanceof Neopixel.Observation)) {
                            this.mainActivity.onRgbLedCharAnimaUpdate(gatt, (NeopixelAnima)o, (Neopixel.Observation)arg);
                        }
                    }
                );

                if (this.neopixelStrip.isValid()) {
                    runOnMain = () -> this.mainActivity.onGattServicesDiscovered(gatt);
                }
            }
        }

        if (null != runOnMain) {
            this.mainActivity.runOnUiThread(runOnMain);
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

        super.onCharacteristicRead(gatt, characteristic, status);

        if (null != characteristic) {
            UUID uuid = characteristic.getUuid();
            if (null != uuid) {
                if (uuid.equals(this.neopixelStrip.uuid())) {
                    this.neopixelStrip.onRead(characteristic.getValue(), status);
                } else if (uuid.equals(this.neopixelColor.uuid())) {
                    this.neopixelColor.onRead(characteristic.getValue(), status);
                } else if (uuid.equals(this.neopixelAnima.uuid())) {
                    this.neopixelAnima.onRead(characteristic.getValue(), status);
                }
            }
        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

        super.onCharacteristicWrite(gatt, characteristic, status);

        if (null != characteristic) {
            UUID uuid = characteristic.getUuid();
            if (null != uuid) {
                if (uuid.equals(this.neopixelStrip.uuid())) {
                    this.neopixelStrip.onWrite(characteristic.getValue(), status);
                } else if (uuid.equals(this.neopixelColor.uuid())) {
                    this.neopixelColor.onWrite(characteristic.getValue(), status);
                } else if (uuid.equals(this.neopixelAnima.uuid())) {
                    this.neopixelAnima.onWrite(characteristic.getValue(), status);
                }
            }
        }
    }

    public boolean isRgbLedServiceReady()  {

        return (null != this.neopixelStrip) && this.neopixelStrip.isValid() ;
    }

    public void requestRgbLedCharColor(@NonNull BluetoothGatt gatt) {

        if (this.isRgbLedServiceReady()) {
            this.neopixelColor.request(gatt);
        }
    }

    public void transmitRgbLedCharColor(BluetoothGatt gatt, int start, int length, int color) {

        if (this.isRgbLedServiceReady()) {
            this.neopixelColor.setData(start, length, color);
            this.neopixelColor.transmit(gatt);
        }
    }

    public void requestRgbLedCharStrip(@NonNull BluetoothGatt gatt) {

        if (this.isRgbLedServiceReady()) {
            this.neopixelStrip.request(gatt);
        }
    }

    public void transmitRgbLedCharStrip(BluetoothGatt gatt, int count, int order, int type) {

        if (this.isRgbLedServiceReady()) {
            this.neopixelStrip.setData(count, order, type);
            this.neopixelStrip.transmit(gatt);
        }
    }

    public void requestRgbLedCharAnima(@NonNull BluetoothGatt gatt) {

        if (this.isRgbLedServiceReady()) {
            this.neopixelAnima.request(gatt);
        }
    }

    public void transmitRgbLedCharAnima(BluetoothGatt gatt, int id, byte[] aniData) {

        if (this.isRgbLedServiceReady()) {
            this.neopixelAnima.setMode(id, aniData);
            this.neopixelAnima.transmit(gatt);
        }
    }

    public void transmitRgbLedCharAnimaRainbow(BluetoothGatt gatt, int speed) {

        if (this.isRgbLedServiceReady()) {
            this.neopixelAnima.setModeRainbow(speed);
            this.neopixelAnima.transmit(gatt);
        }
    }
}
