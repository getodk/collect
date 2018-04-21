package org.odk.collect.android.tasks.sms.contracts;

import android.app.PendingIntent;

import java.util.List;

public interface SmsManagerContract {

    List<String> divideMessage(String text);

    void sendTextMessage(String destinationAddress, String scAddress, String text, PendingIntent sentIntent, PendingIntent deliveryIntent);
}
