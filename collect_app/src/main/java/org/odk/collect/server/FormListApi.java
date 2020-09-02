package org.odk.collect.server;

import org.odk.collect.android.utilities.WebCredentialsUtils;

import java.io.InputStream;
import java.util.List;

public interface FormListApi {

    List<FormListItem> fetchFormList() throws FormApiException;

    ManifestFile fetchManifest(String manifestURL) throws FormApiException;

    InputStream fetchForm(String formURL) throws FormApiException;

    InputStream fetchMediaFile(String mediaFileURL) throws FormApiException;

    void updateUrl(String url);

    void updateWebCredentialsUtils(WebCredentialsUtils webCredentialsUtils);
}
