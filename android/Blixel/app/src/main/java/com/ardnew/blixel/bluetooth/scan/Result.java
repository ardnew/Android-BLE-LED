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

package com.ardnew.blixel.bluetooth.scan;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanRecord;

import androidx.annotation.NonNull;

import com.ardnew.blixel.bluetooth.Device;

public class Result {

    private final android.bluetooth.le.ScanResult scanResult;

    public Result(@NonNull android.bluetooth.le.ScanResult scanResult) {

        this.scanResult = scanResult;
    }

    public android.bluetooth.le.ScanResult content() {

        return this.scanResult;
    }

    public BluetoothDevice device() {

        return this.scanResult.getDevice();
    }

    public ScanRecord scanRecord() {

        return this.scanResult.getScanRecord();
    }

    public String name() {

        BluetoothDevice device = this.scanResult.getDevice();
        String name = null;

        if (null != device) {
            name = device.getName();
            if ((null == name) || (0 == name.trim().length())) {
                ScanRecord scanRecord = this.scanResult.getScanRecord();
                if (null != scanRecord) {
                    name = this.scanResult.getScanRecord().getDeviceName();
                }
            }
        }

        if (null == name) {
            name = Device.INVALID_NAME;
        }

        return name;
    }

    public String address() {

        BluetoothDevice device = this.scanResult.getDevice();
        String address = null;

        if (null != device) {
            address = device.getAddress();
        }

        if (null == address) {
            address = Device.INVALID_ADDRESS;
        }

        return address;
    }

    public int rssi() {

        return this.scanResult.getRssi();
    }
}
