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

package com.google.fpl.gim.examplegame.utils;

import android.net.Uri;

import com.google.fpl.gim.examplegame.Choice;
import com.google.fpl.gim.examplegame.ChoiceMoment;
import com.google.fpl.gim.examplegame.ChoiceMomentData;
import com.google.fpl.gim.examplegame.Mission;
import com.google.fpl.gim.examplegame.Moment;
import com.google.fpl.gim.examplegame.Outcome;
import com.google.fpl.gim.examplegame.SfxMoment;
import com.google.fpl.gim.examplegame.SfxMomentData;
import com.google.fpl.gim.examplegame.SpokenTextMoment;
import com.google.fpl.gim.examplegame.SpokenTextMomentData;
import com.google.fpl.gim.examplegame.TimerMoment;
import com.google.fpl.gim.examplegame.TimerMomentData;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * A class for parsing a mission in a file.
 */
public class MissionParser {
    private static final String TAG = MissionParser.class.getSimpleName();

    private static final String ELEMENT_MISSION = "mission";
    private static final String MISSION_ATTRIBUTE_START_ID = "start_id";
    private static final String MISSION_ATTRIBUTE_NAME = "name";

    private static final String ELEMENT_MOMENT = "moment";

    // Types of moments.
    private static final String MOMENT_TYPE_TIMER = "timer";
    private static final String MOMENT_TYPE_SFX = "sfx";
    private static final String MOMENT_TYPE_SPOKEN_TEXT = "spoken_text";
    private static final String MOMENT_TYPE_CHOICE = "choice";

    // Attributes for all moments.
    private static final String MOMENT_ATTRIBUTE_TYPE = "type";
    private static final String MOMENT_ATTRIBUTE_ID = "id";

    // Attributes for specific types of moments.
    private static final String ELEMENT_CHOICE = "choice";
    private static final String ELEMENT_DESCRIPTION = "description";
    private static final String ELEMENT_NEXT_MOMENT = "next_moment";
    private static final String NEXT_MOMENT_ATTRIBUTE_ID = "id";
    private static final String ELEMENT_LENGTH_MINUTES = "length_minutes";
    private static final String ELEMENT_TIMEOUT_LENGTH_MINUTES = "timeout_length_minutes";
    private static final String ELEMENT_DEFAULT_CHOICE = "default_choice";
    private static final String ELEMENT_URI = "uri";
    private static final String DEFAULT_CHOICE_ATTRIBUTE_ID  = "id";
    private static final String ELEMENT_OUTCOME = "outcome";
    private static final String CHOICE_ATTRIBUTE_ID = "id";
    private static final String ELEMENT_TEXT_TO_SPEAK = "text_to_speak";
    private static final String ELEMENT_FICTIONAL_PROGRESS = "fictional_progress";
    private static final String ELEMENT_ICON = "icon";
    private static final String ICON_ATTRIBUTE_NAME = "name";

    // Attributes for outcomes.
    private static final String OUTCOME_ATTRIBUTE_DEPLETE_WEAPON = "deplete_weapon";
    private static final String OUTCOME_ATTRIBUTE_INCREMENT_ENEMIES = "increment_enemies";

    // ID that signifies the moment whose next is this is an ending moment.
    private static final String DEFAULT_END_ID = null;

    // ID that signifies that this Choice should only be displayed when the user's weapon is
    // charged.
    public static final String FIRE_WEAPON_CHOICE_ID = "fire";

