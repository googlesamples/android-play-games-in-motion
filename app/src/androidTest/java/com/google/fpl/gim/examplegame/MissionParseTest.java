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

import com.google.fpl.gim.examplegame.utils.MissionParseException;
import com.google.fpl.gim.examplegame.utils.MissionParser;
import com.google.fpl.gim.examplegame.utils.Utils;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Tests the functionality of mission parsing from XML.
 */
public class MissionParseTest extends TestCase {

    private static final String TAG = MissionParseTest.class.getSimpleName();

    private Mission mMission;
    private MissionData mMissionData;

    public void setUp() {
        Utils.logDebug(TAG, "Setting up...");
        // Set up mission data that will be used for every test case
        String missionId = "Mission 1";
        float lengthOfGameMinutes = 30f; // 30-minute game
        float lengthOfIntervalMinutes = 1f; // 1-minute intervals
        float challengePaceMinutesPerMile = 8.0f;
        mMissionData = new MissionData(missionId, missionId, lengthOfGameMinutes,
                lengthOfIntervalMinutes, challengePaceMinutesPerMile);
    }

    public void tearDown() {
        Utils.logDebug(TAG, "Tearing down...");
    }

    /**
     * Test for correct construction of a timer moment.
     */
    public void testTimerMomentConstruction() {
        String xml = "";
        xml += createStartMissionXml(               // <mission> [...]
                "start",                            // First moment in mission.
                "Name");                            // Mission name.

        xml += createTimerMomentXml(                // <moment type="timer"> [...]
                "start",                            // Moment id.
                "start",                            // Next moment id.
                0.5);                               // Length of timer moment (minutes).
                                                    // </moment>

        xml += createEndMissionXml();               // </mission>

        InputStream momentInputStream =
                new ByteArrayInputStream(xml.getBytes());
        mMission = new Mission(mMissionData);
        try {
            mMission.readMoments(momentInputStream);
        } catch (MissionParseException e) {
            e.printStackTrace();
        }

        Assert.assertEquals("Mission 1", mMissionData.getMissionId());
        Assert.assertEquals(1, mMissionData.getNumMoments());
        Assert.assertEquals(true, mMissionData.getMomentFromId("start") instanceof TimerMoment);
        Assert.assertEquals("start", mMissionData.getMomentFromId("start").getNextMomentId());
    }

    /**
     * Test for correct construction of a choice moment.
     */
    public void testChoiceMomentConstruction() {
        String xml = "";
        xml += createStartMissionXml(               // <mission> [...]
                "start",                            // First moment in mission.
                "Name");                            // Mission name.

        xml += createStartChoiceMomentXml(          // <moment type="choice"> [...]
                "start",                            // Moment id.
                0.5,                                // Length of choice timeout in minutes.
                "Example ChoiceMoment Description", // Description of choice moment.
                "choice_2");                        // Id of default choice.

        xml += createChoiceXml(                     // <choice> [...]
                "fire",                             // Choice id.
                "Example Choice Description 1",     // Description of choice.
                "start",                            // Next moment id.
                true,                               // Whether the weapon charge should be depleted
                false,                              // Whether the number of enemies defeated should
                                                    //    be incremented.
                "test_icon");                       // Icon resource name.
                                                    // </choice>

        xml += createChoiceXml(                     // <choice> [...]
                "choice_2",                         // Choice id.
                "Example Choice Description 2",     // Description of choice.
                "start",                            // Next moment id.
                false,                              // Whether the weapon charge should be depleted
                false,                              // Whether the number of enemies defeated should
                                                    //    be incremented.
                "test_icon");                       // Icon resource name.
                                                    // </choice>

        xml += createChoiceXml(                     // <choice> [...]
                "choice_3",                         // Choice id.
                "Example Choice Description 3",     // Description of choice.
                "start",                            // Next moment id.
                true,                               // Whether the weapon charge should be depleted
                false,                              // Whether the number of enemies defeated should
                                                    //    be incremented.
                "test_icon");                       // Icon resource name.
                                                    // </choice>

        xml += createEndChoiceMomentXml();          // </moment>

        xml += createEndMissionXml();               // </mission>

        Utils.logDebug(TAG, xml);

        InputStream momentInputStream =
                new ByteArrayInputStream(xml.getBytes());
        mMission = new Mission(mMissionData);
        try {
            mMission.readMoments(momentInputStream);
        } catch (MissionParseException e) {
            e.printStackTrace();
        }

        Assert.assertEquals("Mission 1", mMissionData.getMissionId());
        Assert.assertEquals(1, mMissionData.getNumMoments());
        Assert.assertEquals(true, mMissionData.getMomentFromId("start") instanceof ChoiceMoment);

        ChoiceMoment choiceMoment = ((ChoiceMoment) mMissionData.getMomentFromId("start"));
        Assert.assertEquals(3, choiceMoment.getMomentData().getNumChoices());

        Choice choice1 = choiceMoment.getMomentData().getChoiceById("choice_2");
        Assert.assertEquals("Example Choice Description 2", choice1.getDescription());
        Assert.assertEquals("start", choice1.getNextMomentId());
        Assert.assertEquals(false, choice1.getOutcome().weaponChargeDepleted());
        Assert.assertEquals(false, choice1.getOutcome().numEnemiesDefeatedIncremented());
        Assert.assertEquals("test_icon", choice1.getDrawableResourceName());
        Assert.assertEquals(null, mMissionData.getMomentFromId("start").getNextMomentId());
    }

