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

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.fpl.gim.examplegame.gui.FitnessDataDisplayFragment;
import com.google.fpl.gim.examplegame.gui.GameViews;
import com.google.fpl.gim.examplegame.gui.NotificationOptions;
import com.google.fpl.gim.examplegame.google.GoogleApiClientWrapper;
import com.google.fpl.gim.examplegame.utils.Utils;

import java.util.ArrayList;

/**
 * MainActivity class on the UI thread. It has a game handler for the game loop to execute on.
 */
public class MainActivity extends ActionBarActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    // Defines the action the BroadcastReceiver will receive.
    public static final String ENABLE_BACK
            = "com.google.fpl.gim.examplegame.ENABLE_BACK";
    public static final String MISSION_START
            = "com.google.fpl.gim.examplegame.MISSION_START";
    public static final String MISSION_END
            = "com.google.fpl.gim.examplegame.MISSION_END";
    private static final String UPDATE_FITNESS_STATS
            = "com.google.fpl.gim.examplegame.UPDATE_FITNESS_STATS";

    private MainService mMainService; // Service that runs the game logic.
    private GameViews mGameViews; // Container for all UI fragments.

    // Defines behavior when binding and unbinding mMainService.
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder binder) {
            if (binder == null) {
                throw new IllegalArgumentException("IBinder passed to onServiceConnected was null");
            }
            mMainService = ((MainService.MainBinder) binder).getService();
            mMainService.ConnectGoogleFitApiClient(MainActivity.this);
        }

        public void onServiceDisconnected(ComponentName className) {
            mMainService = null;
        }
    };

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Update the step display fragment to show the current number of steps that the user
            // has taken.
            if (intent.getAction().equals(UPDATE_FITNESS_STATS)) {
                if (mMainService != null) {
                    FitnessDataDisplayFragment fitnessDataDisplayFragment
                                = mGameViews.getFitnessDataDisplayFragment();
                    if (fitnessDataDisplayFragment.isVisible()) {
                        fitnessDataDisplayFragment.setFitnessStats(
                                mMainService.getCurrentMission());
                    }
                }
            }

            // Receives an intent that requests back button to be enabled.
            if (intent.getAction().equals(ENABLE_BACK)) {
                getFragmentManager().popBackStack();
            }

            // Receives an intent that requests end summary screen to display due to a mission end.
            if (intent.getAction().equals(MISSION_START)) {
                displayFitnessStats();
            }

            // Receives an intent that requests end summary screen to display due to a mission end.
            if (intent.getAction().equals(MISSION_END)) {
                checkDisplayEndScreen();
            }
        }
    };

    @Override
    public void onDestroy() {
        Utils.logDebug(TAG, "onDestroy");
        super.onDestroy();
        if (isFinishing()) {
            // Stop the service from running in the background if the app exits.
            Intent intent = new Intent(this, MainService.class);
            intent.setPackage(getPackageName());
            stopService(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            getFragmentManager().popBackStack();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GoogleApiClientWrapper.REQUEST_CODE_OAUTH) {
            if (resultCode == RESULT_OK) {
                // If the user authenticated, try to connect again
                mMainService.userAuthenticated();
                mMainService.reconnectGoogleApi();
            } else {
                // Ideally there's a fail case here, handled by our own UX flow.
                // Keeping it empty for the sample.
            }
        }
    }

    /**
     * Responsible for transitioning from the start menu UI to the mission selection menu UI
     * @param view The view that is invoking this method.
     */
    public void onStartButtonPressed(View view) {
        mGameViews.getStartMenuFragment().onStartButtonPressed();
    }

    public void onEnterPressed(View view) {
        mGameViews.getRunSpecificationsFragment().onEnterPressed();
    }

    public void onStartMissionPressed(View view) {
        // Get user selected run and mission information.
        String missionName = getGameViews().getListOfMissionsFragment().getSelectedMissionName();
        String assetPath = getGameViews().getListOfMissionsFragment().getSelectedAssetPath();
        float missionLengthMinutes =
                getGameViews().getRunSpecificationsFragment().getSelectedMissionLengthMinutes();
        float intervalLengthMinutes =
                getGameViews().getRunSpecificationsFragment().getSelectedIntervalLengthMinutes();
        float challengePaceMinutesPerMile =
                getGameViews().getRunSpecificationsFragment().getSelectedChallengePaceMinutesPerMile();

        loadAndStartMission(assetPath, missionName, missionLengthMinutes, intervalLengthMinutes,
                challengePaceMinutesPerMile);

        // Disable the button and show that we are registering sensors.
        getGameViews().getMusicSelectionFragment().disableReadyButton();
    }

    private void displayFitnessStats() {
        String missionName = getGameViews().getListOfMissionsFragment().getSelectedMissionName();
        Fragment stepDisplayFragment = getGameViews().getFitnessDataDisplayFragment();
        ((FitnessDataDisplayFragment)stepDisplayFragment).setMissionName(missionName);
        Fragment musicSelectionFragment = getGameViews().getMusicSelectionFragment();
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                .remove(musicSelectionFragment)
                .add(R.id.container, stepDisplayFragment, GameViews.FITNESS_DATA_DISPLAY_TAG)
                .addToBackStack(null)
                .commit();

        getFragmentManager().executePendingTransactions();

        // Disable home/back button on action bar.
        displayHomeUp(false);
    }

    public GameViews getGameViews() {
        return this.mGameViews;
    }

    public void loadAndStartMission(String missionFilePath, String missionName, float missionLength,
                                    float intervalLength, float challengePaceMinutesPerMile) {
        if (mMainService != null) {
            mMainService.loadAndStartMission(missionFilePath, missionName, missionLength,
                    intervalLength, challengePaceMinutesPerMile);
        }
    }

    public void displayHomeUp(boolean display) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(display);
        getSupportActionBar().setHomeButtonEnabled(display);
    }

    /**
     * Updates UI as Google Fit's connection status changes.
     */
    public void onFitStatusUpdated(boolean connected) {
        // Hint to the fragments that might need to update their displays.
        mGameViews.getStartMenuFragment().onFitStatusUpdated(connected);
        mGameViews.getEndSummaryFragment().onFitStatusUpdated(connected);

        // End a mission or pop user back to start menu if we are disconnected.
        if (!connected) {
            if (mMainService != null && mMainService.isMissionRunning()) {
                // If a mission is running, end it. No different than a mission ending on its own.
                mMainService.endMission();
            } else {
                // Jump back to the start screen.
                getFragmentManager().popBackStack(GameViews.START_MENU_TAG, 0);
            }

            if (mMainService != null) {
                // Post a notification.
                NotificationOptions notificationOptions =
                        NotificationOptions.getDefaultNotificationOptions();
                notificationOptions.setTitle(getResources().getString(
                        R.string.disconnection_notification_title));
                notificationOptions.setContent(getResources().getString(
                        R.string.disconnection_notification_content));
                notificationOptions.setNotificationId(MainService.FITNESS_DISCONNECT_NOTIFICATION_ID);
                notificationOptions.setPriorityAsHigh();
                notificationOptions.setNotificationDefaults(NotificationCompat.DEFAULT_LIGHTS);
                mMainService.postActionNotification(notificationOptions);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mMainService != null) {
            if (!mMainService.isMissionRunning() && canPressBackButton()) {
                // If not in a mission and not at the start, function like the in-app up button.
                getFragmentManager().popBackStack();
            } else if (mMainService.isMissionRunning()) {
                // If a mission is running, end it. No different than a mission ending on its own.
                mMainService.endMission();
            } else {
                // Will call finish() and end MainActivity.
                super.onBackPressed();
            }
        }
    }

    public void setActionBarTitle(int string_res_id) {
        getSupportActionBar().setTitle(string_res_id);
    }

    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    protected void onStart() {
        Utils.logDebug(TAG, "onStart");
        super.onStart();

        // Register the mReceiver with an intent filter for rendering requests.
        IntentFilter filter = new IntentFilter();
        filter.addAction(ENABLE_BACK);
        filter.addAction(MISSION_START);
        filter.addAction(MISSION_END);
        registerReceiver(mReceiver, filter);

        // Render the mReceiver with an intent filter for displaying an updated num steps
        filter = new IntentFilter();
        filter.addAction(UPDATE_FITNESS_STATS);
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Utils.logDebug(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Start service so it can run in bound and unbound state.
        Intent intent = new Intent(this, MainService.class);
        intent.setPackage(getPackageName());
        startService(intent);

        // create new fragments and initialize data
        if (savedInstanceState == null) {
            mGameViews = new GameViews();
            mGameViews.initializeFragments(this);
        } else {
            // restore old fragments and data
            mGameViews.restoreFragments(this);
        }

        displayHomeUp(canPressBackButton());

        getFragmentManager().addOnBackStackChangedListener(
                new FragmentManager.OnBackStackChangedListener() {
                    @Override
                    public void onBackStackChanged() {
                        displayHomeUp(canPressBackButton());
                    }
                });

    }

    @Override
    protected void onPause() {
        Utils.logDebug(TAG, "onPause");
        super.onPause();
        try {
            unregisterReceiver(mReceiver);
        } catch (IllegalArgumentException e) {
            Utils.logDebug(TAG, "Unable to unregister the Broadcast Receiver.");
        }
        // Unbind to allow service to run in the background.
        unbindService(mConnection);
    }

    @Override
    protected void onResume() {
        Utils.logDebug(TAG, "onResume");
        super.onResume();
        // Bind to the already running MainService so we can communicate with it.
        Intent intent = new Intent(this, MainService.class);
        intent.setPackage(getPackageName());
        bindService(intent, mConnection, Context.BIND_WAIVE_PRIORITY);
        // Handles screen being asleep during run. Will display end screen if the run is over.
        checkDisplayEndScreen();
    }

    @Override
    protected void onStop() {
        Utils.logDebug(TAG, "onStop");
        super.onStop();
    }

    /**
     * The back button should be active as long as there are fragment transactions in the back
     * stack. We always want the first fragment to be around so the back stack count needs to be
     * > 1.
     * @return Whether the back button should be active.
     */
    private boolean canPressBackButton() {
        return getFragmentManager().getBackStackEntryCount() > 1;
    }

    private void checkDisplayEndScreen() {
        if (mMainService != null) {
            if (mMainService.shouldDisplayEndScreen()) {
                displayEndScreen();
            }
        }
    }

    /**
     * Displays end run summary, with fictional and fitness results.
     */
    private void displayEndScreen() {
        getFragmentManager().popBackStack(GameViews.START_MENU_TAG, 0);
        getFragmentManager().beginTransaction()
                .replace(R.id.container, mGameViews.getEndSummaryFragment(),
                        GameViews.END_SUMMARY_TAG)
                .addToBackStack(null)
                .commit();
        getFragmentManager().executePendingTransactions();

        // Get results.
        ArrayList<String> fictionalProgress = new ArrayList<>();
        ArrayList<String> fitnessResults = new ArrayList<>();
        if (mMainService != null) {
            fictionalProgress.addAll(mMainService.getOverallFictionalProgress());
            fitnessResults.addAll(mMainService.getFitnessStatistics());
        }

        // Display results.
        mGameViews.getEndSummaryFragment().displayStats(fictionalProgress, fitnessResults);

        // Unlock first mission achievement
        if(mMainService.unlockAchievement(getString(R.string.ach_id_first_mission))) {
            Utils.logDebug(TAG, "Achievement Unlocked: First Mission");
        } else {
            Utils.logDebug(TAG, "Warning: could not unlock achievement, not connected");
        }

        if (mMainService != null) {
            mMainService.reset();
        }

        displayHomeUp(true);
    }
}
