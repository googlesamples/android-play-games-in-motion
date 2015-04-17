Overview    {#games_in_motion_guide_overview}
========

## Downloading

[Games in Motion][] can be downloaded from [GitHub](http://github.com/googlesamples/android-play-games-in-motion)
or the [releases page](http://github.com/googlesamples/android-play-games-in-motion/releases).

~~~{.sh}
    git clone --recursive https://github.com/googlesamples/android-play-games-in-motion.git
~~~

## Subsystems

Games in Motion code is divided into the following subsystems
- [Core][] -- holds game subsystems, state, and state machine, and UI.
- [Mission System][] -- a simple, game-agnostic system for organizing game
  events.
- [Google Api][] -- an interface with different Google services APIs.
- [Audio][] -- various systems integrated to produce audio and control focus.

## Source Layout

The following bullets describe the directory structure of the game.


| Path                          | Description                                  |
|-------------------------------|----------------------------------------------|
| base directory                | Android Studio project files.                |
| `app/main`                    | Main directory for all code and assets.      |
| `app/main/assets`             | Game mission data in .xml format.            |
| `app/main/java`               | Where the main code lives.                   |
| `app/main/res`                | Android-style assets.                        |
| `app/androidTest`             | JUnit tests for the mission authoring        |
|                               | component.                                   |
| `docs`                        | Documentation source and html files.         |


\s\s

  [Audio]: @ref games_in_motion_guide_audio
  [Core]: @ref games_in_motion_guide_core
  [Games in Motion]: @ref games_in_motion_index
  [Google Api]: @ref games_in_motion_guide_google_api
  [Mission System]: @ref games_in_motion_guide_mission
