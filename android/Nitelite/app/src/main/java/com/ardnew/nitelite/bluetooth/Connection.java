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

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Process;

import androidx.annotation.NonNull;

import com.ardnew.nitelite.Utility;

import top.defaults.colorpicker.ColorObserver;

public class Connection extends Service implements ColorObserver {

    public class ServiceBinder extends Binder {

        public Connection getService() {

            return Connection.this;
        }
    }

    private Handler handler;
    private ServiceBinder serviceBinder;

    private BluetoothGatt bluetoothGatt;
    private GattHandler gattHandler;

    @Override
    public void onCreate() {

        super.onCreate();

        HandlerThread handlerThread = new HandlerThread(this.getClass().getName(), Process.THREAD_PRIORITY_BACKGROUND);
        handlerThread.start();

        this.handler = new Handler(handlerThread.getLooper());
        this.serviceBinder = new ServiceBinder();

        this.bluetoothGatt = null;
        this.gattHandler = null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        super.onStartCommand(intent, flags, startId);

        return START_REDELIVER_INTENT;
    }

    @SuppressWarnings("EmptyMethod")
    @Override
    public void onDestroy() {

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {

        return this.serviceBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {

        return false;
    }

    public void setGattHandler(@NonNull GattHandler gattHandler) {

        this.gattHandler = gattHandler;
    }

    public Notification addConnectionNotification(@NonNull BluetoothDevice bluetoothDevice) {

        Intent notificationIntent = new Intent(this, this.getClass());
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        return new Notification.Builder(this, NotificationChannel.DEFAULT_CHANNEL_ID)
            .setContentTitle("Nitelite")
            .setContentText(Utility.format("Connected to %s", bluetoothDevice.getAddress()))
            .setContentIntent(pendingIntent)
            .setTicker(Utility.format("Connected to %s", bluetoothDevice.getAddress()))
            .build();
    }

    public boolean connectToDevice(BluetoothDevice bluetoothDevice) {

        return this.handler.post(
                () -> this.bluetoothGatt = bluetoothDevice.connectGatt(this.getApplicationContext(), true, this.gattHandler)
        );
    }

    public boolean disconnectFromDevice() {

        return this.handler.post(
                () -> this.bluetoothGatt.disconnect()
        );
    }

    public boolean discoverServices() {

        return this.handler.post(
                () -> this.bluetoothGatt.discoverServices()
        );
    }

    @Override
    public void onColor(int color, boolean fromUser, boolean shouldPropagate) {

        this.handler.post(
                () -> this.gattHandler.transmitPixels(this.bluetoothGatt, 0, 30, color)
        );
    }
}