    /**
     * Test for correct construction of an sfx moment.
     */
    public void testSfxMomentConstruction() {
        String xml = "";
        xml += createStartMissionXml(               // <mission> [...]
                "start",                            // First moment in mission.
                "Name");                            // Mission name.

        xml += createSfxMomentXml(                  // <moment type="sfx"> [...]
                "start",                            // Moment id.
                "start",                            // Next moment id.
                "path/to/something");               // Path to sound effect resource.
                                                    // </moment>

        xml += createEndMissionXml();               // </mission>

        InputStream momentInputStream = new ByteArrayInputStream(xml.getBytes());
        mMission = new Mission(mMissionData);
        try {
            mMission.readMoments(momentInputStream);
        } catch (MissionParseException e) {
            e.printStackTrace();
        }

        Assert.assertEquals("Mission 1", mMissionData.getMissionId());
        Assert.assertEquals(1, mMissionData.getNumMoments());
        Assert.assertEquals(true, mMissionData.getMomentFromId("start") instanceof SfxMoment);
        Assert.assertEquals("start", mMissionData.getMomentFromId("start").getNextMomentId());
    }

    /**
     * Test for correct construction of a spoken text moment.
     */
    public void testSpokenTextMomentConstruction() {
        String xml = "";
        xml += createStartMissionXml(               // <mission> [...]
                "start",                            // First moment in mission.
                "Name");                            // Mission name.

        xml += createSpokenTextMomentXml(           // <moment type="spoken_text"> [...]
                "start",                            // Moment id.
                "start",                            // Next moment id.
                "Hello!");                          // Text to speak out loud.
                                                    // </moment>

        xml += createEndMissionXml();               // </mission>

        InputStream momentInputStream =
                new ByteArrayInputStream(xml.getBytes());
        mMission = new Mission(mMissionData);
        try {
            mMission.readMoments(momentInputStream);
        } catch (MissionParseException e) {
            e.printStackTrace();
        }

        Assert.assertEquals("Mission 1", mMissionData.getMissionId());
        Assert.assertEquals(1, mMissionData.getNumMoments());
        Assert.assertEquals(true,
                mMissionData.getMomentFromId("start") instanceof SpokenTextMoment);
        Assert.assertEquals("start", mMissionData.getMomentFromId("start").getNextMomentId());
    }

    /**
     * Test for correct construction of a mission with four different types of moments.
     */
    public void testFourMomentConstruction() {
        String xml = "";
        xml += createStartMissionXml(               // <mission> [...]
                "start",                            // First moment in mission.
                "Name");                            // Mission name.

        xml += createSpokenTextMomentXml(           // <moment type="spoken_text"> [...]
                "start",                            // Moment id.
                "second",                           // Next moment id.
                "Hello!");                          // Text to speak out loud.
                                                    // </moment>

        xml += createSfxMomentXml(                  // <moment type="sfx"> [...]
                "second",                           // Moment id.
                "third",                            // Next moment id.
                "path/to/something");               // Path to sound effect resource.
                                                    // </moment>

        xml += createStartChoiceMomentXml(          // <moment type="choice"> [...]
                "third",                            // Moment id.
                0.5,                                // Length of choice timeout in minutes.
                "Example ChoiceMoment Description", // Description of choice moment.
                "choice_2");                        // Id of default choice.
        xml += createChoiceXml(                     // <choice> [...]
                "fire",                             // Choice id.
                "Example Choice Description 1",     // Description of choice.
                "fourth",                            // Next moment id.
                true,                               // Whether the weapon charge should be depleted
                false,                              // Whether the number of enemies defeated should
                                                    //    be incremented.
                "test_icon");                       // Icon resource name.
                                                    // </choice>
        xml += createChoiceXml(                     // <choice> [...]
                "choice_2",                         // Choice id.
                "Example Choice Description 2",     // Description of choice.
                "fourth",                           // Next moment id.
                false,                              // Whether the weapon charge should be depleted
                false,                              // Whether the number of enemies defeated should
                                                    //    be incremented.
                "test_icon");                       // Icon resource name.
                                                    // </choice>
        xml += createEndChoiceMomentXml();          // </moment>

        xml += createTimerMomentXml(                // <moment type="timer"> [...]
                "fourth",                           // Moment id.
                null,                               // Next moment id.
                0.25);                              // Length of timer moment (minutes).
                                                    // </moment>

        xml += createEndMissionXml();               // </mission>

        InputStream momentInputStream = new ByteArrayInputStream(xml.getBytes());
        mMission = new Mission(mMissionData);
        try {
            mMission.readMoments(momentInputStream);
        } catch (MissionParseException e) {
            e.printStackTrace();
        }

        Assert.assertEquals("Mission 1", mMissionData.getMissionId());
        Assert.assertEquals(4, mMissionData.getNumMoments());

        Moment spokenTextMoment = mMissionData.getMomentFromId("start");
        Moment sfxMoment = mMissionData.getMomentFromId("second");
        Moment choiceMoment = mMissionData.getMomentFromId("third");
        Moment timerMoment = mMissionData.getMomentFromId("fourth");

        Assert.assertEquals(true, spokenTextMoment instanceof SpokenTextMoment);
        Assert.assertEquals(true, sfxMoment instanceof SfxMoment);
        Assert.assertEquals(true, choiceMoment instanceof ChoiceMoment);
        Assert.assertEquals(true, timerMoment instanceof TimerMoment);

        SpokenTextMoment moment1 = (SpokenTextMoment) spokenTextMoment;
        SfxMoment moment2 = (SfxMoment) sfxMoment;
        ChoiceMoment moment3 = (ChoiceMoment) choiceMoment;
        TimerMoment moment4 = (TimerMoment) timerMoment;

        Assert.assertEquals("second", moment1.getNextMomentId());
        Assert.assertEquals("third", moment2.getNextMomentId());
        // The next ID of a ChoiceMoment is null until the user has made their choice.
        Assert.assertEquals(null, moment3.getNextMomentId());
        Assert.assertEquals("fourth", moment3.getMomentData().getChoices()[0].getNextMomentId());
        // The final moment has a 'null' next moment.
        Assert.assertEquals(null, moment4.getNextMomentId());

        Assert.assertEquals(true, moment3.getMomentData().getChoiceById("fire")
                .requiresChargedWeapon());
        Assert.assertEquals(false, moment3.getMomentData().getChoiceById("choice_2")
                .requiresChargedWeapon());
    }

