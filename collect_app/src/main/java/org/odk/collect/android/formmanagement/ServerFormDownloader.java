package org.odk.collect.android.formmanagement;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.utilities.MultiFormDownloader;

import java.util.Collections;
import java.util.HashMap;

/**
 * Provides a sarcophagus for {@link org.odk.collect.android.utilities.MultiFormDownloader} so it
 * can eventually be disposed of.
 */
public class ServerFormDownloader implements FormDownloader {

    private final MultiFormDownloader multiFormDownloader;

    public ServerFormDownloader(MultiFormDownloader multiFormDownloader) {
        this.multiFormDownloader = multiFormDownloader;
    }

    @Override
    public void downloadForm(ServerFormDetails form) throws FormDownloadException {
        HashMap<ServerFormDetails, String> results = multiFormDownloader.downloadForms(Collections.singletonList(form), null);
        boolean failure = results.values().stream().anyMatch(s -> !s.equals(Collect.getInstance().getString(R.string.success)));
        if (failure) {
            throw new FormDownloadException();
        }
    }
}
