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
 * Describes an option that a user has when presented with a decision to make.
 * Choices are only meaningful when there are 2+ of them.
 */
public class Choice {
    // Unique identifier for this Choice.
    private String mChoiceId;
    // The description of this option.
    private String mDescription;
    // The moment to go to next.
    private String mNextMomentId;
    // The set of changes to make if the player chooses this option.
    private Outcome mOutcome;
    // Whether or not this Choice requires a charged weapon.
    private boolean mRequiresChargedWeapon;
    // Fictional Progress associated with this choice.
    private ArrayList<String> mFictionalProgress;
    // The name of the resource icon to display for this Choice's action.
    private String mDrawableResourceName;

    public Choice(String choiceId, String text, String nextMomentId, Outcome outcome,
                  boolean requiresChargedWeapon, ArrayList<String> fictionalProgress,
                  String drawableResourceName) {
        mChoiceId = choiceId;
        mDescription = text;
        mNextMomentId = nextMomentId;
        mOutcome = outcome;
        mRequiresChargedWeapon = requiresChargedWeapon;
        mFictionalProgress = fictionalProgress;
        mDrawableResourceName = drawableResourceName;
    }

    public String getChoiceId() {
        return mChoiceId;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getNextMomentId() {
        return mNextMomentId;
    }

    public Outcome getOutcome() {
        return mOutcome;
    }

    public ArrayList<String> getFictionalProgress() {
        return mFictionalProgress;
    }

    public boolean requiresChargedWeapon() {
        return mRequiresChargedWeapon;
    }

    public String getDrawableResourceName() {
        return mDrawableResourceName;
    }
}
