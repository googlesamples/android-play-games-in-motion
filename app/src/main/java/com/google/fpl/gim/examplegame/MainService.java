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

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.Toast;

import com.google.android.gms.games.Games;
import com.google.fpl.gim.examplegame.gui.GameViews;
import com.google.fpl.gim.examplegame.gui.NotificationOptions;
import com.google.fpl.gim.examplegame.google.GoogleApiClientWrapper;
import com.google.fpl.gim.examplegame.utils.MissionParseException;
import com.google.fpl.gim.examplegame.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;

/**
 * This is a Runnable for executing on the UI thread, and will add itself back
 * to the UI thread handler at the end of the run() function.
 *
 * While it will block the UI thread while running, it shouldn't block for that long.
 * We could always make a game thread if needed.
 */
public class MainService extends Service implements Runnable, MediaPlayer.OnCompletionListener {

    private final IBinder mBinder = new MainBinder();
    private static final String TAG = MainService.class.getSimpleName();

    private static final String CHOICE_NOTIFICATION_ACTION_1
            = "com.google.fpl.gim.examplegame.CHOICE_NOTIFICATION_ACTION_1";
    private static final String CHOICE_NOTIFICATION_ACTION_2
            = "com.google.fpl.gim.examplegame.CHOICE_NOTIFICATION_ACTION_2";
    private static final String CHOICE_NOTIFICATION_ACTION_3
            = "com.google.fpl.gim.examplegame.CHOICE_NOTIFICATION_ACTION_3";

    // Ids for notifications.
    public static final int CHOICE_NOTIFICATION_ID = 1;
    public static final int FITNESS_STATS_NOTIFICATION_ID = 2;
    public static final int FITNESS_DISCONNECT_NOTIFICATION_ID = 3;

    private static final Locale DEFAULT_TEXT_TO_SPEECH_LOCALE = Locale.UK;

    private Mission mMission; // The mission being played. Has reference to current game state.

    private static final long DELAY_MILLIS = 1000; // Time between updates, used as Handler delay.
    private Handler mUpdateHandler = new Handler();

    // Audio related modules.
    private TextToSpeech mTextToSpeech;
    private boolean mIsTextToSpeechReady = false;
    private AudioManager mAudioManager;
    private AudioManager.OnAudioFocusChangeListener mAudioFocusChangeListener;
    private MediaPlayer mMediaPlayer;

    // A queue of Audio Uris to be played.
    private class AudioQueueItem{
        Uri mUri;
        MediaPlayer.OnCompletionListener mListener;

        AudioQueueItem(Uri uri, MediaPlayer.OnCompletionListener listener) {
            mUri = uri;
            mListener = listener;
        }

        @Override
        public boolean equals(Object o) {
            if (getClass() != o.getClass())
                return false;
            AudioQueueItem audioQueueItem = (AudioQueueItem) o;
            return (mUri.equals(audioQueueItem.mUri));
        }
    }
    private Queue<AudioQueueItem> mAudioQueue = new LinkedList<>();

    private enum State {
        UNINITIALIZED,
        MISSION_LOADED,
        MISSION_RUNNING,
        END_SCREEN
    }
    private State mState = State.UNINITIALIZED;

