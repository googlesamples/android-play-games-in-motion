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

package com.google.fpl.gim.examplegame;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.fpl.gim.examplegame.gui.NotificationOptions;
import com.google.fpl.gim.examplegame.google.FitDataTypeSetting;
import com.google.fpl.gim.examplegame.google.GoogleApiClientWrapper;
import com.google.fpl.gim.examplegame.utils.MissionParseException;
import com.google.fpl.gim.examplegame.utils.MissionParser;
import com.google.fpl.gim.examplegame.utils.Utils;

import java.io.InputStream;
import java.util.ArrayList;

/**
 * A mission is a complete gameplay during which the exercising user will be challenged to defeat
 * fictional pursuers.  The user will have a weapon that can only be charged by running faster.
 */
public class Mission implements OnDataPointListener {

    private static final String TAG = Mission.class.getSimpleName();

    private static final FitDataTypeSetting[] TRACKED_DATA_TYPES = {
        new FitDataTypeSetting(
                true /* isRequired */, DataType.TYPE_STEP_COUNT_DELTA, 1 /* samplingRateSeconds */,
                SensorRequest.ACCURACY_MODE_DEFAULT),
        new FitDataTypeSetting(
                false /* isRequired */, DataType.TYPE_SPEED, 1 /* samplingRateSeconds */,
                SensorRequest.ACCURACY_MODE_HIGH),
    };

    private static final String UPDATE_FITNESS_STATS
            = "com.google.fpl.gim.examplegame.UPDATE_FITNESS_STATS";

    // ID that signifies the moment whose next is this is an ending moment.
    private static final String DEFAULT_END_ID = null;
    private MissionData mData;

    // Access to MainService to obtain and use Android Context.
    private MainService mService;
    private boolean mIsDone = false;
    private boolean mIsStarted = false;

    // Access to GoogleFitApiClient for fit data
    private GoogleApiClientWrapper mGoogleApiClientWrapper;

    // Fitness stats for the mission as a whole.
    private int mTotalNumStepsTaken = 0;
    private int mTotalNumIntervalsCompleted = 0;
    private long mMissionStartTimeNanos;

    // Fitness stats for a small portion of the mission.
    private int mNumStepsSinceBeginningOfSample = 0;
    private long mSampleStartTimeNanos;
    private float mCurrentAverageMinutesPerMile = 0f;
    private static final float AVERAGE_SPEED_SAMPLE_RATE_SECONDS = 10.0f;
    private static final float LENGTH_OF_RUNNING_STRIDE_FEET = 5.5f;
    private static final float MAXIMUM_MINUTES_PER_MILE = 1000f;

    private boolean mIsAtChallengePace = false;
    private long mTimestampStartOfChallengePaceNanos;

    private static final String AT_CHALLENGE_PACE_RESOURCE
            = "android.resource://com.google.fpl.gim.examplegame/raw/atchallengepace";
    private static final String NO_LONGER_AT_CHALLENGE_PACE_RESOURCE
            = "android.resource://com.google.fpl.gim.examplegame/raw/nolongeratchallengepace";
    private static final String WEAPON_CHARGED_RESOURCE
            = "android.resource://com.google.fpl.gim.examplegame/raw/weaponcharged";
    private final Uri AT_CHALLENGE_PACE_URI = Uri.parse(AT_CHALLENGE_PACE_RESOURCE);
    private final Uri NO_LONGER_AT_CHALLENGE_PACE_URI =
            Uri.parse(NO_LONGER_AT_CHALLENGE_PACE_RESOURCE);
    private final Uri WEAPON_CHARGED_URI = Uri.parse(WEAPON_CHARGED_RESOURCE);

    // The current time represented in nanoseconds.
    private long mNowNanos;

    private boolean mIsWeaponCharged = false;
    private float mLastWeaponCharge;
    private int mNumEnemiesDefeated = 0;

    private ArrayList<String> mOverallFictionalProgress = new ArrayList<>();

    public Mission(MissionData data) {
        this.mData = data;
    }

