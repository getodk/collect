package org.odk.collect.shared.strings;

import java.security.SecureRandom;

public class RandomString {

    private RandomString() {

    }

    static final String CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    public static String randomString(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(CHARS.charAt(new SecureRandom().nextInt(CHARS.length())));
        }

        return sb.toString();
    }
}
