package org.odk.collect.android.formmanagement;

import org.odk.collect.android.forms.Form;
import org.odk.collect.android.forms.FormRepository;
import org.odk.collect.android.forms.MediaFileRepository;
import org.odk.collect.android.logic.FormDetails;
import org.odk.collect.android.openrosa.api.FormAPI;

import java.util.HashMap;
import java.util.List;

public class ServerFormListSynchronizer {

    private final FormRepository formRepository;
    private final MediaFileRepository mediaFileRepository;
    private final FormAPI formAPI;
    private final FormDownloader formDownloader;

    public ServerFormListSynchronizer(FormRepository formRepository, MediaFileRepository mediaFileRepository, FormAPI formAPI, FormDownloader formDownloader) {
        this.formRepository = formRepository;
        this.mediaFileRepository = mediaFileRepository;
        this.formAPI = formAPI;
        this.formDownloader = formDownloader;
    }

    public void synchronize() {
        ServerFormsDetailsFetcher listDownloader = new ServerFormsDetailsFetcher(formRepository, mediaFileRepository, formAPI);
        List<FormDetails> formList = listDownloader.downloadFormList();

        List<Form> formsOnDevice = formRepository.getAll();

        formsOnDevice.stream().forEach(form -> {
            if (formList.stream().noneMatch(f -> form.getJrFormId().equals(f.getFormId()))) {
                formRepository.delete(form.getId());
            }
        });

        for (FormDetails form : formList) {
            boolean onDevice = formsOnDevice.stream().anyMatch(f -> f.getJrFormId().equals(form.getFormId()));

            if (!onDevice || form.isNewerFormVersionAvailable() || form.areNewerMediaFilesAvailable()) {
                formDownloader.downloadForm(form);
            }
        }
    }
}
