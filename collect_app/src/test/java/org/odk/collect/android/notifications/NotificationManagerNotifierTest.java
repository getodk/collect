package org.odk.collect.android.notifications;

import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.odk.collect.android.TestSettingsProvider;
import org.odk.collect.android.formmanagement.ServerFormDetails;
import org.odk.collect.forms.FormSourceException;
import org.odk.collect.forms.ManifestFile;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.robolectric.Shadows.shadowOf;

@RunWith(AndroidJUnit4.class)
public class NotificationManagerNotifierTest {

    private NotificationManagerNotifier notifier;
    private NotificationManager notificationManager;

    @Before
    public void setup() {
        Application context = ApplicationProvider.getApplicationContext();
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notifier = new NotificationManagerNotifier(context, TestSettingsProvider.getSettingsProvider());
    }

    @Test
    public void onSync_whenExceptionNull_clearsNotification() {
        notifier.onSync(new FormSourceException.FetchError());
        assertThat(shadowOf(notificationManager).getAllNotifications().size(), is(1));

        notifier.onSync(null);
        assertThat(shadowOf(notificationManager).getAllNotifications().size(), is(0));
    }

    @Test
    public void onUpdatesAvailable_whenUpdatesHaveBeenSeenBefore_doesNotNotifyASecondTime() {
        List<ServerFormDetails> updates = asList(
                new ServerFormDetails("form-1", "http://example.com/form-1", "form-1", "server", "md5:form-1-hash", false, true, null)
        );

        notifier.onUpdatesAvailable(updates);
        assertThat(shadowOf(notificationManager).getAllNotifications().size(), is(1));

        notificationManager.cancelAll();
        notifier.onUpdatesAvailable(updates);
        assertThat(shadowOf(notificationManager).getAllNotifications().size(), is(0));
    }

    @Test
    public void onUpdatesAvailable_whenUpdateForFormHasBeenHasNewHash_notifies() {
        List<ServerFormDetails> updates = asList(
                new ServerFormDetails("form-1", "http://example.com/form-1", "form-1", "server", "md5:form-1-hash", false, true, null)
        );

        notifier.onUpdatesAvailable(updates);
        assertThat(shadowOf(notificationManager).getAllNotifications().size(), is(1));

        updates = asList(
                new ServerFormDetails("form-1", "http://example.com/form-1", "form-1", "server", "md5:form-1-hash-changed", false, true, null)
        );

        notificationManager.cancelAll();
        notifier.onUpdatesAvailable(updates);
        assertThat(shadowOf(notificationManager).getAllNotifications().size(), is(1));
    }

    @Test
    public void onUpdatesAvailable_whenUpdateForFormHasBeenHasNewManifestHash_notifies() {
        List<ServerFormDetails> updates = asList(
                new ServerFormDetails("form-1", "http://example.com/form-1", "form-1", "server", "md5:form-1-hash", false, true, new ManifestFile("manifest-hash", emptyList()))
        );

        notifier.onUpdatesAvailable(updates);
        assertThat(shadowOf(notificationManager).getAllNotifications().size(), is(1));

        updates = asList(
                new ServerFormDetails("form-1", "http://example.com/form-1", "form-1", "server", "md5:form-1-hash", false, true, new ManifestFile("manifest-hash-changed", emptyList()))
        );

        notificationManager.cancelAll();
        notifier.onUpdatesAvailable(updates);
        assertThat(shadowOf(notificationManager).getAllNotifications().size(), is(1));
    }
}
