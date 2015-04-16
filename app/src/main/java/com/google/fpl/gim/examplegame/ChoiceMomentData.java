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
import java.util.HashMap;

/**
 * Encapsulates the data that is needed to define a unique ChoiceMoment.
 */
public class ChoiceMomentData extends MomentData {

    // The text describing the choice the player must make.
    private final String mDescription;
    // All of the choices that the user can have.
    private final HashMap<String, Choice> mChoices;
    // Identifies the default Choice to execute in the case of a timeout.
    private final String mDefaultChoiceId;
    // Time in minutes until the default Choice is executed.
    private final float mTimeoutLengthMinutes;

    /**
     * Constructor to explicitly set all fields for a ChoiceMomentData.
     * @param momentId Identifier for the ChoiceMoment.
     * @param fictionalProgress Fictional progress for this moment.
     * @param description Text description for the decision to be made.
     * @param choices HashMap of Choices available to choose.
     * @param defaultChoiceId Default Choice in case of timeout.
     * @param timeoutLengthMinutes Length of time until timeout.
     */
    public ChoiceMomentData(String momentId, ArrayList<String> fictionalProgress,
            String description, HashMap<String, Choice> choices, String defaultChoiceId,
            float timeoutLengthMinutes) {
        super(momentId, null, fictionalProgress);
        mDescription = description;
        mChoices = choices;
        mDefaultChoiceId = defaultChoiceId;
        mTimeoutLengthMinutes = timeoutLengthMinutes;
    }

    /**
     * Constructor for information from XML.
     * @param momentId Identifier for the ChoiceMoment.
     * @param fictionalProgress Fictional progress for this moment.
     * @param description Text description for the decision to be made.
     * @param defaultChoiceId Default Choice in case of timeout.
     * @param timeoutLengthMinutes Length of time until timeout.
     */
    public ChoiceMomentData(String momentId, ArrayList<String> fictionalProgress,
            String description, String defaultChoiceId, float timeoutLengthMinutes) {
        this(momentId, fictionalProgress, description, new HashMap<String, Choice>(),
                defaultChoiceId, timeoutLengthMinutes);
    }

    public String getText() {
        return mDescription;
    }

    public Choice[] getChoices() {
        return mChoices.values().toArray(new Choice[mChoices.size()]);
    }

    public Choice getChoiceById(String choiceId) {
        return mChoices.get(choiceId);
    }

    public int getNumChoices() {
        return mChoices.size();
    }

    public void addChoice(Choice choice) {
        mChoices.put(choice.getChoiceId(), choice);
    }

    public String getDefaultChoiceId() {
        return mDefaultChoiceId;
    }

    public float getTimeoutLengthMinutes() {
        return mTimeoutLengthMinutes;
    }
}
