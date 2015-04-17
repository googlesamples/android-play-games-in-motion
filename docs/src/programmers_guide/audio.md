Audio {#games_in_motion_guide_audio}
=====

Audio is essential to [Games in Motion][]. It signals game progression to the
player, provides feedback for player actions, and allows the game to be played
without many physical interactions with a device.

We also want to let players choose their own background music.
[Games in Motion][] is designed to encourage people to move more, and often
background music helps set a pace for the activity.

In [Games in Motion][], audio is mostly handled by the [MainService][].

# Media Player {#games_in_motion_guide_mediaplayer}

We use [Android Media Player][] to control the playback of our raw audio files.
It is owned by [MainService][].

In this sample, we do not allow any sound effects to be played on top of each
other because all of them provide important user feedback. Therefore, we have
implemented an audio queue to make sure that only one sound effect is played at
a time.

Currently, only `SfxMoment` and `Mission` are queueing audio to be played by the
[Android Media Player][]. Any audio will only be played if we can properly
obtain [Audio Focus][].

# Text to Speech {#games_in_motion_guide_tts}

For certain game story elements, it is better to be able to author and change
them without having to re-record after every change. We use
[Android Text to Speech][] to generate audio from these texts.

The main Text to Speech module is owned by the [MainService][].
`SpokenTextMoment` obtains a reference to the module in order to convert its
text to audio and play it. Again, the audio will not be played unless we have
properly obtained [Audio focus][].

Unlike raw audio files, we cannot queue Text to Speech requests as easily.
Instead, we restart any `SpokenTextMoment` until we are successful in calling
the Text to Speech module.

# Audio Focus {#games_in_motion_guide_audiofocus}

A piece of audio, regardless of which module it is from, can only be played if
we satisfy these following conditions:

* No audio from the [Media Player][] or the [Text to Speech][] modules is
  currently playing.
* We can obtain audio focus properly from the Android system.

The first condition is important so we are not overlapping any important audio
cues. The second is important because we want to make sure that the Android
system isn't performing any important audio tasks.

Because players can choose any background music from their favourite app, we
need to be able to lower the volume of any audio from other apps before we can
play our own. We return the volume level of those audio to previous levels once
we are done. We manage this by properly implementing Android Audio Focus. Please
review the [Managing Audio Focus][] page if you are not familiar with it.

\s\s

  [Android Media Player]: http://developer.android.com/reference/android/media/MediaPlayer.html
  [Android Text to Speech]: http://developer.android.com/reference/android/speech/tts/TextToSpeech.html
  [Audio Focus]: @ref games_in_motion_guide_audiofocus
  [Games in Motion]: @ref games_in_motion_index
  [MainService]: @ref games_in_motion_guide_mainservice
  [Managing Audio Focus]: http://developer.android.com/training/managing-audio/audio-focus.html
  [Media Player]: @ref games_in_motion_guide_mediaplayer
  [Text to Speech]: @ref games_in_motion_guide_tts
