package org.odk.collect.android.notifications;

import org.odk.collect.android.formmanagement.ServerFormDetails;

import java.util.HashMap;

public interface Notifier {

    void onUpdatesAvailable();

    void onUpdatesDownloaded(HashMap<ServerFormDetails, String> result);
}
