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

import android.bluetooth.BluetoothGattService;
import android.os.ParcelUuid;

import java.util.UUID;

public class RgbLedCharPixel extends RgbLedChar {

    private int start;
    private int length;
    private int color;

    RgbLedCharPixel(BluetoothGattService service) {

        super(service);

        this.start  = 0;
        this.length = 0;
        this.color  = 0;
    }

    void update(int start, int length, int color) {

        this.start  = start;
        this.length = length;
        this.color  = color;
    }

    int start() {

        return this.start;
    }

    int length() {

        return this.length;
    }

    int color() {

        return this.color;
    }

    @Override
    public UUID uuid() {
        return ParcelUuid.fromString("3f1d00c1-632f-4e53-9a14-437dd54bcccb").getUuid();
    }

    @Override
    public byte[] pack() {

        byte startInt16Hi    = (byte)((this.start >> 8) & 0xFF);
        byte startInt16Lo    = (byte)(this.start & 0xFF);

        byte lengthInt16Hi   = (byte)((this.length >> 8) & 0xFF);
        byte lengthInt16Lo   = (byte)(this.length & 0xFF);

        byte colorInt32Alpha = (byte)0xFF; // alpha channel not supported (typecast because byte is signed in java...)
        byte colorInt32Red   = (byte)((this.color >> 16) & 0xFF);
        byte colorInt32Green = (byte)((this.color >> 8) & 0xFF);
        byte colorInt32Blue  = (byte)(this.color & 0xFF);

        return new byte[]{
            startInt16Hi, startInt16Lo,
            lengthInt16Hi, lengthInt16Lo,
            colorInt32Alpha, colorInt32Red, colorInt32Green, colorInt32Blue,
        };
    }

    @Override
    public void unpack(byte[] data) {

        if ((null == data) || (data.length < 8)) {
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
