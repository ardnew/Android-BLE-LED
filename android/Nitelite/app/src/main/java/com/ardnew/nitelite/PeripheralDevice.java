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
import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;

import androidx.annotation.NonNull;

@SuppressWarnings("WeakerAccess")
public class PeripheralDevice implements Parcelable {

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

    PeripheralDevice() {

        this.id = PeripheralDevice.INVALID_ID;
        this.name = PeripheralDevice.INVALID_NAME;
        this.address = PeripheralDevice.INVALID_ADDRESS;
        this.rssi = PeripheralDevice.INVALID_RSSI;

        this.device = null;
        this.isConnectable = false;
        this.mfgData = null;
    }

    PeripheralDevice(int id, BluetoothRadio.ScanResult scanResult) {

        this.id = id;
        this.name = scanResult.name();
        this.address = scanResult.address();
        this.rssi = scanResult.rssi();

        this.device = scanResult.device();
        this.isConnectable = scanResult.content().isConnectable();
        this.mfgData = scanResult.scanRecord().getManufacturerSpecificData();
    }

    protected PeripheralDevice(Parcel in) {

        this.id = in.readInt();
        this.name = in.readString();
        this.address = in.readString();
        this.rssi = in.readInt();

        this.device = in.readParcelable(BluetoothDevice.class.getClassLoader());
        this.isConnectable = in.readByte() != 0;
        this.mfgData = in.readSparseArray(SparseArray.class.getClassLoader());
    }

    public static final Creator<PeripheralDevice> CREATOR = new Creator<PeripheralDevice>() {

        @Override
        public PeripheralDevice createFromParcel(Parcel in) {
            return new PeripheralDevice(in);
        }

        @Override
        public PeripheralDevice[] newArray(int size) {
            return new PeripheralDevice[size];
        }
    };

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

        dest.writeParcelable(this.device, flags);
        dest.writeByte((byte)(this.isConnectable ? 1 : 0));
        dest.writeSparseArray(this.mfgData);
    }
}
