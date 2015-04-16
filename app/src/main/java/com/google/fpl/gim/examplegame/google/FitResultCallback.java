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

import android.util.Log;

import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.FitnessStatusCodes;
import com.google.android.gms.fitness.data.DataType;
import com.google.fpl.gim.examplegame.utils.Utils;

/**
 * Implements ResultCallback<Result> while allowing to record more state so we can change game flow
 * accordingly.
 */
public class FitResultCallback <R extends Result>
        implements com.google.android.gms.common.api.ResultCallback<R> {
    private static final String TAG = "GoogleFitResultCallback";

    // Enum type for keeping track of which API generated this callback.
    public enum RegisterType {
        SENSORS,
        RECORDING,
        SESSION,
    }

    GoogleApiClientWrapper mGoogleApiClient;
    RegisterType mRegisterType;
    DataType mDataType;
    boolean mSubscribe;  // True if subscribe, false if unsubscribe.

    /**
     * Default constructor.
     * @param googleApiClient The GoogleApiClientWrapper to pass data back to.
     * @param registerType The type of call that resulted in this callback.
     * @param dataType The data type we are trying to process.
     * @param subscribe If true, this is a subscribe or a register call.
     */
    public FitResultCallback(
            GoogleApiClientWrapper googleApiClient, RegisterType registerType, DataType dataType,
            boolean subscribe) {
        mGoogleApiClient = googleApiClient;
        mRegisterType = registerType;
        mDataType = dataType;
        mSubscribe = subscribe;
    }

    @Override
    public void onResult(R result) {
        switch (mRegisterType) {
            case SENSORS:
                onSensorResult(result.getStatus());
                break;
            case RECORDING:
                onRecordingResult(result.getStatus());
                break;
            case SESSION:
                onSessionResult(result.getStatus());
                break;
            default:
                Log.e(TAG, "Unknown enum type.");
        }
    }

    private void onSensorResult(Status status) {
        if (status.isSuccess()) {
            // There is a lapse between this callback to actually getting data from the listener,
            // depending on the data type. It is a known issue, by design. You can account for that
            // delay with display text or other mechanisms.
            mGoogleApiClient.sensorRegistered(mDataType);
            Utils.logDebug(TAG, "Successfully registered sensor for " + mDataType.toString());
        } else {
            Utils.logDebug(TAG, "There was a problem registering ." + mDataType + "\n" +
                    status.getStatusMessage());
        }

    }

    private void onRecordingResult(Status status) {
        if (mSubscribe) {
            onRecordingSubscription(status);
        } else {
            onRecordingUnsubscription(status);
        }
    }

    private void onRecordingSubscription(Status status) {
        if (status.isSuccess()) {
            if (status.getStatusCode()
                    == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
                Utils.logDebug(TAG, "Existing subscription for activity detected.");
            } else {
                Utils.logDebug(TAG, "Started recording data for " + mDataType);
            }
        } else {
            // For a better user experience, you can add visual error text indicating the session
            // will not be recorded to the cloud.
            Utils.logDebug(TAG, "Unable to start recording data for " + mDataType);
        }
    }

    private void onRecordingUnsubscription(Status status) {
        if (status.isSuccess()) {
            Utils.logDebug(TAG, "Stopped recording data for " + mDataType);
        } else {
            Utils.logDebug(TAG, "Unable to stop recording data for " + mDataType);
        }
    }

    private void onSessionResult(Status status) {
        if (mSubscribe) {
            onSessionSubscription(status);
        } else {
            onSessionUnsubscription(status);
        }
    }

    private void onSessionSubscription(Status status) {
        if (status.isSuccess()) {
            Utils.logDebug(TAG, "Started data session.");
        } else {
            // For a better user experience, you can add visual error text indicating the session
            // will not be identified in the cloud.
            Utils.logDebug(TAG, "Unable to start data session.");
        }
    }

    private void onSessionUnsubscription(Status status) {
        if (status.isSuccess()) {
            Utils.logDebug(TAG, "Ended data session.");
        } else {
            Utils.logDebug(TAG, "Unable to end data session.");
        }
    }
}
