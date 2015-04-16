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

import android.app.Activity;

import com.google.fpl.gim.examplegame.R;
import com.google.fpl.gim.examplegame.gui.EndSummaryFragment;
import com.google.fpl.gim.examplegame.gui.FitnessDataDisplayFragment;
import com.google.fpl.gim.examplegame.gui.MissionSelectionFragment;
import com.google.fpl.gim.examplegame.gui.MusicSelectionFragment;
import com.google.fpl.gim.examplegame.gui.RunSpecificationSelectionFragment;
import com.google.fpl.gim.examplegame.gui.StartMenuFragment;

/**
 * Container for the game's UI Fragments. This class is implemented as a singleton to ensure that
 * only one instance of the GameViews class ever exists.
 */
public class GameViews {

    // Tags used to save/restore Fragments.
    public static final String START_MENU_TAG =
            "com.google.fpl.gim.examplegame.fragment.StartMenuFragment";
    public static final String LIST_OF_MISSIONS_TAG =
            "com.google.fpl.gim.examplegame.ListOfMissionsFragment";
    public static final String RUN_SPECIFICATIONS_TAG =
            "com.google.fpl.gim.examplegame.RunSpecificationsFragment";
    public static final String MUSIC_SELECTION_TAG =
            "com.google.fpl.gim.examplegame.fragment.MusicSelectionFragment";
    public static final String END_SUMMARY_TAG =
            "com.google.fpl.gim.examplegame.fragment.EndSummaryFragment";
    public static final String FITNESS_DATA_DISPLAY_TAG =
            "com.google.fpl.gim.examplegame.fragment.FitnessDataDisplayFragment";

    // UI Fragments.
    private StartMenuFragment mStartMenuFragment;
    private MissionSelectionFragment mMissionSelectionFragment;
    private RunSpecificationSelectionFragment mRunSpecificationsFragment;
    private MusicSelectionFragment mMusicSelectionFragment;
    private EndSummaryFragment mEndSummaryFragment;
    private FitnessDataDisplayFragment mFitnessDataDisplayFragment;

    public GameViews() {
    }

    /**
     * Called the first time upon starting the game by the 'initializeFragments' method in the Game
     * class. Creates fragments and attaches them to the Activity.
     */
    public void initializeFragments(Activity activity) {
        mStartMenuFragment = new StartMenuFragment();
        activity.getFragmentManager().beginTransaction()
                .add(R.id.container, mStartMenuFragment, START_MENU_TAG)
                .addToBackStack(START_MENU_TAG)
                .commit();
        mStartMenuFragment.setRetainInstance(true);

        mMissionSelectionFragment = new MissionSelectionFragment();
        mMissionSelectionFragment.setRetainInstance(true);

        mRunSpecificationsFragment = new RunSpecificationSelectionFragment();
        mRunSpecificationsFragment.setRetainInstance(true);

        mMusicSelectionFragment = new MusicSelectionFragment();
        mMusicSelectionFragment.setRetainInstance(true);

        mEndSummaryFragment = new EndSummaryFragment();
        mEndSummaryFragment.setRetainInstance(true);

        mFitnessDataDisplayFragment = new FitnessDataDisplayFragment();
        mFitnessDataDisplayFragment.setRetainInstance(true);
    }

    /**
     * Restores information and fragments from the Bundle upon lifecycle restart. Called
     * by the 'restoreFragments' method in the Game class.
     */
    public void restoreFragments(Activity activity) {
        StartMenuFragment foundStartMenuFragment = (StartMenuFragment) activity.getFragmentManager()
                .findFragmentByTag(START_MENU_TAG);
        if (foundStartMenuFragment != null) {
            mStartMenuFragment = foundStartMenuFragment;
        } else {
            mStartMenuFragment = new StartMenuFragment();
        }

        MissionSelectionFragment foundMissionSelectionFragment =
                (MissionSelectionFragment) activity.getFragmentManager()
                .findFragmentByTag(LIST_OF_MISSIONS_TAG);
        if (foundMissionSelectionFragment != null) {
            mMissionSelectionFragment = foundMissionSelectionFragment;
        } else {
            mMissionSelectionFragment = new MissionSelectionFragment();
        }

        RunSpecificationSelectionFragment foundRunSpecificationSelectionFragment =
                (RunSpecificationSelectionFragment) activity.getFragmentManager()
                .findFragmentByTag(RUN_SPECIFICATIONS_TAG);
        if (foundRunSpecificationSelectionFragment != null) {
            mRunSpecificationsFragment = foundRunSpecificationSelectionFragment;
        } else {
            mRunSpecificationsFragment = new RunSpecificationSelectionFragment();
        }

        MusicSelectionFragment foundMusicSelectionFragment =
                (MusicSelectionFragment) activity.getFragmentManager()
                .findFragmentByTag(MUSIC_SELECTION_TAG);
        if (foundMissionSelectionFragment != null) {
            mMusicSelectionFragment = foundMusicSelectionFragment;
        } else {
            mMusicSelectionFragment = new MusicSelectionFragment();
        }

        EndSummaryFragment foundEndSummaryFragment =
                (EndSummaryFragment) activity.getFragmentManager()
                        .findFragmentByTag(END_SUMMARY_TAG);
        if (foundEndSummaryFragment != null) {
            mEndSummaryFragment = foundEndSummaryFragment;
        } else {
            mEndSummaryFragment = new EndSummaryFragment();
        }

        FitnessDataDisplayFragment foundFitnessDataDisplayFragment =
                (FitnessDataDisplayFragment) activity.getFragmentManager()
                        .findFragmentByTag(FITNESS_DATA_DISPLAY_TAG);
        if (foundFitnessDataDisplayFragment != null) {
            mFitnessDataDisplayFragment = foundFitnessDataDisplayFragment;
        } else {
            mFitnessDataDisplayFragment = new FitnessDataDisplayFragment();
        }
    }

    public StartMenuFragment getStartMenuFragment() {
        return this.mStartMenuFragment;
    }

    public MissionSelectionFragment getListOfMissionsFragment() {
        return mMissionSelectionFragment;
    }

    public RunSpecificationSelectionFragment getRunSpecificationsFragment() {
        return mRunSpecificationsFragment;
    }

    public MusicSelectionFragment getMusicSelectionFragment() {
        return mMusicSelectionFragment;
    }

    public EndSummaryFragment getEndSummaryFragment() {
        return mEndSummaryFragment;
    }

    public FitnessDataDisplayFragment getFitnessDataDisplayFragment() {
        return mFitnessDataDisplayFragment;
    }
}
