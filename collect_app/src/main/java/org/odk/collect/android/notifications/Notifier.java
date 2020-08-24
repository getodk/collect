package org.odk.collect.android.notifications;

import org.odk.collect.android.formmanagement.ServerFormDetails;
import org.odk.collect.android.openrosa.api.FormApiException;

import java.util.HashMap;
import java.util.List;

public interface Notifier {

    void onUpdatesAvailable(List<ServerFormDetails> updates);

    void onUpdatesDownloaded(HashMap<ServerFormDetails, String> result);

    void onSync(FormApiException exception);

    void onSubmission(boolean failure, String message);
}
