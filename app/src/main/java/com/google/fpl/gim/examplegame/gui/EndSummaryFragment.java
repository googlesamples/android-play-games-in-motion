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
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.fpl.gim.examplegame.MainActivity;
import com.google.fpl.gim.examplegame.R;

import java.util.ArrayList;

/**
 * Displays post-run summary.
 */
public class EndSummaryFragment extends Fragment {
    private ListView mFictionalProgressList;
    private ListView mFitnessStatisticsList;
    private boolean mGoogleFitConnected = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.end_screen, container, false);
        mFictionalProgressList = (ListView) rootView.findViewById(R.id.fictionalProgressList);
        mFitnessStatisticsList = (ListView) rootView.findViewById(R.id.fitnessStatisticsList);
        return rootView;
    }

    @Override public void onResume() {
        super.onResume();
        if (mGoogleFitConnected) {
            ((MainActivity) getActivity()).setActionBarTitle(R.string.run_finish_text);
        } else {
            ((MainActivity) getActivity()).setActionBarTitle(R.string.fit_disconnected_text);
        }
    }

    public void displayStats(ArrayList<String> fictionalProgress,
            ArrayList<String> fitnessStatistics) {
        mFictionalProgressList.setAdapter(new ArrayAdapter<>(getActivity(),
                R.layout.menu_list_item, R.id.list_item_text, fictionalProgress));
        mFitnessStatisticsList.setAdapter(new ArrayAdapter<>(getActivity(),
                R.layout.menu_list_item, R.id.list_item_text, fitnessStatistics));
    }

    public void onFitStatusUpdated(boolean connected) {
        mGoogleFitConnected = connected;
    }
}
