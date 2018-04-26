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

package org.odk.collect.android.adapters;

import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.logic.Contributor;

import java.util.List;

public class ContributorListAdapter extends RecyclerView.Adapter<ContributorListAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Contributor item);
    }

    private List<Contributor> contributors;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View layout;
        TextView commits;
        TextView name;
        TextView email;

        ViewHolder(View v) {
            super(v);
            layout = v;
            commits = v.findViewById(R.id.commits);
            name = v.findViewById(R.id.name);
            email = v.findViewById(R.id.email);
        }
    }

    public ContributorListAdapter(List<Contributor> contributors) {
        this.contributors = contributors;
    }

    @Override
    public ContributorListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.contributor_list_row, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position % 2 != 0) {
            holder.layout.setBackgroundColor(ContextCompat.getColor(Collect.getInstance(), R.color.white_smoke));
        } else {
            holder.layout.setBackgroundColor(ContextCompat.getColor(Collect.getInstance(), android.R.color.white));
        }
        holder.commits.setText(contributors.get(position).getCommits());
        holder.name.setText(contributors.get(position).getName());
        holder.email.setText(contributors.get(position).getEmail());
    }

    @Override
    public int getItemCount() {
        return contributors.size();
    }
}
