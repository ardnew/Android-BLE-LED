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
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.UUID;

public class NeopixelAnima extends Neopixel {

    public abstract static class Animation implements Parcelable {
        abstract int size();
        abstract byte[] pack();
        abstract void unpack(byte[] data);
    }

    public enum Mode implements Parcelable {

        NONE("None", null),
        WHEEL("Wheel", new Wheel()), // = 1
        CHASE("Chase", new Chase()), // = 2
        SCAN("Scan", new Scan()),    // = 3
        FADE("Fade", new Fade());    // = 4

        private static final Mode DEFAULT = Mode.NONE;

        private final String name;
        private Animation animation;

        Mode(final String name, final Animation animation) {

            this.name      = name;
            this.animation = animation;
        }

        public static final Creator<Mode> CREATOR = new Creator<Mode>() {

            @Override
            public Mode createFromParcel(Parcel in) {

                Mode mode = Mode.fromId(in.readInt());
                if ((null != mode) && (null != mode.animation)) {
                    mode.animation = in.readParcelable(mode.animation.getClass().getClassLoader());
                }
                return mode;
            }

            @Override
            public Mode[] newArray(int size) {

                return new Mode[size];
            }
        };

        @Override
        public void writeToParcel(Parcel dest, int flags) {

            dest.writeInt(this.ordinal());
            dest.writeParcelable(this.animation, flags);
        }

        @Override
        public int describeContents() {

            return 0;
        }

        public static Mode fromId(final int id) {

            Mode[] mode = Mode.values();
            if ((id >= 0) && (id < mode.length)) {
                return mode[id];
            } else {
                return null;
            }
        }

        public Animation animation() {

            return this.animation;
        }

        @NonNull
        @Override
        public String toString() {

            return this.name;
        }
    }

    public static final boolean REVERSE_DEFAULT = true;
    public static final short DELAY_DEFAULT_MS = 5; // milliseconds

    private Mode mode;
    private boolean reverse;
    private short delay; // milliseconds

    public NeopixelAnima(@NonNull BluetoothGattService service) {

        super(service);

        this.mode    = Mode.DEFAULT;
        this.reverse = NeopixelAnima.REVERSE_DEFAULT;
        this.delay   = NeopixelAnima.DELAY_DEFAULT_MS;
    }

    public NeopixelAnima(@NonNull BluetoothGattService service, Mode mode, boolean reverse, int delay, byte[] aniData) {

        super(service);

        if (null != mode) {
            Animation animation = mode.animation();
            if (null != animation) {
                animation.unpack(aniData);
            }
        }
        this.setMode(mode);
        this.setReverse(reverse);
        this.setDelay(delay);
    }

    public NeopixelAnima(@NonNull BluetoothGattService service, byte[] data) {

        this(service);

        this.unpack(data);
    }

    public NeopixelAnima(Parcel in) {

        super(in);

        this.mode = in.readParcelable(Mode.class.getClassLoader());
        this.reverse = in.readByte() != 0;
        this.delay = (short)(in.readInt() & 0xFF);
    }

    public static final Creator<NeopixelAnima> CREATOR = new Creator<NeopixelAnima>() {

        @Override
        public NeopixelAnima createFromParcel(Parcel in) {

            return new NeopixelAnima(in);
        }

        @Override
        public NeopixelAnima[] newArray(int size) {

            return new NeopixelAnima[size];
        }
    };

    @Override
    public int describeContents() {

        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        super.writeToParcel(dest, flags);

        dest.writeParcelable(this.mode, flags);
        dest.writeByte((byte)(this.reverse ? ~0 : 0));
        dest.writeInt((short)(this.delay & 0xFF));
    }

    public void setMode(Mode mode) {

        this.mode = mode;
    }

    public void setMode(Mode mode, byte[] aniData) {

        if (null != mode) {
            Animation animation = mode.animation();
            if (null != animation) {
                animation.unpack(aniData);
            }
        }
        this.setMode(mode);
    }

