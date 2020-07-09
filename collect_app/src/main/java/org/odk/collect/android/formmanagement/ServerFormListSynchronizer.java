package org.odk.collect.android.formmanagement;

import org.odk.collect.android.forms.Form;
import org.odk.collect.android.forms.FormRepository;
import org.odk.collect.android.forms.MediaFileRepository;
import org.odk.collect.android.openrosa.api.FormApiException;
import org.odk.collect.android.openrosa.api.FormListApi;

import java.util.List;

public class ServerFormListSynchronizer {

    private final FormRepository formRepository;
    private final MediaFileRepository mediaFileRepository;
    private final FormListApi formListAPI;
    private final FormDownloader formDownloader;

    public ServerFormListSynchronizer(FormRepository formRepository, MediaFileRepository mediaFileRepository, FormListApi formListAPI, FormDownloader formDownloader) {
        this.formRepository = formRepository;
        this.mediaFileRepository = mediaFileRepository;
        this.formListAPI = formListAPI;
        this.formDownloader = formDownloader;
    }

    public void synchronize() throws FormApiException {
        ServerFormsDetailsFetcher listDownloader = new ServerFormsDetailsFetcher(formRepository, mediaFileRepository, formListAPI);
        List<ServerFormDetails> formList = listDownloader.fetchFormDetails();

        List<Form> formsOnDevice = formRepository.getAll();

        formsOnDevice.stream().forEach(form -> {
            if (formList.stream().noneMatch(f -> form.getJrFormId().equals(f.getFormId()))) {
                formRepository.delete(form.getId());
            }
        });

        for (ServerFormDetails form : formList) {
            boolean onDevice = formsOnDevice.stream().anyMatch(f -> f.getJrFormId().equals(form.getFormId()));

            if (!onDevice || form.isNewerFormVersionAvailable() || form.areNewerMediaFilesAvailable()) {
                formDownloader.downloadForm(form);
            }
        }
    }
}
