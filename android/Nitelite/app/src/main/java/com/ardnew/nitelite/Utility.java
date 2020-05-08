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

import android.app.Activity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

class Utility {

    static void dismissKeyboard(AppCompatActivity activity, View sender) {

        InputMethodManager manager = (InputMethodManager)activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (null != manager)
            { manager.hideSoftInputFromWindow(sender.getWindowToken(), 0); }
    }

    private static Locale locale() {

        return Locale.getDefault();
    }

    static String format(@NonNull String format, Object... args) {

        return String.format(Utility.locale(), format, args);
    }

    @SuppressWarnings("SameParameterValue")
    static String join(List<String> list, String separator) {

        if (null == list)
            { return null; }

        switch (list.size()) {
            case 0:
                return "";

            case 1:
                return list.get(0);

            default:
                StringBuilder stringBuilder = new StringBuilder(list.get(0));
                ListIterator<String> listIterator = list.listIterator(1);
                while (listIterator.hasNext()) {
                    stringBuilder.append(listIterator.next());
                    stringBuilder.append(separator);
                }
                return stringBuilder.toString();
        }
    }
}
