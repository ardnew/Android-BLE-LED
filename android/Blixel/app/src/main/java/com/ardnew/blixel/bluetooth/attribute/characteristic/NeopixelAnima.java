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

public class NeopixelAnima extends Neopixel {

    public interface Animation {
        @SuppressWarnings("SameReturnValue")
        int size();
        byte[] pack();
        void unpack(byte[] data);
    }

    public enum Mode {

        NONE("None", null),        // = 0
        RAINBOW("Rainbow", new Rainbow());  // = 1

        private static final Mode DEFAULT = Mode.NONE;

        private final String    name;
        private final Animation animation;

        Mode(final String name, final Animation animation) {

            this.name      = name;
            this.animation = animation;
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

    private Mode mode;

    public NeopixelAnima(@NonNull BluetoothGattService service) {

        this(service, (Observer)null);
    }

    public NeopixelAnima(@NonNull BluetoothGattService service, Observer...observer) {

        super(service, observer);

        this.mode = Mode.DEFAULT;
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

    public void setModeRainbow(int speed) {

        this.setMode(Mode.RAINBOW.ordinal(), new Rainbow(speed).pack());
    }

    @SuppressWarnings("unused")
    public Mode mode() {

        return this.mode;
    }

    @Override
    public UUID uuid() {

        return ParcelUuid.fromString("3f1d00c3-632f-4e53-9a14-437dd54bcccb").getUuid();
    }

    @Override
    public int size() {

        Animation animation = this.mode.animation();
        if (null != animation) {
            return animation.size() + 2; // +2 for 16-bit command ID (enum Mode value)
        }
        return 2;
    }

    @Override
    public byte[] pack() {

        byte[] data = new byte[this.size()];

        data[0] = (byte)((this.mode.ordinal() >> 8) & 0xFF);
        data[1] = (byte)(this.mode.ordinal() & 0xFF);

        Animation animation = this.mode.animation();
        if ((null != animation) && (animation.size() > 0)) {
            byte[] aniData = animation.pack();
            if (animation.size() >= 0) {
                System.arraycopy(aniData, 0, data, 2, animation.size());
            }
        }

        return data;
    }

    @Override
    public void unpack(byte[] data) {

        if ((null == data) || (data.length < this.size())) {
            this.mode = Mode.DEFAULT;
        } else {
            this.mode = Mode.fromId(((int)data[0] << 8) | (int)data[1]);
            if (null != this.mode) {
                Animation animation = this.mode.animation();
                if (null != animation) {
                    byte[] aniData = new byte[animation.size()];
                    if (animation.size() >= 0) {
                        System.arraycopy(data, 2, aniData, 0, animation.size());
                        animation.unpack(aniData);
                    }
                }
            }
        }
    }

    public static class Rainbow implements Animation {

        static final int DEFAULT_SPEED = 10;

        private int speed;

        Rainbow() {

            this(Rainbow.DEFAULT_SPEED);
        }

        Rainbow(int speed) {

            this.speed = speed;
        }

        @Override
        public int size() {

            return 2;
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
                this.speed = Rainbow.DEFAULT_SPEED;
            } else {
                this.speed = ((int)data[0] << 8) | (int)data[1];
            }
        }
    }
}
