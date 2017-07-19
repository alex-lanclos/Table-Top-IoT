package com.waitr.thelazydev.tabletop;

/*
 * Copyright 2017, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import android.util.FloatProperty;
import android.util.IntProperty;
import android.util.Log;

import com.google.android.things.contrib.driver.apa102.Apa102;
import com.google.android.things.pio.Pwm;

import java.io.IOException;

/**
 * Property to represent the brightness of an LED attached
 * to the provided PWM port.  This is used to allow an
 * ObjectAnimator to modify the value.
 */
public class BrightnessProperty extends IntProperty<Apa102> {
    private static final String TAG = "BrightnessProperty";

    // Cache the last set value since PWM can't report its state
    private int mValue;

    BrightnessProperty() {
        super("PWM Brightness");
    }

    @Override
    public void setValue(Apa102 ledStrip, int value) {
        mValue = value;
        ledStrip.setBrightness(value);
    }

    @Override
    public Integer get(Apa102 pwm) {
        return mValue;
    }
}
