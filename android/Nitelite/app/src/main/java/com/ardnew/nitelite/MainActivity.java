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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    private ConnectionServiceConnection connectionServiceConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.activity_main);

        Toolbar toolBar = this.findViewById(R.id.main_toolbar);
        this.setSupportActionBar(toolBar);

        this.connectionServiceConnection = new ConnectionServiceConnection(this);
    }

    @Override
    protected void onStop() {

        super.onStop();

        this.connectionServiceConnection.disconnect();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {

        super.onActivityResult(requestCode, resultCode, intent);

        //noinspection SwitchStatementWithTooFewBranches
        switch (requestCode) {

            case ScanActivity.REQUEST_DEVICE_SCAN:

                switch (resultCode) {

                    case ScanActivity.SCAN_RESULT_OK:
                        Log.d("scan-activity", Utility.format("result = %d", resultCode));
                        break;

                    case ScanActivity.SCAN_RESULT_CONNECT:
                        if (null != intent) {
                            PeripheralDevice device = ConnectionService.ConnectIntent.deviceToConnect(intent);
                            if (null != device) {
                                Log.d("connect-to-device", device.address());
                                if (!this.connectionServiceConnection.connect(intent)) {
                                    Log.d("connect-to-device-success", device.address());
                                }
                            }
                        }
                        break;

                    case ScanActivity.SCAN_RESULT_ERROR:
                    default:
                        break;
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.toolbar_main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //noinspection SwitchStatementWithTooFewBranches
        switch (item.getItemId()) {

            case R.id.scan_menu_item:
                this.startActivityForResult(
                        new Intent(this, ScanActivity.class),
                        ScanActivity.REQUEST_DEVICE_SCAN
                );
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    static class ConnectionServiceConnection implements ServiceConnection {

        private final MainActivity mainActivity;
        private ConnectionService connectionService;
        private boolean connectionServiceBound;

        ConnectionServiceConnection(MainActivity mainActivity) {

            super();

            this.mainActivity = mainActivity;
            this.connectionService = null;
            this.connectionServiceBound = false;
        }

        public boolean connect(@NonNull Intent intent) {

            this.disconnect();

            ComponentName serviceName = this.mainActivity.startService(intent);
            if (null != serviceName) {
                return this.mainActivity.bindService(intent, this, Context.BIND_IMPORTANT);
            } else {
                return false;
            }
        }

        void disconnect() {

            if (this.connectionServiceBound) {
                this.connectionService.stopService(this);
            }
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            ConnectionService.ConnectionBinder binder = (ConnectionService.ConnectionBinder)service;
            this.connectionService = binder.getService();
            this.connectionServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            this.connectionService = null;
            this.connectionServiceBound = false;
        }
    }

}
