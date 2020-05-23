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

public class RgbLedCharStrip extends RgbLedChar {

    private int count;
    private int order;
    private int type;

    RgbLedCharStrip(BluetoothGattService service) {

        super(service);

        this.count = 0;
        this.order = 0;
        this.type  = 0;
    }

    void update(int count, int order, int type) {

        this.count = count;
        this.order = order;
        this.type  = type;
    }

    int count() {

        return this.count;
    }

    int order() {

        return this.order;
    }

    int type() {

        return this.type;
    }

    @Override
    public UUID uuid() {
        return ParcelUuid.fromString("3f1d00c2-632f-4e53-9a14-437dd54bcccb").getUuid();
    }

    @Override
    public byte[] pack() {

        byte countInt16Hi = (byte)((this.count >> 8) & 0xFF);
        byte countInt16Lo = (byte)(this.count & 0xFF);

        byte orderInt16Hi = (byte)((this.order >> 8) & 0xFF);
        byte orderInt16Lo = (byte)(this.order & 0xFF);

        byte typeInt16Hi  = (byte)((this.type >> 8) & 0xFF);
        byte typeInt16Lo  = (byte)(this.type & 0xFF);

        return new byte[]{
            countInt16Hi, countInt16Lo,
            orderInt16Hi, orderInt16Lo,
            typeInt16Hi, typeInt16Lo,
        };
    }

    @Override
    public void unpack(byte[] data) {

        if ((null == data) || (data.length < 6)) {
            this.count = 0;
            this.order = 0;
            this.type  = 0;
        } else {
            this.count = ((int)data[0] << 8) | (int)data[1];
            this.order = ((int)data[2] << 8) | (int)data[3];
            this.type  = ((int)data[4] << 8) | (int)data[5];
        }
    }
}
