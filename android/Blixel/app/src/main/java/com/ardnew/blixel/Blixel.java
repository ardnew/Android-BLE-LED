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

import android.app.Application;

import com.ardnew.blixel.bluetooth.uuid.Manufacturer;
import com.ardnew.blixel.bluetooth.uuid.Service;

public class Blixel extends Application {

    private static transient Manufacturer manufacturers;
    private static transient Service standardServices;
    private static transient Service memberServices;
    private static transient Service blixelServices;

    private static Blixel blixel = null;

    public void onCreate() {

        super.onCreate();

        if (null == Blixel.blixel) {
            Blixel.blixel = this;
        }

        Blixel.manufacturers = new Manufacturer(this.getApplicationContext(), "manufacturers.properties", "manufacturer");
        Blixel.standardServices = new Service(this.getApplicationContext(), "services.properties", "service.standard");
        Blixel.memberServices = new Service(this.getApplicationContext(), "services.properties", "service.member");
        Blixel.blixelServices = new Service(this.getApplicationContext(), "services.properties", "service.blixel");
    }

    public static Blixel application() {

        return Blixel.blixel;
    }

    public static Manufacturer manufacturers() {

        return Blixel.manufacturers;
    }

    @SuppressWarnings("unused")
    public static Service standardServices() {

        return Blixel.standardServices;
    }

    @SuppressWarnings("unused")
    public static Service memberServices() {

        return Blixel.memberServices;
    }

    public static Service blixelServices() {

        return Blixel.blixelServices;
    }
}
