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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("unused")
public class NeopixelStrip extends Neopixel {

    @SuppressWarnings("PointlessBitwiseExpression")
    public enum ColorOrder {

        RGB("RGB",   (0<<6) | (0<<4) | (1<<2) | (2)),
        RBG("RBG",   (0<<6) | (0<<4) | (2<<2) | (1)),
        GRB("GRB",   (1<<6) | (1<<4) | (0<<2) | (2)),
        GBR("GBR",   (2<<6) | (2<<4) | (0<<2) | (1)),
        BRG("BRG",   (1<<6) | (1<<4) | (2<<2) | (0)),
        BGR("BGR",   (2<<6) | (2<<4) | (1<<2) | (0)),

        // Offset:                W        R        G        B
        WRGB("WRGB", (0<<6) | (1<<4) | (2<<2) | (3)),
        WRBG("WRBG", (0<<6) | (1<<4) | (3<<2) | (2)),
        WGRB("WGRB", (0<<6) | (2<<4) | (1<<2) | (3)),
        WGBR("WGBR", (0<<6) | (3<<4) | (1<<2) | (2)),
        WBRG("WBRG", (0<<6) | (2<<4) | (3<<2) | (1)),
        WBGR("WBGR", (0<<6) | (3<<4) | (2<<2) | (1)),

        RWGB("RWGB", (1<<6) | (0<<4) | (2<<2) | (3)),
        RWBG("RWBG", (1<<6) | (0<<4) | (3<<2) | (2)),
        RGWB("RGWB", (2<<6) | (0<<4) | (1<<2) | (3)),
        RGBW("RGBW", (3<<6) | (0<<4) | (1<<2) | (2)),
        RBWG("RBWG", (2<<6) | (0<<4) | (3<<2) | (1)),
        RBGW("RBGW", (3<<6) | (0<<4) | (2<<2) | (1)),

        GWRB("GWRB", (1<<6) | (2<<4) | (0<<2) | (3)),
        GWBR("GWBR", (1<<6) | (3<<4) | (0<<2) | (2)),
        GRWB("GRWB", (2<<6) | (1<<4) | (0<<2) | (3)),
        GRBW("GRBW", (3<<6) | (1<<4) | (0<<2) | (2)),
        GBWR("GBWR", (2<<6) | (3<<4) | (0<<2) | (1)),
        GBRW("GBRW", (3<<6) | (2<<4) | (0<<2) | (1)),

        BWRG("BWRG", (1<<6) | (2<<4) | (3<<2) | (0)),
        BWGR("BWGR", (1<<6) | (3<<4) | (2<<2) | (0)),
        BRWG("BRWG", (2<<6) | (1<<4) | (3<<2) | (0)),
        BRGW("BRGW", (3<<6) | (1<<4) | (2<<2) | (0)),
        BGWR("BGWR", (2<<6) | (3<<4) | (1<<2) | (0)),
        BGRW("BGRW", (3<<6) | (2<<4) | (1<<2) | (0));

        private static final ColorOrder DEFAULT = ColorOrder.GRB;

        private static Map<Integer, ColorOrder> valueMap;

        private final String name;
        private final int    value;

        static {
            ColorOrder.valueMap = new HashMap<>();
            for (ColorOrder order : ColorOrder.values()) {
                ColorOrder.valueMap.put(order.value, order);
            }
        }

        ColorOrder(final String name, final int value) {

            this.name  = name;
            this.value = value;
        }

        public static ColorOrder fromName(final String name) {

            return ColorOrder.valueOf(name);
        }

        public static ColorOrder fromValue(final int value) {

            return ColorOrder.valueMap.get(value);
        }

        public int value() {

            return this.value;
        }

        @NonNull
        @Override
        public String toString() {

            return this.name;
        }
    }

    public enum PixelType {

        kHz800("800 kHz", 0x0000),
        kHz400("400 kHz", 0x0100);

        private static final PixelType DEFAULT = PixelType.kHz800;

        private static Map<Integer, PixelType> valueMap;

        private final String name;
        private final int    value;

        static {

            PixelType.valueMap = new HashMap<>();
            for (PixelType type : PixelType.values()) {
                PixelType.valueMap.put(type.value, type);
            }
        }

        PixelType(final String name, final int value) {

            this.name  = name;
            this.value = value;
        }

        public static PixelType fromName(final String name) {

            return PixelType.valueOf(name);
        }

        public static PixelType fromValue(final int value) {

            return PixelType.valueMap.get(value);
        }

        public int value() {

            return this.value;
        }

        @NonNull
        @Override
        public String toString() {

            return this.name;
        }
    }

    private int count;
    private ColorOrder order;
    private PixelType type;

    public NeopixelStrip(@NonNull BluetoothGattService service) {

        super(service);

        this.count = 0;
        this.order = ColorOrder.DEFAULT;
        this.type  = PixelType.DEFAULT;
    }

    public NeopixelStrip(@NonNull BluetoothGattService service, int count, ColorOrder order, PixelType type) {

        super(service);

        this.count = count;
        this.order = order;
        this.type  = type;
    }

    public NeopixelStrip(@NonNull BluetoothGattService service, byte[] data) {

        this(service);

        this.unpack(data);
    }

    public NeopixelStrip(Parcel in) {

        super(in);

        this.count = in.readInt();
        this.order = ColorOrder.fromValue(in.readInt());
        this.type = PixelType.fromValue(in.readInt());
    }

    public static final Creator<NeopixelStrip> CREATOR = new Creator<NeopixelStrip>() {

        @Override
        public NeopixelStrip createFromParcel(Parcel in) {

            return new NeopixelStrip(in);
        }

        @Override
        public NeopixelStrip[] newArray(int size) {

            return new NeopixelStrip[size];
        }
    };

    @Override
    public int describeContents() {

        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        super.writeToParcel(dest, flags);

        dest.writeInt(this.count);
        dest.writeInt(this.order.value());
        dest.writeInt(this.type.value());
    }

    public void setData(int count, ColorOrder order, PixelType type) {

        this.count = count;
        this.order = order;
        this.type  = type;
    }

    public void setData(int count, int order, int type) {

        this.setData(count, ColorOrder.fromValue(order), PixelType.fromValue(type));
    }

    public int count() {

        return this.count;
    }

    public ColorOrder order() {

        return this.order;
    }

    public PixelType type() {

        return this.type;
    }

    @Override
    public UUID uuid() {

        return ParcelUuid.fromString("3f1d00c1-632f-4e53-9a14-437dd54bcccb").getUuid();
    }

    @Override
    public int size() {

        return 6;
    }

    @Override
    public byte[] pack() {

        byte[] data = new byte[this.size()];

        data[0] = (byte)((this.count >> 8) & 0xFF);
        data[1] = (byte)(this.count & 0xFF);

        data[2] = (byte)((this.order.value() >> 8) & 0xFF);
        data[3] = (byte)(this.order.value() & 0xFF);

        data[4] = (byte)((this.type.value() >> 8) & 0xFF);
        data[5] = (byte)(this.type.value() & 0xFF);

        return data;
    }

    @Override
    public void unpack(byte[] data) {

        if ((null == data) || (data.length < this.size())) {
            this.count = 0;
            this.order = ColorOrder.DEFAULT;
            this.type  = PixelType.DEFAULT;
        } else {
            this.count = ((int)data[0] << 8) | (int)data[1];
            this.order = ColorOrder.fromValue(((int)data[2] << 8) | ((int)data[3] & 0xFF));
            this.type  = PixelType.fromValue(((int)data[4] << 8) | ((int)data[5] & 0xFF));
        }
    }
}
