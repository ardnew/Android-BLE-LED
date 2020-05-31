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

package com.ardnew.blixel.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.os.ParcelUuid;
import android.util.SparseArray;

import androidx.annotation.NonNull;

import com.ardnew.blixel.Blixel;
import com.ardnew.blixel.Utility;
import com.ardnew.blixel.bluetooth.scan.Result;

import java.util.HashMap;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public class Device {

    public static final int INVALID_ID = -1;
    public static final String INVALID_NAME = "";
    public static final String INVALID_ADDRESS = "";
    public static final int INVALID_RSSI = -256;

    private int id;
    private final String name;
    private final String address;
    private final int rssi;

    private final BluetoothDevice device;
    private final boolean isConnectable;
    private final SparseArray<byte[]> mfgData;
    private final List<ParcelUuid> serviceUuid;

    public Device() {

        this.id = Device.INVALID_ID;
        this.name = Device.INVALID_NAME;
        this.address = Device.INVALID_ADDRESS;
        this.rssi = Device.INVALID_RSSI;

        this.device = null;
        this.isConnectable = false;
        this.mfgData = null;
        this.serviceUuid = null;
    }

    public Device(int id, Result result) {

        this.id = id;
        this.name = result.name();
        this.address = result.address();
        this.rssi = result.rssi();

        this.device = result.device();
        this.isConnectable = result.content().isConnectable();
        this.mfgData = result.scanRecord().getManufacturerSpecificData();
        this.serviceUuid = result.scanRecord().getServiceUuids();
    }

    @NonNull
    @Override
    public String toString() {

        StringBuilder stringBuilder = new StringBuilder(Utility.format("(%d)", this.id));
        if (this.address().length() > 0 && !this.address().equals(Device.INVALID_ADDRESS)) {
            stringBuilder.append(Utility.format(" %s", this.address()));
        }
        if (this.name().length() > 0 && !this.name().equals(Device.INVALID_NAME)) {
            stringBuilder.append(Utility.format(" \"%s\"", this.name()));
        }
        if (Device.INVALID_RSSI != this.rssi()) {
            stringBuilder.append(Utility.format(" [%d dBm]", this.rssi()));
        }
        return stringBuilder.toString();
    }

    public void setId(int id) {

        this.id = id;
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

    public SparseArray<byte[]> mfgData() {

        return this.mfgData;
    }

    @SuppressWarnings("unused")
    public List<ParcelUuid> serviceUuid() {

        return this.serviceUuid;
    }

    public boolean equals(Device device) {

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

        return !this.equals(new Device());
    }

    public boolean hasBlixelServices() {

        if (null == this.serviceUuid) {
            return false;
        }

        HashMap<ParcelUuid, Boolean> uuidSeen = new HashMap<>();
        for (ParcelUuid uuid : Blixel.blixelServices().serviceMap().keySet()) {
            uuidSeen.put(uuid, false);
        }

        if (uuidSeen.size() > this.serviceUuid.size()) {
            return false;
        }

        for (ParcelUuid uuid : this.serviceUuid) {
            if (uuidSeen.containsKey(uuid)) {
                uuidSeen.put(uuid, true);
            }
        }

        for (boolean seen : uuidSeen.values()) {
            if (!seen) {
                return false;
            }
        }
        return true;
    }
}
