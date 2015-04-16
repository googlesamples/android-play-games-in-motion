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
 * Encapsulates the data that is needed to define a unique FictionAudioMoment.
 */
public class SpokenTextMomentData extends MomentData {
    // The text to be read aloud using text-to-speech
    private final String mTextToSpeak;

    /**
     * Constructor to explicitly set all fields for a ChoiceMomentData.
     * @param momentId Identifier for the ChoiceMoment.
     * @param nextMomentId The moment following this one. Null if is the last moment in a mission.
     * @param fictionalProgress Fictional progress for this moment.
     * @param textToSpeak Words associated with this moment.
     */
    public SpokenTextMomentData(String momentId, String nextMomentId,
        ArrayList<String> fictionalProgress, String textToSpeak) {
        super(momentId, nextMomentId, fictionalProgress);
        mTextToSpeak = textToSpeak;
    }

    public String getTextToSpeak() {
        return mTextToSpeak;
    }
}
