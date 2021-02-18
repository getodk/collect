package org.odk.collect.android.forms;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.utilities.WebCredentialsUtils;

import java.io.InputStream;
import java.util.List;

/**
 * A place where forms live (outside the app's storage). Ideally in future this would be
 * a common interface for getting forms from a server, Google Drive or even the disk.
 */
public interface FormSource {

    List<FormListItem> fetchFormList() throws FormSourceException;

    ManifestFile fetchManifest(String manifestURL) throws FormSourceException;

    @NotNull
    InputStream fetchForm(String formURL) throws FormSourceException;

    @NotNull
    InputStream fetchMediaFile(String mediaFileURL) throws FormSourceException;

    /**
     * @deprecated This is specific to the Open Rosa/HTTP implementation so should really move
     * down to that level
     */
    @Deprecated
    void updateUrl(String url);

    /**
     * @deprecated This is specific to the Open Rosa/HTTP implementation so should really move
     * down to that level
     */
    @Deprecated
    void updateWebCredentialsUtils(WebCredentialsUtils webCredentialsUtils);
}