    /**
     * Adds the Moments that define a Mission to that Mission by reading from input. Assumes
     * XML file.
     * @param missionStream The InputStream to read from.
     * @param mission The Mission object to add Moments to.
     * @throws MissionParseException
     */
    public static void parseMission(InputStream missionStream, Mission mission) throws
            MissionParseException {

        Document doc = getDocumentFromInputStream(missionStream);

        doc.getDocumentElement().normalize();

        // Find the Mission's starting Moment.
        NodeList missionNodes = doc.getElementsByTagName(ELEMENT_MISSION);
        String startId = null;
        for (int i = 0; i < missionNodes.getLength(); i++) {
            Node missionNode = missionNodes.item(i);
            if (isElementNode(missionNode)) {
                startId = ((Element) missionNode).getAttribute(MISSION_ATTRIBUTE_START_ID);
                Utils.logDebug(TAG, "Start id is \"" + startId + "\".");
                break;
            }
        }

        mission.setFirstMomentId(startId);

        // Each Moment is represented as an Element
        NodeList momentsList = doc.getElementsByTagName(ELEMENT_MOMENT);

        for (int i = 0; i < momentsList.getLength(); i++) {
            Node momentNode = momentsList.item(i);
            // We check to make sure that we have Elements, though getElementsByTagName should
            // provide only Elements.
            if (isElementNode(momentNode)) {
                Moment moment;
                Element momentElement = (Element) momentNode;
                // Data for all Moments
                String id = momentElement.getAttribute(MOMENT_ATTRIBUTE_ID);
                String momentType = momentElement.getAttribute(MOMENT_ATTRIBUTE_TYPE);

                // Ideally would use Java 7 to switch on the strings themselves, but
                // project is not configured to use Java 7.
                if (momentType.equals(MOMENT_TYPE_CHOICE)) {
                    Utils.logDebug(TAG, "Choice moment created.");
                    ChoiceMomentData momentData = parseChoiceMomentElement(id, momentElement);
                    moment = new ChoiceMoment(mission, momentData);
                } else if (momentType.equals(MOMENT_TYPE_SFX)) {
                    Utils.logDebug(TAG, "Sfx moment created.");
                    SfxMomentData momentData = parseSfxMoment(id, momentElement);
                    moment = new SfxMoment(mission, momentData);
                } else if (momentType.equals(MOMENT_TYPE_TIMER)) {
                    Utils.logDebug(TAG, "Timer moment created.");
                    TimerMomentData momentData = parseTimerMoment(id, momentElement);
                    moment = new TimerMoment(mission, momentData);
                } else if (momentType.equals(MOMENT_TYPE_SPOKEN_TEXT)) {
                    Utils.logDebug(TAG, "Spoken text moment created.");
                    SpokenTextMomentData momentData = parseSpokenTextMoment(id, momentElement);
                    moment = new SpokenTextMoment(mission, momentData);
                } else {
                    throw new MissionParseException("Moment type invalid.");
                }

                mission.addMoment(id, moment);
            }
        }
    }

    /**
     * Everything in an XML document is represented as a Document Object Model Node. Elements are
     * defined between opening and closing tags, for example "<element></element>".
     * @param node a DOM Node from a Document
     * @return true if the DOM Node is an Element.
     */
    private static boolean isElementNode(Node node) {
        return node.getNodeType() == Node.ELEMENT_NODE;
    }

    /**
     * Parses an Element representing an SfxMoment to create SfxMomentData.
     * @param momentId Already parsed data about the Moment.
     * @param momentElement the DOM Element to be parsed.
     * @return data to construct an SfxMoment
     */
    private static SfxMomentData parseSfxMoment(String momentId, Element momentElement)
            throws MissionParseException {
        Uri uri = parseUriElement(findSingleChildElementByTag(momentElement, ELEMENT_URI));
        String nextMomentId = getNextMomentId(momentElement);

        ArrayList<String> fictionalProgress = parseMomentFictionalProgress(momentElement);

        return new SfxMomentData(momentId, nextMomentId, fictionalProgress, uri);
    }

    /**
     * Parses an Element representing a TimerMoment to create TimerMomentData.
     * @param momentId Already parsed data about the Moment.
     * @param momentElement the DOM Element to be parsed.
     * @return data to construct a TimerMoment
     */
    private static TimerMomentData parseTimerMoment(String momentId, Element momentElement)
            throws MissionParseException {
        float momentLengthMinutes = parseLengthMinutesElement(
                findSingleChildElementByTag(momentElement, ELEMENT_LENGTH_MINUTES));

        String nextMomentId = getNextMomentId(momentElement);

        ArrayList<String> fictionalProgress = parseMomentFictionalProgress(momentElement);

        return new TimerMomentData(momentId, nextMomentId, fictionalProgress, momentLengthMinutes);
    }

