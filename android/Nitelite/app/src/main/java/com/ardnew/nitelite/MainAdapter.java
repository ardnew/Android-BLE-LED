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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;

import com.flask.colorpicker.ColorPickerView;
import com.google.android.material.tabs.TabLayout;

public class MainAdapter extends FragmentPagerAdapter {

    public static class ColorPicker extends Fragment {

        private static final String layoutId = "com.ardnew.nitelite.MainAdapter.ColorPicker.layoutId";

        private ColorPickerView colorPickerView;
        private Button colorButton;

        static ColorPicker newInstance() {
            Bundle args = new Bundle();
            args.putInt(ColorPicker.layoutId, R.layout.color_picker);
            ColorPicker cp = new ColorPicker();
            cp.setArguments(args);
            return cp;
        }

        public ColorPicker() {
            // fragments must have global empty constructor!
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            Bundle args = this.getArguments();

            if (null != args) {
                return inflater.inflate(args.getInt(ColorPicker.layoutId), container, false);
            }
            return null;
        }
    }

    public static class ColorPattern extends Fragment {

        private static final String layoutId = "com.ardnew.nitelite.MainAdapter.ColorPattern.layoutId";

        static ColorPattern newInstance() {
            Bundle args = new Bundle();
            args.putInt(ColorPattern.layoutId, R.layout.color_pattern);
            ColorPattern cp = new ColorPattern();
            cp.setArguments(args);
            return cp;
        }

        public ColorPattern() {
            // fragments must have global empty constructor!
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            Bundle args = this.getArguments();

            if (null != args) {
                return inflater.inflate(args.getInt(ColorPattern.layoutId), container, false);
            }
            return null;
        }
    }

    private final ColorPicker colorPicker;
    private final ColorPattern colorPattern;

    private final MainActivity mainActivity;
    private final TabLayout tabLayout;
    private final int numTabs;

    MainAdapter(@NonNull MainActivity mainActivity, @NonNull TabLayout tabLayout, int numTabs) {

        super(mainActivity.getSupportFragmentManager(), tabLayout.getTabCount());

        this.mainActivity = mainActivity;
        this.tabLayout = tabLayout;
        this.numTabs = numTabs;

        this.colorPicker = ColorPicker.newInstance();
        this.colorPattern = ColorPattern.newInstance();
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {

        switch (position) {
        case 0:
            return this.colorPicker;
        case 1:
            return this.colorPattern;
        }
        return new Fragment();
    }

    @Override
    public int getCount() {

        return this.numTabs;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {

        switch (position) {
            case 0:
                return this.mainActivity.getString(R.string.main_tab_color_picker);
            case 1:
                return this.mainActivity.getString(R.string.main_tab_color_pattern);
        }
        return "";
    }

    public ColorPicker colorPicker() {

        return this.colorPicker;
    }

    public ColorPattern colorPattern() {

        return this.colorPattern;
    }
}
