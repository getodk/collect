package org.odk.collect.android.forms;

public class FormSourceException extends Exception {

    public static class Unreachable extends FormSourceException {

        private final String serverUrl;

        public Unreachable(String serverUrl) {
            this.serverUrl = serverUrl;
        }

        public String getServerUrl() {
            return serverUrl;
        }
    }

    public static class AuthRequired extends FormSourceException {
    }

    public static class FetchError extends FormSourceException {

    }

    public static class SecurityError extends FormSourceException {

        private final String serverUrl;

        public SecurityError(String serverURL) {
            this.serverUrl = serverURL;
        }

        public String getServerUrl() {
            return serverUrl;
        }
    }

    public static class ServerError extends FormSourceException {

        private final int statusCode;
        private final String serverUrl;

        public ServerError(int statusCode, String serverUrl) {
            this.statusCode = statusCode;
            this.serverUrl = serverUrl;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getServerUrl() {
            return serverUrl;
        }
    }

    public static class ParseError extends FormSourceException {

        private final String serverUrl;

        public ParseError(String serverUrl) {
            this.serverUrl = serverUrl;
        }

        public String getServerUrl() {
            return serverUrl;
        }
    }
}
