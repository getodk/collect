package org.odk.collect.android.tasks.sms.models;

/**
 * Enum that represents the result from the SmsSentBroadcastReceiver
 */
public enum SmsSendResultStatus {
    Sent,
    AirplaneMode,
    NoReception,
    FatalError,
    NotDelivered,
    Delivered;

    public  SmsStatus toMessageStatus() {
        switch (this) {
            case AirplaneMode:
                return SmsStatus.AirplaneMode;
            case Sent:
                return SmsStatus.Sent;
            case FatalError:
                return SmsStatus.FatalError;
            case NoReception:
                return SmsStatus.NoReception;
        }

        return null;
    }
}
