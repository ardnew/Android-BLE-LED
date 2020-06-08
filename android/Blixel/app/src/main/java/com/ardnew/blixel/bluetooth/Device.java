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
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.content.Context;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.util.SparseArray;

import androidx.annotation.NonNull;

import com.ardnew.blixel.Blixel;
import com.ardnew.blixel.Utility;
import com.ardnew.blixel.bluetooth.scan.Result;

import java.util.HashMap;
import java.util.List;

public class Device implements Parcelable {

    public static final int INVALID_ID = -1;
    public static final String INVALID_NAME = "";
    public static final String INVALID_ADDRESS = "";
    public static final int INVALID_RSSI = -256;
    public static final String INVALID_MFG = "";

    private int id;
    private final String name;
    private final String address;
    private final int rssi;

    private final BluetoothDevice bluetoothDevice;
    private final boolean isConnectable;
    private final boolean hasBlixelServices;
    private final String manufacturer;

    private boolean isConnected;

    public Device() {

        this.id                = Device.INVALID_ID;
        this.name              = Device.INVALID_NAME;
        this.address           = Device.INVALID_ADDRESS;
        this.rssi              = Device.INVALID_RSSI;

        this.bluetoothDevice   = null;
        this.isConnectable     = false;
        this.hasBlixelServices = false;
        this.manufacturer      = null;

        this.isConnected       = false;
    }

    public Device(int id, Result result) {

        this.id                = id;
        this.name              = result.name();
        this.address           = result.address();
        this.rssi              = result.rssi();

        this.bluetoothDevice   = result.device();
        this.isConnectable     = result.content().isConnectable();
        this.hasBlixelServices = Device.hasBlixelServices(result.scanRecord().getServiceUuids());
        this.manufacturer      = Device.parseManufacturer(result.scanRecord().getManufacturerSpecificData());

        this.isConnected       = false;
    }

    protected Device(Parcel in) {

        this.id                = in.readInt();
        this.name              = in.readString();
        this.address           = in.readString();
        this.rssi              = in.readInt();

        this.bluetoothDevice   = in.readParcelable(BluetoothDevice.class.getClassLoader());
        this.isConnectable     = in.readByte() != 0;
        this.hasBlixelServices = in.readByte() != 0;
        this.manufacturer      = in.readString();

        this.isConnected       = in.readByte() != 0;
    }

    public static final Creator<Device> CREATOR = new Creator<Device>() {

        @Override
        public Device createFromParcel(Parcel in) {

            return new Device(in);
        }

        @Override
        public Device[] newArray(int size) {

            return new Device[size];
        }
    };

    @Override
    public int describeContents() {

        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeInt(this.id);
        dest.writeString(this.name);
        dest.writeString(this.address);
        dest.writeInt(this.rssi);

        dest.writeParcelable(this.bluetoothDevice, flags);
        dest.writeByte((byte)(this.isConnectable ? ~0 : 0));
        dest.writeByte((byte)(this.hasBlixelServices ? ~0 : 0));
        dest.writeString(this.manufacturer);

        dest.writeByte((byte)(this.isConnected ? ~0 : 0));
    }

    @NonNull
    @Override
    public String toString() {

        StringBuilder stringBuilder = new StringBuilder(Utility.format("%d:", this.id));
        if (this.address().length() > 0 && !this.address().equals(Device.INVALID_ADDRESS)) {
            stringBuilder.append(Utility.format(" %s", this.address()));
        }
        if (this.name().length() > 0 && !this.name().equals(Device.INVALID_NAME)) {
            stringBuilder.append(Utility.format(" \"%s\"", this.name()));
        }
        if (this.manufacturer().length() > 0 && !this.manufacturer().equals(Device.INVALID_MFG)) {
            stringBuilder.append(Utility.format(" [%s]", this.manufacturer));
        }
        if (Device.INVALID_RSSI != this.rssi()) {
            stringBuilder.append(Utility.format(" (%d dBm)", this.rssi()));
        }
        return stringBuilder.toString();
    }

    public String shortDescription() {

        StringBuilder stringBuilder = new StringBuilder();
        if (this.name().length() > 0 && !this.name().equals(Device.INVALID_NAME)) {
            stringBuilder.append(Utility.format(" %s", this.name()));
        }
        if (this.manufacturer().length() > 0 && !this.manufacturer().equals(Device.INVALID_MFG)) {
            stringBuilder.append(Utility.format(" (%s)", this.manufacturer()));
        }
        if (this.address().length() > 0 && !this.address().equals(Device.INVALID_ADDRESS)) {
            stringBuilder.append(Utility.format(" [%s]", this.address()));
        }
        return stringBuilder.toString().trim();
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

    public BluetoothDevice bluetoothDevice() {

        return this.bluetoothDevice;
    }

    public boolean isConnectable() {

        return this.isConnectable;
    }

    public boolean hasBlixelServices() {

        return this.hasBlixelServices;
    }

    public String manufacturer() {

        return this.manufacturer;
    }

    public boolean isConnected() {

        return this.isConnected;
    }

    public void setIsConnected(boolean isConnected) {

        this.isConnected = isConnected;
    }

    public BluetoothGatt connect(@NonNull Context context, boolean autoConnect, @NonNull BluetoothGattCallback callback) {

        BluetoothDevice bluetoothDevice = this.bluetoothDevice();
        if (null != bluetoothDevice) {
            return bluetoothDevice.connectGatt(context, autoConnect, callback);
        }
        return null;
    }

    public static String parseManufacturer(SparseArray<byte[]> mfgData) {

        if ((null == mfgData) || (0 == mfgData.size())) {
            return Device.INVALID_MFG;
        }
        return Blixel.manufacturers().manufacturer(mfgData.keyAt(0));
    }

    public static boolean hasBlixelServices(List<ParcelUuid> serviceUuid) {

        HashMap<ParcelUuid, Boolean> uuidSeen = new HashMap<>();
        for (ParcelUuid uuid : Blixel.blixelServices().serviceMap().keySet()) {
            uuidSeen.put(uuid, false);
        }

        if ((null == serviceUuid) || (uuidSeen.size() > serviceUuid.size())) {
            return false;
        }

        for (ParcelUuid uuid : serviceUuid) {
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
