package org.odk.collect.android.notifications;

import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.openrosa.api.FormApiException;
import org.robolectric.shadows.ShadowNotificationManager;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.robolectric.Shadows.shadowOf;

@RunWith(AndroidJUnit4.class)
public class NotificationManagerNotifierTest {

    @Test
    public void onSync_whenExceptionNull_clearsNotification() {
        Application context = ApplicationProvider.getApplicationContext();
        ShadowNotificationManager shadowNotificationManager = shadowOf((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE));
        NotificationManagerNotifier notifier = new NotificationManagerNotifier(context);

        notifier.onSync(new FormApiException(FormApiException.Type.FETCH_ERROR));
        assertThat(shadowNotificationManager.getAllNotifications().size(), is(1));

        notifier.onSync(null);
        assertThat(shadowNotificationManager.getAllNotifications().size(), is(0));
    }
}