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

package org.google.android.odk.widgets;

import org.google.android.odk.PromptElement;
import org.javarosa.core.model.Constants;

import android.content.Context;

/**
 * Convenience class that handles creation of widgets.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class WidgetFactory {

    /**
     * Returns the appropriate QuestionWidget for the given PromptElement.
     * 
     * @param p
     * @param context
     */
    static public IQuestionWidget createWidgetFromPrompt(PromptElement p, Context context) {
        IQuestionWidget questionWidget = null;
        switch (p.getQuestionType()) {
            case Constants.CONTROL_INPUT:
                switch (p.getAnswerType()) {
                    case Constants.DATATYPE_DATE:
                        questionWidget = new DateWidget(context);
                        break;
                    case Constants.DATATYPE_DECIMAL:
                        questionWidget = new DecimalWidget(context);
                        break;
                    case Constants.DATATYPE_INTEGER:
                        questionWidget = new IntegerWidget(context);
                        break;
                    case Constants.DATATYPE_GEOPOINT:
                        questionWidget = new GeoPointWidget(context);
                        break;
                    case Constants.DATATYPE_BARCODE:
                        questionWidget = new BarcodeWidget(context);
                        break;
                    case Constants.DATATYPE_AUDIO:
                        questionWidget = new AudioWidget(context);
                        break;
                    case Constants.DATATYPE_VIDEO:
                        questionWidget = new VideoWidget(context);
                        break;
                    default:
                        questionWidget = new StringWidget(context);
                        break;
                }
                break;
            case Constants.CONTROL_IMAGE_CHOOSE:
                questionWidget = new ImageWidget(context);
                break;
            case Constants.CONTROL_SELECT_ONE:
                questionWidget = new SelectOneWidget(context);
                break;
            case Constants.CONTROL_SELECT_MULTI:
                questionWidget = new SelectMultiWidget(context);
                break;
            default:
                questionWidget = new StringWidget(context);
                break;
        }
        questionWidget.buildView(p);
        return questionWidget;
    }

}
