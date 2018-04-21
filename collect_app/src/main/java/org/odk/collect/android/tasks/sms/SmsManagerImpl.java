package org.odk.collect.android.tasks.sms;

import android.app.PendingIntent;
import android.telephony.SmsManager;

import java.util.List;

public class SmsManagerImpl {
    private SmsManager smsManager;

    public SmsManagerImpl(){
        smsManager = SmsManager.getDefault();
    }

    public List<String> divideMessage(String text)
    {
        return smsManager.divideMessage(text);
    }

    public void sendTextMessage(String destinationAddress, String scAddress, String text, PendingIntent sentIntent, PendingIntent deliveryIntent){
        smsManager.sendTextMessage(destinationAddress,scAddress,text,sentIntent,deliveryIntent);
    }
}
