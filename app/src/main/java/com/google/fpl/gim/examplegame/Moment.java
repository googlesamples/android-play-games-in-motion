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
 * A Moment is a discrete event within a Mission. Moments know when they are finished ('isDone()'),
 * and they are never reused.
 */
public abstract class Moment {
    // The mission to which this moment belongs.
    private Mission mMission;

    public boolean mIsDone;
    private boolean mShouldRestart;
    private long mTimeWhenRestartRequestedNanos;
    private long mRestartDelayLengthNanos;

    /**
     * @param mission The Mission to which this moment belongs. Cannot be changed after
     *                instantiation - each Moment is irrevocably associated with a Mission.
     */
    protected Moment(Mission mission) {
        this.mMission = mission;
    }

    public Mission getMission() {
        return mMission;
    }

    /**
     * Called on the current moment to determine if it is appropriate to move on.
     * @return true if this moment no longer should be active as the current moment.
     */
    public boolean isDone() {
        return mIsDone;
    }

    public void setIsDone(boolean isDone) {
        this.mIsDone = isDone;
    }

    /**
     * Update the moment information for the current time. Default behavior checks if the Moment
     * should restart, and does.
     * @param nowNanos The current time, represented in nanoseconds.
     */
    public void update(long nowNanos) {
        if (mShouldRestart
                && nowNanos > mTimeWhenRestartRequestedNanos + mRestartDelayLengthNanos) {
            restart(nowNanos);
        }
    }

    /**
     * Read the id of the next Moment associated with this Moment.
     * @return String identifying the next Moment associated with this Moment.
     */

    public abstract String getNextMomentId();

    /**
     * Make this moment active. Runs when the moment begins.
     * @param nowNanos The current time, represented in nanoseconds.
     */
    public void start(long nowNanos) {
        this.mIsDone = false;
        this.mShouldRestart = false;
    }

    /**
     * Ends the current moment.
     */
    public abstract void end();

    /**
     * Restarts a moment.
     */
    public abstract void restart(long nowNanos);

    public void restartWithDelay(long nowNanos, float secondsDelayRestart) {
        mShouldRestart = true;
        mTimeWhenRestartRequestedNanos = nowNanos;
        mRestartDelayLengthNanos = Utils.secondsToNanos(secondsDelayRestart);
    }

    public abstract ArrayList<String> getFictionalProgress();
}
