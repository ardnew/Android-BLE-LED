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

package com.ardnew.blixel.bluetooth.attribute.characteristic;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import androidx.annotation.NonNull;

import java.util.Observable;
import java.util.Observer;
import java.util.UUID;

@SuppressWarnings("unused")
public abstract class Neopixel extends Observable {

    private final BluetoothGattService service;
    private final BluetoothGattCharacteristic characteristic;

    public Neopixel() {

        this.service = null;
        this.characteristic = null;
    }

    public Neopixel(@NonNull BluetoothGattService service) {

        this(service, (Observer[])null);
    }

    public Neopixel(@NonNull BluetoothGattService service, Observer ...observer) {

        this.service = service;
        this.characteristic = this.service.getCharacteristic(this.uuid());

        if (null != observer) {
            for (Observer o : observer) {
                this.addObserver(o);
            }
        }
    }

    private static boolean isValid(BluetoothGattService service, BluetoothGattCharacteristic characteristic) {

        return (null != service) && (null != characteristic);
    }

    public boolean isValid() {

        return Neopixel.isValid(this.service, this.characteristic);
    }

    public void transmit(BluetoothGatt gatt) {

        if (this.isValid()) {
            this.characteristic.setValue(this.pack());
            gatt.writeCharacteristic(this.characteristic);
        }
    }

    public void request(BluetoothGatt gatt) {

        if (this.isValid()) {
            gatt.readCharacteristic(this.characteristic);
        }
    }

    public void onRead(byte[] data, int status) {

        Observation observation = new Observation(Observation.Operation.Read, status);
        if (observation.isReadSuccess()) {
            this.unpack(data);
            this.setChanged();
            this.notifyObservers(observation);
        }
    }

    public void onWrite(byte[] data, int status) {

        Observation observation = new Observation(Observation.Operation.Write, status);
        if (observation.isWriteSuccess()) {
            this.unpack(data);
            this.setChanged();
            this.notifyObservers(observation);
        }
    }

    public abstract UUID uuid();
    public abstract int size();
    public abstract byte[] pack();
    public abstract void unpack(byte[] data);

    public static class Observation {

        public enum Operation {
            Read, Write
        }

        private final Operation operation;
        private final int status;

        public Observation(Operation operation, int status) {

            this.operation = operation;
            this.status = status;
        }

        public Operation operation() {

            return this.operation;
        }

        public boolean isReadSuccess() {

            return this.isSuccess() && Operation.Read == this.operation;
        }

        public boolean isWriteSuccess() {

            return this.isSuccess() && Operation.Write == this.operation;
        }

        public int status() {

            return this.status;
        }

        public boolean isSuccess() {

            return BluetoothGatt.GATT_SUCCESS == this.status;
        }
    }
}
