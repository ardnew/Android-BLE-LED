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
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.UUID;

abstract class RgbLedChar {

    private final BluetoothGattService service;
    private final BluetoothGattCharacteristic characteristic;

    RgbLedChar() {

        this.service = null;
        this.characteristic = null;
    }

    RgbLedChar(BluetoothGattService service) {

        this.service = service;
        this.characteristic = this.service.getCharacteristic(this.uuid());
    }

    private static boolean isValid(BluetoothGattService service, BluetoothGattCharacteristic characteristic) {

        return (null != service) && (null != characteristic);
    }

    boolean isValid() {

        return RgbLedChar.isValid(this.service, this.characteristic);
    }

    void transmit(BluetoothGatt gatt) {

        if (this.isValid()) {
            this.characteristic.setValue(this.pack());
            gatt.writeCharacteristic(this.characteristic);
        }
    }

    void request(BluetoothGatt gatt) {

        if (this.isValid()) {
            gatt.readCharacteristic(this.characteristic);
        }
    }

    void onRead(byte[] data, int status) {

        this.unpack(data);
    }

    void onWrite(byte[] data, int status) {

        this.unpack(data);
    }

    abstract UUID uuid();
    abstract byte[] pack();
    abstract void unpack(byte[] data);

}
