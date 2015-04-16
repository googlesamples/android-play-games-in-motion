Modifying Assets {#games_in_motion_guide_assets}
================

### Overview

The game utilizes a data-driven component for gameplay. Games in Motion uses a
XML-based data component to describe missions.

To add a Mission, add a new XML file in the `app/src/main/assets/missions`
folder. There are existing XML files in both the `app/src/main/assets/missions`
and the `app/src/main/assets/legacy_missions` folders, which can be used as a
starting point.

The game loads gameplay assets from the `app/src/main/assets` directory, and
other assets (art, strings, layouts) from the `app/src/main/res` directory, as
is conventional in [Android Studio][].

### Authoring

As outlined at the [Mission][] system page, missions are comprised of moments.

Each moment signifies a different kind of event, and there is a `type` and an
`id` associated with each of them. Each kind of moment can define specific XML
tags. Optionally, each moment can have multiple `fictional_progress` tags, which
are information displayed to the user at the end of the mission.

Each mission has a `name` and a `start_id`. `start_id` signifies the first
moment of the mission. A mission progresses through its moments linearly, the
order of which is determined by the `next_moment_id` tag. The first moment will
always have an `id` identical to the mission's `start_id` property, whereas the
last moment will not have a `next_moment_id` tag.

For example, here is a simple mission with one moment:

    <mission
        start_id="foo"
        name="Foo Example">
        <moment
            type="timer"
            id="foo">
            <length_minutes>0.5</length_minutes>
            <fictional_progress>Played a mission!</fictional_progress>
            <fictional_progress>Played a timer moment</fictional_progress>
        </moment>
    </mission>

Here is a mission with two moments:

    <mission
        start_id="bar"
        name="Bar Example">
        <moment
            type="sfx"
            id="bar">
            <next_moment id="half_minute_timer"/>
            <uri>android.resource://com.google.fpl.gim.examplegame/raw/bar</uri>
            <fictional_progress>Bar!</fictional_progress>
        </moment>
        <moment
            type="timer"
            id="half_minute_timer">
            <length_minutes>0.5</length_minutes>
        </moment>
    </mission>

### Testing

There are simple unit tests written for the mission parser. The tests are in the
`app/src/androidTest` directory.

The tests can be run by changing the run configuration to `All Tests`.

<img src="change_config.png"
     width="300em"
     align="left" />

\s\s

  [Android Studio]: http://developer.android.com/tools/studio/index.html
  [Mission]: @ref games_in_motion_guide_mission
