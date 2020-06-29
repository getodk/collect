package org.odk.collect.android.forms;

import org.odk.collect.android.openrosa.api.FormAPI;
import org.odk.collect.android.openrosa.api.FormAPIError;
import org.odk.collect.android.openrosa.api.FormListItem;
import org.odk.collect.android.utilities.FormDownloader;

import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.odk.collect.android.logic.FormDetails.toFormDetails;

public class ServerFormListSynchronizer {

    private final FormRepository formRepository;
    private final FormAPI formAPI;
    private final FormDownloader formDownloader;

    public ServerFormListSynchronizer(FormRepository formRepository, FormAPI formAPI, FormDownloader formDownloader) {
        this.formRepository = formRepository;
        this.formAPI = formAPI;
        this.formDownloader = formDownloader;
    }

    public void synchronize() {
        try {
            List<FormListItem> formsOnServer = formAPI.fetchFormList();
            List<Form> formsOnDevice = formRepository.getAll();

            deleteFormsNotServer(formsOnServer, formsOnDevice);
            downloadNewAndUpdatedForms(formsOnServer, formsOnDevice);
        } catch (FormAPIError ignored) {
            // ignored
        }
    }

    private void deleteFormsNotServer(List<FormListItem> formList, List<Form> formsOnDevice) {
        formsOnDevice.stream().forEach(form -> {
            if (formList.stream().noneMatch(f -> form.getJrFormId().equals(f.getFormID()))) {
                formRepository.delete(form.getId());
            }
        });
    }

    private void downloadNewAndUpdatedForms(List<FormListItem> formsOnServer, List<Form> formsOnDevice) {
        for (FormListItem formListItem : formsOnServer) {
            Optional<Form> formOnDevice = formsOnDevice.stream().filter(f -> f.getJrFormId().equals(formListItem.getFormID())).findFirst();

            if (!formOnDevice.isPresent() || !formOnDevice.get().getMD5Hash().equals(formListItem.getHash())) {
                formDownloader.downloadForms(asList(toFormDetails(formListItem)), null);
            }
        }
    }
}