    public void setMode(int id, byte[] aniData) {

        this.setMode(Mode.fromId(id), aniData);
    }

    public void setReverse(boolean reverse) {

        this.reverse = reverse;
    }

    public void setReverse(int reverse) {

        this.reverse = 0 != reverse;
    }

    public void setDelay(short delay) {

        this.delay = delay;
    }

    public void setDelay(int delay) {

        this.setDelay((short)(delay & 0xFF));
    }

    public void setModeNone() {

        this.setMode(Mode.NONE);
    }

    public void setModeWheel(int speed) {

        this.setMode(Mode.WHEEL, new Wheel(speed).pack());
    }

    public void setModeChase(int speed, int length, int color1, int color2) {

        this.setMode(Mode.CHASE, new Chase(speed, length, color1, color2).pack());
    }

    public void setModeScan(int speed) {

        this.setMode(Mode.SCAN, new Scan(speed).pack());
    }

    public void setModeFade(int speed, int length, int color1, int color2) {

        this.setMode(Mode.FADE, new Fade(speed, length, color1, color2).pack());
    }

    public Mode mode() {

        return this.mode;
    }

    public boolean reverse() {

        return this.reverse;
    }

    public int delay() {

        return this.delay;
    }

    public int fixedSize() {

        return 3; // size (in bytes) of the fixed region of the characteristic packet common to all
                  // animation modes.
                  // this includes 1 byte for mode + 1 byte for reverse + 1 byte for delay.
    }

    @Override
    public UUID uuid() {

        return ParcelUuid.fromString("3f1d00c3-632f-4e53-9a14-437dd54bcccb").getUuid();
    }

    @Override
    public int size() {

        Animation animation = this.mode.animation();
        if (null != animation) {
            return animation.size() + this.fixedSize();
        }
        return this.fixedSize();
    }

    @Override
    public byte[] pack() {

        byte[] data = new byte[this.size()];

        data[0] = (byte)(this.mode.ordinal() & 0xFF);
        data[1] = (byte)(this.reverse ? ~0 : 0);
        data[2] = (byte)(this.delay & 0xFF);

        Animation animation = this.mode.animation();
        if ((null != animation) && (animation.size() > 0)) {
            byte[] aniData = animation.pack();
            if (animation.size() >= 0) {
                System.arraycopy(aniData, 0, data, this.fixedSize(), animation.size());
            }
        }

        return data;
    }

    @Override
    public void unpack(byte[] data) {

        if ((null == data) || (data.length < this.size())) {
            this.mode = Mode.DEFAULT;
            this.reverse = NeopixelAnima.REVERSE_DEFAULT;
            this.delay = NeopixelAnima.DELAY_DEFAULT_MS;
        } else {
            this.mode = Mode.fromId(data[0]);
            this.reverse = 0 != data[1];
            this.delay = data[2];
            if (null != this.mode) {
                Animation animation = this.mode.animation();
                if (null != animation) {
                    byte[] aniData = new byte[animation.size()];
                    if (animation.size() >= 0) {
                        System.arraycopy(data, this.fixedSize(), aniData, 0, animation.size());
                        animation.unpack(aniData);
                    }
                }
            }
        }
    }

    public static class Wheel extends Animation {

        static final int DEFAULT_SPEED = 1;

        private int speed;

        Wheel() {

            this(Wheel.DEFAULT_SPEED);
        }

        Wheel(int speed) {

            this.speed = speed;
        }

        Wheel(Parcel in) {

            this.speed = in.readInt();
        }

        public static final Creator<Wheel> CREATOR = new Creator<Wheel>() {

            @Override
            public Wheel createFromParcel(Parcel in) {

                return new Wheel(in);
            }

            @Override
            public Wheel[] newArray(int size) {

                return new Wheel[size];
            }
        };

