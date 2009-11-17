/*
 * Copyright (C) 2009 University of Washington
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

import android.content.Context;

import org.javarosa.core.model.Constants;
import org.odk.collect.android.logic.PromptElement;

/**
 * Convenience class that handles creation of widgets.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class WidgetFactory {

    /**
     * Returns the appropriate QuestionWidget for the given PromptElement.
     * 
     * @param pe prompt element to be rendered
     * @param context Android context
     * @param instancePath path to the instance file
     */
    static public IQuestionWidget createWidgetFromPrompt(PromptElement pe, Context context,
            String instancePath) {
        IQuestionWidget questionWidget = null;
        switch (pe.getQuestionType()) {
            case Constants.CONTROL_INPUT:
                switch (pe.getAnswerType()) {
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
                    default:
                        questionWidget = new StringWidget(context);
                        break;
                }
                break;
            case Constants.CONTROL_IMAGE_CHOOSE:
                questionWidget = new MediaWidget(context, "image", instancePath);
                break;
            case Constants.CONTROL_AUDIO_CAPTURE:
                questionWidget = new MediaWidget(context, "audio", instancePath);
                break;
            case Constants.CONTROL_VIDEO_CAPTURE:
                questionWidget = new MediaWidget(context, "video", instancePath);
                break;
            case Constants.CONTROL_SELECT_ONE:
                questionWidget = new SelectOneWidget(context);
                break;
            case Constants.CONTROL_SELECT_MULTI:
                questionWidget = new SelectMultiWidget(context);
                break;
            case Constants.CONTROL_TRIGGER:
                questionWidget = new TriggerWidget(context);
                break;
            default:
                questionWidget = new StringWidget(context);
                break;
        }
        questionWidget.buildView(pe);
        return questionWidget;
    }

}
