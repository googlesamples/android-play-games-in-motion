/*
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.fpl.gim.examplegame.utils;

import android.util.Log;

import com.google.fpl.gim.examplegame.BuildConfig;

import java.io.File;
import java.util.ArrayList;

/**
 * A few utility functions and classes used by the prototype.
 */
public class Utils {

    public static final float SECONDS_TO_NANOS_SCALE = (float) Math.pow(10, 9);
    public static final float NANOS_TO_SECONDS_SCALE = (float) Math.pow(10, -9);
    public static final int MINUTES_TO_SECONDS_SCALE = 60;
    public static final float SECONDS_TO_MINUTES_SCALE = 1.0f / 60;
    public static final float MILES_TO_FEET_SCALE = 5280f;
    public static final float SECONDS_PER_METER_TO_MINUTES_PER_MILE_SCALE = 26.8224f;

    // user control for displaying log messages
    private static final boolean DEBUG_LOG = true;

    public static float nanosToSeconds(long nanos) {
        return nanos / SECONDS_TO_NANOS_SCALE;
    }

    public static long secondsToNanos(float seconds) {
        return (long) (seconds * SECONDS_TO_NANOS_SCALE);
    }

    public static long minutesToNanos(float min) {
        return (long) (min * MINUTES_TO_SECONDS_SCALE * SECONDS_TO_NANOS_SCALE);
    }

    public static float nanosToMinutes(long nanos) {
        return (nanos / SECONDS_TO_NANOS_SCALE / MINUTES_TO_SECONDS_SCALE);
    }

    public static float feetToMiles(float feet) {
        return feet / MILES_TO_FEET_SCALE;
    }

    public static float secondsToMinutes(float seconds) {
        return seconds / MINUTES_TO_SECONDS_SCALE;
    }

    public static float metersPerSecondToMinutesPerMile(float metersPerSecond) {
        if (metersPerSecond == 0.0f) {
            return 0.0f;
        }
        float secondsPerMeter = 1 / metersPerSecond;  // seconds per meter
        return secondsPerMeter * SECONDS_PER_METER_TO_MINUTES_PER_MILE_SCALE;
    }

    /**
     * Prints debugging messages to the console.
     *
     * Disabled for non-debug builds.
     *
     * @param message - The message to print to the console.
     */
    public static void logDebug(String tag, String message) {
        if (BuildConfig.DEBUG || DEBUG_LOG) {
            Log.d(tag, message);
        }
    }

    /**
     * Builds a file path.
     * @param rootDirectory Highest directory or file name, if not nested.
     * @param subDirectories Sub-Directories, or the file name.
     * @return A string of the file path.
     */
    public static String makeFilePath(String rootDirectory, ArrayList<String> subDirectories) {
        File file = new File(rootDirectory);
        for (String subDirectory : subDirectories) {
            file = new File(file, subDirectory);
        }
        return file.toString();
    }
}
