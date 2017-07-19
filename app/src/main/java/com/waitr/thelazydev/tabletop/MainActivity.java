package com.waitr.thelazydev.tabletop;

import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.BounceInterpolator;

import com.google.android.things.contrib.driver.apa102.Apa102;
import com.google.android.things.contrib.driver.ht16k33.AlphanumericDisplay;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.Pwm;

import java.io.IOException;
import java.util.Arrays;
import java.util.Observable;

import rx.Subscriber;


/**
 * Skeleton of an Android Things activity.
 * <p>
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 * <p>
 * <pre>{@code
 * PeripheralManagerService service = new PeripheralManagerService();
 * mLedGpio = service.openGpio("BCM6");
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
 * mLedGpio.setValue(true);
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 */
public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    // Default LED brightness
    private static final int LEDSTRIP_BRIGHTNESS = 1;
    // Brightness values
    private static final int BRIGHTNESS_START = 1;
    private static final int BRIGHTNESS_END = 5;

    // Animation duration
    private static final int DURATION_MS = 2000;

    // Peripheral drivers
    private AlphanumericDisplay mDisplay;
    private Apa102 mLedstrip;

    private ObjectAnimator mGreenAnimator;

    private Handler mLEDBrightnessCycleHandler;
    private Runnable mLEDBrightnessCycleRunnable;

    private int mBrightnessCounter = 0;
    private int mBrightnessModulator = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Weather Station Started");


        // Initialize 7-segment display
        try {
            mDisplay = new AlphanumericDisplay(BoardDefaults.getI2cBus());
            mDisplay.setEnabled(true);
            mDisplay.display("1234");
            Log.d(TAG, "Initialized I2C Display");
        } catch (IOException e) {
            throw new RuntimeException("Error initializing display", e);
        }

        // Initialize LED strip
        try {
            mLedstrip = new Apa102(BoardDefaults.getSpiBus(), Apa102.Mode.BGR);
            mLedstrip.setBrightness(LEDSTRIP_BRIGHTNESS);
            int[] colors = new int[7];
            Arrays.fill(colors, Color.RED);
            mLedstrip.write(colors);
            // Because of a known APA102 issue, write the initial value twice.
            mLedstrip.write(colors);
            Log.d(TAG, "Initialized SPI LED strip");
        } catch (IOException e) {
            throw new RuntimeException("Error initializing LED strip", e);
        }

        mLEDBrightnessCycleHandler = new Handler();

        mLEDBrightnessCycleRunnable = new Runnable() {
            @Override
            public void run() {
                if (mLedstrip != null) {

                    mLedstrip.setBrightness(mBrightnessCounter);
                    mBrightnessCounter = mBrightnessCounter + mBrightnessModulator;

                    if (mBrightnessCounter > BRIGHTNESS_END) {
                        mBrightnessModulator = -1;
                        mBrightnessCounter = BRIGHTNESS_END - 1;
                    } else if (mBrightnessCounter < BRIGHTNESS_START) {
                        mBrightnessModulator = 1;
                        mBrightnessCounter = BRIGHTNESS_START + 1;
                    }

                    if (mBrightnessCounter == 0) {
                        Log.e("error", "blahh");
                    }

                }

                try {
                    int[] colors = RainbowUtil.getTableIsReady();
                    mLedstrip.write(colors);
                    // Because of a known APA102 issue, write the initial value twice.
                    mLedstrip.write(colors);
                } catch (IOException e) {
                    throw new RuntimeException("Error initializing LED strip", e);
                }

                mLEDBrightnessCycleHandler.postDelayed(mLEDBrightnessCycleRunnable, 100);
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();

        tableIsReady();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mDisplay != null) {
            try {
                mDisplay.clear();
                mDisplay.setEnabled(false);
                mDisplay.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing display", e);
            } finally {
                mDisplay = null;
            }
        }

        if (mLedstrip != null) {
            try {
                mLedstrip.write(new int[7]);
                mLedstrip.setBrightness(0);
                mLedstrip.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing LED strip", e);
            } finally {
                mLedstrip = null;
            }
        }
    }


    private void tableIsReady() {
        // Initialize LED strip
        mLEDBrightnessCycleHandler.postDelayed(mLEDBrightnessCycleRunnable, 100);
    }

    private void tableIsReserved() {
        // Initialize LED strip
        try {
            mLedstrip.setBrightness(LEDSTRIP_BRIGHTNESS);
            int[] colors = new int[7];
            Arrays.fill(colors, Color.RED);
            mLedstrip.write(colors);
            // Because of a known APA102 issue, write the initial value twice.
            mLedstrip.write(colors);
            Log.d(TAG, "Initialized SPI LED strip");
        } catch (IOException e) {
            throw new RuntimeException("Error initializing LED strip", e);
        }
    }

    private void occupyTable() {
        // Initialize LED strip
        try {
            mLedstrip.setBrightness(0);
            int[] colors = new int[7];
            Arrays.fill(colors, Color.RED);
            mLedstrip.write(colors);
            // Because of a known APA102 issue, write the initial value twice.
            mLedstrip.write(colors);
            Log.d(TAG, "Initialized SPI LED strip");
        } catch (IOException e) {
            throw new RuntimeException("Error initializing LED strip", e);
        }
    }


}