    /**
     * Parses an Element representing a SpokenTextMoment to create SpokenTextMomentData.
     * @param momentId Already parsed data about the Moment.
     * @param momentElement the DOM Element to be parsed.
     * @return data to construct an SpokenTextMoment
     */
    private static SpokenTextMomentData parseSpokenTextMoment(String momentId,
            Element momentElement) throws MissionParseException {
        String textToSpeak = parseTextToSpeakElement(
                findSingleChildElementByTag(momentElement, ELEMENT_TEXT_TO_SPEAK));

        String nextMomentId = getNextMomentId(momentElement);

        ArrayList<String> fictionalProgress = parseMomentFictionalProgress(momentElement);

        return new SpokenTextMomentData(momentId, nextMomentId, fictionalProgress, textToSpeak);
    }

    /**
     * Parses an Element representing a ChoiceMoment to create ChoiceMomentData.
     * @param momentId Already parsed data about the Moment.
     * @param momentElement the DOM Element to be parsed.
     * @return data to construct an ChoiceMoment
     */
    private static ChoiceMomentData parseChoiceMomentElement(String momentId,
            Element momentElement) throws MissionParseException {
        String description = getDescription(momentElement);

        float timeoutLengthMinutes = parseTimeoutLengthMinutesElement(
                findSingleChildElementByTag(momentElement, ELEMENT_TIMEOUT_LENGTH_MINUTES));

        String defaultChoiceId = parseDefaultChoiceElement(
                findSingleChildElementByTag(momentElement, ELEMENT_DEFAULT_CHOICE));

        ArrayList<String> fictionalProgress = parseMomentFictionalProgress(momentElement);

        ChoiceMomentData data = new ChoiceMomentData(momentId, fictionalProgress, description,
                defaultChoiceId, timeoutLengthMinutes);

        NodeList choiceNodes = momentElement.getElementsByTagName(ELEMENT_CHOICE);
        if (choiceNodes.getLength() > ChoiceMoment.MAXIMUM_NUM_OF_CHOICES) {
            throw new MissionParseException("ChoiceMoments can have no more than "
                    + ChoiceMoment.MAXIMUM_NUM_OF_CHOICES + " Choices.");
        }
        if (choiceNodes.getLength() < ChoiceMoment.MINIMUM_NUM_OF_CHOICES) {
            throw new MissionParseException("ChoiceMoments can have no fewer than "
                    + ChoiceMoment.MINIMUM_NUM_OF_CHOICES + " Choices.");
        }
        boolean defaultChoiceIsExistingChoice = false;
        for (int i = 0; i < choiceNodes.getLength(); i++) {
            Node choiceNode = choiceNodes.item(i);
            if (isElementNode(choiceNode)) {
                Choice choice = parseChoiceElement((Element) choiceNode);
                if (choice.getChoiceId() == defaultChoiceId ||
                        choice.getChoiceId().equals(defaultChoiceId)) {
                    defaultChoiceIsExistingChoice = true;
                }
                data.addChoice(choice);
            }
        }
        if (!defaultChoiceIsExistingChoice) {
            throw new MissionParseException("Default choice ID is not a valid Choice.");
        }

        return data;
    }

    /**
     * Creates a Choice from a DOM Element representing a Choice.
     * @param choiceElement The DOM element representing a Choice.
     * @return a Choice as defined in the DOM Element.
     */
    private static Choice parseChoiceElement(Element choiceElement) throws MissionParseException {
        String id = choiceElement.getAttribute(CHOICE_ATTRIBUTE_ID);

        String description = getDescription(choiceElement);

        String nextMomentId = getNextMomentId(choiceElement);

        Outcome outcome =
                parseOutcomeElement(findSingleChildElementByTag(choiceElement, ELEMENT_OUTCOME));

        boolean requiresChargedWeapon = false;
        if (id.equals(MissionParser.FIRE_WEAPON_CHOICE_ID)) {
            requiresChargedWeapon = true;
        }

        ArrayList<String> fictionalProgress = parseNestedFictionalProgress(choiceElement);

        String iconName =
                parseIconElement(findSingleChildElementByTag(choiceElement, ELEMENT_ICON));
        return new Choice(id, description, nextMomentId, outcome, requiresChargedWeapon,
                fictionalProgress, iconName);
    }

