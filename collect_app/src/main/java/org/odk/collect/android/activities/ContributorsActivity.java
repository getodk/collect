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

package org.odk.collect.android.activities;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.R;
import org.odk.collect.android.adapters.ContributorListAdapter;
import org.odk.collect.android.logic.Contributor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ContributorsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contributors);

        initToolbar();
        initHeader();
        initContributorList();
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void initHeader() {
        LinearLayout header = findViewById(R.id.header);
        header.setBackgroundColor(ContextCompat.getColor(this, R.color.tintColor));

        TextView commits =  header.findViewById(R.id.commits);
        TextView name =  header.findViewById(R.id.name);
        TextView email =  header.findViewById(R.id.email);

        commits.setText(R.string.commits);
        name.setText(R.string.name_of_contributor);
        email.setText(R.string.email_of_contributor);

        commits.setTypeface(null, Typeface.BOLD);
        name.setTypeface(null, Typeface.BOLD);
        email.setTypeface(null, Typeface.BOLD);
    }

    private void initContributorList() {
        List<Contributor> contributors = new ArrayList<>();
        String commits;
        String name;
        String email;

        for (String contributor : getContributorList()) {
            if (!contributor.isEmpty()) {
                commits = contributor.substring(0, contributor.indexOf('\t')).trim();
                name = contributor.substring(contributor.indexOf(commits) + commits.length() + 1, contributor.indexOf(" <"));
                email = contributor.substring(contributor.indexOf(name) + name.length() + 2, contributor.length() - 1);

                if (!name.equals("README Bot")) { // don't show bot's contributions
                    contributors.add(new Contributor(commits, name, email));
                }
            }
        }

        RecyclerView recyclerView = findViewById(R.id.contributor_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new ContributorListAdapter(contributors));
    }

    private List<String> getContributorList() {
        return Arrays.asList(
                BuildConfig.CONTRIBUTOR_LIST.substring(0, BuildConfig.CONTRIBUTOR_LIST.length() - 1)
                        .substring(1).split(","));
    }
}
