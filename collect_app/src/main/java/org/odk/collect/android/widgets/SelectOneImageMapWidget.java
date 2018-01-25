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

package org.odk.collect.android.widgets;

import android.content.Context;
import android.webkit.WebView;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.form.api.FormEntryPrompt;

/**
 * A widget which is responsible for multi select questions represented by
 * an svg map. You can use maps of the world, countries, human body etc.
 */
public class SelectOneImageMapWidget extends SelectImageMapWidget {
    public SelectOneImageMapWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        if (prompt.getAnswerValue() != null) {
            selections.add((Selection) prompt.getAnswerValue().getValue());
            refreshSelectedItemsLabel();
        }
    }

    @Override
    protected void highlightSelections(WebView view) {
        if (!selections.isEmpty()) {
            view.loadUrl("javascript:addSelectedArea('" + selections.get(0).getValue() + "')");
        }
    }

    @Override
    public IAnswerData getAnswer() {
        return selections.isEmpty() ? null
                : new SelectOneData(selections.get(0));
    }
}