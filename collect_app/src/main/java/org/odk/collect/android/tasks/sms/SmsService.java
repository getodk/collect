package org.odk.collect.android.tasks.sms;

import android.content.Context;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.tasks.sms.contracts.SmsManagerContract;

import javax.inject.Inject;

public class SmsService {
    private Context context;

    @Inject
    SmsManagerContract smsManager;

    public SmsService(Context context) {
        this.context = context;

        Collect.getInstance().getComponent().inject(this);
    }


}
