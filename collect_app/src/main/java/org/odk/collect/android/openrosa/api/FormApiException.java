package org.odk.collect.android.openrosa.api;

public class FormApiException extends Exception {

    public enum Type {
        AUTH_REQUIRED,
        FETCH_ERROR,
        PARSE_ERROR,
        LEGACY_PARSE_ERROR
    }

    private final Type type;

    public FormApiException(Type type, String message) {
        super(message);
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return "FormAPIError{" +
                "type=" + type +
                "message=" + getMessage() +
                '}';
    }
}
