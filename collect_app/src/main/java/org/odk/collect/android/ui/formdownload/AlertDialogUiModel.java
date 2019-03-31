package org.odk.collect.android.ui.formdownload;

public class AlertDialogUiModel {

    private final String title;
    private final String message;
    private final boolean shouldExit;

    public AlertDialogUiModel(String title, String message, boolean shouldExit) {
        this.title = title;
        this.message = message;
        this.shouldExit = shouldExit;
    }

    public boolean shouldExit() {
        return shouldExit;
    }

    public String getMessage() {
        return message;
    }

    public String getTitle() {
        return title;
    }
}
