package org.odk.collect.android.openrosa;

public final class OpenRosaConstants {
    // HTTP Header strings
    public static final String VERSION_HEADER = "X-OpenRosa-Version";
    public static final String ACCEPT_CONTENT_LENGTH_HEADER = "X-OpenRosa-Accept-Content-Length";

    // Endpoints
    public static final String FORM_LIST = "/formList";
    public static final String SUBMISSION = "/submission";

    private OpenRosaConstants() {
        // cannot construct
    }
}
