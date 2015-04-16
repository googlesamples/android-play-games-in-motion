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

import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import com.google.fpl.gim.examplegame.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Describes a Moment in which the user listens to a piece of fiction as part of the gameplay.
 * The fiction will be read aloud using Android's text-to-speech capabilities.
 */
public class SpokenTextMoment extends Moment {

    private static final String TAG = SpokenTextMoment.class.getSimpleName();

    private SpokenTextMomentData mData;

    private static final float RETRY_WAIT_TIME_SECONDS = 2.5f;
    // Buffer time before speaking with TextToSpeech.
    private static final long SILENCE_LENGTH_MILLIS = 500;

    // Determines behavior to execute during TextToSpeech speaking.
    private UtteranceProgressListener mUtteranceProgressListener = new UtteranceProgressListener() {
        @Override
        public void onStart(String utteranceId) {
        }

        @Override
        /**
         * Determines behavior when TextToSpeech has completed speaking, or stopped.
         */
        public void onDone(String utteranceId) {
            setIsDone(true);
            getMission().getService().endPlayback();
        }

        @Override
        public void onError(String utteranceId) {
        }
    };

    public SpokenTextMoment(Mission mission, SpokenTextMomentData data) {
        super(mission);
        this.mData = data;
    }

    @Override
    public void start(long nowNanos) {
        super.start(nowNanos);
        Utils.logDebug(TAG, "SpokenTextMoment \"" + mData.getMomentId() + "\" started.");

        if (getMission().getService().obtainAudioFocus()) {
            speak();
        } else {
            // Try again at a future time.
            restartWithDelay(nowNanos, RETRY_WAIT_TIME_SECONDS);
        }
    }

    @Override
    public void end() {
        Utils.logDebug(TAG, "SpokenTextMoment \"" + mData.getMomentId() + "\" ended.");
        getMission().getService().getTextToSpeech().setOnUtteranceProgressListener(null);
        getMission().getService().getTextToSpeech().stop();
    }

    public String getNextMomentId() {
        return mData.getNextMomentId();
    }

    /**
     * Use TextToSpeech to say the words associated with this Moment.
     */
    private void speak() {
        TextToSpeech textToSpeech = getMission().getService().getTextToSpeech();
        textToSpeech.setOnUtteranceProgressListener(mUtteranceProgressListener);
        textToSpeech.playSilence(SILENCE_LENGTH_MILLIS, TextToSpeech.QUEUE_ADD, null);
        HashMap<String, String> map = new HashMap<>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, mData.getMomentId());
        textToSpeech.speak(mData.getTextToSpeak(), TextToSpeech.QUEUE_ADD, map);
    }

    @Override
    public void restart(long nowNanos) {
        // Stop anything in progress.
        end();
        start(nowNanos);
    }

    public SpokenTextMomentData getMomentData() {
        return this.mData;
    }

    @Override
    public ArrayList<String> getFictionalProgress() {
        return mData.getFictionalProgress();
    }
}
