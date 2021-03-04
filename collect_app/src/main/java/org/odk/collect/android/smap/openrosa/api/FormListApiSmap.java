package org.odk.collect.android.smap.openrosa.api;

import org.odk.collect.android.forms.FormListItem;
import org.odk.collect.android.forms.ManifestFile;
import org.odk.collect.android.utilities.WebCredentialsUtils;

import java.io.InputStream;
import java.util.List;

public interface FormListApiSmap {
    List<FormListItem> fetchFormList() throws FormApiExceptionSmap;

    ManifestFile fetchManifest(String manifestURL) throws FormApiExceptionSmap;

    InputStream fetchForm(String formURL, boolean credentials) throws FormApiExceptionSmap;             // smap add credentials

    InputStream fetchMediaFile(String mediaFileURL, boolean credentials) throws FormApiExceptionSmap;   // smap add credentials

    void updateUrl(String url);

    void updateWebCredentialsUtils(WebCredentialsUtils webCredentialsUtils);

}
