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

package com.ardnew.blixel.activity.main.ui.color;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.ardnew.blixel.R;
import com.ardnew.blixel.activity.main.MainViewModel;
import com.ardnew.blixel.bluetooth.gatt.characteristic.NeopixelColor;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorChangedListener;
import com.flask.colorpicker.slider.AlphaSlider;
import com.flask.colorpicker.slider.LightnessSlider;

public class ColorFragment extends Fragment implements OnColorChangedListener {

    private MainViewModel viewModel;
    private ColorPickerView colorPickerView;
    private AlphaSlider alphaSlider;
    private LightnessSlider lightnessSlider;

    private boolean isInitialized = false;

    private int selectedColor;
    private int selectedAlpha;
    private int selectedBright;

    @SuppressWarnings("unused")
    public static ColorFragment newInstance() {

        return new ColorFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_color, container, false);

        if (!this.isInitialized) {

            final FragmentActivity activity = this.getActivity();
            this.viewModel = new ViewModelProvider((null != activity) ? activity : this).get(MainViewModel.class);

            this.colorPickerView = root.findViewById(R.id.color_picker_view);
            this.colorPickerView.addOnColorChangedListener(this);

            this.alphaSlider = root.findViewById(R.id.color_picker_alpha_slider);
            this.alphaSlider.setColorPicker(this.colorPickerView);
            this.alphaSlider.setOnValueChangedListener(this::onAlphaChanged);
            this.alphaSlider.setVisibility(View.GONE);

            this.lightnessSlider = root.findViewById(R.id.color_picker_lightness_slider);
            this.lightnessSlider.setColorPicker(this.colorPickerView);
            this.lightnessSlider.setOnValueChangedListener(this::onLightnessChanged);

            this.isInitialized = true;
        }

        return root;
    }

    @Override
    public void onResume() {

        super.onResume();

        if (null != this.viewModel) {
            final NeopixelColor neopixelColor = this.viewModel.getNeopixelColor();
            if ((null != neopixelColor) && neopixelColor.isValid()) {

                this.selectedColor  = neopixelColor.color();
                this.selectedAlpha  = neopixelColor.alpha();
                this.selectedBright = neopixelColor.bright();

                this.colorPickerView.setColor(this.selectedColor, false);
                this.alphaSlider.setColor(this.selectedColor);
                this.lightnessSlider.setColor(this.selectedColor);
            }
        }
    }

    @Override
    public void onColorChanged(int selectedColor) {

        this.selectedColor = selectedColor;

        this.viewModel.updateNeopixelColor(selectedColor, this.selectedAlpha, this.selectedBright);
    }

    public void onAlphaChanged(float value) {

        this.selectedAlpha = Math.round(value * NeopixelColor.MAX_ALPHA) & 0xFF;
    }

    public void onLightnessChanged(float value) {

        this.selectedBright = Math.round(value * NeopixelColor.MAX_BRIGHT) & 0xFF;
    }

}
