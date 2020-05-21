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
    private static final UUID rgbLedPixelCharUuid = ParcelUuid.fromString("3f1d00c1-632f-4e53-9a14-437dd54bcccb").getUuid();
    private static final UUID rgbLedCountCharUuid = ParcelUuid.fromString("3f1d00c2-632f-4e53-9a14-437dd54bcccb").getUuid();

    private static final int INVALID_PIXEL_COUNT = 0;

    private final MainActivity mainActivity;

    private BluetoothGattService rgbLedService;
    private BluetoothGattCharacteristic rgbLedPixelChar;
    private BluetoothGattCharacteristic rgbLedCountChar;

    private RgbLedPixel rgbLedPixel;
    private RgbLedCount rgbLedCount;

    public GattHandler(@NonNull MainActivity mainActivity) {

        super();

        this.mainActivity = mainActivity;

        this.rgbLedService = null;
        this.rgbLedPixelChar = null;
        this.rgbLedCountChar = null;

        this.rgbLedPixel = null;
        this.rgbLedCount = null;
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
                this.rgbLedPixelChar = this.rgbLedService.getCharacteristic(GattHandler.rgbLedPixelCharUuid);
                this.rgbLedCountChar = this.rgbLedService.getCharacteristic(GattHandler.rgbLedCountCharUuid);
                if ((null != this.rgbLedPixelChar) && (null != this.rgbLedCountChar)) {
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

        if (characteristic.getUuid().equals(this.rgbLedPixelChar.getUuid())) {
            this.rgbLedPixel = new RgbLedPixel(characteristic.getValue());
        } else if (characteristic.getUuid().equals(this.rgbLedCountChar.getUuid())) {
            this.rgbLedCount = new RgbLedCount(characteristic.getValue());
            this.mainActivity.setPixelCount(this.rgbLedCount.count());
        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

        super.onCharacteristicWrite(gatt, characteristic, status);

        if (characteristic.getUuid().equals(this.rgbLedPixelChar.getUuid())) {
            this.rgbLedPixel = new RgbLedPixel(characteristic.getValue());
        } else if (characteristic.getUuid().equals(this.rgbLedCountChar.getUuid())) {
            this.rgbLedCount = new RgbLedCount(characteristic.getValue());
            this.mainActivity.setPixelCount(this.rgbLedCount.count());
        }
    }

    int pixelColor() {

        return this.rgbLedPixel.color();
    }

    int pixelCount() {

        return this.rgbLedCount.count();
    }

    void requestRgbLedPixel(@NonNull BluetoothGatt gatt) {

        if (null != this.rgbLedPixelChar) {
            gatt.readCharacteristic(this.rgbLedPixelChar);
        }
    }

    void transmitRgbLedPixel(BluetoothGatt gatt, int start, int length, int color) {

        this.rgbLedPixelChar.setValue(new RgbLedPixel(start, length, color).pack());
        gatt.writeCharacteristic(this.rgbLedPixelChar);
    }

    void requestRgbLedCount(@NonNull BluetoothGatt gatt) {

        if (null != this.rgbLedCountChar) {
            gatt.readCharacteristic(this.rgbLedCountChar);
        }
    }

    void transmitRgbLedCount(BluetoothGatt gatt, int count) {

        this.rgbLedCountChar.setValue(new RgbLedCount(count).pack());
        gatt.writeCharacteristic(this.rgbLedCountChar);
    }

    private static class RgbLedPixel {

        int start, length, color;

        RgbLedPixel(int start, int length, int color) {

            this.start = start;
            this.length = length;
            this.color = color;
        }

        RgbLedPixel(byte[] data) {

            if ((null == data) || (data.length < 8)) {
                this.start = 0;
                this.length = 0;
                this.color = 0;
            } else {
                this.start = ((int)data[0] << 8) | (int)data[1];
                this.length = ((int)data[2] << 8) | (int)data[3];
                this.color = ((int)data[4] << 24) | ((int)data[5] << 16) | ((int)data[6] << 8) | (int)data[7];
            }
        }

        byte[] pack() {

            byte startInt16Hi = (byte)((this.start >> 8) & 0xFF);
            byte startInt16Lo = (byte)(this.start & 0xFF);

            byte lengthInt16Hi = (byte)((this.length >> 8) & 0xFF);
            byte lengthInt16Lo = (byte)(this.length & 0xFF);

            byte colorInt32Alpha = (byte)0xFF; // alpha channel not supported (typecast because byte is signed in java...)
            byte colorInt32Red = (byte)((this.color >> 16) & 0xFF);
            byte colorInt32Green = (byte)((this.color >> 8) & 0xFF);
            byte colorInt32Blue = (byte)(this.color & 0xFF);

            return new byte[]{
                startInt16Hi, startInt16Lo, lengthInt16Hi, lengthInt16Lo,
                colorInt32Alpha, colorInt32Red, colorInt32Green, colorInt32Blue,
            };
        }

        int start() {

            return this.start;
        }

        int length() {

            return this.length;
        }

        int color() {

            return this.color;
        }
    }

    private static class RgbLedCount {

        int count;

        RgbLedCount(int count) {

            this.count = count;
        }

        RgbLedCount(byte[] data) {

            this.count = ((int)data[1] << 8) | (int)data[0];
        }

        byte[] pack() {

            byte countInt16Hi = (byte)((this.count >> 8) & 0xFF);
            byte countInt16Lo = (byte)(this.count & 0xFF);

            return new byte[]{
                countInt16Lo, countInt16Hi,
            };
        }

        int count() {

            return this.count;
        }
    }
}
