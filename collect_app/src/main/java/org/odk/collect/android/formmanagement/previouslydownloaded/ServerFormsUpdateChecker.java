package org.odk.collect.android.formmanagement.previouslydownloaded;

import org.jetbrains.annotations.Nullable;
import org.odk.collect.android.formmanagement.ServerFormDetails;
import org.odk.collect.android.formmanagement.ServerFormsDetailsFetcher;
import org.odk.collect.android.notifications.NotificationRepository;
import org.odk.collect.android.openrosa.api.FormApiException;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;

public class ServerFormsUpdateChecker {

    private final ServerFormsDetailsFetcher serverFormsDetailsFetcher;
    private final NotificationRepository notificationRepository;

    public ServerFormsUpdateChecker(ServerFormsDetailsFetcher serverFormsDetailsFetcher, NotificationRepository notificationRepository) {
        this.serverFormsDetailsFetcher = serverFormsDetailsFetcher;
        this.notificationRepository = notificationRepository;
    }

    public List<ServerFormDetails> check() {
        try {
            List<ServerFormDetails> updatedForms = fetchUpdatedForms();
           List<ServerFormDetails > newUpdates = new ArrayList<>();

            for (ServerFormDetails serverFormDetails : updatedForms) {
                String formHash = serverFormDetails.getHash();
                String manifestFileHash = serverFormDetails.getManifestFileHash() != null ? serverFormDetails.getManifestFileHash() : "";

                if (!notificationRepository.hasFormUpdateBeenNotified(formHash, manifestFileHash)) {
                    newUpdates.add(serverFormDetails);
                    notificationRepository.markFormUpdateNotified(serverFormDetails.getFormId(), formHash, manifestFileHash);
                }
            }

            return newUpdates;
        } catch (FormApiException e) {
            return emptyList();
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
}