    /**
     * Test for correct error handling with a moment has an invalid 'type'.
     */
    public void testMomentWithInvalidTypeErrorHandling() {
        String xml = "";

        xml += createStartMissionXml(               // <mission> [...]
                "start",                            // First moment in mission.
                "Name");                            // Mission name.

        xml += createStartMomentXml(                // <moment> [...]
                "invalid",                          // Moment type is invalid.
                "start");                           // Moment id.
        xml += createNextMomentXml("start");        // Next moment id.
        xml += createLengthMinutesXml(0.25);        // Length of (timer) moment in minutes.
        xml += createEndMomentXml();                // </moment>

        xml += createEndMissionXml();               // </mission>

        InputStream momentInputStream = new ByteArrayInputStream(xml.getBytes());
        mMission = new Mission(mMissionData);
        boolean didMissionParseFail = false;


        try {
            mMission.readMoments(momentInputStream);
        } catch (MissionParseException e) {
            didMissionParseFail = true;
        }

        Assert.assertEquals(true, didMissionParseFail);
    }

    /**
     * Test for correct error handling with a moment has no 'type' attribute.
     */
    public void testMomentWithNoTypeErrorHandling() {
        String xml = "";

        xml += createStartMissionXml(               // <mission> [...]
                "start",                            // First moment in mission.
                "Name");                            // Mission name.

        xml += "<moment " +                         // <moment> [...]
                "id='start' >";                     // Moment id. Moment type is missing.
        xml += createNextMomentXml("start");        // Next moment id.
        xml += createLengthMinutesXml(0.25);        // Length of (timer) moment in minutes.
        xml += createEndMomentXml();                // </moment>

        xml += createEndMissionXml();               // </mission>

        InputStream momentInputStream = new ByteArrayInputStream(xml.getBytes());
        mMission = new Mission(mMissionData);
        boolean didMissionParseFail = false;
        try {
            mMission.readMoments(momentInputStream);
        } catch (MissionParseException e) {
            didMissionParseFail = true;
        }

        Assert.assertEquals(true, didMissionParseFail);
    }

    /*
    * Test for correct behavior when a moment has no 'next_moment' attribute (signifies that this
    * moment is the final moment in the mission.)
    */
    public void testTimerMomentMissingNextMomentMission() {
        String xml = "";

        xml += createStartMissionXml(               // <mission> [...]
                "start",                            // First moment in mission.
                "Name");                            // Name of mission.

        xml += createStartMomentXml(                // <moment> [...]
                "timer",                            // Moment type.
                "start");                           // Moment id.
        xml += createLengthMinutesXml(0.25);        // Length of (timer) moment in minutes.

        xml += createEndMomentXml();                // </moment>

        xml += createEndMissionXml();               // </mission>

        InputStream momentInputStream = new ByteArrayInputStream(xml.getBytes());
        mMission = new Mission(mMissionData);
        try {
            mMission.readMoments(momentInputStream);
        } catch (MissionParseException e) {
            e.printStackTrace();
        }

        Assert.assertEquals("Mission 1", mMissionData.getMissionId());
        Assert.assertEquals(1, mMissionData.getNumMoments());
        Assert.assertEquals(true, mMissionData.getMomentFromId("start") instanceof TimerMoment);
        Assert.assertEquals(null, mMissionData.getMomentFromId("start").getNextMomentId());
    }

    /**
     * Test for correct reading of a mission name.
     */
    public void testMissionNameConstruction() {
        String xml = "";

        xml += createStartMissionXml(               // <mission> [...]
                "",                                 // First moment in mission.
                "Name");                            // Mission name.

        xml += createEndMissionXml();               // </mission>

        InputStream missionInputStream = new ByteArrayInputStream(xml.getBytes());
        String missionName = null;
        try {
            missionName = MissionParser.getMissionName(missionInputStream);
        } catch (MissionParseException e) {
            e.printStackTrace();
        }

        Assert.assertEquals("Name", missionName);
    }

    /**
     * Test for correct error handling for a mission with no "name" attribute.
     */
    public void testMissingMissionNameHandling() {
        String xml = "";

        xml += "<mission " +                        // <mission> [...]
                "start_id='' >";                    // First moment in mission. Mission name is
                                                    // missing.

        xml += createEndMissionXml();               // </mission>

        InputStream missionInputStream = new ByteArrayInputStream(xml.getBytes());
        boolean didMissionNameParseFail = false;
        try {
            MissionParser.getMissionName(missionInputStream);
        } catch (MissionParseException e) {
            didMissionNameParseFail = true;
        }

        Assert.assertEquals(true, didMissionNameParseFail);
    }

