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

import android.app.Activity;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.gms.fitness.FitnessStatusCodes;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.result.SessionStopResult;
import com.google.android.gms.games.Games;
import com.google.fpl.gim.examplegame.MainActivity;
import com.google.fpl.gim.examplegame.utils.Utils;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Container class to hold a static instance of GoogleApiClient.  This allows the MainActivity
 * class to implement the GoogleApiClient.OnConnectionFailedListener interface (which
 * requires an Activity to request authentications from the user) and the MainService class to
 * implement the GoogleApiClient.ConnectionCallbacks interface.  This also allows the Google
 * API client to be subsequently accessed by either class.
 */
public class GoogleApiClientWrapper implements ConnectionCallbacks, OnConnectionFailedListener {
    private static final String TAG = GoogleApiClientWrapper.class.getSimpleName();
    private static final String SESSION_NAME = "Games-in-Motion Mission";
    public static final int REQUEST_CODE_OAUTH = 1;

    private boolean mAuthInProgress = false;

    private Activity mActivity; // Activity for GoogleApiClient to launch visual elements on.
    private GoogleApiClient mGoogleApiClient;

    private Set<FitDataTypeSetting> sensorsAwaitingRegistration = new HashSet<>();

    /**
     * Builds a GoogleApiClient that connects to the Fitness Api.
     * @param activity The activity through which authorization UI will be launched.
     */
    public void buildGoogleApiClient(Activity activity) {
        if (mGoogleApiClient != null) {
            return;
        }
        Utils.logDebug(TAG, "Building the Google Api Client.");

        mActivity = activity;

        // Create the Google API Client.
        mGoogleApiClient = new GoogleApiClient.Builder(activity, this, this)
                .addApi(Fitness.API)                    // Fitness API
                .addScope(Fitness.SCOPE_ACTIVITY_READ)  // Fitness Scopes
                .addScope(Fitness.SCOPE_LOCATION_READ)
                .addApi(Games.API)                      // Games API
                .addScope(Games.SCOPE_GAMES)            // Games Scope
                .build();
    }

