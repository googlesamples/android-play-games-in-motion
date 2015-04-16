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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.fpl.gim.examplegame.MainActivity;
import com.google.fpl.gim.examplegame.Mission;
import com.google.fpl.gim.examplegame.R;

/**
 * Display fitness data during a run.
 */
public class FitnessDataDisplayFragment extends Fragment {

    private TextView mNumSteps;
    private TextView mMinutesPerMile;
    private TextView mTimeExercised;
    private TextView mWeaponChargedPercentage;
    private ProgressBar mProgressBar;
    private String mMissionName = "Mission";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.step_display, container, false);
        mNumSteps = (TextView) rootView.findViewById(R.id.num_steps_taken);
        mMinutesPerMile = (TextView) rootView.findViewById(R.id.minutes_per_mile);
        mTimeExercised = (TextView) rootView.findViewById(R.id.time_exercised);
        mWeaponChargedPercentage = (TextView) rootView.findViewById(R.id.percent_weapon_charged);

        mProgressBar = (ProgressBar) rootView.findViewById(R.id.circular_progress_bar);
        mProgressBar.setMax(100);
        mProgressBar.setProgress(0);

        return rootView;
    }

    @Override public void onResume() {
        super.onResume();
        ((MainActivity)getActivity()).setActionBarTitle(mMissionName);
    }

    public void setNumSteps(int numSteps) {
        mNumSteps.setText(String.format("%d steps", numSteps));
    }

    public void setMinutesPerMile(float minutesPerMile, float challengePace) {
        if (minutesPerMile >= Mission.getMaximumMinutesPerMile()) {
            mMinutesPerMile.setText(getActivity().getString(R.string.not_moving_speed));

        } else {
            String minutesPerMileText = String.format("%.2f\nmins/mile", minutesPerMile);
            mMinutesPerMile.setText(minutesPerMileText);
            if (minutesPerMile <= challengePace) {
                mMinutesPerMile.setTextColor(getResources().getColor(R.color.green));
            } else {
                mMinutesPerMile.setTextColor(getResources().getColor(R.color.red));
            }
        }
    }

    public void setTimeExercised(int numMinutes, int numSeconds) {
        mTimeExercised.setText(String.format("%d:%02d", numMinutes, numSeconds));
    }

    public void setWeaponChargedPercentage(int weaponChargedPercentage) {
        mWeaponChargedPercentage.setText(String.format("%d%%", weaponChargedPercentage));
        mProgressBar.setProgress(weaponChargedPercentage);
    }

    public void setFitnessStats(Mission mission) {
        setNumSteps(mission.getNumSteps());
        setMinutesPerMile(mission.getMinutesPerMile(), mission.getChallengePace());
        setTimeExercised(mission.getNumMinutesExercised(), mission.getNumSecondsExercised());
        setWeaponChargedPercentage(mission.getWeaponChargedPercentage());
    }

    public void setMissionName(String missionName) {
        mMissionName = missionName;
    }
}
