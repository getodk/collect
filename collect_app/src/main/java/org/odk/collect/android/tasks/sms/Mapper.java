package org.odk.collect.android.tasks.sms;

import org.odk.collect.android.tasks.sms.models.MessageResultStatus;
import org.odk.collect.android.tasks.sms.models.MessageStatus;

public class Mapper {
    private Mapper() {

    }

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
