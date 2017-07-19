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

package com.waitr.thelazydev.tabletop;

import android.graphics.Color;

import com.google.android.things.contrib.driver.apa102.Apa102;

import java.util.Arrays;

/**
 * Helper methods for computing outputs on the Rainbow HAT
 */
public class RainbowUtil {

    /* LED Strip Color Constants*/
    private static int[] sRainbowColors;
    static {
        sRainbowColors = new int[7];
        for (int i = 0; i < sRainbowColors.length; i++) {
            float[] hsv = {i * 360.f / sRainbowColors.length, 1.0f, 1.0f};
            sRainbowColors[i] = Color.HSVToColor(255, hsv);
        }
    }

    /**
     * Return an array of colors for the LED strip based on the given pressure.
     * @return Array of colors to set on the LED strip.
     */
    public static int[] getTableIsReady() {
        int[] colors = new int[7];
        Arrays.fill(colors, Color.GREEN);

        return colors;
    }




}
