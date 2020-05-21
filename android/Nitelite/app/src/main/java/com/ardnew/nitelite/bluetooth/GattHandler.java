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
import android.bluetooth.BluetoothProfile;
import android.os.ParcelUuid;

import androidx.annotation.NonNull;

import com.ardnew.nitelite.MainActivity;

import java.util.UUID;

public class GattHandler extends BluetoothGattCallback {

    private static final UUID rgbLedServiceUuid = ParcelUuid.fromString("3f1d00c0-632f-4e53-9a14-437dd54bcccb").getUuid();
    private static final UUID rgbLedPixelCharUuid = ParcelUuid.fromString("3f1d00c1-632f-4e53-9a14-437dd54bcccb").getUuid();

    private final MainActivity mainActivity;

    public GattHandler(@NonNull MainActivity mainActivity) {

        super();

        this.mainActivity = mainActivity;
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
            runOnMain = () -> this.mainActivity.onGattServicesDiscovered(gatt);
        }
        if (null != runOnMain) {
            this.mainActivity.runOnUiThread(runOnMain);
        }
    }

    void transmitPixels(BluetoothGatt gatt, int start, int length, int color) {

        byte phi = (byte)((start >> 16) & 0xFF);
        byte plo = (byte)(start & 0xFF);

        byte lhi = (byte)((length >> 16) & 0xFF);
        byte llo = (byte)(length & 0xFF);

        BluetoothGattCharacteristic characteristic =
            gatt.getService(GattHandler.rgbLedServiceUuid).getCharacteristic(GattHandler.rgbLedPixelCharUuid);

        characteristic.setValue(
                new byte[]{
                        phi,
                        plo,
                        lhi,
                        llo,
                        (byte)0xFF,
                        (byte)((color >> 16) & 0xFF),
                        (byte)((color >> 8) & 0xFF),
                        (byte)((color) & 0xFF)
                }
        );
        gatt.writeCharacteristic(characteristic);
    }
}
