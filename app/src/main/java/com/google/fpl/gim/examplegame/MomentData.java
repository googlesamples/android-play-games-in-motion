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

import java.util.ArrayList;

/**
 * Encapsulates the data that is needed to define a unique Moment.
 */
public abstract class MomentData {

    // The id of this moment. Must be unique across the moments in the mission.
    private final String mMomentId;
    // The moment to go to next.
    private final String mNextMomentId;

    private ArrayList<String> mFictionalProgress;

    /**
     * Constructor to explicitly set all fields for a ChoiceMomentData.
     * @param momentId Identifier for the ChoiceMoment.
     * @param nextMomentId The moment following this one. Null if is the last moment in a mission.
     * @param fictionalProgress Fictional progress for this moment.
     */
    public MomentData(String momentId, String nextMomentId, ArrayList<String> fictionalProgress) {
        mMomentId = momentId;
        mNextMomentId = nextMomentId;
        mFictionalProgress = fictionalProgress;
    }

    public String getMomentId() {
        return mMomentId;
    }

    public String getNextMomentId() {
        return mNextMomentId;
    }

    public ArrayList<String> getFictionalProgress() {
        return mFictionalProgress;
    }
}
