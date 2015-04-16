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

package com.google.fpl.gim.examplegame.google;

import com.google.android.gms.fitness.data.DataType;

/**
 * This keeps track of the sensor data types and their registration settings.
 */
public class FitDataTypeSetting {
    private boolean mRequired; // Is this data type required for the game to start?
    private DataType mDataType;
    private long mSamplingRateSeconds;
    private int mAccuracyMode;

    public FitDataTypeSetting(
            boolean required, DataType dataType, long samplingRateSeconds, int accuracyMode) {
        mRequired = required;
        mDataType = dataType;
        mSamplingRateSeconds = samplingRateSeconds;
        mAccuracyMode = accuracyMode;
    }

    public boolean isRequired() {
        return mRequired;
    }

    public DataType getDataType() {
        return mDataType;
    }

    public long getSamplingRateSeconds() {
        return mSamplingRateSeconds;
    }

    public int getAccuracyMode() {
        return mAccuracyMode;
    }
}
