package org.odk.collect.android.tasks.sms;

import android.content.Context;
import android.telephony.SmsManager;

import org.odk.collect.android.application.Collect;

import javax.inject.Inject;

public class SmsService {
    private Context context;

    @Inject
    SmsManager smsManager;

    public SmsService(Context context) {
        this.context = context;

        Collect.getInstance().getComponent().inject(this);
    }

    public void sendMessage(String instanceId)
    {
        String text;


    }




}
