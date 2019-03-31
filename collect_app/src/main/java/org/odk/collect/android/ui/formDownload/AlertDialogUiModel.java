package org.odk.collect.android.ui.formDownload;

class AlertDialogUiModel {

    private String title;
    private String message;
    private boolean shouldExit;

    AlertDialogUiModel(String title, String message, boolean shouldExit) {
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