    /**
     * Makes the moment referred to by nextMomentId the current Moment. Checks if the game should
     * end by checking the nextMomentId.
     * @param nextMomentId The ID of the moment to make the current moment.
     */
    public void changeCurrentMoment(String nextMomentId, long now) {
        if (nextMomentId == DEFAULT_END_ID || nextMomentId.equals(DEFAULT_END_ID)) {
            mIsDone = true;
            return;
        }

        mData.setCurrentMomentId(nextMomentId);
        mData.getCurrentMoment().start(now);
    }

    /**
     * Loads the moments read from an xml file that define a mission into the mission data.
     * @param missionStream An input stream for an xml file.
     * @throws com.google.fpl.gim.examplegame.utils.MissionParseException Thrown when file parsing failed due to parser configuration,
     * input exceptions, or incorrectly structured file.
     */
    public void readMoments(InputStream missionStream) throws MissionParseException {
        // Exceptions may be thrown from MissionParser.parseMission
        MissionParser.parseMission(missionStream, this);
    }

    public void start() {
        mNowNanos = System.nanoTime();
        mMissionStartTimeNanos = mNowNanos;
        changeCurrentMoment(mData.getFirstMomentId(), mNowNanos);
        mIsStarted = true;
        mSampleStartTimeNanos = mNowNanos;
        mLastWeaponCharge = 0f;
        // Create the notification to notify the user of their current fitness statistics.
        postFitnessNotification(getFitnessNotificationTitle());
    }

    public void cleanup() {
        Utils.logDebug(TAG, mOverallFictionalProgress.toString());

        // Clean up the current moment.
        if (mData.getCurrentMoment() != null) {
            mData.getCurrentMoment().end();
        }

        NotificationManager notificationManager = (NotificationManager) getService()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(MainService.CHOICE_NOTIFICATION_ID);
        notificationManager.cancel(MainService.FITNESS_STATS_NOTIFICATION_ID);

        if (mGoogleApiClientWrapper != null) {
            mGoogleApiClientWrapper.endFitDataSession(TRACKED_DATA_TYPES, this);
            mGoogleApiClientWrapper = null;
        }
    }

    public void addMoment(String id, Moment moment) {
        this.mData.addMoment(id, moment);
    }

    public void setFirstMomentId(String firstMomentId) {
        this.mData.setFirstMomentId(firstMomentId);
    }

    public void update() {
        mNowNanos = System.nanoTime();

        // Calculate average speed at a consistent time interval.
        float timePassedSeconds = Utils.nanosToSeconds(mNowNanos - mSampleStartTimeNanos);
        if (timePassedSeconds >= AVERAGE_SPEED_SAMPLE_RATE_SECONDS) {
            calculateAverageMinutesPerMile();
        }

        Moment currentMoment = mData.getCurrentMoment();
        currentMoment.update(mNowNanos);
        if (currentMoment.isDone()) {
            mOverallFictionalProgress.addAll(currentMoment.getFictionalProgress());
            currentMoment.end();
            changeCurrentMoment(currentMoment.getNextMomentId(), mNowNanos);
        }

        if (mLastWeaponCharge != getWeaponChargedPercentage()) {
            mLastWeaponCharge = getWeaponChargedPercentage();
            // Create the notification to notify the user of their current fitness statistics.
            postFitnessNotification(getFitnessNotificationTitle());
        }

        Intent updateFitnessStatsIntent = new Intent();
        updateFitnessStatsIntent.setAction(UPDATE_FITNESS_STATS);
        getService().sendBroadcast(updateFitnessStatsIntent);
    }

    public void postFitnessNotification(String title) {
        NotificationOptions notificationOptions =
                NotificationOptions.getDefaultNotificationOptions();
        notificationOptions.setTitle(title);
        notificationOptions.setContent(getService()
                .getString(R.string.weapon_status_notification_text));
        notificationOptions.setNotificationId(MainService.FITNESS_STATS_NOTIFICATION_ID);
        notificationOptions.setPriorityAsHigh();
        notificationOptions.setNotificationDefaults(NotificationCompat.DEFAULT_LIGHTS);
        getService().postActionNotification(notificationOptions);
    }

