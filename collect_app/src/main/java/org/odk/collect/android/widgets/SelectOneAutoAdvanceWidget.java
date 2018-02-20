/*
 * Copyright (C) 2011 University of Washington
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;

import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.listeners.AdvanceToNextListener;

/**
 * SelectOneWidgets handles select-one fields using radio buttons. Unlike the classic
 * SelectOneWidget, when a user clicks an option they are then immediately advanced to the next
 * question.
 *
 * @author Jeff Beorse (jeff@beorse.net)
 */
@SuppressLint("ViewConstructor")
public class SelectOneAutoAdvanceWidget extends SelectOneWidget implements OnCheckedChangeListener, View.OnClickListener {

    @Nullable
    private AdvanceToNextListener listener;

    public SelectOneAutoAdvanceWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        if (context instanceof AdvanceToNextListener) {
            listener = (AdvanceToNextListener) context;
        }
    }

    @Override
    public void onClick(View v) {
        if (listener != null) {
            listener.advance();
        }
    }

    @Override
    protected void createLayout() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        Bitmap b = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.expander_ic_right);

        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                @SuppressLint("InflateParams")
                RelativeLayout thisParentLayout = (RelativeLayout) inflater.inflate(R.layout.quick_select_layout, null);

                RadioButton radioButton = createRadioButton(i);
                radioButton.setOnClickListener(this);

                ImageView rightArrow = (ImageView) thisParentLayout.getChildAt(1);
                rightArrow.setImageBitmap(b);

                buttons.add(radioButton);

                LinearLayout questionLayout = (LinearLayout) thisParentLayout.getChildAt(0);
                questionLayout.addView(createMediaLayout(i, radioButton));
                answerLayout.addView(thisParentLayout);
            }
            addAnswerView(answerLayout);
        }
    }
}
