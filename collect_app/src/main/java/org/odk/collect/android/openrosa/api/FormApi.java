package org.odk.collect.android.openrosa.api;

import java.util.List;

public interface FormApi {

    List<FormListItem> fetchFormList() throws FormApiException;

    ManifestFile fetchManifest(String manifestURL) throws FormApiException;
}