        @Override
        public int describeContents() {

            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {

            dest.writeInt(this.speed);
        }

        @Override
        public int size() {

            return 1; // size (in bytes) of the additional animation data specific to this mode.
                      // includes 1 byte for speed (8-bit).
        }

        @Override
        public byte[] pack() {

            byte[] data = new byte[this.size()];

            data[0] = (byte)(this.speed & 0xFF);

            return data;
        }

        @Override
        public void unpack(byte[] data) {

            if ((null == data) || (data.length < this.size())) {
                this.speed = Wheel.DEFAULT_SPEED;
            } else {
                this.speed = data[0];
            }
        }
    }

    public static class Chase extends Animation {

        static final int DEFAULT_SPEED  = 1;
        static final int DEFAULT_LENGTH = 3;
        static final int DEFAULT_COLOR1 = 0xFFFFFFFF;
        static final int DEFAULT_COLOR2 = 0x00000000;

        private int speed;
        private int length;
        private int color1;
        private int color2;

        Chase() {

            this(Chase.DEFAULT_SPEED, Chase.DEFAULT_LENGTH, Chase.DEFAULT_COLOR1, Chase.DEFAULT_COLOR2);
        }

        Chase(int speed, int length, int color1, int color2) {

            this.speed  = speed;
            this.length = length;
            this.color1 = color1;
            this.color2 = color2;
        }

        Chase(Parcel in) {

            this.speed = in.readInt();
            this.length = in.readInt();
            this.color1 = in.readInt();
            this.color2 = in.readInt();
        }

        public static final Creator<Chase> CREATOR = new Creator<Chase>() {

            @Override
            public Chase createFromParcel(Parcel in) {

                return new Chase(in);
            }

            @Override
            public Chase[] newArray(int size) {

                return new Chase[size];
            }
        };

        @Override
        public int describeContents() {

            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {

            dest.writeInt(this.speed);
            dest.writeInt(this.length);
            dest.writeInt(this.color1);
            dest.writeInt(this.color2);
        }

        @Override
        public int size() {

            return 10; // size (in bytes) of the additional animation data specific to this mode.
                       // includes 1 byte for speed (8-bit), 1 byte for length (8-bit),
                       // 8 bytes for color1/color2 (both 32-bit).
        }

        @Override
        public byte[] pack() {

            byte[] data = new byte[this.size()];

            data[0] = (byte)(this.speed & 0xFF);
            data[1] = (byte)(this.length & 0xFF);
            data[2] = (byte)((this.color1 >> 24) & 0xFF);
            data[3] = (byte)((this.color1 >> 16) & 0xFF);
            data[4] = (byte)((this.color1 >> 8) & 0xFF);
            data[5] = (byte)(this.color1 & 0xFF);
            data[6] = (byte)((this.color2 >> 24) & 0xFF);
            data[7] = (byte)((this.color2 >> 16) & 0xFF);
            data[8] = (byte)((this.color2 >> 8) & 0xFF);
            data[9] = (byte)(this.color2 & 0xFF);
            return data;
        }

        @Override
        public void unpack(byte[] data) {

            if ((null == data) || (data.length < this.size())) {
                this.speed = Chase.DEFAULT_SPEED;
                this.length = Chase.DEFAULT_LENGTH;
                this.color1 = Chase.DEFAULT_COLOR1;
                this.color2 = Chase.DEFAULT_COLOR2;
            } else {
                this.speed = data[0];
                this.length = data[1];
                this.color1 = ((int)data[2] << 24) | ((int)data[3] << 16) | ((int)data[4] << 8) | ((int)data[5] & 0xFF);
                this.color2 = ((int)data[6] << 24) | ((int)data[7] << 16) | ((int)data[8] << 8) | ((int)data[9] & 0xFF);
            }
        }
    }

    public static class Scan extends Animation {

        static final int DEFAULT_SPEED = 10;

        private int speed;

        Scan() {

            this(Scan.DEFAULT_SPEED);
        }

        Scan(int speed) {

            this.speed = speed;
        }

        Scan(Parcel in) {

            this.speed = in.readInt();
        }

        public static final Creator<Scan> CREATOR = new Creator<Scan>() {

            @Override
            public Scan createFromParcel(Parcel in) {

                return new Scan(in);
            }

            @Override
            public Scan[] newArray(int size) {

                return new Scan[size];
            }
        };

