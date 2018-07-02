package org.odk.collect.android.tasks.sms.models;

public enum MessageStatus {
    Sending,
    Sent,
    AirplaneMode,
    NoReception,
    FatalError,
    Ready,
    NoMessage,
    Canceled,
    InvalidGateway,
    Queued,
    Delivered,
    NotDelivered,
    Encrypted;

    public static MessageStatus toMessageStatus(MessageResultStatus status) {
        switch (status) {
            case AirplaneMode:
                return MessageStatus.AirplaneMode;
            case Sent:
                return MessageStatus.Sent;
            case FatalError:
                return MessageStatus.FatalError;
            case NoReception:
                return MessageStatus.NoReception;
        }

        return null;
    }
}