    private GoogleApiClientWrapper mGoogleApiClientWrapper =
            new GoogleApiClientWrapper(); // Container for the GoogleApiClient

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mMission != null && mMission.getMissionData().getCurrentMoment() != null) {
                // Choice moment handles choice selection.
                ((ChoiceMoment) mMission.getMissionData().getCurrentMoment())
                        .onReceive(context, intent);
            }
        }
    };

    /**
     * This is the main game loop. Whenever it is done, it adds itself back to the handler.
     */
    @Override
    public void run() {
        if (mState == State.MISSION_LOADED || mState == State.MISSION_RUNNING) {
            // This is where we can call the game state and the game logic.
            update();
        }
        mUpdateHandler.postDelayed(this, DELAY_MILLIS);
    }

    public void userAuthenticated() {
        mGoogleApiClientWrapper.userAuthenticated();
    }

    public void reconnectGoogleApi() {
        Utils.logDebug(TAG, "Reconnecting to Google API.");
        mGoogleApiClientWrapper.connect();
    }

    public void ConnectGoogleFitApiClient(Activity activity) {
        mGoogleApiClientWrapper.buildGoogleApiClient(activity);
        mGoogleApiClientWrapper.connect();
    }

    /**
     * Unlock a Play Games achievement.
     *
     * @param achievementId the ID of the achievement from the Google Play Developer Console,
     * @return true if Achievement unlocked, false otherwise.
     */
    public boolean unlockAchievement(String achievementId) {
        if (mGoogleApiClientWrapper.isSignedIn()) {
            Games.Achievements.unlock(mGoogleApiClientWrapper.getGoogleApiClient(), achievementId);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Loads and begins a mission.
     */
    public void loadAndStartMission(String missionFilePath, String missionName,
                                    float missionLengthMinutes, float intervalLengthMinutes,
                                    float challengePaceMinutesPerMile) {
        if (!canEnterState(State.MISSION_LOADED)) {
            return;
        }
        MissionData data = new MissionData(missionName, missionFilePath, missionLengthMinutes,
                intervalLengthMinutes, challengePaceMinutesPerMile);
        mMission = new Mission(data);
        mMission.setService(this);

        // Open an InputStream from the given missionFileName.
        InputStream missionStream;
        try {
            missionStream = getAssets().open(missionFilePath);
        } catch (IOException e) {
            e.printStackTrace();
            requestReselection();
            return;
        }

        // Load the Moments.
        try {
            mMission.readMoments(missionStream);
        } catch (MissionParseException e) {
            e.printStackTrace();
            requestReselection();
            return;
        }

        try {
            missionStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        startMission();
    }

    /**
     * Starts a mission.
     */
    private void startMission() {
        setAndInitNextState(State.MISSION_LOADED);
    }

    /**
     * Ends a mission by halting updates.
     */
    public void endMission() {
        setAndInitNextState(State.END_SCREEN);
    }

    @Override
    public void onCreate() {
        // The service is being created.
        Utils.logDebug(TAG, "onCreate");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CHOICE_NOTIFICATION_ACTION_1);
        intentFilter.addAction(CHOICE_NOTIFICATION_ACTION_2);
        intentFilter.addAction(CHOICE_NOTIFICATION_ACTION_3);
        registerReceiver(mReceiver, intentFilter);

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        // Determines the behavior for handling Audio Focus surrender.
        mAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT
                        || focusChange == AudioManager.AUDIOFOCUS_LOSS) {

                    if (mTextToSpeech.isSpeaking()) {
                        mTextToSpeech.setOnUtteranceProgressListener(null);
                        mTextToSpeech.stop();
                    }

                    if (mMediaPlayer.isPlaying()) {
                        mMediaPlayer.stop();
                    }

                    // Abandon Audio Focus, if it's requested elsewhere.
                    mAudioManager.abandonAudioFocus(mAudioFocusChangeListener);

                    // Restart the current moment if AudioFocus was lost. Since AudioFocus is only
                    // requested away from this application if this application was using it,
                    // only Moments that play sound will restart in this way.
                    if (mMission != null) {
                        mMission.restartMoment();
                    }
                }
            }
        };

        // Asynchronously prepares the TextToSpeech.
        mTextToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    // Check if language is available.
                    switch (mTextToSpeech.isLanguageAvailable(DEFAULT_TEXT_TO_SPEECH_LOCALE)) {
                        case TextToSpeech.LANG_AVAILABLE:
                        case TextToSpeech.LANG_COUNTRY_AVAILABLE:
                        case TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE:
                            Utils.logDebug(TAG, "TTS locale supported.");
                            mTextToSpeech.setLanguage(DEFAULT_TEXT_TO_SPEECH_LOCALE);
                            mIsTextToSpeechReady = true;
                            break;
                        case TextToSpeech.LANG_MISSING_DATA:
                            Utils.logDebug(TAG, "TTS missing data, ask for install.");
                            Intent installIntent = new Intent();
                            installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                            startActivity(installIntent);
                            break;
                        default:
                            Utils.logDebug(TAG, "TTS local not supported.");
                            break;
                    }
                }
            }
        });

        mMediaPlayer = new MediaPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // The service is starting, due to a call to startService()
        Utils.logDebug(TAG, "onStartCommand");
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // A client is binding to the service with bindService()
        Utils.logDebug(TAG, "onBind");

        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // All clients have unbound with unbindService()
        Utils.logDebug(TAG, "onUnbind");
        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        // A client is binding to the service with bindService(),
        // after onUnbind() has already been called
        Utils.logDebug(TAG, "onRebind");

        if (mMission != null) {
            mMission.onRebind();
        }
    }

    @Override
    public void onDestroy() {
        // The service is no longer used and is being destroyed.
        Utils.logDebug(TAG, "onDestroy");
        mGoogleApiClientWrapper.disconnect();
        if (mIsTextToSpeechReady) {
            mTextToSpeech.shutdown();
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
        }
        mUpdateHandler.removeCallbacks(this);
        unregisterReceiver(mReceiver);
        if (mMission != null) {
            mMission.cleanup();
        }
    }

    /**
     * Callback listener for MediaPlayer.
     * @param player MediaPlayer instance.
     */
    @Override
    public void onCompletion(MediaPlayer player) {
        endPlayback();
    }

    /**
     * A Binder for the connection between MainService and MainActivity that allows MainActivity
     * to get the running instance of MainService.
     */
    public class MainBinder extends Binder {
        MainService getService() {
            return MainService.this;
        }
    }

    /**
     * Create a notification action that upon selection triggers the provided action.
     * @param intent Intent to carry out when the notification action is selected.
     * @param actionIconResourceId Resource Id of icon for this action.
     * @param actionDescription Name of this action.
     * @return A notification action that can be selected.
     */
    public NotificationCompat.Action makeNotificationAction(Intent intent,
            int actionIconResourceId, String actionDescription) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        return new NotificationCompat.Action(actionIconResourceId,
                actionDescription, pendingIntent);
    }

    /**
     * Builds and posts a notification from a set of options.
     * @param options The options to build the notification.
     */
    public void postActionNotification(NotificationOptions options) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(options.getSmallIconResourceId());
        builder.setContentTitle(options.getTitle());
        builder.setContentText(options.getContent());
        builder.setDefaults(options.getNotificationDefaults());
        builder.setPriority(options.getNotificationPriority());
        builder.setVibrate(options.getVibratePattern());
        if (options.getActions() != null) {
            for (NotificationCompat.Action action : options.getActions()) {
                builder.addAction(action);
            }
        }
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);
        notificationManager.notify(options.getNotificationId(), builder.build());
    }

    public boolean isMissionRunning() {
        return mState == State.MISSION_RUNNING;
    }

    public boolean shouldDisplayEndScreen() {
        return mState == State.END_SCREEN;
    }

    public void reset() {
        setAndInitNextState(State.UNINITIALIZED);
    }

    public ArrayList<String> getOverallFictionalProgress() {
        return mMission.getOverallFictionalProgress();
    }

    /**
     * Gets fitness statistics for the last played game.
     * @return An array list of fitness statistics to display.
     */
    public ArrayList<String> getFitnessStatistics() {
        return getCurrentMission().getFitnessStatistics();
    }

    public Mission getCurrentMission() {
        return this.mMission;
    }

    /**
     * Queue a sound into the audio queue.
     * @param uri The Uri of the sound.
     * @param listener The listener to the sound. This is usually MainService but can be overridden.
     */
    public void queueSound(Uri uri, MediaPlayer.OnCompletionListener listener) {
        mAudioQueue.offer(new AudioQueueItem(uri, listener));
    }

    /**
     * Removes the first instance of a sound from the audio queue.
     * @param uri Uri of the item to be removed.
     */
    public void dequeueSound(Uri uri) {
        mAudioQueue.remove(new AudioQueueItem(uri, null));
    }

    /**
     * Obtain audio focus for the application. This also checks if we are currently playing any
     * other audio clips, so it checks for "audio focus" within the app.
     * @return True if audio focus is obtained. False otherwise.
     */
    public boolean obtainAudioFocus() {
        if (mMediaPlayer.isPlaying() || mTextToSpeech.isSpeaking()) {
            return false;
        }

        int result = mAudioManager.requestAudioFocus(
                mAudioFocusChangeListener, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        return (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
    }

    /**
     * End audio playback, and abandon audio focus.
     */
    public void endPlayback() {
        mMediaPlayer.reset();
        mAudioManager.abandonAudioFocus(mAudioFocusChangeListener);
    }

    protected TextToSpeech getTextToSpeech() { return mIsTextToSpeechReady? mTextToSpeech : null; }

    /**
     * Checks if the state transition is valid.
     * @param state State to transition to.
     * @return True if the transition is valid, false if not.
     */
    private boolean canEnterState(State state) {
        if (mState == state) {
            return false;
        }

        boolean canEnterState = true;
        switch (state) {
            case UNINITIALIZED:
                break;
            case MISSION_LOADED:
                if (mState == State.MISSION_RUNNING) {
                    canEnterState = false;
                    Utils.logDebug(TAG,
                            "Can not enter MISSION_LOADED state from MISSION_RUNNING state.");
                }
                break;
            case MISSION_RUNNING:
                if (mState != State.MISSION_LOADED) {
                    canEnterState = false;
                    Utils.logDebug(TAG,
                            "Can only enter MISSION_RUNNING state from MISSION_LOADED state.");
                }
                break;
            case END_SCREEN:
                if (mState != State.MISSION_RUNNING) {
                    canEnterState = false;
                    Utils.logDebug(TAG,
                            "Can only enter END_SCREEN state from MISSION_RUNNING state.");
                }
                break;
        }
        return canEnterState;
    }

    /**
     * Sets the next state if possible.
     * @param state State to transition to.
     */
    private void setAndInitNextState(State state) {
        if (!canEnterState(state)) {
            return;
        }
        mState = state;
        switch (mState) {
            case UNINITIALIZED:
                break;
            case MISSION_LOADED:
                mMission.prepare(mGoogleApiClientWrapper);
                mUpdateHandler.post(this);
                break;
            case MISSION_RUNNING:
                mMission.start();
                broadcastStart();
                break;
            case END_SCREEN:
                mUpdateHandler.removeCallbacks(this);
                mMission.cleanup();
                broadcastEnd();
                break;
        }
    }

    private void update() {
        if (mState == State.MISSION_LOADED && isReady()) {
            setAndInitNextState(State.MISSION_RUNNING);
        }
        if (mState == State.MISSION_RUNNING) {
            mMission.update();

            if (mMission.isDone()) {
                endMission();
            }
        }

        // Consume the audio queue if it's not empty and if we are able to obtain audio focus.
        if (!mAudioQueue.isEmpty() && obtainAudioFocus()) {
            playFirstInQueue();
        }
    }

    /**
     * Play the first item in the audio queue.
     */
    private void playFirstInQueue() {
        AudioQueueItem queueItem = mAudioQueue.poll();
        try {
            mMediaPlayer.setDataSource(this, queueItem.mUri);
        } catch (IOException e) {
            e.printStackTrace();
            // Data source does not exist. Skip playback.
            endPlayback();
            return;
        }
        mMediaPlayer.setOnCompletionListener(queueItem.mListener);
        try {
            mMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            // Error in reading the data source. Skip playback.
            endPlayback();
            return;
        }
        mMediaPlayer.start();
    }

    /**
     * @return True if asynchronous preparations are all done, so that a mission can be started.
     */
    private boolean isReady() {
        return mIsTextToSpeechReady && mGoogleApiClientWrapper.isClientReady();
    }

    /**
     * Broadcast to MainActivity to enable back navigation.
     */
    private void enableBackNavigation() {
        sendBroadcast(new Intent(MainActivity.ENABLE_BACK));
    }

    /**
     * Display a Toast that requests user to reselect their mission.
     */
    private void requestReselection() {
        Toast.makeText(this, "Mission load failure. Select again.", Toast.LENGTH_SHORT).show();
        enableBackNavigation();
    }

    /**
     * Broadcast to MainActivity that mission has started.
     */
    private void broadcastStart() {
        sendBroadcast(new Intent(MainActivity.MISSION_START));
    }

    /**
     * Broadcast to MainActivity that mission has ended.
     */
    private void broadcastEnd() {
        sendBroadcast(new Intent(MainActivity.MISSION_END));
    }
}
