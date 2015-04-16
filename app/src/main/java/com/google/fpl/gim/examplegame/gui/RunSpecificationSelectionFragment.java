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

package com.google.fpl.gim.examplegame.gui;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.fpl.gim.examplegame.MainActivity;
import com.google.fpl.gim.examplegame.R;

/**
 * A UI Fragment that represents the game's run specification selection screen.
 */
public class RunSpecificationSelectionFragment extends Fragment {

    private static final String TAG = RunSpecificationSelectionFragment.class.getSimpleName();

    // The minimum allowed challenge pace.
    private static final float CHALLENGE_PACE_MIN_MINUTES_PER_MILE = 8f;
    // The maximum allowed challenge pace.
    private static final float CHALLENGE_PACE_MAX_MINUTES_PER_MILE = 30f;
    // The range of the challenge pace.
    private static final float CHALLENGE_PACE_RANGE_MINUTES_PER_MILE =
            CHALLENGE_PACE_MAX_MINUTES_PER_MILE - CHALLENGE_PACE_MIN_MINUTES_PER_MILE;
    // The default challenge pace.
    private static final float CHALLENGE_PACE_DEFAULT_MINUTES_PER_MILE = 20f;

    private static final int CHALLENGE_PACE_INCREMENTS_PER_MINUTE = 4;

    // The maximum value of the challenge pace seek bar.
    private static final int CHALLENGE_PACE_SEEK_BAR_MAX = (int)
            CHALLENGE_PACE_RANGE_MINUTES_PER_MILE * CHALLENGE_PACE_INCREMENTS_PER_MINUTE;

    // The default value for the progress bar.(int)(
    private static final int CHALLENGE_PACE_PROGRESS_BAR_DEFAULT = (int) (
            (1.0f -
            (CHALLENGE_PACE_DEFAULT_MINUTES_PER_MILE - CHALLENGE_PACE_MIN_MINUTES_PER_MILE)
            / CHALLENGE_PACE_RANGE_MINUTES_PER_MILE) * CHALLENGE_PACE_SEEK_BAR_MAX);

    private static final float MISSION_LENGTH_MINUTES = 30.0f;
    private static final float INTERVAL_LENGTH_MINUTES = 1.5f;

    // SeekBar for selecting a challenge pace.
    SeekBar mChallengePaceSeekBar;
    // Text display of the current selected challenge pace.
    TextView mChallengePaceText;

    private float mSelectedMissionLengthMinutes;
    private float mSelectedIntervalLengthMinutes;
    private float mSelectedChallengePaceMinutesPerMile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.menu_run_specifications, container, false);

        mChallengePaceSeekBar = (SeekBar) rootView.findViewById(R.id.challenge_speed_field);
        mChallengePaceSeekBar.setMax(CHALLENGE_PACE_SEEK_BAR_MAX);
        mChallengePaceSeekBar.setProgress(CHALLENGE_PACE_PROGRESS_BAR_DEFAULT);
        mChallengePaceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateChallengePaceText(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mChallengePaceText = (TextView) rootView.findViewById(R.id.challenge_speed_text);
        updateChallengePaceText(CHALLENGE_PACE_PROGRESS_BAR_DEFAULT);

        return rootView;
    }

    @Override public void onResume() {
        super.onResume();
        ((MainActivity)getActivity()).setActionBarTitle(R.string.run_specification_title);
    }

    public void onEnterPressed() {
        // Currently does not query the user for a run length. Assume each mission is 30 minutes.
        mSelectedMissionLengthMinutes = MISSION_LENGTH_MINUTES;
        // Currently does not query the user for an interval length. Assume each interval is 1.5
        // minutes.
        mSelectedIntervalLengthMinutes = INTERVAL_LENGTH_MINUTES;
        mSelectedChallengePaceMinutesPerMile =
                calculateChallengePaceFromProgress(mChallengePaceSeekBar.getProgress());

        // Display MusicSelectionFragment.
        Fragment musicSelectionFragment =
                ((MainActivity) getActivity()).getGameViews().getMusicSelectionFragment();
        getActivity().getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                .remove(this)
                .add(R.id.container, musicSelectionFragment, GameViews.MUSIC_SELECTION_TAG)
                .addToBackStack(null)
                .commit();
    }


    /**
     * Displays the currently selected Challenge Pace based on SeekBar progress.
     * @param progress Int representing progress from 0 to CHALLENGE_PACE_SEEK_BAR_MAX.
     */
    private void updateChallengePaceText(int progress) {
        float minutesPerMile = calculateChallengePaceFromProgress(progress);
        String formattedText = String.format(getActivity()
                .getString(R.string.challenge_speed_display), minutesPerMile);
        mChallengePaceText.setText(formattedText);
    }

    /**
     * Calculates Challenge Pace based on SeekBar progress.
     * @param progress Int representing progress from 0 to CHALLENGE_PACE_SEEK_BAR_MAX.
     * @return A challenge pace in minutes per mile.
     */
    private float calculateChallengePaceFromProgress(int progress) {
        return CHALLENGE_PACE_MIN_MINUTES_PER_MILE
                + ((1.0f - ((float) progress) / CHALLENGE_PACE_SEEK_BAR_MAX)
                * CHALLENGE_PACE_RANGE_MINUTES_PER_MILE);
    }

    public float getSelectedMissionLengthMinutes() {
        return mSelectedMissionLengthMinutes;
    }

    public float getSelectedIntervalLengthMinutes() {
        return mSelectedIntervalLengthMinutes;
    }

    public float getSelectedChallengePaceMinutesPerMile() {
        return mSelectedChallengePaceMinutesPerMile;
    }
}