    /**
     * Creates an Outcome from a DOM Element representing an Outcome.
     * @param outcomeElement The DOM element representing an Outcome.
     * @return an Outcome as defined in the DOM Element.
     */
    private static Outcome parseOutcomeElement(Element outcomeElement) {
        boolean depleteWeapon =
                Boolean.valueOf(outcomeElement.getAttribute(OUTCOME_ATTRIBUTE_DEPLETE_WEAPON));
        boolean incrementEnemies =
                Boolean.valueOf(outcomeElement.getAttribute(OUTCOME_ATTRIBUTE_INCREMENT_ENEMIES));
        return new Outcome(depleteWeapon, incrementEnemies);
    }

    /**
     * Given an XML element that has a nested next_moment element, finds the next moment ID.
     * @param element An XML element.
     * @return The next moment ID for the element.
     */
    private static String getNextMomentId(Element element) throws MissionParseException {
        Element nextMomentElement = findSingleChildElementByTag(element, ELEMENT_NEXT_MOMENT);
        // If the element does not have a next moment, then set to the default end signifier.
        if (nextMomentElement == null) {
            return DEFAULT_END_ID;
        }
        return parseNextMomentElement(nextMomentElement);
    }

    /**
     * Given an XML element that has a nested description element, finds the description.
     * @param element An XML element.
     * @return The description for the element.
     */
    private static String getDescription(Element element) throws MissionParseException {
        return parseDescriptionElement(findSingleChildElementByTag(element, ELEMENT_DESCRIPTION));
    }

    /**
     * Finds the first occurrence of a child Element of a given tag within a parent's tree.
     * @param parent An Element with child Elements
     * @param tag The Element to find
     * @return The first occurrence of an Element of type tag within the parent's child tree.
     */
    private static Element findSingleChildElementByTag(Element parent, String tag)
            throws MissionParseException {
        Node node = null;
        NodeList nodes = parent.getElementsByTagName(tag);
        for (int i = 0; i < nodes.getLength(); i++) {
            node = nodes.item(i);
            if (isElementNode(node)) {
                break;
            }
            node = null;
        }
        // All attributes are required except the 'next_moment' attribute.  The lack of a
        // 'next_moment' attribute signifies that the moment is the last moment in the mission.
        if (!tag.equals(ELEMENT_NEXT_MOMENT) && node == null) {
            throw new MissionParseException(tag + " could not be found.");
        }
        return (Element) node;
    }

    private static String parseNextMomentElement(Element nextMomentElement) {
        String nextMomentId = nextMomentElement.getAttribute(NEXT_MOMENT_ATTRIBUTE_ID);
        if (nextMomentId == null || nextMomentId.equals("")) {
            return DEFAULT_END_ID;
        }
        return nextMomentId;
    }

    private static float parseLengthMinutesElement(Element lengthMinutesElement)
            throws MissionParseException {
        Node node = lengthMinutesElement.getFirstChild();
        if (node == null) {
            throw new MissionParseException("Length minutes element could not be found.");
        }
        return Float.parseFloat(node.getTextContent());
    }

    private static Uri parseUriElement(Element uriElement) throws MissionParseException {
        Node node = uriElement.getFirstChild();
        if (node == null) {
            throw new MissionParseException("URI element could not be found.");
        }
        return Uri.parse(node.getTextContent());
    }

    private static String parseTextToSpeakElement(Element textToSpeakElement)
            throws MissionParseException {
        Node node = textToSpeakElement.getFirstChild();
        if (node == null) {
            throw new MissionParseException("Text to speak element could not be found.");
        }
        return node.getTextContent();
    }

    private static String parseDescriptionElement(Element descriptionElement)
            throws MissionParseException {
        Node node = descriptionElement.getFirstChild();
        if (node == null) {
            throw new MissionParseException("Description element could not be found.");
        }
        return descriptionElement.getTextContent();
    }

    private static float parseTimeoutLengthMinutesElement(Element timeoutLengthMinutesElement)
            throws MissionParseException {
        Node node = timeoutLengthMinutesElement.getFirstChild();
        if (node == null) {
            throw new MissionParseException("Timeout length minutes element could not be found.");
        }
        return Float.parseFloat(timeoutLengthMinutesElement.getFirstChild().getTextContent());
    }

