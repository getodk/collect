package org.odk.collect.android.sms;

import android.content.Intent;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.tasks.sms.SmsSentBroadcastReceiver;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowApplication;

import java.util.List;

import static org.odk.collect.android.tasks.sms.SmsSender.SMS_SEND_ACTION;

@RunWith(RobolectricTestRunner.class)
public class SmsSentBroadcastReceiverTest {

    @Test
    public void testBroadcastReceiverRegistered() {
        ShadowApplication application = ShadowApplication.getInstance();
        List<ShadowApplication.Wrapper> registeredReceivers = application.getRegisteredReceivers();

        Assert.assertFalse(registeredReceivers.isEmpty());

        boolean receiverFound = false;
        for (ShadowApplication.Wrapper wrapper : registeredReceivers) {
            if (!receiverFound) {
                receiverFound = SmsSentBroadcastReceiver.class.getSimpleName().equals(
                        wrapper.broadcastReceiver.getClass().getSimpleName());
            }
        }

        Assert.assertTrue(receiverFound); //will be false if not found
    }

    @Test
    public void testIntentHandling() {
        /**
         * Testing to see if the broadcast receiver will receive SMS events from Collect.
         */
        Intent intent = new Intent(SMS_SEND_ACTION);

        ShadowApplication shadowApplication = ShadowApplication.getInstance();
        Assert.assertTrue(shadowApplication.hasReceiverForIntent(intent));
    }
}
