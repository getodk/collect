package org.odk.collect.android.openrosa.api;

public class FormAPIError extends Exception {

    enum Type {
        AUTH_REQUIRED,
        FETCH_ERROR,
        PARSE_ERROR,
        LEGACY_PARSE_ERROR
    }

    private final Type type;

    public FormAPIError(Type type, String message) {
        super(message);
        this.type = type;
    }

    public Type getType() {
        return type;
    }
}
