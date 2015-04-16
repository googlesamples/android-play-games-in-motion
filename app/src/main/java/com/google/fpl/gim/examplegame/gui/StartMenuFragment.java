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
import android.widget.Button;

import com.google.fpl.gim.examplegame.MainActivity;
import com.google.fpl.gim.examplegame.R;
import com.google.fpl.gim.examplegame.utils.Utils;

/**
 * A UI fragment that represents the game's start screen.  No data is stored in this fragment.
 */
public class StartMenuFragment extends Fragment {
    private static final String TAG = StartMenuFragment.class.getSimpleName();

    private Button mStartButton;
    private boolean isGoogleFitConnected;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.menu_start, container, false);
        mStartButton = (Button) rootView.findViewById(R.id.start_button);
        updateStartButton();
        return rootView;
    }

    @Override public void onResume() {
        super.onResume();
        ((MainActivity)getActivity()).setActionBarTitle(R.string.app_name);
    }

    public void onStartButtonPressed() {
        Utils.logDebug(TAG, "onStartButtonPressed");

        // Display MissionSelectionFragment
        MissionSelectionFragment missionSelectionFragment =
                ((MainActivity) getActivity()).getGameViews().getListOfMissionsFragment();
        getActivity().getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                .remove(this)
                .add(R.id.container, missionSelectionFragment, GameViews.LIST_OF_MISSIONS_TAG)
                .addToBackStack(null)
                .commit();
    }

    public void onFitStatusUpdated(boolean connected) {
        isGoogleFitConnected = connected;
        updateStartButton();
    }

    private void updateStartButton() {
        if (isGoogleFitConnected) {
            mStartButton.setEnabled(true);
            mStartButton.setText(R.string.start_button_ready);
        } else {
            mStartButton.setEnabled(false);
            mStartButton.setText(R.string.start_button_not_connected);
        }
    }
}
