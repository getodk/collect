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
import androidx.annotation.NonNull;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import org.odk.collect.android.application.Collect;

public class SoftKeyboardController {
    public void showSoftKeyboard(@NonNull View view) {
        if (view.requestFocus()) {
            getInputMethodManager().showSoftInput(view, 0);
        }
    }

    public void hideSoftKeyboard(@NonNull View view) {
        getInputMethodManager().hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private InputMethodManager getInputMethodManager() {
        return (InputMethodManager) Collect.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE);
    }
}
