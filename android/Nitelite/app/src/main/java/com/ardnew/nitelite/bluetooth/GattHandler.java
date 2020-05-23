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

package com.ardnew.nitelite.bluetooth;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.ParcelUuid;

import androidx.annotation.NonNull;

import com.ardnew.nitelite.MainActivity;

import java.util.UUID;

public class GattHandler extends BluetoothGattCallback {

    private static final UUID rgbLedServiceUuid = ParcelUuid.fromString("3f1d00c0-632f-4e53-9a14-437dd54bcccb").getUuid();

    private static final int INVALID_PIXEL_COUNT = 0;

    private final MainActivity mainActivity;

    private BluetoothGattService rgbLedService;
    private RgbLedCharPixel rgbLedCharPixel;
    private RgbLedCharStrip rgbLedCharStrip;

    public GattHandler(@NonNull MainActivity mainActivity) {

        super();

        this.mainActivity = mainActivity;

        this.rgbLedService = null;
        this.rgbLedCharPixel = null;
        this.rgbLedCharStrip = null;
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
            this.rgbLedService = gatt.getService(GattHandler.rgbLedServiceUuid);
            if (null != this.rgbLedService) {
                this.rgbLedCharPixel = new RgbLedCharPixel(this.rgbLedService);
                this.rgbLedCharStrip = new RgbLedCharStrip(this.rgbLedService);
                if (this.rgbLedCharPixel.isValid() && this.rgbLedCharStrip.isValid()) {
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

        if (characteristic.getUuid().equals(this.rgbLedCharPixel.uuid())) {
            this.rgbLedCharPixel.onRead(characteristic.getValue(), status);
        } else if (characteristic.getUuid().equals(this.rgbLedCharStrip.uuid())) {
            this.rgbLedCharStrip.onRead(characteristic.getValue(), status);
            this.mainActivity.setPixelCount(this.rgbLedCharStrip.count());
        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

        super.onCharacteristicWrite(gatt, characteristic, status);

        if (characteristic.getUuid().equals(this.rgbLedCharPixel.uuid())) {
            this.rgbLedCharPixel.onWrite(characteristic.getValue(), status);
        } else if (characteristic.getUuid().equals(this.rgbLedCharStrip.uuid())) {
            this.rgbLedCharStrip.onWrite(characteristic.getValue(), status);
            this.mainActivity.setPixelCount(this.rgbLedCharStrip.count());
        }
    }

    private boolean isRgbLedServiceReady()  {

        return (null != this.rgbLedCharPixel) && this.rgbLedCharPixel.isValid() &&
               (null != this.rgbLedCharStrip) && this.rgbLedCharStrip.isValid() ;
    }

    int pixelColor() {

        return null != this.rgbLedCharPixel ? this.rgbLedCharPixel.color() : 0;
    }

    int pixelCount() {

        return null != this.rgbLedCharStrip ? this.rgbLedCharStrip.count() : 0;
    }

    void requestRgbLedCharPixel(@NonNull BluetoothGatt gatt) {

        if (this.isRgbLedServiceReady()) {
            this.rgbLedCharPixel.request(gatt);
        }
    }

    void transmitRgbLedCharPixel(BluetoothGatt gatt, int start, int length, int color) {

        if (this.isRgbLedServiceReady()) {
            this.rgbLedCharPixel.update(start, length, color);
            this.rgbLedCharPixel.transmit(gatt);
        }
    }

    void requestRgbLedCharStrip(@NonNull BluetoothGatt gatt) {

        if (this.isRgbLedServiceReady()) {
            this.rgbLedCharStrip.request(gatt);
        }
    }

    void transmitRgbLedCharStrip(BluetoothGatt gatt, int count, int order, int type) {

        if (this.isRgbLedServiceReady()) {
            this.rgbLedCharStrip.update(count, order, type);
            this.rgbLedCharStrip.transmit(gatt);
        }
    }
}
