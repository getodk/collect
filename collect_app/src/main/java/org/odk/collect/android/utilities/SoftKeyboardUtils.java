/*
 * Copyright 2018 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.utilities;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.logic.FormController;

public class SoftKeyboardUtils {

    private SoftKeyboardUtils() {
    }

    public static void showSoftKeyboard(@NonNull View view) {
        if (shouldSoftKeyboardBeShown()) {
            if (view.requestFocus()) {
                getInputMethodManager().showSoftInput(view, 0);
            }
        }
    }

    public static void hideSoftKeyboard(@NonNull View view) {
        getInputMethodManager().hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private static InputMethodManager getInputMethodManager() {
        return (InputMethodManager) Collect.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    // The keyboard should be show automatically if we have only one question displayed
    private static boolean shouldSoftKeyboardBeShown() {
        FormController formController = Collect.getInstance().getFormController();
        return formController != null
                && (!formController.indexIsInFieldList() || formController.getQuestionPrompts().length == 1);
    }
}
