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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ardnew.nitelite.bluetooth.Device;
import com.ardnew.nitelite.bluetooth.ScanResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ScanAdapter extends RecyclerView.Adapter<ScanAdapter.ViewHolder> {

    private final Object deviceLock = new Object();

    private final ScanActivity scanActivity;
    private final List<Device> device;
    private final ConcurrentHashMap<String, Device> deviceMap; // keyed on device address

    ScanAdapter(@NonNull ScanActivity scanActivity) {

        this.scanActivity = scanActivity;
        this.device = new ArrayList<>();
        this.deviceMap = new ConcurrentHashMap<>();
    }

    @NonNull
    @Override
    public ScanAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.device_card, parent, false);

        return new ScanAdapter.ViewHolder(this.scanActivity, itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ScanAdapter.ViewHolder holder, int position) {

        Device device;

        try {
            synchronized (this.deviceLock) {
                device = this.device.get(position);
            }
        } catch (Exception ex) {
            device = new Device();
        }

        holder.addressLabel.setText(device.address());

        if ((null != device.name()) && (device.name().trim().length() > 0)) {
            holder.nameLabel.setText(Utility.format("%s", device.name()));
            holder.nameLabel.setVisibility(View.VISIBLE);
        } else {
            holder.nameLabel.setText("");
            holder.nameLabel.setVisibility(View.GONE);
        }

        holder.rssiLabel.setText(Utility.format("%d dBm", device.rssi()));

        holder.setIsExpanded(false, true);
        holder.setIsConnectable(device.isConnectable() && device.hasNiteliteServices(), true);

        holder.itemView.setOnClickListener(
                v -> {
                    holder.scanActivity.searchBar.clearFocus();
                    holder.setIsExpanded(!holder.isExpanded());
                }
        );

        Device finalDevice = device;
        holder.connectButton.setOnClickListener(
                v -> ScanAdapter.this.scanActivity.onConnectButtonClick(finalDevice)
        );

        String manufacturer = null;
        if (device.mfgData().size() > 0) {
            manufacturer = Nitelite.manufacturers().manufacturer(device.mfgData().keyAt(0));
        }
        if ((null != manufacturer) && (manufacturer.trim().length() > 0)) {
            holder.manufacturerLabel.setText(manufacturer);
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

    @SuppressWarnings("WeakerAccess")
    public int count() {

        return this.deviceMap.size();
    }

    @SuppressWarnings("WeakerAccess")
    public Device get(String address) {

        return this.deviceMap.get(address);
    }

    @SuppressWarnings("unused")
    public Device get(int index) {

        synchronized (this.deviceLock) {
            if ((index >= 0) && (index < this.device.size())) {
                return this.device.get(index);
            }
        }
        return null;
    }

    @SuppressWarnings("WeakerAccess")
    public void add(@NonNull ScanResult scanResult) {

        int rssi = scanResult.rssi();

        Device periph = this.get(scanResult.address());
        if (null == periph || rssi > periph.rssi()) {
            Device insert;
            synchronized (this.deviceLock) {

                int id = 0;
                while (id < this.device.size() && this.device.get(id).rssi() >= rssi) {
                    ++id;
                }

                insert = new Device(id, scanResult);

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

    @SuppressWarnings("WeakerAccess")
    public void clear() {

        this.deviceMap.clear();
        synchronized (this.deviceLock) {
            this.device.clear();
            this.notifyDataSetChanged();
        }
    }

    @SuppressWarnings("unused")
    protected void refresh() {

        this.notifyDataSetChanged();
    }


    @SuppressWarnings("unused")
    static class SortByRssi implements Comparator<Device> {

        @Override
        public int compare(Device p1, Device p2) {

            return p2.rssi() - p1.rssi();
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private static final int COLLAPSED_IMAGE_ROTATION = 0;
        private static final int EXPANDED_IMAGE_ROTATION = 180;
        private static final String TEXT_VIEW_NEWLINE = "\n";

        final ScanActivity scanActivity;

        @SuppressWarnings("unused")
        final View parent;
        final TextView addressLabel;
        final TextView rssiLabel;
        final ImageView expandImage;
        final TextView nameLabel;
        final TextView manufacturerLabel;
        final TextView characteristicsView;
        final Button connectButton;

        @SuppressWarnings("unused")
        boolean isConnectable;
        boolean isExpanded;

        ViewHolder(@NonNull ScanActivity scanActivity, @NonNull View itemView) {

            super(itemView);

            this.scanActivity = scanActivity;

            this.parent = itemView;
            this.addressLabel = itemView.findViewById(R.id.device_card_address_label);
            this.rssiLabel = itemView.findViewById(R.id.device_card_rssi_label);
            this.expandImage = itemView.findViewById(R.id.device_card_expand_image);
            this.nameLabel = itemView.findViewById(R.id.device_card_name_label);
            this.manufacturerLabel = itemView.findViewById(R.id.device_card_manufacturer_label);
            this.characteristicsView = itemView.findViewById(R.id.device_card_characteristics_view);
            this.connectButton = itemView.findViewById(R.id.device_card_connect_button);

            this.setCharacteristics(null);

            this.setIsExpanded(false, true);
            this.setIsConnectable(false, true);
        }

        boolean isExpanded() {

            return this.isExpanded;
        }

        void setIsExpanded(boolean isExpanded) {

            this.setIsExpanded(isExpanded, false);
        }

        private void setIsExpanded(boolean isExpanded, boolean override) {

            boolean expandedChanged = this.isExpanded != isExpanded;

            this.isExpanded = isExpanded;

            if (expandedChanged || override) {
                this.setCharacteristicsVisible(isExpanded);
                this.expandImage.setRotation(
                        isExpanded
                                ? ViewHolder.EXPANDED_IMAGE_ROTATION
                                : ViewHolder.COLLAPSED_IMAGE_ROTATION
                );
            }
        }

        @SuppressWarnings("unused")
        protected boolean isConnectable() {

            return this.isConnectable;
        }

        @SuppressWarnings("unused")
        protected void setIsConnectable(boolean isConnectable) {

            this.setIsConnectable(isConnectable, false);
        }

        private void setIsConnectable(boolean isConnectable, boolean override) {

            boolean connectableChanged = this.isConnectable != isConnectable;

            if (connectableChanged || override) {
                this.connectButton.setEnabled(isConnectable);
            }
        }

        @SuppressWarnings("SameParameterValue")
        private void setCharacteristics(List<String> characteristics) {

            this.characteristicsView.setText(Utility.join(characteristics, ViewHolder.TEXT_VIEW_NEWLINE));
        }

        private void setCharacteristicsVisible(boolean visible) {

            int currentVisibility = this.characteristicsView.getVisibility();
            CharSequence characteristicsText = this.characteristicsView.getText();
            boolean hasCharacteristicsText = (null != characteristicsText) && (characteristicsText.length() > 0);

            boolean visibilityChanged =
                    (visible && (currentVisibility != View.VISIBLE)) ||
                            (!visible && (currentVisibility == View.VISIBLE));

            if (visibilityChanged) {
                this.characteristicsView.setVisibility((visible && hasCharacteristicsText) ? View.VISIBLE : View.GONE);
            }
        }
    }
}
