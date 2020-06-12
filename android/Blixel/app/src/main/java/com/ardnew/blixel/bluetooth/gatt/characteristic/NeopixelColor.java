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

package com.ardnew.blixel.bluetooth.gatt.characteristic;

import android.bluetooth.BluetoothGattService;
import android.os.Parcel;
import android.os.ParcelUuid;

import androidx.annotation.NonNull;

import java.util.UUID;

public class NeopixelColor extends Neopixel {

    public static final int MAX_ALPHA  = 0xFF;
    public static final int MAX_BRIGHT = 0xFF;

    public static final int DEFAULT_START  = 0;
    public static final int DEFAULT_LENGTH = 0;
    public static final int DEFAULT_COLOR  = 0xFFFFFFFF;
    public static final int DEFAULT_ALPHA  = NeopixelColor.MAX_ALPHA;
    public static final int DEFAULT_BRIGHT = NeopixelColor.MAX_BRIGHT;

    private int start;
    private int length;
    private int color;
    private int alpha;
    private int bright;

    public NeopixelColor(@NonNull BluetoothGattService service) {

        super(service);

        this.start  = NeopixelColor.DEFAULT_START;
        this.length = NeopixelColor.DEFAULT_LENGTH;
        this.color  = NeopixelColor.DEFAULT_COLOR;
        this.alpha  = NeopixelColor.DEFAULT_ALPHA;
        this.bright = NeopixelColor.DEFAULT_BRIGHT;
    }

    public NeopixelColor(@NonNull BluetoothGattService service, int start, int length, int color, int alpha, int bright) {

        super(service);

        this.start  = start;
        this.length = length;
        this.color  = color;
        this.alpha  = alpha;
        this.bright = bright;
    }

    public NeopixelColor(@NonNull BluetoothGattService service, byte[] data) {

        this(service);

        this.unpack(data);
    }

    public NeopixelColor(Parcel in) {

        super(in);

        this.start  = in.readInt();
        this.length = in.readInt();
        this.color  = in.readInt();
        this.alpha  = in.readInt();
        this.bright = in.readInt();
    }

    public static final Creator<NeopixelColor> CREATOR = new Creator<NeopixelColor>() {

        @Override
        public NeopixelColor createFromParcel(Parcel in) {

            return new NeopixelColor(in);
        }

        @Override
        public NeopixelColor[] newArray(int size) {

            return new NeopixelColor[size];
        }
    };

    @Override
    public int describeContents() {

        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        super.writeToParcel(dest, flags);

        dest.writeInt(this.start);
        dest.writeInt(this.length);
        dest.writeInt(this.color);
        dest.writeInt(this.alpha);
        dest.writeInt(this.bright);
    }

    public void setData(int start, int length, int color, int alpha, int bright) {

        this.start  = start;
        this.length = length;
        this.color  = color;
        this.alpha  = alpha;
        this.bright = bright;
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

    public int alpha() {

        return this.alpha;
    }

    public int bright() {

        return this.bright;
    }

    @Override
    public UUID uuid() {

        return ParcelUuid.fromString("3f1d00c2-632f-4e53-9a14-437dd54bcccb").getUuid();
    }

    @Override
    public int size() {

        return 10;
    }

    @Override
    public byte[] pack() {

        byte[] data = new byte[this.size()];

        data[0] = (byte)((this.start >> 8) & 0xFF);
        data[1] = (byte)(this.start & 0xFF);

        data[2] = (byte)((this.length >> 8) & 0xFF);
        data[3] = (byte)(this.length & 0xFF);

        data[4] = (byte)((this.color >> 24) & 0xFF);
        data[5] = (byte)((this.color >> 16) & 0xFF);
        data[6] = (byte)((this.color >> 8) & 0xFF);
        data[7] = (byte)(this.color & 0xFF);

        data[8] = (byte)(this.alpha & 0xFF);

        data[9] = (byte)(this.bright & 0xFF);

        return data;
    }

    @Override
    public void unpack(byte[] data) {

        if ((null == data) || (data.length < this.size())) {
            this.start  = NeopixelColor.DEFAULT_START;
            this.length = NeopixelColor.DEFAULT_LENGTH;
            this.color  = NeopixelColor.DEFAULT_COLOR;
            this.alpha  = NeopixelColor.DEFAULT_ALPHA;
            this.bright = NeopixelColor.DEFAULT_BRIGHT;
        } else {
            this.start  = ((int)data[0] << 8) | ((int)data[1] & 0xFF);
            this.length = ((int)data[2] << 8) | ((int)data[3] & 0xFF);
            this.color  = ((int)data[4] << 24) | ((int)data[5] << 16) | ((int)data[6] << 8) | ((int)data[7] & 0xFF);
            this.alpha  = NeopixelColor.DEFAULT_ALPHA;
            this.bright = NeopixelColor.DEFAULT_BRIGHT;
        }
    }
}
