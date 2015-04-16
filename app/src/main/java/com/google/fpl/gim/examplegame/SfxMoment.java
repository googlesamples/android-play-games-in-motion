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

import android.media.MediaPlayer;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Describes a Moment in which the user listens to a pre-recorded sound effect as part of the
 * gameplay.
 */
public class SfxMoment extends Moment implements MediaPlayer.OnCompletionListener {

    private SfxMomentData mData;

    public SfxMoment(Mission mission, SfxMomentData data) {
        super(mission);
        this.mData = data;
    }

    @Override
    public void start(long nowNanos) {
        super.start(nowNanos);
        getMission().getService().queueSound(mData.getUriAsset(), this);
    }

    @Override
    public void end() {
    }

    @Override
    public String getNextMomentId() {
        return mData.getNextMomentId();
    }

    /**
     * We need to know when our specific sfx is done playing. Then we fallback to the default
     * onCompletionListener to finish cleaning up.
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        setIsDone(true);
        getMission().getService().onCompletion(mp);
    }

    @Override
    public void restart(long nowNanos) {
        getMission().getService().dequeueSound(mData.getUriAsset());
        // Try to start again.
        start(nowNanos);
    }

    public SfxMomentData getMomentData() {
        return this.mData;
    }

    @Override
    public ArrayList<String> getFictionalProgress() {
        return mData.getFictionalProgress();
    }
}
