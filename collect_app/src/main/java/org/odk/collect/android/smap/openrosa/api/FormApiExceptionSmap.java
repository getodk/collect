package org.odk.collect.android.smap.openrosa.api;

public class FormApiExceptionSmap extends Exception {
    public enum Type {
        UNREACHABLE,
        AUTH_REQUIRED,
        FETCH_ERROR,
        SECURITY_ERROR
    }

    private final Type type;
    private final String serverUrl;

    public FormApiExceptionSmap(Type type) {
        this.type = type;
        this.serverUrl = null;
    }

    public FormApiExceptionSmap(Type type, String serverUrl) {
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
