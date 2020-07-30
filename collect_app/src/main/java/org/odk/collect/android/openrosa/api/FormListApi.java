package org.odk.collect.android.openrosa.api;

import java.io.InputStream;
import java.util.List;

public interface FormListApi {

    List<FormListItem> fetchFormList() throws FormApiException;

    ManifestFile fetchManifest(String manifestURL) throws FormApiException;

    InputStream fetchForm(String formURL) throws FormApiException;

    InputStream fetchMediaFile(String mediaFileURL) throws FormApiException;
}
