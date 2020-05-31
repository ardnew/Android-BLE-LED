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

package com.ardnew.blixel.bluetooth.uuid;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.ParcelUuid;
import android.util.Log;

import androidx.annotation.NonNull;

import com.ardnew.blixel.Utility;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

import static android.content.res.AssetManager.ACCESS_BUFFER;

//
// TODO: generalize and combine classes Manufacturer and ServiceUuid
//
public class Service {

    private static final String BLUETOOTH_SIG_BASE_UUID = "0000-1000-8000-00805F9B34FB";

    private static String SigUuidFrom16Bit(int uuid) {

        if (Utility.isValidUint(uuid) && (uuid <= 0xFFFF)) {
            return Utility.format("0000%04X-%s", uuid, Service.BLUETOOTH_SIG_BASE_UUID);
        } else {
            return null;
        }
    }

    @SuppressWarnings("FieldCanBeLocal")
    private final Properties properties;
    private final HashMap<ParcelUuid, String> serviceMap;

    @SuppressWarnings("SameParameterValue")
    public Service(@NonNull Context context, String properties, String keyPrefix) {

        AssetManager assetManager = context.getAssets();

        this.properties = new Properties();
        this.serviceMap = new HashMap<>();

        try (InputStream inputStream = assetManager.open(properties, ACCESS_BUFFER)) {

            this.properties.load(inputStream);

            if (!keyPrefix.endsWith(".")) {
                keyPrefix += ".";
            }

            for (String key : this.properties.stringPropertyNames()) {

                String normKey = Utility.lowerCase(key);

                if (normKey.startsWith(Utility.lowerCase(keyPrefix))) {

                    String subkey = normKey.substring(keyPrefix.length());
                    Integer uintKey = Utility.parseUint(subkey);
                    ParcelUuid uuid = null;

                    if (Utility.isValidUint(uintKey)) {
                        String sigUuid = Service.SigUuidFrom16Bit(uintKey);
                        if (null != sigUuid) {

                            try {
                                uuid = ParcelUuid.fromString(sigUuid);
                            } catch (Exception ignored) {
                            }
                        }
                    } else {
                        uuid = ParcelUuid.fromString(subkey);
                    }

                    if (null != uuid) {
                        this.serviceMap.put(uuid, this.properties.getProperty(key));
                    }
                }
            }
        } catch (Exception ex) {
            Log.d(this.getClass().getCanonicalName(), String.format("failed to load properties asset: \"%s\"", properties), ex);
        }
    }

    @SuppressWarnings("unused")
    public HashMap<ParcelUuid, String> serviceMap() {

        return this.serviceMap;
    }

    public String service(ParcelUuid key) {

        if (!this.serviceMap.containsKey(key)) {
            return null; // key actually does not exist.
        }

        String attr = this.serviceMap.get(key);

        if (null == attr) {
            return ""; // replace with empty string if key exists but maps to null.
        }

        return attr;
    }
}
