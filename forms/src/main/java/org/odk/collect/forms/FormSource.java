package org.odk.collect.forms;

import org.jetbrains.annotations.NotNull;

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
}
