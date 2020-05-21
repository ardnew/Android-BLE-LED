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

import android.app.Application;

import com.ardnew.nitelite.bluetooth.Manufacturer;
import com.ardnew.nitelite.bluetooth.ServiceUuid;

public class Nitelite extends Application {

    private static transient Manufacturer manufacturers;
    private static transient ServiceUuid standardServices;
    private static transient ServiceUuid memberServices;
    private static transient ServiceUuid niteliteServices;

    public void onCreate() {

        super.onCreate();

        Nitelite.manufacturers = new Manufacturer(this.getApplicationContext(), "manufacturers.properties", "manufacturer");
        Nitelite.standardServices = new ServiceUuid(this.getApplicationContext(), "services.properties", "service.standard");
        Nitelite.memberServices = new ServiceUuid(this.getApplicationContext(), "services.properties", "service.member");
        Nitelite.niteliteServices = new ServiceUuid(this.getApplicationContext(), "services.properties", "service.nitelite");
    }

    public static Manufacturer manufacturers() {

        return Nitelite.manufacturers;
    }

    @SuppressWarnings("unused")
    public static ServiceUuid standardServices() {

        return Nitelite.standardServices;
    }

    @SuppressWarnings("unused")
    public static ServiceUuid memberServices() {

        return Nitelite.memberServices;
    }

    public static ServiceUuid niteliteServices() {

        return Nitelite.niteliteServices;
    }
}
