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

package com.ardnew.nitelite;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.os.Process;
import android.widget.Toast;

import androidx.annotation.NonNull;

public class ConnectionService extends Service {

    private static final int INVALID_START_ID = -1;

    private final ConnectionBinder connectionBinder;

    private Looper looper;
    private ServiceHandler serviceHandler;
    private int startId;

    public ConnectionService() {

        super();

        this.connectionBinder = new ConnectionBinder();
        this.startId = ConnectionService.INVALID_START_ID;
    }

    @Override
    public void onCreate() {

        super.onCreate();

        HandlerThread handlerThread = new HandlerThread(this.getClass().getName(), Process.THREAD_PRIORITY_BACKGROUND);
        handlerThread.start();

        this.looper = handlerThread.getLooper();
        this.serviceHandler = new ServiceHandler(this, this.looper);
    }

    @Override
    public void onDestroy() {

        super.onDestroy();

        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        this.startId = startId;

        return START_REDELIVER_INTENT;
    }

    public void stopService(@NonNull MainActivity.ConnectionServiceConnection serviceConnection) {

        if (ConnectionService.INVALID_START_ID != this.startId) {
            this.stopSelf(this.startId);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {

        return this.connectionBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {

        return false;
    }

    class ConnectionBinder extends Binder {

        ConnectionService getService() {

            return ConnectionService.this;
        }
    }

    private static class ServiceHandler extends Handler {

        private final ConnectionService connectionService;

        ServiceHandler(ConnectionService connectionService, Looper looper) {

            super(looper);

            this.connectionService = connectionService;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {

            super.handleMessage(msg);

            try {
                switch (msg.arg1) {
                    default:
                        Thread.sleep(10000);
                        break;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    static final class ConnectIntent {

        static final String DEVICE_TO_CONNECT = "device-to-connect";
        //static final String INVOKED_BY_COMPONENT = "invoked-by-component";

        static Intent create(@NonNull Context context, @NonNull PeripheralDevice peripheralDevice) {

            return ConnectIntent.setDeviceToConnect(new Intent(context, ConnectionService.class), peripheralDevice);
        }

        static PeripheralDevice deviceToConnect(@NonNull Intent intent) {

            Parcelable parcelable = intent.getParcelableExtra(ConnectIntent.DEVICE_TO_CONNECT);
            if (parcelable instanceof PeripheralDevice) {
                return (PeripheralDevice)parcelable;
            } else {
                return null;
            }
        }

        static Intent setDeviceToConnect(@NonNull Intent intent, @NonNull PeripheralDevice peripheralDevice) {

            return intent.putExtra(ConnectIntent.DEVICE_TO_CONNECT, peripheralDevice);
        }
    }
}
