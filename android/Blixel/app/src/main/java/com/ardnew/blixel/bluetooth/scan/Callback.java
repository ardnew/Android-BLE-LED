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

package com.ardnew.blixel.bluetooth.scan;

import androidx.annotation.NonNull;

import com.ardnew.blixel.activity.scan.ScanActivity;

import java.util.List;

public class Callback extends android.bluetooth.le.ScanCallback {

    private final ScanActivity scanActivity;

    public Callback(@NonNull ScanActivity scanActivity) {

        this.scanActivity = scanActivity;
    }

    @Override
    public void onScanResult(int callbackType, android.bluetooth.le.ScanResult result) {

        super.onScanResult(callbackType, result);
        this.scanActivity.runOnUiThread(
                () -> this.scanActivity.onScanResult(new Result(result))
        );
    }

    @Override
    public void onScanFailed(int errorCode) {

        super.onScanFailed(errorCode);
    }

    @Override
    public void onBatchScanResults(List<android.bluetooth.le.ScanResult> results) {

        super.onBatchScanResults(results);
    }
}
