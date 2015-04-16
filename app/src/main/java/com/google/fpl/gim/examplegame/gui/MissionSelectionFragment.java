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
import android.app.ListFragment;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.fpl.gim.examplegame.MainActivity;
import com.google.fpl.gim.examplegame.utils.MissionParseException;
import com.google.fpl.gim.examplegame.utils.MissionParser;
import com.google.fpl.gim.examplegame.R;
import com.google.fpl.gim.examplegame.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * A UI fragment that represents the game's mission selection screen. No data is stored in this
 * fragment.
 */
public class MissionSelectionFragment extends ListFragment {

    private static final String TAG = MissionSelectionFragment.class.getSimpleName();

    private ArrayList<String> mAssetNames = new ArrayList<>();
    private ArrayList<String> mMissionNames = new ArrayList<>();

    private String mSelectedMissionName;
    private String mSelectedAssetPath;

    private static final String MISSION_ASSET_FOLDER_NAME = "missions";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mMissionNames.size() == 0) {
            // Needs access to a Context in order to access the assets folder.
            AssetManager assetManager = getActivity().getAssets();
            String[] missionFileNames = new String[0];
            try {
                // Obtain the files in assets/missions.
                missionFileNames = assetManager.list(MISSION_ASSET_FOLDER_NAME);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Open the missions to obtain plain text names.
            for (String missionFileName : missionFileNames) {
                // Open a stream for the mission.
                InputStream stream;
                try {
                    // All missions should be in the assets/missions folder.
                    ArrayList<String> subDirectories = new ArrayList<>();
                    subDirectories.add(missionFileName);
                    stream = assetManager
                            .open(Utils.makeFilePath(MISSION_ASSET_FOLDER_NAME, subDirectories));
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }

                // Find the mission name.
                String missionName;
                try {
                    missionName = MissionParser.getMissionName(stream);
                } catch (MissionParseException e) {
                    e.printStackTrace();
                    missionName = "Description failed.";
                }

                // Close the stream.
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mMissionNames.add(missionName);
                mAssetNames.add(missionFileName);
            }
        }
        setListAdapter(new ArrayAdapter<>(getActivity(), R.layout.menu_list_item,
                R.id.list_item_text, mMissionNames));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.menu_mission_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        onMissionSelected(position);
    }

    @Override public void onResume() {
        super.onResume();
        ((MainActivity)getActivity()).setActionBarTitle(R.string.select_mission_title);
    }

    public void onMissionSelected(int position) {
        Utils.logDebug(TAG, "onMissionSelected - Mission #" + (position + 1) +
                " has been selected!");

        mSelectedMissionName = mMissionNames.get(position);

        ArrayList<String> subDirectories = new ArrayList<>();
        subDirectories.add(mAssetNames.get(position));
        mSelectedAssetPath = Utils.makeFilePath(MISSION_ASSET_FOLDER_NAME, subDirectories);

        // Display RunSpecificationSelectionFragment.
        Fragment runSpecificationsFragment =
                ((MainActivity) getActivity()).getGameViews().getRunSpecificationsFragment();
        getActivity().getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                .remove(this)
                .add(R.id.container, runSpecificationsFragment, GameViews.RUN_SPECIFICATIONS_TAG)
                .addToBackStack(null)
                .commit();
    }

    public String getSelectedMissionName() {
        return mSelectedMissionName;
    }

    public String getSelectedAssetPath() {
        return mSelectedAssetPath;
    }
}