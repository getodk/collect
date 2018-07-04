package org.odk.collect.android.tasks.sms.models;


import android.os.Parcel;
import android.os.Parcelable;

/**
 * Encapsulates all the data that's received when the SmsSentBroadcastReceiver
 * is triggered. This is then passed to the SmsService and Notification Receiver for processing.
 */
public class SentMessageResult implements Parcelable {
    private SmsSendResultStatus smsSendResultStatus;
    private int messageId;
    private String instanceId;

    public SmsSendResultStatus getSmsSendResultStatus() {
        return smsSendResultStatus;
    }

    public void setSmsSendResultStatus(SmsSendResultStatus smsSendResultStatus) {
        this.smsSendResultStatus = smsSendResultStatus;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.smsSendResultStatus == null ? -1 : this.smsSendResultStatus.ordinal());
        dest.writeInt(this.messageId);
        dest.writeString(this.instanceId);
    }

    public SentMessageResult() {
    }

    private SentMessageResult(Parcel in) {
        int tmpMessageResultStatus = in.readInt();
        this.smsSendResultStatus = tmpMessageResultStatus == -1 ? null : SmsSendResultStatus.values()[tmpMessageResultStatus];
        this.messageId = in.readInt();
        this.instanceId = in.readString();
    }

    public static final Parcelable.Creator<SentMessageResult> CREATOR = new Parcelable.Creator<SentMessageResult>() {
        @Override
        public SentMessageResult createFromParcel(Parcel source) {
            return new SentMessageResult(source);
        }

        @Override
        public SentMessageResult[] newArray(int size) {
            return new SentMessageResult[size];
        }
    };
}
