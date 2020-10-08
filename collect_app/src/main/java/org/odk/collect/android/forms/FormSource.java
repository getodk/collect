package org.odk.collect.android.forms;

import org.odk.collect.android.utilities.WebCredentialsUtils;

import java.io.InputStream;
import java.util.List;

public interface FormSource {

    List<FormListItem> fetchFormList() throws FormSourceException;

    ManifestFile fetchManifest(String manifestURL) throws FormSourceException;

    InputStream fetchForm(String formURL) throws FormSourceException;

    InputStream fetchMediaFile(String mediaFileURL) throws FormSourceException;

    void updateUrl(String url);

    void updateWebCredentialsUtils(WebCredentialsUtils webCredentialsUtils);
}
