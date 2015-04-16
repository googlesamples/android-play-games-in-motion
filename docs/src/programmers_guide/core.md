Games in Motion Core    {#games_in_motion_guide_core}
====================

There are two main components of the game, `MainActivity` and `MainService`.
Find them in `app/src/main/java/com/google/fpl/gim/examplegame`.

# MainActivity

All Android applications have a main [Activity][] class which the application
will show on launch. Please review the [Activity Lifecycle][] if you are not
familiar with it.

For Games in Motion, `MainActivity` manages all the UI elements that are shown
on screen. It holds a pointer to `MainService` so it can query game state
changes in order to update the UI dynamically.

# MainService {#games_in_motion_guide_mainservice}

`MainService` is included in `AndroidManifest.xml`. It will automatically be
started when the app is launched. Please review the [Service Basics][] if you
are not familiar with Services. `MainService` is run in the background to allow
the game to continue running when the player turns off the phone screen, or when
`MainActivity` is paused for any reason.

`MainService::Run` contains the main game loop. Every call to that function is
one frame of the game update.

`MainService` holds references to all the key systems that are needed during
gameplay. This includes the [Mission system][], [Android Audio Manager][], 
[Android Text to Speech][], and the [Google Api Wrapper][]. `MainService` is
also responsible for starting up these systems and channeling queries to them.

# UI

Most of Games in Motion's UI is handled using [Fragments][].
`MainActivity` holds all the fragments and handles all incoming requests to
advance the UI, or delegate them to the fragments. The UI fragments reside in
`app/src/main/java/com/google/fpl/gim/examplegame/gui`.

Games in Motion also uses [Notifications][] extensively. They allow the user to
interact with the game, either through the notifications menu, or an Android
Wear device. Notifications are typically issued through `MainService` as they
can occur while `MainActivity` is not in display.

# State Machine

There is a simple state machine that resides in `MainService`. The `mState`
variable dictates the mission flow. There are two functions that help control
the states: `canEnterState` checks if we can enter a state from the current
state, and `setAndInitNextState` sets the state machine to the next state while
doing all the initializations needed for the transition. The states and their 
transitions are described below.

  * `UNINITIALIZED` -- No mission is loaded. The player is still going through
    the UI flow and hasn't started a game mission yet.

    You can enter this state from any other states. It happens when the player
    hit the back button from `END_SCREEN`, when the connection to Google Apis
    has been disconnected, or when some other sort of error has occurred.

  * `MISSION_LOADED` -- The game mission has been loaded and we are waiting for
    a few key systems to finish starting up before we start the mission.
    
    When the player starts a mission, it will trigger a load from the `assets`
    folder. It will also trigger subscriptions to the appropriate [Google Fit][]
    data streams.

    When the game is ready, the game transitions to `MISSION_RUNNING`.

  * `MISSION_RUNNING` -- The misson is started when we transition to this state.
    While in this state, the mission is also running, even if the device screen
    is not on.
    
    This state can only be transitioned from `MISSION_LOADED`.

  * `END_SCREEN` -- When a mission is over (all its moments have been played),
    or when a player aborts a mission by hitting the back button, the game will
    transition to this state.
    
    The game will display the statistics from the mission while in this state.

    This state can only be transitioned from `MISSION_RUNNING`.

\s\s

  [Activity]: http://developer.android.com/reference/android/app/Activity.html
  [Activity Lifecycle]: http://developer.android.com/reference/android/app/Activity.html#ActivityLifecycle
  [Android Audio Manager]: http://developer.android.com/reference/android/media/AudioManager.html
  [Android Text to Speech]: http://developer.android.com/reference/android/speech/tts/TextToSpeech.html
  [Fragments]: http://developer.android.com/reference/android/app/Activity.html#Fragments
  [Google Api Wrapper]: @ref games_in_motion_guide_google_api
  [Google Fit]: https://developers.google.com/fit/
  [Mission system]: @ref games_in_motion_guide_mission
  [Notifications]: http://developer.android.com/guide/topics/ui/notifiers/notifications.html
  [Service Basics]: http://developer.android.com/guide/components/services.html
