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

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public final class ConnectIntent {

    private static final String DEVICE_TO_CONNECT = "device-to-connect";

    public static Intent initConnection(@NonNull Context context) {

        return new Intent(context, Connection.class);
    }

    public static Intent connectToDevice(@NonNull Context context, @NonNull BluetoothDevice device) {

        return new Intent(context, Connection.class).putExtra(ConnectIntent.DEVICE_TO_CONNECT, device);
    }

    public static BluetoothDevice getDevice(@NonNull Intent intent) {

        Parcelable parcelable = intent.getParcelableExtra(ConnectIntent.DEVICE_TO_CONNECT);
        if (parcelable instanceof BluetoothDevice) {
            return (BluetoothDevice)parcelable;
        } else {
            return null;
        }
    }
}
