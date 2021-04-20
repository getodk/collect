package org.odk.collect.forms;

public class FormListItem {

    private final String downloadURL;
    private final String formID;
    private final String version;
    private final String hash;
    private final String name;
    private final String manifestURL;

    public FormListItem(String downloadURL, String formID, String version, String hash, String name, String manifestURL) {
        this.downloadURL = downloadURL;
        this.formID = formID;
        this.version = version;
        this.hash = hash;
        this.name = name;
        this.manifestURL = manifestURL;
    }

    public String getFormID() {
        return formID;
    }

    public String getHashWithPrefix() {
        return hash;
    }

    public String getName() {
        return name;
    }

    public String getDownloadURL() {
        return downloadURL;
    }

    public String getManifestURL() {
        return manifestURL;
    }

    public String getVersion() {
        return version;
    }
}
