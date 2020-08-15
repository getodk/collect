package org.odk.collect.android.formmanagement;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.forms.Form;
import org.odk.collect.android.forms.FormsRepository;
import org.odk.collect.android.utilities.MultiFormDownloader;
import org.odk.collect.android.utilities.TranslationHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Provides a sarcophagus for {@link org.odk.collect.android.utilities.MultiFormDownloader} so it
 * can eventually be disposed of.
 */
public class ServerFormDownloader implements FormDownloader {

    private final MultiFormDownloader multiFormDownloader;
    private final FormsRepository formsRepository;

    public ServerFormDownloader(MultiFormDownloader multiFormDownloader, FormsRepository formsRepository) {
        this.multiFormDownloader = multiFormDownloader;
        this.formsRepository = formsRepository;
    }

    @Override
    public void downloadForm(ServerFormDetails form) throws FormDownloadException {
        Form formOnDevice = formsRepository.get(form.getFormId(), form.getFormVersion());
        if (formOnDevice != null && formOnDevice.isDeleted()) {
            formsRepository.restore(formOnDevice.getId());
        }

        HashMap<ServerFormDetails, String> results = multiFormDownloader.downloadForms(Collections.singletonList(form), null);
        String result = new ArrayList<>(results.values()).get(0);

        if (!result.equals(TranslationHandler.getString(Collect.getInstance(), R.string.success))) {
            throw new FormDownloadException(result);
        }
    }
}
