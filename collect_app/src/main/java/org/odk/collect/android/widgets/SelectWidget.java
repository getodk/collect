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

package org.odk.collect.android.widgets;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.javarosa.core.model.SelectChoice;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.adapters.AbstractSelectListAdapter;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.utilities.ThemeUtils;
import org.odk.collect.android.utilities.WidgetAppearanceUtils;

import static org.odk.collect.android.formentry.media.FormMediaUtils.getPlayableAudioURI;

public abstract class SelectWidget extends ItemsWidget {

    /**
     * A list of choices can have thousands of items. To increase loading and scrolling performance,
     * a RecyclerView is used. Because it is nested inside a ScrollView, by default, all of
     * the RecyclerView's items are loaded and there is no performance benefit over a ListView.
     * This constant is used to bound the number of items loaded. The value 40 was chosen because
     * it is around the maximum number of elements that can be shown on a large tablet.
     */
    private static final int MAX_ITEMS_WITHOUT_SCREEN_BOUND = 40;

    LinearLayout answerLayout;
    protected int numColumns = 1;

    public SelectWidget(Context context, QuestionDetails questionDetails) {
        super(context, questionDetails);
        answerLayout = new LinearLayout(context);
        answerLayout.setOrientation(LinearLayout.VERTICAL);

        logAnalytics(questionDetails);
    }

    @Override
    @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
    public void setOnLongClickListener(OnLongClickListener l) {
    }

    protected RecyclerView setUpRecyclerView() {
        numColumns = WidgetAppearanceUtils.getNumberOfColumns(getFormEntryPrompt(), getContext());

        RecyclerView recyclerView = (RecyclerView) LayoutInflater.from(getContext()).inflate(R.layout.recycler_view, null); // keep in an xml file to enable the vertical scrollbar

        if (numColumns == 1) {
            DividerItemDecoration divider = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
            Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.inset_divider_64dp);

            if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
                DrawableCompat.setTint(DrawableCompat.wrap(drawable), new ThemeUtils(getContext()).getColorOnSurface());
            }
            
            divider.setDrawable(drawable);
            recyclerView.addItemDecoration(divider);
        }

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), numColumns));

        return recyclerView;
    }

    void adjustRecyclerViewSize(AbstractSelectListAdapter adapter, RecyclerView recyclerView) {
        if (adapter.getItemCount() > MAX_ITEMS_WITHOUT_SCREEN_BOUND) {
            // Only let the RecyclerView take up 90% of the screen height in order to speed up loading if there are many items
            DisplayMetrics displayMetrics = new DisplayMetrics();
            ((FormEntryActivity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            recyclerView.getLayoutParams().height = (int) (displayMetrics.heightPixels * 0.9);
        } else {
            recyclerView.setNestedScrollingEnabled(false);
        }
    }

    private void logAnalytics(QuestionDetails questionDetails) {
        if (items != null) {
            for (SelectChoice choice : items) {
                String audioURI = getPlayableAudioURI(questionDetails.getPrompt(), choice, getReferenceManager());

                if (audioURI != null) {
                    analytics.logEvent("Prompt", "AudioChoice", questionDetails.getFormAnalyticsID());
                    break;
                }
            }
        }
    }
}
