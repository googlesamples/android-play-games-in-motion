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

/**
 * A fragment notifying the user to go select music to listen to during their run.
 */
public class MusicSelectionFragment extends Fragment {
    private static final String TAG = MusicSelectionFragment.class.getSimpleName();

    private Button mReadyButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.menu_music_selection, container, false);
        mReadyButton = (Button) rootView.findViewById(R.id.start_mission_button);
        mReadyButton.setEnabled(true);
        mReadyButton.setText(R.string.start_mission_button);
        return rootView;
    }

    @Override public void onResume() {
        super.onResume();
        ((MainActivity)getActivity()).setActionBarTitle(R.string.select_music_title);
    }

    public void disableReadyButton() {
        mReadyButton.setEnabled(false);
        mReadyButton.setText(R.string.start_mission_pressed);
    }
}
