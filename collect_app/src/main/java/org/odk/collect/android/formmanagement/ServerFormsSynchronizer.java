package org.odk.collect.android.formmanagement;

import org.odk.collect.android.forms.Form;
import org.odk.collect.android.forms.FormRepository;
import org.odk.collect.android.forms.MediaFileRepository;
import org.odk.collect.android.openrosa.api.FormApiException;
import org.odk.collect.android.openrosa.api.FormListApi;

import java.util.List;

public class ServerFormsSynchronizer {

    private final FormRepository formRepository;
    private final FormDownloader formDownloader;
    private final ServerFormsDetailsFetcher serverFormsDetailsFetcher;

    public ServerFormsSynchronizer(FormRepository formRepository, MediaFileRepository mediaFileRepository, FormListApi formListAPI, FormDownloader formDownloader, DiskFormsSynchronizer diskFormsSynchronizer) {
        this.formRepository = formRepository;
        this.formDownloader = formDownloader;
        this.serverFormsDetailsFetcher = new ServerFormsDetailsFetcher(formRepository, mediaFileRepository, formListAPI, diskFormsSynchronizer);
    }

    public ServerFormsSynchronizer(ServerFormsDetailsFetcher serverFormsDetailsFetcher, FormRepository formRepository, FormDownloader formDownloader) {
        this.serverFormsDetailsFetcher = serverFormsDetailsFetcher;
        this.formRepository = formRepository;
        this.formDownloader = formDownloader;
    }

    public void synchronize() throws FormApiException {
        List<ServerFormDetails> formList = serverFormsDetailsFetcher.fetchFormDetails();
        List<Form> formsOnDevice = formRepository.getAll();

        formsOnDevice.stream().forEach(form -> {
            if (formList.stream().noneMatch(f -> form.getJrFormId().equals(f.getFormId()))) {
                formRepository.delete(form.getId());
            }
        });

        for (ServerFormDetails form : formList) {
            if (form.isNotOnDevice() || form.isUpdated()) {
                formDownloader.downloadForm(form);
            }
        }
    }
}