    public void setService(MainService service) {
        this.mService = service;
    }

    public MainService getService() {
        return this.mService;
    }

    public boolean isDone() {
        return mIsDone;
    }

    public void prepare(GoogleApiClientWrapper googleApiClientWrapper) {
        Utils.logDebug(TAG, "Mission prepared.");

        // Start collecting Google Fit data
        mGoogleApiClientWrapper = googleApiClientWrapper;
        mGoogleApiClientWrapper.startFitDataSession(
                TRACKED_DATA_TYPES, getMissionData().getMissionName(), this);
    }

    public boolean isWeaponCharged() {
        return mIsWeaponCharged;
    }

    public MissionData getMissionData() {
        return this.mData;
    }

    /**
     * Restarts the current moment with a time delay.
     */
    public void restartMoment() {
        if (mData.getCurrentMoment() != null) {
            mData.getCurrentMoment().restartWithDelay(mNowNanos, 0f);
        }
    }

    public void applyOutcome(Outcome outcome) {
        if (outcome.numEnemiesDefeatedIncremented()) {
            mNumEnemiesDefeated++;
        }
        if (outcome.weaponChargeDepleted()) {
            mIsWeaponCharged = false;
        }
    }

    public ArrayList<String> getOverallFictionalProgress() {
        return mOverallFictionalProgress;
    }

    @Override
    public void onDataPoint(DataPoint dataPoint) {
        // If we get data before the mission has started, discard them.
        if (!mIsStarted) {
            return;
        }
        DataType dataType = dataPoint.getDataType();
        for (Field field : dataType.getFields()) {
            Value val = dataPoint.getValue(field);
            if (dataType.equals(DataType.TYPE_STEP_COUNT_DELTA)) {
                onStepTaken(val.asInt());
            } else if (dataType.equals(DataType.TYPE_SPEED)) {
                // Data comes in as meters per second, have to convert to minutes per mile.
                float speedMetersPerSeconds = val.asFloat();
                updateChallengePace(Utils.metersPerSecondToMinutesPerMile(speedMetersPerSeconds));
            }
        }
    }

    public void onStepTaken(int steps) {
        mNumStepsSinceBeginningOfSample += steps;
        mTotalNumStepsTaken += steps;
        Utils.logDebug(TAG,
                "Fit data update. You have now taken " + mTotalNumStepsTaken + " steps.");

        // Update UI whenever a step is taken
        Intent updateFitnessStatsIntent = new Intent();
        updateFitnessStatsIntent.setAction(UPDATE_FITNESS_STATS);
        getService().sendBroadcast(updateFitnessStatsIntent);
    }

    public int getNumSteps() {
        return this.mTotalNumStepsTaken;
    }

    public float getMinutesPerMile() {
        return this.mCurrentAverageMinutesPerMile;
    }

    public int getNumMinutesExercised() {
        long timePassedNanos = mNowNanos - mMissionStartTimeNanos;
        int timePassedSeconds = (int) Utils.nanosToSeconds(timePassedNanos);
        int timePassedMinutes = timePassedSeconds / Utils.MINUTES_TO_SECONDS_SCALE;
        return timePassedMinutes;
    }

    public int getNumSecondsExercised() {
        long timePassedNanos = mNowNanos - mMissionStartTimeNanos;
        int timePassedSeconds = (int) Utils.nanosToSeconds(timePassedNanos);
        return timePassedSeconds % Utils.MINUTES_TO_SECONDS_SCALE;
    }

    public int getWeaponChargedPercentage() {
        if (mIsWeaponCharged) {
            return 100;
        }
        if (!mIsAtChallengePace) {
            return 0;
        }
        long timeAtChallengePaceNanos = mNowNanos - mTimestampStartOfChallengePaceNanos;
        float timeAtChallengePaceMinutes = Utils.nanosToMinutes(timeAtChallengePaceNanos);
        float weaponChargedPercentage = timeAtChallengePaceMinutes
                / mData.getLengthOfIntervalMinutes() * 100;
        return (int) weaponChargedPercentage;
    }

