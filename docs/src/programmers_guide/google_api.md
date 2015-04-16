Google API {#games_in_motion_guide_google_api}
==========

Google APIs provide feature access that is important to Games in Motion. These
APIs are accessed using the [GoogleApiClient][] which handles all connections to
Google-provided services.

The `GoogleApiClientWrapper` class handles all connections to
[GoogleApiClient][]. Find it in
`app/src/main/java/com/google/fpl/gim/examplegame/google`.

At start up, Games in Motion attempts to connect to various Google API services.
The connection flow is delegated to the appropriate service by default.

# Google Fit

[Google Fit][] is a core part of Games in Motion. It keeps track of step count,
tracks speed, and keeps data in the cloud so the player can retrieve it via
other apps that connect to [Google Fit][].

When the player is ready to start a mission, Games in Motion connects to
[Google Fit][] to register callbacks for step count and speed. When all the
sensors are connected properly (detected via callbacks), Games in Motion start
the mission.

Note that for certain Android devices, there might be a noticeable delay between
successfully registering a sensor and actually getting sensor data. If the
device has hardware sensors: the sensors might take some time to start up. Games
in Motion does not account for the delay.

For tracking speed, Games in Motion provide a fallback mechanism to calculating
speed in case sensor data is unreliable.

# Google Play Games Service

In the `GoogleApiClientWrapper` class, Games in Motion connects to
[Google Play Games Services][] in the same manner as it connects to
[Google Fit][]. Games in Motion provides an example of unlocking achievements.
Look for the `unlockAchievement` method in `MainService.java`.

\s\s

  [GoogleApiClient]: https://developer.android.com/reference/com/google/android/gms/common/api/GoogleApiClient.html
  [Google Fit]: https://developers.google.com/fit/
  [Google Play Games Services]: https://developer.android.com/google/play-services/games.html
