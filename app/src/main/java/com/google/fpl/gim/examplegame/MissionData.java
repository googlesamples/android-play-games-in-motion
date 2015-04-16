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

import java.util.HashMap;

/**
 * Encapsulates all of the game data that is necessary to represent a mission.
 */
public class MissionData {

    private String mMissionName; // User-facing name of mission.
    // ID must be unique to each mission
    private String mMissionId;
    private float mLengthOfMissionMinutes;
    private float mLengthOfIntervalMinutes;
    private float mChallengePaceMinutesPerMile;
    private HashMap<String, Moment> mAllMoments;
    private String mFirstMomentId;
    private String mCurrentMomentId;

    /**
     * Most general constructor. Used when wanting to construct most pieces of MissionData
     * explicitly.
     * @param missionId String identifying the Mission.
     * @param lengthOfGameMinutes Total length of game.
     * @param lengthOfIntervalMinutes Length of a running interval.
     * @param allMoments HashMap containing all Moments that define this Mission.
     * @param firstMomentId The first Moment to be executed during the Mission.
     * @param currentMomentId The current Moment of the Mission.
     */
    public MissionData(String missionName, String missionId, float lengthOfGameMinutes,
            float lengthOfIntervalMinutes, float challengePaceMinutesPerMile,
            HashMap<String, Moment> allMoments, String firstMomentId, String currentMomentId) {
        this.mMissionName = missionName;
        this.mMissionId = missionId;
        this.mLengthOfMissionMinutes = lengthOfGameMinutes;
        this.mLengthOfIntervalMinutes = lengthOfIntervalMinutes;
        this.mChallengePaceMinutesPerMile = challengePaceMinutesPerMile;
        this.mAllMoments = allMoments;
        this.mFirstMomentId = firstMomentId;
        this.mCurrentMomentId = currentMomentId;
    }

    /**
     * Constructor used when populating MissionData with information from menu screens.
     * @param missionId String identifying the Mission.
     * @param lengthOfGameMinutes Total length of game.
     * @param lengthOfIntervalMinutes Length of a running interval.
     */
    public MissionData(String missionName, String missionId, float lengthOfGameMinutes,
            float lengthOfIntervalMinutes, float challengePaceMinutesPerMile) {
        this(missionName, missionId, lengthOfGameMinutes, lengthOfIntervalMinutes,
                challengePaceMinutesPerMile, new HashMap<String, Moment>(), null, null);
    }

    public String getMissionName() {
        return this.mMissionName;
    }

    public String getMissionId() {
        return this.mMissionId;
    }

    public float getLengthOfMissionMinutes() {
        return this.mLengthOfMissionMinutes;
    }

    public float getLengthOfIntervalMinutes() {
        return this.mLengthOfIntervalMinutes;
    }

    public float getChallengePaceMinutesPerMile() {
        return this.mChallengePaceMinutesPerMile;
    }

    public Moment getMomentFromId(String momentId) {
        return mAllMoments.get(momentId);
    }

    public String getCurrentMomentId() {
        return this.mCurrentMomentId;
    }

    public String getFirstMomentId() {
        return this.mFirstMomentId;
    }

    public void setCurrentMomentId(String currentMomentId) {
        this.mCurrentMomentId = currentMomentId;
    }

    public void addMoment(String momentId, Moment moment) {
        this.mAllMoments.put(momentId, moment);
    }

    public void setFirstMomentId(String firstMomentId) {
        this.mFirstMomentId = firstMomentId;
    }

    public Moment getCurrentMoment() {
        return mAllMoments.get(mCurrentMomentId);
    }

    public int getNumMoments() {
        return mAllMoments.size();
    }

}
