package org.odk.collect.android.formmanagement.previouslydownloaded;

import org.jetbrains.annotations.Nullable;
import org.odk.collect.android.formmanagement.ServerFormDetails;
import org.odk.collect.android.formmanagement.ServerFormsDetailsFetcher;
import org.odk.collect.android.notifications.NotificationRepository;
import org.odk.collect.android.notifications.Notifier;
import org.odk.collect.android.openrosa.api.FormApiException;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.utilities.MultiFormDownloader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.odk.collect.android.preferences.GeneralKeys.KEY_AUTOMATIC_UPDATE;

public class ServerFormsUpdateNotifier {

    private final MultiFormDownloader multiFormDownloader;
    private final ServerFormsDetailsFetcher serverFormsDetailsFetcher;
    private final NotificationRepository notificationRepository;
    private final Notifier notifier;

    public ServerFormsUpdateNotifier(MultiFormDownloader multiFormDownloader, ServerFormsDetailsFetcher serverFormsDetailsFetcher, NotificationRepository notificationRepository, Notifier notifier) {
        this.multiFormDownloader = multiFormDownloader;
        this.serverFormsDetailsFetcher = serverFormsDetailsFetcher;
        this.notificationRepository = notificationRepository;
        this.notifier = notifier;
    }

    public void checkAndNotify() {
        try {
            List<ServerFormDetails> newDetectedForms = fetchUpdatedForms();

            if (!newDetectedForms.isEmpty()) {
                if (GeneralSharedPreferences.getInstance().getBoolean(KEY_AUTOMATIC_UPDATE, false)) {
                    final HashMap<ServerFormDetails, String> result = multiFormDownloader.downloadForms(newDetectedForms, null);
                    notifyAboutDownloadedForms(result);
                } else {
                    notifyAboutUpdatedForms(newDetectedForms);
                }
            }
        } catch (FormApiException e) {
            return;
        }
    }

    @Nullable
    @SuppressWarnings("PMD.AvoidRethrowingException")
    private List<ServerFormDetails> fetchUpdatedForms() throws FormApiException {
        List<ServerFormDetails> formList = null;

        try {
            formList = serverFormsDetailsFetcher.fetchFormDetails();
        } catch (FormApiException e) {
            switch (e.getType()) {
                case AUTH_REQUIRED:
                    try {
                        serverFormsDetailsFetcher.fetchFormDetails();
                    } catch (FormApiException ex) {
                        throw ex;
                    }

                    break;

                case FETCH_ERROR:
                case PARSE_ERROR:
                case LEGACY_PARSE_ERROR:
                    throw e;
            }
        }

        List<ServerFormDetails> newDetectedForms = new ArrayList<>();
        for (ServerFormDetails serverFormDetails : formList) {
            if (serverFormDetails.isUpdated()) {
                newDetectedForms.add(serverFormDetails);
            }
        }

        return newDetectedForms;
    }

    private void notifyAboutUpdatedForms(List<ServerFormDetails> newDetectedForms) {
        boolean needsNotification = false;

        for (ServerFormDetails serverFormDetails : newDetectedForms) {
            String formHash = serverFormDetails.getHash();
            String manifestFileHash = serverFormDetails.getManifestFileHash() != null ? serverFormDetails.getManifestFileHash() : "";

            if (!notificationRepository.hasFormUpdateBeenNotified(formHash, manifestFileHash)) {
                needsNotification = true;
                notificationRepository.markFormUpdateNotified(serverFormDetails.getFormId(), formHash, manifestFileHash);
            }
        }

        if (needsNotification) {
            notifier.onUpdatesAvailable();
        }
    }

    private void notifyAboutDownloadedForms(HashMap<ServerFormDetails, String> result) {
        notifier.onUpdatesDownloaded(result);
    }
}
