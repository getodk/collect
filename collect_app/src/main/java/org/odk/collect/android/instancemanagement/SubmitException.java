package org.odk.collect.android.instancemanagement;

public class SubmitException extends Exception {

    public enum Type {
        GOOGLE_ACCOUNT_NOT_SET,
        GOOGLE_ACCOUNT_NOT_PERMITTED,
        NOTHING_TO_SUBMIT;
    }

    private final Type type;

    public SubmitException(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }
}
