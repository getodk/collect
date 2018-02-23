package org.odk.collect.android.utilities;

import java.util.Stack;

import timber.log.Timber;

/**
 * This tree should be used only for testing the logs generated during the tests.
 * For usage see {@link QRCodeUtilsTest}
 */
class LogStackTree extends Timber.Tree {

    private Stack<String> logs = new Stack<>();

    @Override
    protected void log(int priority, String tag, String message, Throwable t) {
        logs.push(message);
    }

    public void clear() {
        logs.clear();
    }

    public String pop() {
        return logs.pop();
    }
}
