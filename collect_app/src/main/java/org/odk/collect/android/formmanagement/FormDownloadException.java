package org.odk.collect.android.formmanagement;

public class FormDownloadException extends Exception {

    public enum Type {
        GENERIC,
        DUPLICATE_FORMID_VERSION
    }

    private final Type type;

    public FormDownloadException() {
        this.type = Type.GENERIC;
    }

    public FormDownloadException(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }
}
