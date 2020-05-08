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

import android.os.ParcelUuid;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DeviceScanAdapter extends RecyclerView.Adapter<DeviceScanAdapter.ViewHolder> {

    private final Object deviceLock = new Object();

    private final ScanActivity activity;
    private final List<PeripheralDevice> device;

    DeviceScanAdapter(@NonNull ScanActivity activity) {

        this.activity = activity;
        this.device = new ArrayList<>();
    }

    @NonNull
    @Override
    public DeviceScanAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.device_card, parent, false);

        return new DeviceScanAdapter.ViewHolder(this.activity, itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceScanAdapter.ViewHolder holder, int position) {

        PeripheralDevice device;

        try {
            synchronized (this.deviceLock) {
                device = this.device.get(position);
            }
        } catch (Exception ex) {
            device = new PeripheralDevice();
        }

        holder.nameLabel.setText(Utility.format("%s", device.name()));
        holder.addressLabel.setText(device.address());
        holder.rssiLabel.setText(Utility.format("%d dBm", device.rssi()));

        holder.setIsExpanded(false, true);
        holder.setIsConnectable(device.isConnectable(), true);

        ArrayList<String> services = new ArrayList<>();
        for (ParcelUuid uuid : device.serviceData().keySet()) {
            services.add(uuid.getUuid().toString());
        }
        holder.setCharacteristics(services);
    }

    @Override
    public int getItemCount() {

        return this.count();
    }

    @SuppressWarnings("WeakerAccess")
    public int count() {

        synchronized (this.deviceLock) {
            return this.device.size();
        }
    }

    @SuppressWarnings("WeakerAccess")
    public PeripheralDevice get(String address) {

        synchronized (this.deviceLock) {
            for (int i = 0; i < this.device.size(); ++i) {
                PeripheralDevice curr = this.device.get(i);
                if ((null != curr) && address.equals(curr.address())) {
                    return curr;
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unused")
    public PeripheralDevice get(int index) {

        synchronized (this.deviceLock) {
            if ((index >= 0) && (index < this.device.size())) {
                return this.device.get(index);
            }
        }
        return null;
    }

    @SuppressWarnings("WeakerAccess")
    public void add(@NonNull BluetoothRadio.DeviceScanResult scanResult) {

        int rssi = scanResult.rssi();

        PeripheralDevice periph = this.get(scanResult.address());
        if (null == periph || rssi > periph.rssi()) {
            synchronized (this.deviceLock) {

                int insertIndex = 0;
                while (insertIndex < this.device.size() && this.device.get(insertIndex).rssi() >= rssi) {
                    ++insertIndex;
                }

                PeripheralDevice insertPeriph = new PeripheralDevice(insertIndex, scanResult);

                if (null != periph) {
                    this.device.remove(periph.id());
                    this.device.add(insertIndex, insertPeriph);
                    this.notifyItemMoved(periph.id(), insertIndex);
                    this.notifyItemChanged(insertIndex);
                } else {
                    this.device.add(insertIndex, insertPeriph);
                    this.notifyItemInserted(insertIndex);
                }

                // update ID for all items that were shifted over
                for (int i = insertIndex + 1; i < this.device.size(); ++i) {
                    this.device.get(i).setId(i);
                }
            }
        }
    }

    @SuppressWarnings("WeakerAccess")
    public void clear() {

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
    static class PeripheralDeviceSortByRssi implements Comparator<PeripheralDevice> {

        @Override
        public int compare(PeripheralDevice p1, PeripheralDevice p2) {

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
        final TextView characteristicsView;
        final Button connectButton;

        boolean isExpanded;
        @SuppressWarnings("unused")
        boolean isConnectable;

        ViewHolder(@NonNull ScanActivity scanActivity, @NonNull View itemView) {

            super(itemView);

            this.scanActivity = scanActivity;

            this.parent = itemView;
            this.addressLabel = itemView.findViewById(R.id.device_card_address_label);
            this.rssiLabel = itemView.findViewById(R.id.device_card_rssi_label);
            this.expandImage = itemView.findViewById(R.id.device_card_expand_image);
            this.nameLabel = itemView.findViewById(R.id.device_card_name_label);
            this.characteristicsView = itemView.findViewById(R.id.device_card_characteristics_view);
            this.connectButton = itemView.findViewById(R.id.device_card_connect_button);

            this.setCharacteristics(null);

            this.setIsExpanded(false, true);
            this.setIsConnectable(false, true);

            this.itemView.setOnClickListener(new CardEventListener(this));
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

        static class CardEventListener implements View.OnClickListener {

            final ViewHolder viewHolder;

            CardEventListener(@NonNull ViewHolder viewHolder) {

                this.viewHolder = viewHolder;
            }

            @Override
            public void onClick(View view) {

                this.viewHolder.scanActivity.searchBar.clearFocus();
                this.viewHolder.setIsExpanded(!this.viewHolder.isExpanded());
            }
        }

    }
}