    /**
     * Test for correct parsing of fictional progress for a SpokenText moment.
     */
    public void testFictionalProgressSpokenTextMomentParsing() {
        String xml = "";
        xml += createStartMissionXml(               // <mission> [...]
                "start",                            // First moment in mission.
                "Name");                            // Mission name.

        xml += createSpokenTextMomentWithFictionalProgressXML(
                                                    // <moment> [...]
                "start",                            // Moment id.
                null,                               // Next moment id.
                "Text to speak",                    // Text to speak out loud.
                "Fictional progress.");             // Fictional progress.
                                                    // </moment>

        xml += createEndMissionXml();               // </mission>

        InputStream momentInputStream =
                new ByteArrayInputStream(xml.getBytes());
        mMission = new Mission(mMissionData);
        try {
            mMission.readMoments(momentInputStream);
        } catch (MissionParseException e) {
            e.printStackTrace();
        }

        Assert.assertEquals(1, mMissionData.getMomentFromId("start").getFictionalProgress().size());
        Assert.assertEquals("Fictional progress.",
                mMissionData.getMomentFromId("start").getFictionalProgress().get(0));
    }

    /**
     * Test for correct parsing of fictional progress for a Timer moment.
     */
    public void testFictionalProgressTimerMomentParsing() {
        String xml = "";
        xml += createStartMissionXml(               // <mission> [...]
                "start",                            // First moment in mission.
                "Name");                            // Mission name.

        xml += createTimerMomentWithFictionalProgressXML(
                                                    // <moment> [...]
                "start",                            // Moment id.
                null,                               // Next moment id.
                1.0,                                // Length of timer moment (minutes).
                "Fictional progress.");             // Fictional progress.
                                                    // </moment>

        xml += createEndMissionXml();               // </mission>

        InputStream momentInputStream =
                new ByteArrayInputStream(xml.getBytes());
        mMission = new Mission(mMissionData);
        try {
            mMission.readMoments(momentInputStream);
        } catch (MissionParseException e) {
            e.printStackTrace();
        }

        Assert.assertEquals(1, mMissionData.getMomentFromId("start").getFictionalProgress().size());
        Assert.assertEquals("Fictional progress.",
                mMissionData.getMomentFromId("start").getFictionalProgress().get(0));
    }

    /**
     * Test for correct parsing of fictional progress for an Sfx moment.
     */
    public void testFictionalProgressSfxMomentParsing() {
        String xml = "";
        xml += createStartMissionXml(               // <mission> [...]
                "start",                            // First moment in mission.
                "Name");                            // Mission name.

        xml += createSfxMomentWithFictionalProgressXML(
                                                    // <moment> [...]
                "start",                            // Moment id.
                null,                               // Next moment id.
                "path/to/something",                // Path to sound effect resource.
                "Fictional progress.");             // Fictional progress.
                                                    // </moment>

        xml += createEndMissionXml();               // </mission>

        InputStream momentInputStream =
                new ByteArrayInputStream(xml.getBytes());
        mMission = new Mission(mMissionData);
        try {
            mMission.readMoments(momentInputStream);
        } catch (MissionParseException e) {
            e.printStackTrace();
        }

        Assert.assertEquals(1, mMissionData.getMomentFromId("start").getFictionalProgress().size());
        Assert.assertEquals("Fictional progress.",
                mMissionData.getMomentFromId("start").getFictionalProgress().get(0));
    }

    /**
     * Test for correct parsing of fictional progress for a Choice moment, and for correct parsing
     * of fictional progress for Choices.
     */
    public void testFictionalProgressChoiceMomentParsing() {
        String xml = "";
        xml += createStartMissionXml(               // <mission> [...]
                "start",                            // First moment in mission.
                "Name");                            // Mission name.

        xml += createStartChoiceMomentWithFictionalProgressXML(
                                                    // <moment> [...]
                "start",                            // Moment id.
                1.0,                                // Next moment id.
                "Choice Description",               // Description of choice moment.
                "choice_2",                         // Id of default choice.
                "Fictional progress.");             // Fictional progress.

        xml += createChoiceWithFictionalProgressXML(
                                                    // <choice> [...]
                "fire",                             // Choice id.
                "Example Choice Description 1",     // Description of choice.
                null,                               // Next moment id.
                true,                               // Whether the weapon charge should be depleted.
                false,                              // Whether the number of enemies defeated should
                                                    //    be incremented.
                "Description 1",                    // Fictional progress.
                "test_icon");                       // Icon resource name.
                                                    // </choice>

        xml += createChoiceXml(                     // <choice> [...]
                "choice_2",                         // Choice id.
                "Choice 2 description",             // Description of choice.
                null,                               // Next moment id.
                false,                              // Whether the weapon charge should be depleted.
                false,                              // Whether the number of enemies defeated should
                //    be incremented.
                "test_icon");                       // Icon resource name.
                                                    // </choice>

        xml += createEndMomentXml();                // </moment>

        xml += createEndMissionXml();               // </mission>

        InputStream momentInputStream =
                new ByteArrayInputStream(xml.getBytes());
        mMission = new Mission(mMissionData);
        try {
            mMission.readMoments(momentInputStream);
        } catch (MissionParseException e) {
            e.printStackTrace();
        }

        ChoiceMoment choiceMoment = ((ChoiceMoment) mMissionData.getMomentFromId("start"));

        Choice fireChoice = choiceMoment.getMomentData().getChoiceById("fire");
        Assert.assertEquals(1, fireChoice.getFictionalProgress().size());
        Assert.assertEquals("Description 1", fireChoice.getFictionalProgress().get(0));

        Choice choice2 = choiceMoment.getMomentData().getChoiceById("choice_2");
        Assert.assertEquals(0, choice2.getFictionalProgress().size());

        Assert.assertEquals(1, mMissionData.getMomentFromId("start").getFictionalProgress().size());
        Assert.assertEquals("Fictional progress.",
                mMissionData.getMomentFromId("start").getFictionalProgress().get(0));
    }

