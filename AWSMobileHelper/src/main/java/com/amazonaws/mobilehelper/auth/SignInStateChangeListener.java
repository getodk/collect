package com.amazonaws.mobilehelper.auth;

/**
 * Implement this interface to receive callbacks when the user's sign-in state changes
 * from signed-in to not signed-in or vice versa.
 */
public interface SignInStateChangeListener {

    /**
     * Invoked when the user completes sign-in.
     */
    void onUserSignedIn();

    /**
     * Invoked when the user signs out.
     */
    void onUserSignedOut();
}
