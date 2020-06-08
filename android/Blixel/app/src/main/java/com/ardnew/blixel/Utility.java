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

package com.ardnew.blixel;

import android.app.Activity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

@SuppressWarnings("WeakerAccess")
public class Utility {

    private static final int INVALID_UINT = -1;

    public static void dismissKeyboard(AppCompatActivity activity, View sender) {

        InputMethodManager manager = (InputMethodManager)activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (null != manager) {
            manager.hideSoftInputFromWindow(sender.getWindowToken(), 0);
        }
    }

    public static Locale locale() {

        return Locale.getDefault();
    }

    public static String format(@NonNull String format, Object... args) {

        return String.format(Utility.locale(), format, args);
    }

    public static String lowerCase(@NonNull String string) {

        return string.toLowerCase(Utility.locale());
    }

    @SuppressWarnings("SameParameterValue")
    public static String join(List<String> list, String separator) {

        if (null == list) {
            return null;
        }

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

    public static boolean isValidUint(int uint) {

        return uint >= 0;
    }

    public static Integer parseUint(@NonNull String string, int base) {

        int uint;

        try {
            uint = Integer.parseUnsignedInt(string, base);
        } catch (Exception ex) {
            uint = INVALID_UINT;
        }

        return uint;
    }

    public static Integer parseUint(@NonNull String string) {

        string = string.trim();

        if (string.startsWith("+")) {
            string = string.substring(1);
        }

        int base;

        if (string.startsWith("0x") || string.startsWith("\\x")) {
            string = string.substring(2);
            base = 16;
        } else if (string.startsWith("x") || string.startsWith("$") || string.startsWith("#")) {
            string = string.substring(1);
            base = 16;
        } else if (string.matches("[a-f]") && string.matches("^[a-f0-9]+$")) {
            base = 16;
        } else if (string.startsWith("0o") || string.startsWith("\\o")) {
            string = string.substring(2);
            base = 8;
        } else if (string.startsWith("o")) {
            string = string.substring(1);
            base = 8;
        } else if (string.startsWith("0b") || string.startsWith("\\b")) {
            string = string.substring(2);
            base = 2;
        } else if (string.startsWith("b")) {
            string = string.substring(1);
            base = 2;
        } else {
            base = 10;
        }

        if (string.length() > 0) {
            return Utility.parseUint(string, base);
        } else {
            return INVALID_UINT;
        }
    }

    public static Snackbar makeSnackBar(View parent, int duration, String text) {

        Snackbar snackbar = Snackbar.make(parent, text, duration);

        snackbar.setBackgroundTint(parent.getContext().getColor(R.color.color_snackbar_surface));
        snackbar.setTextColor(parent.getContext().getColor(R.color.color_snackbar_text));

        return snackbar;
    }

    public static Snackbar makeSnackBar(View parent, int duration, int textResId) {

        return Utility.makeSnackBar(parent, duration, parent.getContext().getString(textResId));
    }

    public static Snackbar makeSnackBar(View parent, int duration, String text, String actionText, View.OnClickListener action) {

        Snackbar snackbar = Utility.makeSnackBar(parent, duration, text);

        snackbar.setActionTextColor(parent.getContext().getColor(R.color.color_snackbar_button_text));
        snackbar.setAction(actionText, action);

        return snackbar;
    }

    public static Snackbar makeSnackBar(View parent, int duration, int textResId, int actionTextResId, View.OnClickListener action) {

        return Utility.makeSnackBar(parent, duration,
            parent.getContext().getString(textResId), parent.getContext().getString(actionTextResId), action);
    }

    public static class RunList extends ArrayList<Runnable> implements Runnable {
        @Override
        public void run() {
            for (Runnable runner : this) {
                runner.run();
            }
        }
    }

}
