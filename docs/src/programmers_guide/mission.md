Mission System {#games_in_motion_guide_mission}
==============

The data-driven mission system is controlled by the `Mission` class. Only one
`Mission`, consisting of a list `Moments`, may be active at any point during
gameplay. Only one `Moment` is active at a time while the player plays.

Each of the data-driven classes (`Mission` and various `Moment` classes) has
an equivalent Data class that contains the actual data parsed from the input XML
files. Find them in `app/src/main/java/com/google/fpl/gim/examplegame`.

# Mission

`Mission` keeps track of the game progression, as well as all the data generated
by the user during the game. Its `update()` method is being called by
`MainService` per game frame, and it will trigger the relevant `Moment`s as time
progresses.

On mission start, `Mission` will register listeners with [Google Fit][]. This
allows us to obtain speed and step data, which is critical to the game.

# Moment

A `Moment` is a discrete event within a `Mission`. `Moment`s know when they
finish, the next `Moment` to start, are sequential, and are never reused.

We have implemented a few types of `Moment`s for a basic mission flow.

* Choice moments.

  These give the player a couple choices to choose from. The choices are
  shown to players as [Notifications][].

* Sfx moments.

  These queue a simple sound effect to be played.

* Spoken Text moments.

  These use [Android Text to Speech][] to generate spoken speech to be played
  when the app gets [Audio Focus][].

* Timer moments.

  These are simple timers to space out the other `Moment`s.

As you can see, each of these `Moment`s are small encapsulated game events. They
are highly specialized, but can be authored to allow for variety and flexibility
in each game mission.

# Parser

In order to parse our custom data formats for `Mission`s and `Moment`s, we have
written a `MissionParser`. Find it in
`app/src/main/java/com/google/fpl/gim/examplegame/utils`.

This is a runtime parser that will parse XML data when the player selected a
mission. It is also the only component that has unit testing. Find the test in
`app/src/androidTest/`.

\s\s

  [Android Text to Speech]: http://developer.android.com/reference/android/speech/tts/TextToSpeech.html
  [Audio Focus]: http://developer.android.com/training/managing-audio/audio-focus.html
  [Google Fit]: https://developers.google.com/fit/
  [Notifications]: http://developer.android.com/guide/topics/ui/notifiers/notifications.html