        @Override
        public int describeContents() {

            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {

            dest.writeInt(this.speed);
        }

        @Override
        public int size() {

            return 2; // size (in bytes) of the additional animation data specific to this mode.
                      // includes 2 bytes for speed (16-bit).
        }

        @Override
        public byte[] pack() {

            byte[] data = new byte[this.size()];

            data[0] = (byte)((this.speed >> 8) & 0xFF);
            data[1] = (byte)(this.speed & 0xFF);

            return data;
        }

        @Override
        public void unpack(byte[] data) {

            if ((null == data) || (data.length < this.size())) {
                this.speed = Scan.DEFAULT_SPEED;
            } else {
                this.speed = ((int)data[0] << 8) | (int)data[1];
            }
        }
    }


    public static class Fade extends Animation {

        static final int DEFAULT_SPEED  = 5;
        static final int DEFAULT_LENGTH = 0xFF;
        static final int DEFAULT_COLOR1 = 0xFFFFFFFF;
        static final int DEFAULT_COLOR2 = 0x00000000;

        private int speed;
        private int length;
        private int color1;
        private int color2;

        Fade() {

            this(Fade.DEFAULT_SPEED, Fade.DEFAULT_LENGTH, Fade.DEFAULT_COLOR1, Fade.DEFAULT_COLOR2);
        }

        Fade(int speed, int length, int color1, int color2) {

            this.speed  = speed;
            this.length = length;
            this.color1 = color1;
            this.color2 = color2;
        }

        Fade(Parcel in) {

            this.speed = in.readInt();
            this.length = in.readInt();
            this.color1 = in.readInt();
            this.color2 = in.readInt();
        }

        public static final Creator<Fade> CREATOR = new Creator<Fade>() {

            @Override
            public Fade createFromParcel(Parcel in) {

                return new Fade(in);
            }

            @Override
            public Fade[] newArray(int size) {

                return new Fade[size];
            }
        };

        @Override
        public int describeContents() {

            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {

            dest.writeInt(this.speed);
            dest.writeInt(this.length);
            dest.writeInt(this.color1);
            dest.writeInt(this.color2);
        }

        @Override
        public int size() {

            return 10; // size (in bytes) of the additional animation data specific to this mode.
                       // includes 1 byte for speed (8-bit), 1 byte for length (8-bit),
                       // 8 bytes for color1/color2 (both 32-bit).
        }

        @Override
        public byte[] pack() {

            byte[] data = new byte[this.size()];

            data[0] = (byte)(this.speed & 0xFF);
            data[1] = (byte)(this.length & 0xFF);
            data[2] = (byte)((this.color1 >> 24) & 0xFF);
            data[3] = (byte)((this.color1 >> 16) & 0xFF);
            data[4] = (byte)((this.color1 >> 8) & 0xFF);
            data[5] = (byte)(this.color1 & 0xFF);
            data[6] = (byte)((this.color2 >> 24) & 0xFF);
            data[7] = (byte)((this.color2 >> 16) & 0xFF);
            data[8] = (byte)((this.color2 >> 8) & 0xFF);
            data[9] = (byte)(this.color2 & 0xFF);
            return data;
        }

        @Override
        public void unpack(byte[] data) {

            if ((null == data) || (data.length < this.size())) {
                this.speed = Fade.DEFAULT_SPEED;
                this.length = Fade.DEFAULT_LENGTH;
                this.color1 = Fade.DEFAULT_COLOR1;
                this.color2 = Fade.DEFAULT_COLOR2;
            } else {
                this.speed = data[0];
                this.length = data[1];
                this.color1 = ((int)data[2] << 24) | ((int)data[3] << 16) | ((int)data[4] << 8) | ((int)data[5] & 0xFF);
                this.color2 = ((int)data[6] << 24) | ((int)data[7] << 16) | ((int)data[8] << 8) | ((int)data[9] & 0xFF);
            }
        }
    }
}
