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

package com.ardnew.blixel.activity.main.ui.device;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.ardnew.blixel.R;
import com.ardnew.blixel.Utility;
import com.ardnew.blixel.activity.main.MainViewModel;

public class DeviceFragment extends PreferenceFragmentCompat {

    public static final String CAT_DEVICE_KEY        = "com.ardnew.blixel.preferences.category.device";
    public static final String PREF_DEVICE_KEY       = "com.ardnew.blixel.preferences.preference.device";
    public static final String PREF_AUTO_CONNECT_KEY = "com.ardnew.blixel.preferences.preference.device_auto_connect";
    public static final String CAT_STRIP_KEY         = "com.ardnew.blixel.preferences.category.strip";
    public static final String PREF_STRIP_TYPE_KEY   = "com.ardnew.blixel.preferences.preference.strip_type";
    public static final String PREF_COLOR_ORDER_KEY  = "com.ardnew.blixel.preferences.preference.color_order";
    public static final String PREF_STRIP_LENGTH_KEY = "com.ardnew.blixel.preferences.preference.strip_length";
    public static final String PREF_AUTO_SEND_KEY    = "com.ardnew.blixel.preferences.preference.device_auto_send";

    private MainViewModel     mainViewModel;
    private SharedPreferences sharedPreferences;

    private boolean isInitialized = false;

    private PreferenceCategory categoryDevice;
    private Preference         preferenceDevice;
    private Preference         preferenceAutoConnect;
    private PreferenceCategory categoryStrip;
    private Preference         preferenceStripType;
    private Preference         preferenceColorOrder;
    private Preference         preferenceStripLength;
    private Preference         preferenceAutoSend;

    @SuppressWarnings("unused")
    public static DeviceFragment newInstance() {

        return new DeviceFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public static void setPreference(@NonNull SharedPreferences sharedPreferences, @NonNull String key, Object value) {

        SharedPreferences.Editor editor = sharedPreferences.edit();
        switch (key) {
            case DeviceFragment.PREF_DEVICE_KEY:
                editor.putString(key, (String)value);
                break;
            case DeviceFragment.PREF_AUTO_CONNECT_KEY:
                if (value instanceof Boolean) {
                    editor.putString(key, value.toString());
                } else if (value instanceof String) {
                    editor.putString(key, (String)value);
                }
                break;
            case DeviceFragment.PREF_STRIP_TYPE_KEY:
            case DeviceFragment.PREF_COLOR_ORDER_KEY:
            case DeviceFragment.PREF_STRIP_LENGTH_KEY:
                if (value instanceof Integer) {
                    editor.putString(key, Utility.format("%d", value));
                } else if (value instanceof String) {
                    editor.putString(key, (String)value);
                }
                break;
        }
        editor.apply();
    }

    public static Object getPreference(@NonNull SharedPreferences sharedPreferences, @NonNull String key) {

        String value;
        switch (key) {
            case DeviceFragment.PREF_DEVICE_KEY:
                return sharedPreferences.getString(key, null);
            case DeviceFragment.PREF_AUTO_CONNECT_KEY:
                value = sharedPreferences.getString(key, "true");
                return Boolean.valueOf(value);
            case DeviceFragment.PREF_STRIP_TYPE_KEY:
            case DeviceFragment.PREF_COLOR_ORDER_KEY:
            case DeviceFragment.PREF_STRIP_LENGTH_KEY:
                value = sharedPreferences.getString(key, null);
                if  (null != value) {
                    return Utility.parseUint(value);
                }
                break;
        }
        return null;
    }

    @Override
    public void onResume() {

        super.onResume();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        this.setPreferencesFromResource(R.xml.device_config, rootKey);

        if (!this.isInitialized) {

            if (null == this.mainViewModel) {
                final FragmentActivity activity = this.getActivity();
                this.mainViewModel = new ViewModelProvider((null != activity) ? activity : this).get(MainViewModel.class);
            }

            PreferenceManager preferenceManager = this.getPreferenceManager();
            if (null != preferenceManager) {
                this.sharedPreferences = preferenceManager.getSharedPreferences();
            }

            this.categoryDevice        = this.findPreference(DeviceFragment.CAT_DEVICE_KEY);
            this.preferenceDevice      = this.findPreference(DeviceFragment.PREF_DEVICE_KEY);
            this.preferenceAutoConnect = this.findPreference(DeviceFragment.PREF_AUTO_CONNECT_KEY);
            this.categoryStrip         = this.findPreference(DeviceFragment.CAT_STRIP_KEY);
            this.preferenceStripType   = this.findPreference(DeviceFragment.PREF_STRIP_TYPE_KEY);
            this.preferenceColorOrder  = this.findPreference(DeviceFragment.PREF_COLOR_ORDER_KEY);
            this.preferenceStripLength = this.findPreference(DeviceFragment.PREF_STRIP_LENGTH_KEY);
            this.preferenceAutoSend    = this.findPreference(DeviceFragment.PREF_AUTO_SEND_KEY);
            this.isInitialized         = true;
        }
    }
}
