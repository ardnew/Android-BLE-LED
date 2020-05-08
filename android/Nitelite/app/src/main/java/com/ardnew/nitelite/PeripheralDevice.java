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
import android.os.ParcelUuid;

import androidx.annotation.NonNull;

import java.util.Map;

@SuppressWarnings("WeakerAccess")
public class PeripheralDevice {

    public static final int INVALID_ID = -1;
    public static final String INVALID_NAME = "";
    public static final String INVALID_ADDRESS = "";
    public static final int INVALID_RSSI = -256;

    private int id;
    private String name;
    private String address;
    private int rssi;

    private final BluetoothDevice device;
    private final boolean isConnectable;
    private final Map<ParcelUuid, byte[]> serviceData;

    PeripheralDevice() {

        this.id = PeripheralDevice.INVALID_ID;
        this.name = PeripheralDevice.INVALID_NAME;
        this.address = PeripheralDevice.INVALID_ADDRESS;
        this.rssi = PeripheralDevice.INVALID_RSSI;

        this.device = null;
        this.isConnectable = false;
        this.serviceData = null;
    }

    PeripheralDevice(int id, BluetoothRadio.DeviceScanResult scanResult) {

        this.id = id;
        this.name = scanResult.name();
        this.address = scanResult.address();
        this.rssi = scanResult.rssi();

        this.device = scanResult.device();
        this.isConnectable = scanResult.content().isConnectable();
        this.serviceData = scanResult.scanRecord().getServiceData();

    }

    @NonNull
    @Override
    public String toString() {

        StringBuilder stringBuilder = new StringBuilder(Utility.format("(%d)", this.id));
        if (this.address().length() > 0 && !this.address().equals(PeripheralDevice.INVALID_ADDRESS)) {
            stringBuilder.append(Utility.format(" %s", this.address()));
        }
        if (this.name().length() > 0 && !this.name().equals(PeripheralDevice.INVALID_NAME)) {
            stringBuilder.append(Utility.format(" \"%s\"", this.name()));
        }
        if (PeripheralDevice.INVALID_RSSI != this.rssi()) {
            stringBuilder.append(Utility.format(" [%d dBm]", this.rssi()));
        }
        return stringBuilder.toString();
    }

    @SuppressWarnings("unused")
    public void setId(int id) {
        this.id = id;
    }

    @SuppressWarnings("unused")
    public void setName(String name) {
        this.name = name;
    }

    @SuppressWarnings("unused")
    public void setAddress(String address) {
        this.address = address;
    }

    @SuppressWarnings("unused")
    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public int id() {
        return this.id;
    }

    public String name() {
        return this.name.trim();
    }

    public String address() {
        return this.address.trim();
    }

    public int rssi() {
        return this.rssi;
    }

    public BluetoothDevice device() {
        return this.device;
    }

    public boolean isConnectable() {
        return this.isConnectable;
    }

    public Map<ParcelUuid, byte[]> serviceData() {
        return this.serviceData;
    }

    public boolean equals(PeripheralDevice device) {

        return
                this.id() == device.id() &&
                        this.name().equalsIgnoreCase(device.name()) &&
                        this.address().equalsIgnoreCase(device.address()) &&
                        this.rssi() == device.rssi() &&
                        this.device().equals(device.device()) &&
                        this.isConnectable() == device.isConnectable();

    }

    @SuppressWarnings("unused")
    public boolean isValid() {

        return !this.equals(new PeripheralDevice());
    }
}