    /**
     * Test for correct error handling for a fictional progress element that is empty.
     */
    public void testFictionalProgressErrorHandling() {
        String xml = "";
        xml += createStartMissionXml(               // <mission> [...]
                "start",                            // First moment in mission.
                "Name");                            // Mission name.

        xml += createTimerMomentWithFictionalProgressXML(
                                                    // <moment> [...]
                "start",                            // Moment id.
                null,                               // Next moment id.
                1.0,                                // Length of timer moment (minutes).
                "");                                // Empty fictional progress.
                                                    // </moment>

        xml += createEndMissionXml();               // </mission>

        InputStream momentInputStream = new ByteArrayInputStream(xml.getBytes());
        mMission = new Mission(mMissionData);
        boolean didMissionParseFail = false;


        try {
            mMission.readMoments(momentInputStream);
        } catch (MissionParseException e) {
            didMissionParseFail = true;
        }

        Assert.assertEquals(true, didMissionParseFail);
    }

    /**
     * Test for correct error handling for a Choice that has no icon element.
     */
    public void testChoiceMissingIconErrorHandling() {
        String xml = "";
        xml += createStartMissionXml(               // <mission> [...]
                "start",                            // First moment in mission.
                "Name");                            // Mission name.

        xml += createStartChoiceMomentXml(          // <moment type="choice"> [...]
                "start",                            // Moment id.
                0.5,                                // Length of choice timeout in minutes.
                "Example ChoiceMoment Description", // Description of choice moment.
                "choice_2");                        // Id of default choice.

        xml += createChoiceXml(                     // <choice> [...]
                "fire",                             // Choice id.
                "Example Choice Description 1",     // Description of choice.
                "start",                            // Next moment id.
                true,                               // Whether the weapon charge should be depleted
                false,                              // Whether the number of enemies defeated should
                                                    //    be incremented.
                "test_icon");                       // Icon resource name.
                                                    // </choice>


        xml += createChoiceXml(                     // <choice> [...]
                "choice_2",                         // Choice id.
                "Example Choice Description 2",     // Description of choice.
                "start",                            // Next moment id.
                false,                               // Whether the weapon charge should be depleted
                false,                              // Whether the number of enemies defeated should
                                                    //    be incremented.
                null);                              // Missing icon element.
                                                    // </choice>

        xml += createEndChoiceMomentXml();          // </moment>

        xml += createEndMissionXml();               // </mission>

        Utils.logDebug(TAG, xml);

        InputStream momentInputStream =
                new ByteArrayInputStream(xml.getBytes());
        mMission = new Mission(mMissionData);
        boolean didMissionParseFail = false;
        try {
            mMission.readMoments(momentInputStream);
        } catch (MissionParseException e) {
            e.printStackTrace();
            didMissionParseFail = true;
        }

        Assert.assertEquals(true, didMissionParseFail);
    }

    /**
     * Test for correct error handling for a Choice that has an empty icon element.
     */
    public void testChoiceEmptyIconErrorHandling() {
        String xml = "";
        xml += createStartMissionXml(               // <mission> [...]
                "start",                            // First moment in mission.
                "Name");                            // Mission name.

        xml += createStartChoiceMomentXml(          // <moment type="choice"> [...]
                "start",                            // Moment id.
                0.5,                                // Length of choice timeout in minutes.
                "Example ChoiceMoment Description", // Description of choice moment.
                "choice_2");                        // Id of default choice.

        xml += createChoiceXml(                     // <choice> [...]
                "fire",                             // Choice id.
                "Example Choice Description 1",     // Description of choice.
                "start",                            // Next moment id.
                true,                               // Whether the weapon charge should be depleted
                false,                              // Whether the number of enemies defeated should
                                                    //    be incremented.
                "test_icon");                       // Icon resource name.
                                                    // </choice>

        xml += createChoiceXml(                     // <choice> [...]
                "choice_2",                             // Choice id.
                "Example Choice Description 2",     // Description of choice.
                "start",                            // Next moment id.
                false,                              // Whether the weapon charge should be depleted
                false,                              // Whether the number of enemies defeated should
                                                    //    be incremented.
                "");                                // Empty icon resource name.
                                                    // </choice>


        xml += createEndChoiceMomentXml();          // </moment>

        xml += createEndChoiceMomentXml();          // </moment>

        xml += createEndMissionXml();               // </mission>

        Utils.logDebug(TAG, xml);

        InputStream momentInputStream =
                new ByteArrayInputStream(xml.getBytes());
        mMission = new Mission(mMissionData);
        boolean didMissionParseFail = false;
        try {
            mMission.readMoments(momentInputStream);
        } catch (MissionParseException e) {
            e.printStackTrace();
            didMissionParseFail = true;
        }

        Assert.assertEquals(true, didMissionParseFail);
    }

