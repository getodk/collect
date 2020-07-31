package org.odk.collect.android.notifications;

import org.odk.collect.android.formmanagement.ServerFormDetails;
import org.odk.collect.android.openrosa.api.FormApiException;

import java.util.HashMap;

public interface Notifier {

    void onUpdatesAvailable();

    void onUpdatesDownloaded(HashMap<ServerFormDetails, String> result);

    void onSync(FormApiException exception);

    void onSubmission(boolean failure, String message);
}
