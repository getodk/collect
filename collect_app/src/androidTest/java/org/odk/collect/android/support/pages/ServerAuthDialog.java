package org.odk.collect.android.support.pages;

import org.odk.collect.android.R;

public class ServerAuthDialog extends Page<ServerAuthDialog> {

    @Override
    public ServerAuthDialog assertOnPage() {
        assertText(R.string.server_requires_auth);
        return this;
    }

    public ServerAuthDialog fillUsername(String username) {
        inputText(R.string.username, username);
        return this;
    }

    public ServerAuthDialog fillPassword(String password) {
        inputText(R.string.password, password);
        return this;
    }

    public <D extends Page<D>> D clickOK(D destination) {
        clickOnString(R.string.ok);
        return destination.assertOnPage();
    }
}