    /**
     * A helper function to create an XML string representing the start of a mission.
     * @param startMomentId The id of the first moment in the mission.
     * @param missionName The plain text name of the mission.
     * @return An XML string of the following format:
     *      <?xml version='1.0' encoding='utf-8'?>
     *      <mission start_id="[startMomentId]" name="[missionName]">
     */
    private String createStartMissionXml(String startMomentId, String missionName) {
        return "<?xml version='1.0' encoding='utf-8'?>" +
               "<mission " +
                "start_id='" + startMomentId + "' " +
                "name='" + missionName + "' >";
    }

    /**
     * A helper function to create an XML string representing the end of a mission.
     * @return A mission XML end-tag: </mission>
     */
    private String createEndMissionXml() {
        return "</mission>";
    }

    /**
     * A helper function to create an XML string representing the start of a generic moment.
     * @param momentType The type of moment to be created.
     * @param momentId The unique id of this moment.
     * @return An XML string of the following format:
     *      <moment type="[momentType]" id="[momentId]">
     */
    private String createStartMomentXml(String momentType, String momentId) {
        return  "<moment " +
                "type='" + momentType + "' " +
                "id='" + momentId + "' >";
    }

    /**
     * A helper function to create an XML string representing the end of a moment.
     * @return A moment XML end-tag: </moment>
     */
    private String createEndMomentXml() {
        return "</moment>";
    }

    /**
     * A helper function to create an XML string representing a timer moment.
     * @param momentId The unique id of the moment.
     * @param nextMomentId The id of the next moment if this choice is chosen.  Can be null if this
     *                     moment is the last moment in the mission.
     * @param lengthMinutes The length of the timer moment in minutes.
     * @return An XML string of the following format:
     *      <moment type="timer" id="[momentId]">
     *          <next_moment id="[nextMomentId]" />
     *          <length_minutes>[lengthMinutes]</length_minutes>
     *      </moment>
     */
    private String createTimerMomentXml(String momentId, String nextMomentId,
                                        double lengthMinutes) {
        String xml = "";

        xml += createStartMomentXml("timer", momentId);
        xml += createNextMomentXml(nextMomentId);
        xml += createLengthMinutesXml(lengthMinutes);
        xml += createEndMomentXml();

        return xml;
    }

    /**
     * A helper function to create an XML string representing a 'next_moment id' attribute.
     * All moment types can have 'next_moment id''s, and the lack of a 'next_moment id' signifies
     * that the current moment is the last moment in the mission.
     * @param nextMomentId The id of the next moment if this choice is chosen.  Can be null if this
     *                     moment is the last moment in the mission.
     * @return If 'nextMomentId' is null, return an empty string.  Otherwise, an XML string of the
     * following format is returned:
     *      <next_moment id="[nextMomentId]" />
     */
    private String createNextMomentXml(String nextMomentId) {
        if (nextMomentId == null) {
            return "";
        }
        return "<next_moment id='" + nextMomentId + "' />";
    }

    /**
     * A helper function to create an XML string representing the start of a choice moment.
     * @param momentId The unique id of the moment.
     * @param timeoutLengthMinutes The length of time in minutes after which the default choice
     *                             should be chosen for the user.
     * @param choiceDescription A meaningful, human-readable description of the choice to be made,
     *                          which is most likely in the form of either a direct or indirect
     *                          question.
     * @param defaultChoiceId The id of the choice that should be chosen if the decision times out
     *                        before the user has chosen their own choice.
     * @return An XML string with the following format:
     *      <moment type="choice" id="[momentId]">
     *          <timeout_length_minutes>[timeoutLengthMinutes]</timeout_length_minutes>
     *          <description>[choiceDescription]</description>
     *          <default_choice id="[defaultChoiceId]"/>
     *      </moment>
     */
    private String createStartChoiceMomentXml(String momentId, double timeoutLengthMinutes,
                                              String choiceDescription, String defaultChoiceId) {
        String xml = "";

        xml += createStartMomentXml("choice", momentId);
        xml += "<timeout_length_minutes>" + Double.toString(timeoutLengthMinutes)
                + "</timeout_length_minutes>";
        xml += "<description>" + choiceDescription + "</description>";
        xml += "<default_choice id='" + defaultChoiceId + "'/>";

        return xml;
    }

