package org.odk.collect.android.forms;

import org.odk.collect.android.logic.FormDetails;
import org.odk.collect.android.utilities.FormDownloader;
import org.odk.collect.android.utilities.FormListDownloader;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;

public class ServerFormListSynchronizer {

    private final FormRepository formRepository;
    private final FormListDownloader formListDownloader;
    private final FormDownloader formDownloader;

    public ServerFormListSynchronizer(FormRepository formRepository, FormListDownloader formListDownloader, FormDownloader formDownloader) {
        this.formRepository = formRepository;
        this.formListDownloader = formListDownloader;
        this.formDownloader = formDownloader;
    }

    public void synchronize() {
        Collection<FormDetails> formsOnServer = formListDownloader.downloadFormList(true).values();
        List<Form> formsOnDevice = formRepository.getAll();

        deleteFormsNotServer(formsOnServer, formsOnDevice);
        downloadNewAndUpdatedForms(formsOnServer, formsOnDevice);
    }

    private void deleteFormsNotServer(Collection<FormDetails> formList, List<Form> formsOnDevice) {
        formsOnDevice.stream().forEach(form -> {
            if (formList.stream().noneMatch(f -> form.getJrFormId().equals(f.getFormId()))) {
                formRepository.delete(form.getId());
            }
        });
    }

    private void downloadNewAndUpdatedForms(Collection<FormDetails> formsOnServer, List<Form> formsOnDevice) {
        for (FormDetails formDetails : formsOnServer) {
            Optional<Form> formOnDevice = formsOnDevice.stream().filter(f -> f.getJrFormId().equals(formDetails.getFormId())).findFirst();

            if (!formOnDevice.isPresent() || !formOnDevice.get().getMD5Hash().equals(formDetails.getHash())) {
                formDownloader.downloadForms(asList(formDetails), null);
            }
        }
    }
}
