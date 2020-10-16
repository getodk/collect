package org.odk.collect.android.forms;

public class FormSourceException extends Exception {

    public enum Type {
        UNREACHABLE,
        AUTH_REQUIRED,
        FETCH_ERROR,
        SECURITY_ERROR
    }

    private final Type type;
    private final String serverUrl;

    public FormSourceException(Type type) {
        this.type = type;
        this.serverUrl = null;
    }

    public FormSourceException(Type type, String serverUrl) {
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
