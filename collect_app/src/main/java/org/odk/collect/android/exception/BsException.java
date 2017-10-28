package org.odk.collect.android.exception;

/**
 * Created on 10/28/17
 * by nishon.tan@gmail.com
 */

public class BsException extends Exception {
    public BsException(String message) {
        super(message);
    }

    public BsException(String message, Throwable cause) {
        super(message, cause);
    }
}
