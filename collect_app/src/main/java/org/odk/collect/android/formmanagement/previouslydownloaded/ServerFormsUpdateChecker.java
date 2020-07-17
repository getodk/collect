package org.odk.collect.android.formmanagement.previouslydownloaded;

import org.jetbrains.annotations.Nullable;
import org.odk.collect.android.formmanagement.ServerFormDetails;
import org.odk.collect.android.formmanagement.ServerFormsDetailsFetcher;
import org.odk.collect.android.forms.FormRepository;
import org.odk.collect.android.openrosa.api.FormApiException;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;

public class ServerFormsUpdateChecker {

    private final ServerFormsDetailsFetcher serverFormsDetailsFetcher;
    private final FormRepository formRepository;

    public ServerFormsUpdateChecker(ServerFormsDetailsFetcher serverFormsDetailsFetcher, FormRepository formRepository) {
        this.serverFormsDetailsFetcher = serverFormsDetailsFetcher;
        this.formRepository = formRepository;
    }

    public List<ServerFormDetails> check() {
        try {
            List<ServerFormDetails> updatedForms = fetchUpdatedForms();
            List<ServerFormDetails> newUpdates = new ArrayList<>();

            for (ServerFormDetails serverFormDetails : updatedForms) {
                String formHash = serverFormDetails.getHash();
                String manifestFileHash = serverFormDetails.getManifestFileHash() != null ? serverFormDetails.getManifestFileHash() : "";

                if (formRepository.getByLastDetectedUpdate(formHash, manifestFileHash) == null) {
                    newUpdates.add(serverFormDetails);
                    formRepository.setLastDetectedUpdated(serverFormDetails.getFormId(), formHash, manifestFileHash);
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
