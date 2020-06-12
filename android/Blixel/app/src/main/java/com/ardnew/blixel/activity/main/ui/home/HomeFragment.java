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

package com.ardnew.blixel.activity.main.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.ardnew.blixel.R;
import com.ardnew.blixel.Utility;
import com.ardnew.blixel.activity.main.MainViewModel;

public class HomeFragment extends Fragment {

    public static final String UNDEF_DEVICE_ATTR = "--";

    private MainViewModel mainViewModel;
    private boolean isConnectedToDevice = false;
    private boolean isInitialized = false;

    @SuppressWarnings("unused")
    public static HomeFragment newInstance() {

        return new HomeFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        if (!this.isInitialized) {

            final FragmentActivity activity = this.getActivity();
            this.mainViewModel = new ViewModelProvider((null != activity) ? activity : this).get(MainViewModel.class);

            final View.OnClickListener connectButtonClick = v -> this.mainViewModel.setIsScanning(true);
            final View.OnClickListener disconnectButtonClick = v -> this.mainViewModel.setDevice(null);

            final LinearLayout disconnectedLayout           = root.findViewById(R.id.home_fragment_connect_card_disconnected_layout);
            final LinearLayout connectedLayout              = root.findViewById(R.id.home_fragment_connect_card_connected_layout);
            final LinearLayout connectedDeviceAddressLayout = root.findViewById(R.id.home_fragment_connect_card_connected_address_layout);

            final Button   connectButton                    = root.findViewById(R.id.home_fragment_connect_button);
            final TextView connectedDeviceTextView          = root.findViewById(R.id.home_fragment_connect_card_connected_device_label);
            final TextView connectedAddressTextView         = root.findViewById(R.id.home_fragment_connect_card_connected_address_label);
            final TextView connectedChipsetTextView         = root.findViewById(R.id.home_fragment_connect_card_connected_chipset_label);
            final TextView connectedLengthTextView          = root.findViewById(R.id.home_fragment_connect_card_connected_length_label);

            connectButton.setOnClickListener(connectButtonClick);
            connectButton.setText(this.getString(R.string.home_fragment_connect_button_disconnected));

            connectedDeviceTextView.setText(HomeFragment.UNDEF_DEVICE_ATTR);
            connectedAddressTextView.setText(HomeFragment.UNDEF_DEVICE_ATTR);
            connectedChipsetTextView.setText(HomeFragment.UNDEF_DEVICE_ATTR);
            connectedLengthTextView.setText(HomeFragment.UNDEF_DEVICE_ATTR);

            connectedDeviceAddressLayout.setVisibility(View.GONE);
            disconnectedLayout.setVisibility(View.VISIBLE);
            connectedLayout.setVisibility(View.GONE);

            this.mainViewModel.connectedDevice().observe(this.getViewLifecycleOwner(), device -> {
                final boolean isConnectedToDevice = null != device;
                if (this.isConnectedToDevice != isConnectedToDevice) {
                    this.isConnectedToDevice = isConnectedToDevice;
                    if (this.isInitialized) {
                        if (this.isConnectedToDevice) {

                            String deviceName = device.displayName();
                            connectedDeviceTextView.setText(deviceName);

                            String deviceAddress = device.address();
                            connectedAddressTextView.setText(deviceAddress);

                            connectedDeviceAddressLayout.setVisibility(
                                deviceName.equalsIgnoreCase(deviceAddress) ? View.GONE : View.VISIBLE
                            );
                            disconnectedLayout.setVisibility(View.GONE);
                            connectedLayout.setVisibility(View.VISIBLE);

                            connectButton.setOnClickListener(disconnectButtonClick);
                            connectButton.setText(this.getString(R.string.home_fragment_connect_button_connected));

                        } else {

                            connectedDeviceTextView.setText(HomeFragment.UNDEF_DEVICE_ATTR);
                            connectedAddressTextView.setText(HomeFragment.UNDEF_DEVICE_ATTR);
                            connectedChipsetTextView.setText(HomeFragment.UNDEF_DEVICE_ATTR);
                            connectedLengthTextView.setText(HomeFragment.UNDEF_DEVICE_ATTR);

                            connectedDeviceAddressLayout.setVisibility(View.GONE);
                            disconnectedLayout.setVisibility(View.VISIBLE);
                            connectedLayout.setVisibility(View.GONE);

                            connectButton.setOnClickListener(connectButtonClick);
                            connectButton.setText(this.getString(R.string.home_fragment_connect_button_disconnected));
                        }
                    }
                }
            });

            this.mainViewModel.neopixelStrip().observe(this.getViewLifecycleOwner(), neopixelStrip -> {
                if (this.isConnectedToDevice && (null != neopixelStrip)) {

                    String type    = neopixelStrip.type().toString();
                    String order   = neopixelStrip.order().toString();
                    String chipset = Utility.format("Neopixel %s (%s)", type, order);
                    String length  = Utility.format("%d pixels", neopixelStrip.count());

                    connectedChipsetTextView.setText(chipset);
                    connectedLengthTextView.setText(length);

                } else {

                    connectedChipsetTextView.setText(HomeFragment.UNDEF_DEVICE_ATTR);
                    connectedLengthTextView.setText(HomeFragment.UNDEF_DEVICE_ATTR);

                }
            });

            this.isInitialized = true;
        }

        return root;
    }
}
