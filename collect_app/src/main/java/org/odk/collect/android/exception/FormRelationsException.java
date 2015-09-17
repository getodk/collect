/* The MIT License (MIT)
 *
 *       Copyright (c) 2015 PMA2020
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.odk.collect.android.exception;

/**
 *  Defines custom exception for dealing with form relations.
 *
 *  Creator: James K. Pringle
 *  E-mail: jpringle@jhu.edu
 *  Last modified: 25 August 2015
 */
public class FormRelationsException extends Exception {

    private final int ERROR_CODE;
    private final String INFO;

    public FormRelationsException() {
        super();
        ERROR_CODE = 0;
        INFO = "";
    }

    public FormRelationsException(String msg) {
        super(msg);
        ERROR_CODE = 0;
        INFO = "";
    }

    public FormRelationsException(int errorCode) {
        super();
        ERROR_CODE = errorCode;
        INFO = "";
    }

    public FormRelationsException(int errorCode, String info) {
        super();
        ERROR_CODE = errorCode;
        INFO = info;
    }

    public int getErrorCode() {
        return ERROR_CODE;
    }

    public String getInfo() {
        return INFO;
    }
}
