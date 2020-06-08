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

package com.ardnew.blixel.bluetooth;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Process;

import androidx.annotation.NonNull;

public class Connection extends Service implements Parcelable {

    public class ServiceBinder extends Binder {

        public Connection getService() {

            return Connection.this;
        }
    }

    // factory class for constructing Intent objects for BLE connections
    public static final class ServiceIntent {

        private static final String DEVICE_TO_CONNECT = "com.ardnew.blixel.bluetooth.Connection.ServiceIntent.device";

        public static Intent start(@NonNull Context context) {

            return new Intent(context, Connection.class);
        }

        public static Intent connect(@NonNull Context context, @NonNull Device device) {

            return new Intent(context, Connection.class).putExtra(ServiceIntent.DEVICE_TO_CONNECT, device);
        }

        public static Device deviceFor(@NonNull Intent intent) {

            Parcelable parcelable = intent.getParcelableExtra(ServiceIntent.DEVICE_TO_CONNECT);
            if (parcelable instanceof Device) {
                return (Device)parcelable;
            } else {
                return null;
            }
        }
    }

    private Handler handler;

    public Connection() {
    }

    protected Connection(Parcel in) {

        /* TBD: no stateful data to persist? */

        // it's assumed this class's onCreate() will still be called, and thus a thread will be
        // allocated for this instance
    }

    public static final Creator<Connection> CREATOR = new Creator<Connection>() {

        @Override
        public Connection createFromParcel(Parcel in) {

            return new Connection(in);
        }

        @Override
        public Connection[] newArray(int size) {

            return new Connection[size];
        }
    };

    @Override
    public int describeContents() {

        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        /* TBD: no stateful data to persist? */
    }

    @Override
    public void onCreate() {

        super.onCreate();

        HandlerThread handlerThread = new HandlerThread(this.getClass().getName(), Process.THREAD_PRIORITY_BACKGROUND);
        handlerThread.start();

        this.handler = new Handler(handlerThread.getLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        super.onStartCommand(intent, flags, startId);

        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {

        return new ServiceBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {

        return false;
    }

    public boolean run(@NonNull Runnable runnable) {

        return this.handler.post(runnable);
    }
}
