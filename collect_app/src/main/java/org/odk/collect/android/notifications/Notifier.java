package org.odk.collect.android.notifications;

import org.odk.collect.android.formmanagement.ServerFormDetails;
import org.odk.collect.android.formmanagement.matchexactly.SyncException;

import java.util.HashMap;

public interface Notifier {

    void onUpdatesAvailable();

    void onUpdatesDownloaded(HashMap<ServerFormDetails, String> result);

    void onSyncFailure(SyncException exception);
}
