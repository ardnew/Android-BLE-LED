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

package com.ardnew.blixel.activity.scan;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ardnew.blixel.R;
import com.ardnew.blixel.Utility;
import com.ardnew.blixel.bluetooth.Device;
import com.ardnew.blixel.bluetooth.scan.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ScanAdapter extends RecyclerView.Adapter<ScanAdapter.ViewHolder> {

    private final List<Device> device;
    private final ConcurrentHashMap<String, Device> deviceMap; // keyed on device address
    private final ConnectClickListener connectClickListener;

    public interface ConnectClickListener {

        void onConnectClick(@NonNull Device device);
    }

    ScanAdapter(ConnectClickListener connectClickListener) {

        this.device = new ArrayList<>();
        this.deviceMap = new ConcurrentHashMap<>();
        this.connectClickListener = connectClickListener;
    }

    @NonNull
    @Override
    public ScanAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.view_device_scan, parent, false);

        return new ScanAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ScanAdapter.ViewHolder holder, int position) {

        Device d;
        try {
            synchronized (this) {
                d = this.device.get(position);
            }
        } catch (Exception ex) {
            d = new Device();
        }
        final Device device = d;

        holder.addressLabel.setText(device.address());

        if ((null != device.name()) && (device.name().trim().length() > 0)) {
            holder.nameLabel.setText(Utility.format("%s", device.name()));
            holder.nameLabel.setVisibility(View.VISIBLE);
        } else {
            holder.nameLabel.setText("");
            holder.nameLabel.setVisibility(View.GONE);
        }

        holder.rssiLabel.setText(Utility.format("%d dBm", device.rssi()));

        holder.setIsConnectable(device.isConnectable() && device.hasBlixelServices());

        holder.connectButton.setOnClickListener(
                v -> this.connectClickListener.onConnectClick(device)
        );

        if ((null != device.manufacturer()) && (device.manufacturer().trim().length() > 0)) {
            holder.manufacturerLabel.setText(device.manufacturer());
            holder.manufacturerLabel.setVisibility(View.VISIBLE);
        } else {
            holder.manufacturerLabel.setText("");
            holder.manufacturerLabel.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {

        return this.count();
    }

    public int count() {

        return this.deviceMap.size();
    }

    public Device get(String address) {

        return this.deviceMap.get(address);
    }

    public void add(@NonNull Result result) {

        int rssi = result.rssi();

        Device periph = this.get(result.address());
        if (null == periph || rssi > periph.rssi()) {
            Device insert;
            synchronized (this) {

                int id = 0;
                while (id < this.device.size() && this.device.get(id).rssi() >= rssi) {
                    ++id;
                }

                insert = new Device(id, result);

                if (null != periph) {
                    this.device.remove(periph.id());
                    this.device.add(id, insert);
                    this.notifyItemMoved(periph.id(), id);
                    this.notifyItemChanged(id);
                } else {
                    this.device.add(id, insert);
                    this.notifyItemInserted(id);
                }

                // update ID for all items that were shifted over
                for (int i = id + 1; i < this.device.size(); ++i) {
                    this.device.get(i).setId(i);
                }
            }
            this.deviceMap.put(insert.address(), insert);
        }
    }

    public void addAll(List<Result> result) {

        for (Result r : result) {
            this.add(r);
        }
    }

    public void clear() {

        this.deviceMap.clear();
        synchronized (this) {
            this.device.clear();
            this.notifyDataSetChanged();
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        final View parent;
        final TextView addressLabel;
        final TextView rssiLabel;
        final ImageView bluetoothImage;
        final TextView nameLabel;
        final TextView manufacturerLabel;
        final Button connectButton;

        ViewHolder(@NonNull View itemView) {

            super(itemView);

            this.parent = itemView;
            this.addressLabel = itemView.findViewById(R.id.device_card_address_label);
            this.rssiLabel = itemView.findViewById(R.id.device_card_rssi_label);
            this.bluetoothImage = itemView.findViewById(R.id.device_card_bluetooth_image);
            this.nameLabel = itemView.findViewById(R.id.device_card_name_label);
            this.manufacturerLabel = itemView.findViewById(R.id.device_card_manufacturer_label);
            this.connectButton = itemView.findViewById(R.id.device_card_connect_button);

            this.setIsConnectable(false);
        }

        private void setIsConnectable(boolean isConnectable) {

            this.connectButton.setEnabled(isConnectable);
        }
    }
}
