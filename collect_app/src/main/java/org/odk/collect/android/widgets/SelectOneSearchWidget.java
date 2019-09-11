/*
 * Copyright (C) 2009 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.widgets;

import android.annotation.SuppressLint;
import android.content.Context;

import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.listeners.AudioPlayListener;
import org.odk.collect.android.utilities.SoftKeyboardUtils;

/**
 * SelectOneSearchWidget allows the user to enter a value in an editable text box and based on
 * input, the searched
 * options only appear which can then be chosen. This is used to narrow down the Select One options
 * For now, audio/video/image etc will be ignored
 *
 * @author Raghu Mittal (raghu.mittal@handsrel.com)
 */
@SuppressLint("ViewConstructor")
public class SelectOneSearchWidget extends AbstractSelectOneWidget implements AudioPlayListener {
    public SelectOneSearchWidget(Context context, FormEntryPrompt prompt, boolean autoAdvance) {
        super(context, prompt, autoAdvance);
        createLayout();
        setUpSearchBox();
    }

    @Override
    protected void doSearch(String searchStr) {
        if (adapter != null) {
            adapter.getFilter().filter(searchStr);
        }
    }

    @Override
    public void setFocus(Context context) {
        SoftKeyboardUtils.showSoftKeyboard(searchStr);
    }
}