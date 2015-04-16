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

package com.google.fpl.gim.examplegame.gui;

import android.support.v4.app.NotificationCompat;

import com.google.fpl.gim.examplegame.R;

/**
 * Options to build a notification from. Subset of all options that an Android notification may
 * have.
 */
public class NotificationOptions {
    private int mNotificationId;
    private int mSmallIconResourceId;
    private String mTitle;
    private String mContent;
    private NotificationCompat.Action[] mActions = null;
    private int mNotificationDefaults;
    private int mNotificationPriority;
    private long[] mVibratePattern;

    private static final int DEFAULT_ID = 1234;
    private static final int DEFAULT_SMALL_ICON_RESOURCE_ID = R.drawable.ic_notification;
    private static final String DEFAULT_TITLE = "Enemy!";
    private static final String DEFAULT_CONTENT = "An enemy appeared, what will you do?";
    // Phone notification settings for vibration, sound, and lights.
    private static final int APP_NOTIFICATION_DEFAULTS = NotificationCompat.DEFAULT_ALL;
    // Maximum priority, so it will be the first notification on the wearable device.
    private static final int DEFAULT_PRIORITY = NotificationCompat.PRIORITY_DEFAULT;

    /**
     * Default constructor.
     */
    public NotificationOptions() {
    }

    /**
     * Constructor to set all build options at creation.
     */
    public NotificationOptions(int notificationId, int smallIconResourceId, String title,
                               String content, NotificationCompat.Action[] actions,
                               int notificationDefaults, int notificationPriority,
                               long[] vibratePattern) {
        this.mNotificationId = notificationId;
        this.mSmallIconResourceId = smallIconResourceId;
        this.mTitle = title;
        this.mContent = content;
        this.mActions = actions;
        this.mNotificationDefaults = notificationDefaults;
        this.mNotificationPriority = notificationPriority;
        this.mVibratePattern = vibratePattern;
    }

    /**
     * Statically create default build options.
     *
     * @return Default build options for a notification.
     */
    public static NotificationOptions getDefaultNotificationOptions() {
        NotificationOptions options = new NotificationOptions();
        options.mNotificationId = DEFAULT_ID;
        options.mSmallIconResourceId = DEFAULT_SMALL_ICON_RESOURCE_ID;
        options.mTitle = DEFAULT_TITLE;
        options.mContent = DEFAULT_CONTENT;
        options.mNotificationDefaults = APP_NOTIFICATION_DEFAULTS;
        options.mNotificationPriority = DEFAULT_PRIORITY;
        return options;
    }

    /**
     * Small icon, title, and text must be set for all notifications.
     *
     * @param smallIconResourceId The resource Id of a picture to use as the small icon.
     * @param title     A string to use as the notification title.
     * @param content   A string to use as the notification content.
     */
    public void setRequiredOptions(int smallIconResourceId, String title,
                                   String content) {
        this.mSmallIconResourceId = smallIconResourceId;
        this.mTitle = title;
        this.mContent = content;
    }

    /**
     * Set actions for this notification.
     *
     * @param actions Actions associated with the notification.
     */
    public void setActions(NotificationCompat.Action[] actions) {
        this.mActions = actions;
    }

    /**
     * Notification defaults are ringtones, vibration patterns, and LED colors for the notification.
     *
     * @param notificationDefaults Defaults to use for this notification.
     */
    public void setNotificationDefaults(int notificationDefaults) {
        this.mNotificationDefaults = notificationDefaults;
    }

    /**
     * Priority determines the notification's position in the notification list.\
     * @param notificationPriority Priority to use for this notification.
     */
    public void setNotificationPriority(int notificationPriority) {
        this.mNotificationPriority = notificationPriority;
    }

    public void setNotificationId(int id) {
        this.mNotificationId = id;
    }

    public void setPriorityAsDefault() {
        mNotificationPriority = NotificationCompat.PRIORITY_DEFAULT;
    }

    public void setPriorityAsHigh() {
        mNotificationPriority = NotificationCompat.PRIORITY_HIGH;
    }

    public void setPriorityAsMax() {
        mNotificationPriority = NotificationCompat.PRIORITY_MAX;
    }

    public void setContent(String content) {
        this.mContent = content;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public void setVibratePattern(long[] vibratePattern) { this.mVibratePattern = vibratePattern; }

    public int getNotificationPriority() {
        return mNotificationPriority;
    }

    public int getNotificationId() {
        return mNotificationId;
    }

    public int getSmallIconResourceId() {
        return mSmallIconResourceId;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getContent() {
        return mContent;
    }

    public NotificationCompat.Action[] getActions() {
        return mActions;
    }

    public int getNotificationDefaults() {
        return mNotificationDefaults;
    }

    public long[] getVibratePattern() { return mVibratePattern; }
}