    /**
     * A helper function to create an XML string representing a choice.  Choices are found within
     * choice moments.
     * @param choiceId The id of the choice.
     * @param choiceDescription A meaningful, human-readable description of the choice.
     * @param nextMomentId The id of the next moment if this choice is chosen.  Can be null if this
     *                     moment is the last moment in the mission.
     * @param depleteWeaponCharge Whether the user's weapon should be depleted if they select this
     *                            choice.
     * @param incrementNumEnemiesDefeated Whether the number of enemies defeated should be
     *                                    incremented if the user selects this choice.
     * @param iconResourceName The name of the drawable resource for this choice action.
     * @return An XML string with the following format:
     *      <choice id="[choiceId]">
     *          <description>[choiceDescription]</description>
     *          <next_moment id="[nextMomentId]"/>
     *          <outcome
     *              deplete_weapon="[depleteWeaponCharge]"
     *              increment_enemies="[incrementNumEnemiesDefeated]"/>
     *          <icon name="[iconResourceName]" />
     *      </choice>
     */
    private String createChoiceXml(String choiceId, String choiceDescription, String nextMomentId,
                                   boolean depleteWeaponCharge,
                                   boolean incrementNumEnemiesDefeated, String iconResourceName) {
        String xml = "";

        xml += "<choice ";
        xml += "id='" + choiceId + "' >";
        xml += "<description>" + choiceDescription + "</description>";
        xml += createNextMomentXml(nextMomentId);
        xml += "<outcome ";
        xml += "deplete_weapon='" + Boolean.toString(depleteWeaponCharge) + "' ";
        xml += "increment_enemies='" + Boolean.toString(incrementNumEnemiesDefeated) + "' />";
        if (iconResourceName != null) {
            xml += createIconXML(iconResourceName);
        }
        xml += "</choice>";

        return xml;
    }

    /**
     * A helper function to create an XML string representing the end of a choice moment.
     * @return A moment XML end-tag: </moment>
     */
    private String createEndChoiceMomentXml() {
        return createEndMomentXml();
    }

    /**
     * A helper function to create an XML string representing a spoken text moment.
     * @param momentId The unique id of this moment.
     * @param nextMomentId The id of the next moment in the mission.  Can be null if this moment is
     *                     the last moment in the mission.
     * @param textToSpeak A string that should be spoken aloud as part of this moment.
     * @return An XML string with the following format:
     *      <moment type="spoken_text" id="[momentId]">
     *          <next_moment id="[nextMomentId]" />
     *          <text_to_speak>[textToSpeak]</text_to_speak>
     *      </moment>
     */
    private String createSpokenTextMomentXml(String momentId, String nextMomentId,
                                             String textToSpeak) {
        String xml = "";

        xml += createStartMomentXml("spoken_text", momentId);
        xml += createNextMomentXml(nextMomentId);
        xml += "<text_to_speak>" + textToSpeak + "</text_to_speak>";
        xml += createEndMomentXml();

        return xml;
    }

    /**
     * A helper function to create an XML string representing a sfx moment.
     * @param momentId The unique id of this moment.
     * @param nextMomentId The id of the next moment in the mission. Can be null if this moment is
     *                     the last moment in the mission.
     * @param pathToResource The absolute path to the sound resource to be played as part of this
     *                       moment.
     * @return An XML string with the following format:
     *      <moment type="sfx" id="[momentId]">
     *          <next_moment id="[nextMomentId]" />
     *          <uri>[pathToResource]</uri>
     *       </moment>
     */
    private String createSfxMomentXml(String momentId, String nextMomentId, String pathToResource) {
        String xml = "";

        xml += createStartMomentXml("sfx", momentId);
        xml += createNextMomentXml(nextMomentId);
        xml += "<uri>" + pathToResource + "</uri>";
        xml += createEndMomentXml();

        return xml;
    }

    /**
     * A helper function to create an XML string representing a timer moment's 'length_minutes'
     * element.
     * @param lengthMinutes The length of the timer moment in minutes.
     * @return An XML string with the following format:
     *      <length_minutes>[lengthMinutes]</length_minutes>
     */
    private String createLengthMinutesXml(double lengthMinutes) {
        return "<length_minutes>" + Double.toString(lengthMinutes) + "</length_minutes>";
    }

    /**
     * A helper function to create an XML string representing a 'fictional_progress' element.
     * @param progressDescription The fictional progress made.
     * @return An XML string with the following format:
     *      <fictional_progress>[progressDescription]</fictional_progress>
     */
    private String createFictionalProgressXml(String progressDescription) {
        return "<fictional_progress>" + progressDescription + "</fictional_progress>";
    }

    /**
     * A helper function to create an XML string representing an 'icon' element.
     * @param iconResourceName The name of the drawable resource for this choice action.
     * @return An XML string with the following format:
     *      <icon name="[iconResourceName]" />
     */
    private String createIconXML(String iconResourceName) {
        return "<icon name='" + iconResourceName + "'/>";
    }

    /**
     * A helper function to create an XML string representing a spoken text moment with fictional
     * progress.
     * @param momentId The unique id of this moment.
     * @param nextMomentId The id of the next moment in the mission.  Can be null if this moment is
     *                     the last moment in the mission.
     * @param textToSpeak A string that should be spoken aloud as part of this moment.
     * @param progressDescription A string describing the fictional progress.
     * @return An XML string with the following format:
     *      <moment type="spoken_text" id="[momentId]">
     *          <next_moment id="[nextMomentId]" />
     *          <text_to_speak>[textToSpeak]</text_to_speak>
     *          <fictional_progress>[progressDescription]</fictional_progress>
     *      </moment>
     */
    private String createSpokenTextMomentWithFictionalProgressXML(String momentId,
            String nextMomentId, String textToSpeak, String progressDescription) {
        String xml = "";
        xml += createStartMomentXml("spoken_text", momentId);
        xml += createNextMomentXml(nextMomentId);
        xml += "<text_to_speak>" + textToSpeak + "</text_to_speak>";
        xml += createFictionalProgressXml(progressDescription);
        xml += createEndMomentXml();
        return xml;
    }

