package org.odk.collect.android.openrosa.api;

public class FormApiException extends Exception {

    public enum Type {
        UNKNOWN_HOST,
        AUTH_REQUIRED,
        FETCH_ERROR
    }

    private final Type type;
    private final String serverUrl;

    public FormApiException(Type type) {
        this.type = type;
        this.serverUrl = null;
    }

    public FormApiException(Type type, String serverUrl) {
        this.type = type;
        this.serverUrl = serverUrl;
    }

    public Type getType() {
        return type;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    @Override
    public String toString() {
        return "FormAPIException{" +
                "type=" + type +
                '}';
    }
}
