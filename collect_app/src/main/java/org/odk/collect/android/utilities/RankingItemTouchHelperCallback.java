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

package org.odk.collect.android.utilities;

import android.graphics.Canvas;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import org.odk.collect.android.adapters.RankingListAdapter;

public class RankingItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private final RankingListAdapter adapter;

    public RankingItemTouchHelperCallback(RankingListAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return false;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.START | ItemTouchHelper.END);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder source, RecyclerView.ViewHolder target) {
        if (source.getItemViewType() != target.getItemViewType()) {
            return false;
        }

        adapter.onItemMove(source.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int i) {
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) { // change only the active item
            ((RankingListAdapter.ItemViewHolder) viewHolder).onItemSelected();
        }

        super.onSelectedChanged(viewHolder, actionState);
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);

        ((RankingListAdapter.ItemViewHolder) viewHolder).onItemClear();
    }

    @Override
    // Prevent out of bounds dragging
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float x, float y, int actionState, boolean isCurrentlyActive) {
        float topY = viewHolder.itemView.getTop() + y;
        float bottomY = topY + viewHolder.itemView.getHeight();
        if (topY < 0) {
            y = 0;
        } else if (bottomY > recyclerView.getHeight()) {
            y = recyclerView.getHeight() - viewHolder.itemView.getHeight() - viewHolder.itemView.getTop();
        }
        super.onChildDraw(c, recyclerView, viewHolder, x, y, actionState, isCurrentlyActive);
    }
}
