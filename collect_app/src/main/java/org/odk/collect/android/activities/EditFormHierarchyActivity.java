/*
 * Copyright (C) 2017 Shobhit
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

package org.odk.collect.android.activities;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.view.KeyEvent;

import org.javarosa.core.model.FormIndex;
import org.odk.collect.android.R;
import org.odk.collect.android.adapters.HierarchyListAdapter;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.exception.JavaRosaException;
import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.logic.HierarchyElement;

import java.util.ArrayList;

import timber.log.Timber;

public class EditFormHierarchyActivity extends FormHierarchyActivity {

    @Override
    public void onElementClick(HierarchyElement element) {
        int position = formList.indexOf(element);
        FormIndex index = element.getFormIndex();
        if (index == null) {
            goUpLevel();
            return;
        }

        switch (element.getType()) {
            case EXPANDED:
                Collect.getInstance().getActivityLogger().logInstanceAction(this, "onListItemClick",
                        "COLLAPSED", element.getFormIndex());
                element.setType(COLLAPSED);
                ArrayList<HierarchyElement> children = element.getChildren();
                for (int i = 0; i < children.size(); i++) {
                    formList.remove(position + 1);
                }
                element.setIcon(ContextCompat.getDrawable(this, R.drawable.expander_ic_minimized));
                break;
            case COLLAPSED:
                Collect.getInstance().getActivityLogger().logInstanceAction(this, "onListItemClick",
                        "EXPANDED", element.getFormIndex());
                element.setType(EXPANDED);
                ArrayList<HierarchyElement> children1 = element.getChildren();
                for (int i = 0; i < children1.size(); i++) {
                    Timber.i("adding child: %s", children1.get(i).getFormIndex());
                    formList.add(position + 1 + i, children1.get(i));

                }
                element.setIcon(ContextCompat.getDrawable(this, R.drawable.expander_ic_maximized));
                break;
            case QUESTION:
                Collect.getInstance().getActivityLogger().logInstanceAction(this, "onListItemClick",
                        "QUESTION-JUMP", index);
                Collect.getInstance().getFormController().jumpToIndex(index);
                if (Collect.getInstance().getFormController().indexIsInFieldList()) {
                    try {
                        Collect.getInstance().getFormController().stepToPreviousScreenEvent();
                    } catch (JavaRosaException e) {
                        Timber.d(e);
                        createErrorDialog(e.getCause().getMessage());
                        return;
                    }
                }
                setResult(RESULT_OK);
                finish();
                return;
            case CHILD:
                Collect.getInstance().getActivityLogger().logInstanceAction(this, "onListItemClick",
                        "REPEAT-JUMP", element.getFormIndex());
                Collect.getInstance().getFormController().jumpToIndex(element.getFormIndex());
                setResult(RESULT_OK);
                refreshView();
                return;
        }

        recyclerView.setAdapter(new HierarchyListAdapter(formList, this::onElementClick));
        ((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPositionWithOffset(position, 0);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                Collect.getInstance().getActivityLogger().logInstanceAction(this, "onKeyDown",
                        "KEYCODE_BACK.JUMP", startIndex);
                FormController fc = Collect.getInstance().getFormController();
                if (fc != null) {
                    fc.getTimerLogger().exitView();
                    fc.jumpToIndex(startIndex);
                }
        }
        return super.onKeyDown(keyCode, event);
    }
}
