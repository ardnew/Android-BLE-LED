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

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.ardnew.nitelite.Utility;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

import static android.content.res.AssetManager.ACCESS_BUFFER;

public class Manufacturer {

    @SuppressWarnings("FieldCanBeLocal")
    private final Properties properties;
    private final HashMap<Integer, String> manufacturerMap;

    @SuppressWarnings("SameParameterValue")
    public Manufacturer(@NonNull Context context, String properties, String keyPrefix) {

        AssetManager assetManager = context.getAssets();

        this.properties = new Properties();
        this.manufacturerMap = new HashMap<>();

        try (InputStream inputStream = assetManager.open(properties, ACCESS_BUFFER)) {

            this.properties.load(inputStream);

            if (!keyPrefix.endsWith(".")) {
                keyPrefix += ".";
            }

            Enumeration keys = this.properties.propertyNames();

            while (keys.hasMoreElements()) {

                String key = keys.nextElement().toString();
                String normKey = Utility.lowerCase(key);

                if (normKey.startsWith(Utility.lowerCase(keyPrefix))) {

                    String subkey = normKey.substring(keyPrefix.length());
                    Integer uintKey = Utility.parseUint(subkey);

                    if (Utility.isValidUint(uintKey)) {
                        this.manufacturerMap.put(uintKey, this.properties.getProperty(key));
                    }
                }
            }
        } catch (Exception ex) {
            Log.d(this.getClass().getCanonicalName(), String.format("failed to load properties asset: \"%s\"", properties), ex);
        }
    }

    @SuppressWarnings("unused")
    HashMap<Integer, String> manufacturerMap() {

        return this.manufacturerMap;
    }

    public String manufacturer(Integer key) {

        if (!this.manufacturerMap.containsKey(key)) {
            return null; // key actually does not exist.
        }

        String attr = this.manufacturerMap.get(key);

        if (null == attr) {
            return ""; // replace with empty string if key exists but maps to null.
        }

        return attr;
    }
}
