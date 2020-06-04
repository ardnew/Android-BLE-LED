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

import android.bluetooth.BluetoothGattService;
import android.os.ParcelUuid;

import androidx.annotation.NonNull;

import java.util.Observer;
import java.util.UUID;

@SuppressWarnings("unused")
public class NeopixelColor extends Neopixel {

    private int start;
    private int length;
    private int color;

    public NeopixelColor(@NonNull BluetoothGattService service) {

        this(service, (Observer)null);
    }

    public NeopixelColor(@NonNull BluetoothGattService service, Observer...observer) {

        super(service, observer);

        this.start  = 0;
        this.length = 0;
        this.color  = 0;
    }

    public void setData(int start, int length, int color) {

        this.start  = start;
        this.length = length;
        this.color  = color;
    }

    public int start() {

        return this.start;
    }

    public int length() {

        return this.length;
    }

    public int color() {

        return this.color;
    }

    @Override
    public UUID uuid() {

        return ParcelUuid.fromString("3f1d00c2-632f-4e53-9a14-437dd54bcccb").getUuid();
    }

    @Override
    public int size() {

        return 8;
    }

    @Override
    public byte[] pack() {

        byte[] data = new byte[this.size()];

        data[0] = (byte)((this.start >> 8) & 0xFF);
        data[1] = (byte)(this.start & 0xFF);

        data[2] = (byte)((this.length >> 8) & 0xFF);
        data[3] = (byte)(this.length & 0xFF);

        data[4] = (byte)0xFF; // alpha channel not supported (typecast because byte is signed in java...)
        data[5] = (byte)((this.color >> 16) & 0xFF);
        data[6] = (byte)((this.color >> 8) & 0xFF);
        data[7] = (byte)(this.color & 0xFF);

        return data;
    }

    @Override
    public void unpack(byte[] data) {

        if ((null == data) || (data.length < this.size())) {
            this.start  = 0;
            this.length = 0;
            this.color  = 0;
        } else {
            this.start  = ((int)data[0] << 8) | (int)data[1];
            this.length = ((int)data[2] << 8) | (int)data[3];
            this.color  = ((int)data[4] << 24) | ((int)data[5] << 16) | ((int)data[6] << 8) | (int)data[7];
        }
    }
}
