/*
 * Copyright 2017 Nafundi
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

package org.odk.collect.android.widgets.range;

import android.annotation.SuppressLint;
import android.content.Context;

import org.odk.collect.android.formentry.questions.QuestionDetails;

@SuppressLint("ViewConstructor")
public class RangeIntegerWidget extends RangeBaseWidget {
    public RangeIntegerWidget(Context context,
                              QuestionDetails prompt,
                              Dependencies dependencies) {
        super(context, prompt, dependencies, true);
    }
}
