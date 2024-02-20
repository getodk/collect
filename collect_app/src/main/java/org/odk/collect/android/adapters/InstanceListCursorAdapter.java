/*
 * Copyright 2017 SDRC
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

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;

import org.odk.collect.android.database.DatabaseObjectMapper;
import org.odk.collect.android.instancemanagement.InstanceListItemView;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.forms.instances.Instance;

public class InstanceListCursorAdapter extends SimpleCursorAdapter {
    private final boolean shouldCheckDisabled;

    public InstanceListCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, boolean shouldCheckDisabled) {
        super(context, layout, c, from, to);
        this.shouldCheckDisabled = shouldCheckDisabled;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        Instance instance = DatabaseObjectMapper.getInstanceFromCurrentCursorPosition(
                getCursor(),
                new StoragePathProvider().getOdkDirPath(StorageSubdirectory.INSTANCES)
        );

        InstanceListItemView.setInstance(view, instance, shouldCheckDisabled);
        return view;
    }
}
