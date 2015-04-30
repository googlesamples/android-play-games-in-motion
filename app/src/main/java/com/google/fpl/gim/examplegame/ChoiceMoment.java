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
import android.support.v4.app.NotificationCompat;

import com.google.fpl.gim.examplegame.gui.NotificationOptions;
import com.google.fpl.gim.examplegame.utils.Utils;

import java.util.ArrayList;

/**
 * Describes a Moment in which the user is presented with a decision to make.
 */
public class ChoiceMoment extends Moment {

    private static final String TAG = ChoiceMoment.class.getSimpleName();
    public static final int MAXIMUM_NUM_OF_CHOICES = 3;
    public static final int MINIMUM_NUM_OF_CHOICES = 2;

    private static final String CHOICE_NOTIFICATION_ACTION_1
            = "com.google.fpl.gim.examplegame.CHOICE_NOTIFICATION_ACTION_1";
    private static final String CHOICE_NOTIFICATION_ACTION_2
            = "com.google.fpl.gim.examplegame.CHOICE_NOTIFICATION_ACTION_2";
    private static final String CHOICE_NOTIFICATION_ACTION_3
            = "com.google.fpl.gim.examplegame.CHOICE_NOTIFICATION_ACTION_3";
    private static final String CHOICE_ID_KEY
            = "com.google.fpl.gim.examplegame.CHOICE_ID_KEY";

    private static final String ICON_RESOURCE_FOLDER = "drawable";
    private static long[] VIBRATE_PATTERN = {0, 300, 100, 300, 100, 300};

    private ChoiceMomentData mData;
    private long mStartTimeNanos;
    private Choice mSelectedChoice = null;

    public ChoiceMoment (Mission mission, ChoiceMomentData data) {
        super(mission);
        this.mData = data;
    }

    public void update(long nowNanos) {
        Utils.logDebug(TAG, "ChoiceMoment \"" + mData.getMomentId() + "\" update.");
        if (!isDone() && hasTimeToMakeChoiceExpired(nowNanos)) {
            // Pick default choice
            if (noChoiceSelectedYet()) {
                selectChoice(mData.getDefaultChoiceId());
            }
            setIsDone(true);
        }
    }

    @Override
    public void start(long nowNanos) {
        super.start(nowNanos);
        Utils.logDebug(TAG, "ChoiceMoment \"" + mData.getMomentId() + "\" started.");
        setStartTimeNanos(nowNanos);

        // If the user's weapon is not charged, the choice to fire their weapon should not be
        // displayed.
        Choice[] choices = mData.getChoices();
        int numActions = mData.getNumChoices();
        if (!getMission().isWeaponCharged()) {
            numActions--;
        }

        // Create notification actions using the valid choices.
        NotificationCompat.Action[] actions = new NotificationCompat.Action[numActions];
        int index = 0;
        String[] allActions = {CHOICE_NOTIFICATION_ACTION_1, CHOICE_NOTIFICATION_ACTION_2,
                CHOICE_NOTIFICATION_ACTION_3};
        for (Choice choice : choices) {
            if (!choice.requiresChargedWeapon() || getMission().isWeaponCharged()) {
                // Bounds checked in MissionParser.java, which requires each choice moment to have
                // 2 or 3 choices associated with it.
                Intent actionIntent = new Intent(allActions[index]);
                actionIntent.putExtra(CHOICE_ID_KEY, choice.getChoiceId());

                String resourceName = choice.getDrawableResourceName();
                String packageName = getMission().getService().getPackageName();
                int resource = getMission().getService().getResources()
                        .getIdentifier(resourceName, ICON_RESOURCE_FOLDER, packageName);

                // If the resource does not exist, default to using application icon.
                if (resource == 0) {
                    resource = getMission().getService().getApplicationInfo().icon;
                }

                actions[index] = getMission().getService()
                        .makeNotificationAction(actionIntent, resource,
                        choice.getDescription());
                index++;
            }
        }

        // Create the notification to warn the user of an approaching enemy.
        NotificationOptions notificationOptions =
                NotificationOptions.getDefaultNotificationOptions();
        notificationOptions.setNotificationId(MainService.CHOICE_NOTIFICATION_ID);
        notificationOptions.setPriorityAsMax();
        notificationOptions.setActions(actions);
        notificationOptions.setNotificationDefaults(0);
        notificationOptions.setVibratePattern(VIBRATE_PATTERN);
        getMission().getService().postActionNotification(notificationOptions);
    }

    @Override
    public void end() {
        Utils.logDebug(TAG, "ChoiceMoment \"" + mData.getMomentId() + "\" ended.");
        // Remove choice notification.
        dismissNotification();
    }

    /**
     * The next moment is not defined for a ChoiceMoment until the user has selected a choice.
     * @return Returns null until a choice is made, then returns the nextMomentId.
     */
    @Override
    public String getNextMomentId() {
        if (mSelectedChoice == null) {
            return null;
        } else {
            return mSelectedChoice.getNextMomentId();
        }
    }

    @Override
    public void restart(long nowNanos) {
        start(nowNanos);
    }

    public ChoiceMomentData getMomentData() {
        return this.mData;
    }

    public void setStartTimeNanos(long startTimeNanos) {
        this.mStartTimeNanos = startTimeNanos;
    }

    public boolean hasTimeToMakeChoiceExpired(long nowNanos) {
        return (nowNanos - mStartTimeNanos) >= Utils.minutesToNanos(mData
                .getTimeoutLengthMinutes());
    }

    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(CHOICE_NOTIFICATION_ACTION_1)
                || intent.getAction().equals(CHOICE_NOTIFICATION_ACTION_2)
                || intent.getAction().equals(CHOICE_NOTIFICATION_ACTION_3)) {
            String choiceId = intent.getStringExtra(CHOICE_ID_KEY);
            selectChoice(choiceId);
        }
    }

    public synchronized void selectChoice(String choiceId) {
        if (!isDone()) {
            Utils.logDebug(TAG, "Choice with id \"" + choiceId + "\" selected.");
            mSelectedChoice = mData.getChoiceById(choiceId);
            getMission().applyOutcome(mSelectedChoice.getOutcome());
            setIsDone(true);
        }
    }

    public void dismissNotification() {
        NotificationManager notificationManager = (NotificationManager) getMission().getService()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(MainService.CHOICE_NOTIFICATION_ID);
    }

    public boolean noChoiceSelectedYet() {
        return mSelectedChoice == null;
    }

    @Override
    public ArrayList<String> getFictionalProgress() {
        ArrayList<String> progress = new ArrayList<>();
        progress.addAll(mData.getFictionalProgress());
        if (!noChoiceSelectedYet()) {
            progress.addAll(mSelectedChoice.getFictionalProgress());
        }
        return progress;
    }
}
