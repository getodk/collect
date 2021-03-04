package org.odk.collect.android.smap.openrosa.api;

import org.odk.collect.android.forms.FormListItem;
import org.odk.collect.android.forms.ManifestFile;
import org.odk.collect.android.utilities.WebCredentialsUtils;

import java.io.InputStream;
import java.util.List;

public class FormListApiSmap {
    List<FormListItem> fetchFormList() throws FormApiException;

    ManifestFile fetchManifest(String manifestURL) throws FormApiException;

    InputStream fetchForm(String formURL, boolean credentials) throws FormApiException;             // smap add credentials

    InputStream fetchMediaFile(String mediaFileURL, boolean credentials) throws FormApiException;   // smap add credentials

    void updateUrl(String url);

    void updateWebCredentialsUtils(WebCredentialsUtils webCredentialsUtils);

}
