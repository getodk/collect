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

package org.odk.collect.android.fragments.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.ItemTouchHelper.Callback;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.SelectChoice;
import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.R;
import org.odk.collect.android.R.string;
import org.odk.collect.android.adapters.RankingListAdapter;
import org.odk.collect.android.fragments.viewmodels.RankingViewModel;
import org.odk.collect.android.utilities.QuestionFontSizeUtils;
import org.odk.collect.android.utilities.RankingItemTouchHelperCallback;

import java.util.ArrayList;
import java.util.List;

public class RankingWidgetDialog extends DialogFragment {
    private RankingListener listener;
    private RankingListAdapter rankingListAdapter;
    private List<SelectChoice> items;
    private FormIndex formIndex;
    private RankingViewModel viewModel;

    public interface RankingListener {
        void onRankingChanged(List<SelectChoice> items);
    }

    public RankingWidgetDialog() {
    }

    public RankingWidgetDialog(List<SelectChoice> items, FormIndex formIndex) {
        this.items = new ArrayList<>(items);
        this.formIndex = formIndex;
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        if (context instanceof RankingListener) {
            listener = (RankingListener) context;
        }
        viewModel = new ViewModelProvider(this, new RankingViewModel.Factory(items, formIndex)).get(RankingViewModel.class);
        if (viewModel.getItems() == null) {
            dismiss();
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new MaterialAlertDialogBuilder(getActivity())
                .setView(setUpRankingLayout())
                .setPositiveButton(string.ok, (dialog, id) -> {
                    listener.onRankingChanged(rankingListAdapter.getItems());
                    dismiss();
                })
                .setNegativeButton(string.cancel, (dialog, id) -> dismiss())
                .create();
    }

    private NestedScrollView setUpRankingLayout() {
        LinearLayout rankingLayout = new LinearLayout(getContext());
        rankingLayout.setOrientation(LinearLayout.HORIZONTAL);
        rankingLayout.addView(setUpPositionsLayout());
        rankingLayout.addView(setUpRecyclerView());
        rankingLayout.setPadding(10, 0, 10, 0);

        NestedScrollView scrollView = new NestedScrollView(getContext());
        scrollView.addView(rankingLayout);
        return scrollView;
    }

    private LinearLayout setUpPositionsLayout() {
        LinearLayout positionsLayout = new LinearLayout(getContext());
        positionsLayout.setOrientation(LinearLayout.VERTICAL);

        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 0, 10, 0);
        positionsLayout.setLayoutParams(layoutParams);

        for (SelectChoice item : viewModel.getItems()) {
            FrameLayout positionLayout = (FrameLayout) LayoutInflater.from(getContext()).inflate(R.layout.ranking_item, positionsLayout, false);
            TextView textView = positionLayout.findViewById(R.id.rank_item_text);
            textView.setText(String.valueOf(viewModel.getItems().indexOf(item) + 1));
            textView.setTextSize(QuestionFontSizeUtils.getQuestionFontSize());

            positionsLayout.addView(positionLayout);
        }
        return positionsLayout;
    }

    private RecyclerView setUpRecyclerView() {
        rankingListAdapter = new RankingListAdapter(viewModel.getItems(), viewModel.getFormIndex());

        RecyclerView recyclerView = new RecyclerView(getContext());
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(rankingListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        Callback callback = new RankingItemTouchHelperCallback(rankingListAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        return recyclerView;
    }
}