    public void connect() {
        // Make sure the app is not already connected or attempting to connect
        if (!mGoogleApiClient.isConnecting() && !mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    public void disconnect() {
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Utils.logDebug(TAG, "Connected!");

        // Send the hint to Activity for UI updates
        ((MainActivity) mActivity).onFitStatusUpdated(true);
    }

    @Override
    public void onConnectionSuspended(int i) {
        // If your connection gets lost at some point,
        // you'll be able to determine the reason and react to it here.
        if (i == ConnectionCallbacks.CAUSE_NETWORK_LOST) {
            Utils.logDebug(TAG, "Connection lost.  Cause: Network Lost.");
        } else if (i == ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
            Utils.logDebug(TAG, "Connection lost.  Reason: Service Disconnected");
        }
        // Send the hint to Activity for UI updates
        ((MainActivity) mActivity).onFitStatusUpdated(false);
        // Attempt to reconnect
        connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.e(TAG, "Connection failed. Cause: " + result.toString());
        if (!result.hasResolution()) {
            // Show the localized error dialog
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), mActivity, 0).show();
            return;
        }
        // The failure has a resolution. Resolve it.
        // Called typically when the app is not yet authorized, and an
        // authorization dialog is displayed to the user.
        if (!mAuthInProgress && (result.getErrorCode() == ConnectionResult.SIGN_IN_REQUIRED
                || result.getErrorCode() == FitnessStatusCodes.NEEDS_OAUTH_PERMISSIONS)) {
            try {
                Utils.logDebug(TAG, "Attempting to resolve failed connection");
                mAuthInProgress = true;
                result.startResolutionForResult(mActivity, REQUEST_CODE_OAUTH);
            } catch (IntentSender.SendIntentException e) {
                mAuthInProgress = false;
                Log.e(TAG, "Exception while starting resolution activity", e);
            }
        }
    }
    /**
     * Starts a new session for Fit data. This will take care of registering all the sensors,
     * recording the sensor data, and registering the data set as a session to Google Fit.
     * @param dataTypeSettings Types of data to listen to, in an array.
     * @param sessionDescription The description of the session.
     * @param listener The OnDataPointListener to receive sensor events.
     */
    public void startFitDataSession(FitDataTypeSetting[] dataTypeSettings,
                                    String sessionDescription, OnDataPointListener listener) {
        for (FitDataTypeSetting dataTypeSetting : dataTypeSettings) {
            registerFitDataListener(dataTypeSetting, listener);
            startRecordingFitData(dataTypeSetting);
        }

        Session session = new Session.Builder()
                .setName(SESSION_NAME)
                .setDescription(sessionDescription)
                .setActivity(FitnessActivities.RUNNING_JOGGING)
                .setStartTime(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .build();

        PendingResult<Status> pendingResult =
                Fitness.SessionsApi.startSession(mGoogleApiClient, session);
        pendingResult.setResultCallback(new FitResultCallback<Status>(
                this, FitResultCallback.RegisterType.SESSION, null /* dataType */,
                true /* subscribe */));
    }

    /**
     * Ends the session for Fit data. This will take care of un-registering all the sensors,
     * stop recording the sensor data, and stop recording the data set as a session to Google Fit.
     * @param dataTypeSettings Types of data to listen to, in an array.
     * @param listener The OnDataPointListener to receive sensor events.
     */
    public void endFitDataSession(
            FitDataTypeSetting[] dataTypeSettings, OnDataPointListener listener) {
        if (mGoogleApiClient.isConnected()) {
            PendingResult<SessionStopResult> pendingResult =
                    Fitness.SessionsApi.stopSession(mGoogleApiClient, null);
            pendingResult.setResultCallback(new FitResultCallback<SessionStopResult>(
                    this, FitResultCallback.RegisterType.SESSION, null /* dataType */,
                    false /* subscribe */));

            for (FitDataTypeSetting dataTypeSetting : dataTypeSettings) {
                stopRecordingFitData(dataTypeSetting);
                unregisterFitDataListener(listener);
            }
        }
    }

    public boolean isSignedIn() {
        return (mGoogleApiClient != null && mGoogleApiClient.isConnected());
    }

    public void userAuthenticated() {
        mAuthInProgress = false;
    }

    public boolean isClientReady() {
        // Make sure all the required sensors are registered.
        boolean hasUnregisteredSensor = false;
        for (FitDataTypeSetting fitDataTypeSetting : sensorsAwaitingRegistration) {
            if (fitDataTypeSetting.isRequired()) {
                hasUnregisteredSensor = true;
                break;
            }
        }

        return (mGoogleApiClient.isConnected() && !hasUnregisteredSensor);
    }

    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    protected void sensorRegistered(DataType dataType) {
        for (FitDataTypeSetting fitDataTypeSetting : sensorsAwaitingRegistration) {
            if (fitDataTypeSetting.getDataType().equals(dataType)) {
                sensorsAwaitingRegistration.remove(fitDataTypeSetting);
                break;
            }

        }
    }

    /**
     * Add RecordingApi listener for recording to GoogleFit backend. Can be called repeatedly on
     * multiple data types.
     * @param dataTypeSetting Type of data to listen to.
     */
    private void startRecordingFitData(FitDataTypeSetting dataTypeSetting) {
        Fitness.RecordingApi.subscribe(mGoogleApiClient, dataTypeSetting.getDataType())
                .setResultCallback(new FitResultCallback<Status>(
                        this, FitResultCallback.RegisterType.RECORDING,
                        dataTypeSetting.getDataType(), true));
    }

    private void stopRecordingFitData(FitDataTypeSetting dataTypeSetting) {
        Fitness.RecordingApi.unsubscribe(mGoogleApiClient, dataTypeSetting.getDataType())
                .setResultCallback(new FitResultCallback<Status>(
                        this, FitResultCallback.RegisterType.RECORDING,
                        dataTypeSetting.getDataType(), false));
    }

    /**
     * Add SensorsApi listener for real-time display of sensor data. Can be called repeatedly on
     * multiple data types.
     * @param dataTypeSetting Type of data to listen to.
     * @param listener Listener for callbacks from SensorsApi.
     */
    private void registerFitDataListener(
            FitDataTypeSetting dataTypeSetting, OnDataPointListener listener) {
        sensorsAwaitingRegistration.add(dataTypeSetting);
        Fitness.SensorsApi.add(
                mGoogleApiClient,
                new SensorRequest.Builder()
                        .setDataType(dataTypeSetting.getDataType())
                        .setSamplingRate(dataTypeSetting.getSamplingRateSeconds(), TimeUnit.SECONDS)
                        .setAccuracyMode(dataTypeSetting.getAccuracyMode())
                        .build(),
                listener)
                .setResultCallback(new FitResultCallback<Status>(
                        this, FitResultCallback.RegisterType.SENSORS, dataTypeSetting.getDataType(),
                        true));
    }

    private void unregisterFitDataListener(OnDataPointListener listener) {
        Fitness.SensorsApi.remove(mGoogleApiClient, listener);
    }
}