    public float getChallengePace() {
        return mData.getChallengePaceMinutesPerMile();
    }

    public void onRebind() {
        // Update UI after app wakes up (after the Activity is rebound to the Service)
        Intent updateFitnessStatsIntent = new Intent();
        updateFitnessStatsIntent.setAction(UPDATE_FITNESS_STATS);
        getService().sendBroadcast(updateFitnessStatsIntent);
    }

    public static float getMaximumMinutesPerMile() {
        return MAXIMUM_MINUTES_PER_MILE;
    }

    public ArrayList<String> getFitnessStatistics() {
        ArrayList<String> fitnessStats = new ArrayList<>();

        String numStepsTaken = String.format(getService()
                .getString(R.string.fitness_stat_num_steps), mTotalNumStepsTaken);
        fitnessStats.add(numStepsTaken);

        String numIntervalsCompleted = String.format(getService()
                .getString(R.string.fitness_stat_num_intervals), mTotalNumIntervalsCompleted);
        fitnessStats.add(numIntervalsCompleted);

        return fitnessStats;
    }

    private void calculateAverageMinutesPerMile() {
        float timePassedSeconds = Utils.nanosToSeconds(mNowNanos - mSampleStartTimeNanos);
        float timePassedMinutes = Utils.secondsToMinutes(timePassedSeconds);
        float distanceTraveledFeet = mNumStepsSinceBeginningOfSample
                * LENGTH_OF_RUNNING_STRIDE_FEET;
        float distanceTraveledMiles = Utils.feetToMiles(distanceTraveledFeet);

        if (distanceTraveledMiles > 0) {
            updateChallengePace(timePassedMinutes / distanceTraveledMiles);
        } else {
            updateChallengePace(0.0f);
        }
    }

    private void updateChallengePace(float averageMinutesPerMile) {
        String updateText;
        if (averageMinutesPerMile > 0) {
            mCurrentAverageMinutesPerMile = averageMinutesPerMile;
            updateText = (int)(mCurrentAverageMinutesPerMile)
                    + getService().getString(R.string.log_message_minutes_per_mile);
        } else {
            mCurrentAverageMinutesPerMile = MAXIMUM_MINUTES_PER_MILE;
            updateText = getService().getString(R.string.log_message_you_are_not_moving);
        }

        Utils.logDebug(TAG, updateText);

        evaluateChallengePace();

        // Reset start time and number of steps for next average speed sample.
        mSampleStartTimeNanos = mNowNanos;
        mNumStepsSinceBeginningOfSample = 0;
    }

    private void evaluateChallengePace() {
        // Player is currently at challenge pace.
        if (mCurrentAverageMinutesPerMile <= mData.getChallengePaceMinutesPerMile()) {
            // At last check, player was not at challenge pace.
            if (!mIsAtChallengePace) {
                mService.queueSound(AT_CHALLENGE_PACE_URI, mService);

                mTimestampStartOfChallengePaceNanos = mNowNanos;
            }
            // Player has been running at challenge pace for enough time to charge their weapon.
            else if (!mIsWeaponCharged &&
                    Utils.nanosToMinutes(mNowNanos - mTimestampStartOfChallengePaceNanos)
                            >= mData.getLengthOfIntervalMinutes()) {
                mService.queueSound(WEAPON_CHARGED_URI, mService);

                mIsWeaponCharged = true;
                mTotalNumIntervalsCompleted++;
            }
            mIsAtChallengePace = true;
        }
        // Player is not currently at challenge pace.
        else {
            // At last check, player was at challenge pace.
            if (mIsAtChallengePace) {
                mService.queueSound(NO_LONGER_AT_CHALLENGE_PACE_URI, mService);
            }
            mIsAtChallengePace = false;
        }
    }

    private String getFitnessNotificationTitle() {
        return getWeaponChargedPercentage()
                + getService().getString(R.string.weapon_status_notification_title);
    }
}
