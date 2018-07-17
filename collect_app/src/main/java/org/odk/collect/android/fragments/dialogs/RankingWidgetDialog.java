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
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.support.v7.widget.helper.ItemTouchHelper.Callback;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;

import org.odk.collect.android.R;
import org.odk.collect.android.R.string;
import org.odk.collect.android.adapters.RankingListAdapter;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.utilities.RankingItemTouchHelperCallback;

import java.io.Serializable;
import java.util.List;

public class RankingWidgetDialog extends DialogFragment {

    private static final String VALUES = "values";

    private RankingListener listener;

    private RankingListAdapter rankingListAdapter;
    private List<String> values;

    public interface RankingListener {
        void onRankingChanged(List<String> values);
    }

    public static RankingWidgetDialog newInstance(List<String> values) {
        RankingWidgetDialog dialog = new RankingWidgetDialog();
        Bundle bundle = new Bundle();
        bundle.putSerializable(VALUES, (Serializable) values);
        dialog.setArguments(bundle);

        return dialog;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof RankingListener) {
            listener = (RankingListener) context;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        values = (List<String>) (savedInstanceState == null
                        ? getArguments().getSerializable(VALUES)
                        : savedInstanceState.getSerializable(VALUES));
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new Builder(getActivity())
                .setView(setUpRankingLayout(values))
                .setPositiveButton(string.ok, (dialog, id) -> {
                    listener.onRankingChanged(rankingListAdapter.getValues());
                    dismiss();
                })
                .setNegativeButton(string.cancel, (dialog, id) -> dismiss())
                .create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(VALUES, (Serializable) rankingListAdapter.getValues());
        super.onSaveInstanceState(outState);
    }

    private ScrollView setUpRankingLayout(List<String> values) {
        LinearLayout rankingLayout = new LinearLayout(getContext());
        rankingLayout.setOrientation(LinearLayout.HORIZONTAL);
        rankingLayout.addView(setUpPositionsLayout(values));
        rankingLayout.addView(setUpRecyclerView(values));
        rankingLayout.setPadding(10, 0, 10, 0);

        ScrollView scrollView = new ScrollView(getContext());
        scrollView.addView(rankingLayout);
        return scrollView;
    }

    private LinearLayout setUpPositionsLayout(List<String> values) {
        LinearLayout positionsLayout = new LinearLayout(getContext());
        positionsLayout.setOrientation(LinearLayout.VERTICAL);

        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 0, 10, 0);
        positionsLayout.setLayoutParams(layoutParams);

        for (String value : values) {
            FrameLayout positionLayout = (FrameLayout) LayoutInflater.from(getContext()).inflate(R.layout.ranking_item, positionsLayout, false);
            TextView textView = positionLayout.findViewById(R.id.rank_item_text);
            textView.setText(String.valueOf(values.indexOf(value) + 1));
            textView.setTextSize(Collect.getQuestionFontsize());

            positionsLayout.addView(positionLayout);
        }
        return positionsLayout;
    }

    private RecyclerView setUpRecyclerView(List<String> values) {
        rankingListAdapter = new RankingListAdapter(values);

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
