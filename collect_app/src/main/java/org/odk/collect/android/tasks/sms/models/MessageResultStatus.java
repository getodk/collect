package org.odk.collect.android.tasks.sms.models;

/**
 * Enum that represents the result from the SmsSentBroadcastReceiver
 */
public enum MessageResultStatus {
    Sent,
    AirplaneMode,
    NoReception,
    FatalError,
    NotDelivered,
    Delivered
}
