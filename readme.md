Games in Motion   {#games_in_motion_readme}
===============

Games in Motion Version 1.0.0

[Games in Motion][] is a simple game that motivates you to run.

## Motivation

[Games in Motion][] is a demonstration for several Android-specific
technologies, including the [Google Fit][] API, the [Android Wear][] API, and a
simple data-driven design mechanic. It is also compatible with
[Material design][]. This sample shows how all these pieces can be put together
in a fun context, while also giving players a good motivation to exercise.

[Games in Motion][] is developed entirely using [Android Studio][].

## Downloading

[Games in Motion][] can be downloaded from:
   * [GitHub][] (source)
   * [GitHub Releases Page](http://github.com/googlesamples/android-play-games-in-motion/releases) (source)

~~~{.sh}
    git clone --recursive https://github.com/googlesamples/android-play-games-in-motion.git
~~~

## Documentation

The documentation is include with the GitHub codebase. It is in the `docs`
directory.

Required libraries:
    * Python 2.7
    * [fplutil][] library

[fplutil][] is referenced as a submodule from the [Games in Motion][]
repository, so the download command referenced above will automatically download
it as well.

After all required libraries are downloaded, run:

~~~{.sh}
    ./docs/generate_docs.py
~~~

The generated documentation will be in `docs/html`.

To contribute the this project see [CONTRIBUTING][]. The license file is at
[LICENSE][].

\s\s

  [Android Studio]: http://developer.android.com/tools/studio/index.html
  [Android Wear]: https://developer.android.com/wear/index.html
  [Build and Run Games in Motion]: http://github.com/googlesamples/android-play-games-in-motiongames_in_motion_guide_building.html
  [fplutil]: http://google.github.io/fplutil/
  [Games in Motion]: http://github.com/googlesamples/android-play-games-in-motion
  [Google Fit]: https://developers.google.com/fit/
  [Material design]: http://www.google.com/design/spec/material-design/introduction.html
  [Programmer's Guide]: http://github.com/googlesamples/android-play-games-in-motion/games_in_motion_guide_overview.html
  [CONTRIBUTING]: http://github.com/googlesamples/android-play-games-in-motion/blob/master/CONTRIBUTING
  [LICENSE]: http://github.com/googlesamples/android-play-games-in-motion/blob/master/LICENSE
  [GitHub]: http://github.com/googlesamples/android-play-games-in-motion
