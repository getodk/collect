/*
 * Copyright (C) 2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.widgets;

import org.javarosa.core.model.data.IAnswerData;
import org.odk.collect.android.logic.PromptElement;

import android.content.Context;

/**
 * QuestionWidgets are the main elements in QuestionView. QuestionView is a
 * ScrollView, so widget designers don't need to worry about scrolling.
 * 
 * Each widget does need to handle the 'ReadOnly' case in BuildView().
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public interface IQuestionWidget {
    public IAnswerData getAnswer();
    public void clearAnswer();
    public void buildView(PromptElement prompt);
    public void setFocus(Context context);
}