    /**
     * A helper function to create an XML string representing a timer moment with fictional
     * progress.
     * @param momentId The unique id of the moment.
     * @param nextMomentId The id of the next moment if this choice is chosen.  Can be null if this
     *                     moment is the last moment in the mission.
     * @param lengthMinutes The length of the timer moment in minutes.
     * @param progressDescription A string describing the fictional progress.
     * @return An XML string of the following format:
     *      <moment type="timer" id="[momentId]">
     *          <next_moment id="[nextMomentId]" />
     *          <length_minutes>[lengthMinutes]</length_minutes>
     *          <fictional_progress>[progressDescription]</fictional_progress>
     *      </moment>
     */
    private String createTimerMomentWithFictionalProgressXML(String momentId, String nextMomentId,
            double lengthMinutes, String progressDescription) {
        String xml = "";

        xml += createStartMomentXml("timer", momentId);
        xml += createNextMomentXml(nextMomentId);
        xml += createLengthMinutesXml(lengthMinutes);
        xml += createFictionalProgressXml(progressDescription);
        xml += createEndMomentXml();

        return xml;
    }

    /**
     * A helper function to create an XML string representing a sfx moment with fictional progress.
     * @param momentId The unique id of this moment.
     * @param nextMomentId The id of the next moment in the mission. Can be null if this moment is
     *                     the last moment in the mission.
     * @param pathToResource The absolute path to the sound resource to be played as part of this
     *                       moment.
     * @param progressDescription A string describing the fictional progress.
     * @return An XML string with the following format:
     *      <moment type="sfx" id="[momentId]">
     *          <next_moment id="[nextMomentId]" />
     *          <uri>[pathToResource]</uri>
     *          <fictional_progress>[progressDescription]</fictional_progress>
     *       </moment>
     */
    private String createSfxMomentWithFictionalProgressXML(String momentId, String nextMomentId,
            String pathToResource, String progressDescription) {
        String xml = "";

        xml += createStartMomentXml("sfx", momentId);
        xml += createNextMomentXml(nextMomentId);
        xml += "<uri>" + pathToResource + "</uri>";
        xml += createFictionalProgressXml(progressDescription);
        xml += createEndMomentXml();

        return xml;

    }

    /**
     * A helper function to create an XML string representing the start of a choice moment with
     * fictional progress.
     * @param momentId The unique id of the moment.
     * @param timeoutLengthMinutes The length of time in minutes after which the default choice
     *                             should be chosen for the user.
     * @param choiceDescription A meaningful, human-readable description of the choice to be made,
     *                          which is most likely in the form of either a direct or indirect
     *                          question.
     * @param defaultChoiceId The id of the choice that should be chosen if the decision times out
     *                        before the user has chosen their own choice.
     * @param progressDescription A string describing the fictional progress.
     * @return An XML string with the following format:
     *      <moment type="choice" id="[momentId]">
     *          <timeout_length_minutes>[timeoutLengthMinutes]</timeout_length_minutes>
     *          <description>[choiceDescription]</description>
     *          <default_choice id="[defaultChoiceId]"/>
     *          <fictional_progress>[progressDescription]</fictional_progress>
     */
    private String createStartChoiceMomentWithFictionalProgressXML (String momentId,
            double timeoutLengthMinutes, String choiceDescription, String defaultChoiceId,
            String progressDescription) {
        String xml = "";
        xml += createStartChoiceMomentXml(momentId, timeoutLengthMinutes, choiceDescription,
                defaultChoiceId);
        xml += createFictionalProgressXml(progressDescription);
        return xml;
    }

    /**
     * A helper function to create an XML string representing a choice.  Choices are found within
     * choice moments. Contains a piece of fictional progress.
     * @param choiceId The id of the choice.
     * @param choiceDescription A meaningful, human-readable description of the choice.
     * @param nextMomentId The id of the next moment if this choice is chosen.  Can be null if this
     *                     moment is the last moment in the mission.
     * @param depleteWeaponCharge Whether the user's weapon should be depleted if they select this
     *                            choice.
     * @param incrementNumEnemiesDefeated Whether the number of enemies defeated should be
     *                                    incremented if the user selects this choice.
     * @param iconResourceName The name of the drawable resource for this choice action.
     * @return An XML string with the following format:
     *      <choice id="[choiceId]">
     *          <description>[choiceDescription]</description>
     *          <next_moment id="[nextMomentId]"/>
     *          <outcome
     *              deplete_weapon="[depleteWeaponCharge]"
     *              increment_enemies="[incrementNumEnemiesDefeated]"/>
     *          <fictional_progress>[progressDescription]</fictional_progress>
     *          <icon name="[iconResourceName]" />
     *      </choice>
     */
    private String createChoiceWithFictionalProgressXML(String choiceId, String choiceDescription,
            String nextMomentId, boolean depleteWeaponCharge, boolean incrementNumEnemiesDefeated,
            String progressDescription, String iconResourceName) {
        String xml = "";

        xml += "<choice ";
        xml += "id='" + choiceId + "' >";
        xml += "<description>" + choiceDescription + "</description>";
        xml += createNextMomentXml(nextMomentId);
        xml += "<outcome ";
        xml += "deplete_weapon='" + Boolean.toString(depleteWeaponCharge) + "' ";
        xml += "increment_enemies='" + Boolean.toString(incrementNumEnemiesDefeated) + "' />";
        xml += createFictionalProgressXml(progressDescription);
        if (iconResourceName != null) {
            xml += createIconXML(iconResourceName);
        }
        xml += "</choice>";

        return xml;
    }
}
