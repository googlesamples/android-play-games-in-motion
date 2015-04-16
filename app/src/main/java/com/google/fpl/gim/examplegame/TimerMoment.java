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

import com.google.fpl.gim.examplegame.utils.Utils;

import java.util.ArrayList;

/**
 * Describes a Moment in which there is a gap in gameplay for a given amount of time.
 */
public class TimerMoment extends Moment {
    private static final String TAG = TimerMoment.class.getSimpleName();
    private TimerMomentData mData;

    private long mStartTimeNanos;

    public TimerMoment(Mission mission, TimerMomentData data) {
        super(mission);
        this.mData = data;
    }

    @Override
    public void update(long nowNanos) {
        setIsDone(hasMomentTimeElapsed(nowNanos));
    }

    @Override
    public void start(long nowNanos) {
        super.start(nowNanos);
        Utils.logDebug(TAG, "TimerMoment \"" + mData.getMomentId() + "\" started.");
        setStartTimeNanos(nowNanos);
    }

    @Override
    public void end() {
        Utils.logDebug(TAG, "TimerMoment \"" + mData.getMomentId() + "\" ended.");
    }

    /**
     * Determines if the TimerMoment has elapsed.
     * @param nowNanos The current time.
     * @return True if the time since the TimerMoment started is greater than or equal to the length
     *         of the timer moment.
     */
    public boolean hasMomentTimeElapsed(long nowNanos) {
        return (nowNanos - mStartTimeNanos) >= Utils.minutesToNanos(mData.getLengthMinutes());
    }

    public void setStartTimeNanos(long startTimeNanos) {
        this.mStartTimeNanos = startTimeNanos;
    }

    @Override
    public String getNextMomentId() {
        return mData.getNextMomentId();
    }

    @Override
    public void restart(long nowNanos) {
        // No additional tear down or resetting necessary.
        start(nowNanos);
    }

    public TimerMomentData getMomentData() {
        return this.mData;
    }

    @Override
    public ArrayList<String> getFictionalProgress() {
        return mData.getFictionalProgress();
    }
}