    private static String parseDefaultChoiceElement(Element defaultChoiceElement)
            throws MissionParseException {
        String defaultChoice = defaultChoiceElement.getAttribute(DEFAULT_CHOICE_ATTRIBUTE_ID);
        if (defaultChoice == null) {
            throw new MissionParseException("Default choice element could not be found.");
        }
        if (defaultChoice.equals(FIRE_WEAPON_CHOICE_ID)) {
            throw new MissionParseException("Default choice cannot be 'fire'.");
        }
        return defaultChoice;
    }

    /**
     * Determines the name of a mission.
     * @param missionStream InputStream to read from.
     * @return A string of the name of the mission. Empty string if a name is not specified.
     * @throws MissionParseException
     */
    public static String getMissionName(InputStream missionStream) throws  MissionParseException {

        Document doc = getDocumentFromInputStream(missionStream);
        doc.getDocumentElement().normalize();

        NodeList missionNodes = doc.getElementsByTagName(ELEMENT_MISSION);
        String missionName;
        for (int i = 0; i < missionNodes.getLength(); i++) {
            Node missionNode = missionNodes.item(i);
            if (isElementNode(missionNode)) {
                // Gives an empty string if the attribute is missing.
                missionName = ((Element) missionNode).getAttribute(MISSION_ATTRIBUTE_NAME);
                if (missionName.equals("")) {
                    throw new MissionParseException("Mission name missing.");
                }
                Utils.logDebug(TAG, "Mission name is " + missionName);
                return missionName;
            }
        }

        // No Element Node for Mission.
        throw new MissionParseException("Mission element could not be found.");
    }

    /**
     * Creates a Document given an InputStream.
     * @param missionStream The stream to open a document from.
     * @return The document, successfully parsed from xml.
     * @throws MissionParseException
     */
    private static Document getDocumentFromInputStream(InputStream missionStream) throws
            MissionParseException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            throw new MissionParseException("ParserConfigurationException while reading mission.");
        }

        Document doc;
        try {
            doc = builder.parse(missionStream);
        } catch (SAXException e) {
            e.printStackTrace();
            throw new MissionParseException("SAXException while reading mission.");
        } catch (IOException e) {
            e.printStackTrace();
            throw new MissionParseException("IOException  while reading mission.");
        }

        return doc;
    }

    /**
     * Determines the Fictional Progress strings associated with a moment.
     * @param momentElement The moment to parse.
     * @return A list of Fictional Progress strings.
     */
    private static ArrayList<String> parseMomentFictionalProgress(Element momentElement)
            throws MissionParseException {
        ArrayList<String> progress = new ArrayList<>();
        NodeList fictionalProgressNodes =
                momentElement.getElementsByTagName(ELEMENT_FICTIONAL_PROGRESS);
        for (int i = 0; i < fictionalProgressNodes.getLength(); i++) {
            Node node = fictionalProgressNodes.item(i);
            Element parent = (Element) node.getParentNode();
            if (isElementNode(node) && parent.getTagName().equals(ELEMENT_MOMENT)) {
                String progressString = parseFictionalProgressElement((Element) node);
                progress.add(progressString);
            }
        }
        return progress;
    }

    private static String parseFictionalProgressElement(Element element)
            throws MissionParseException {
        Node node = element.getFirstChild();
        if (node == null) {
            throw new MissionParseException("Fictional Progress Element not found");
        }
        String progressString = element.getFirstChild().getTextContent();
        if (progressString.equals("")) {
            throw new MissionParseException("Fictional Progress empty.");
        }
        return progressString;
    }

    private static ArrayList<String> parseNestedFictionalProgress(Element parent)
            throws MissionParseException {
        ArrayList<String> progressStrings = new ArrayList<>();
        NodeList nodeList = parent.getElementsByTagName(ELEMENT_FICTIONAL_PROGRESS);
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (isElementNode(node)) {
                String progressString = parseFictionalProgressElement((Element) node);
                progressStrings.add(progressString);
            }
        }
        return progressStrings;
    }

    private static String parseIconElement(Element iconElement) throws MissionParseException {
        String iconResourceName = iconElement.getAttribute(ICON_ATTRIBUTE_NAME);
        if (iconResourceName == null || iconResourceName.equals("")) {
            throw new MissionParseException("Icon element has no name attribute.");
        }
        return iconResourceName;
    }
}
