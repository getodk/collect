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

package org.odk.collect.android.backgroundwork;

import android.content.Context;

import androidx.work.WorkerParameters;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.formmanagement.FormsUpdater;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.async.TaskSpec;
import org.odk.collect.async.WorkerAdapter;

import java.util.Map;
import java.util.function.Supplier;

import javax.inject.Inject;

public class AutoUpdateTaskSpec implements TaskSpec {

    public static final String DATA_PROJECT_ID = "projectId";

    @Inject
    FormsUpdater formsUpdater;

    @NotNull
    @Override
    public Supplier<Boolean> getTask(@NotNull Context context, @NotNull Map<String, String> inputData) {
        DaggerUtils.getComponent(context).inject(this);

        return () -> {
            String projectId = inputData.get(DATA_PROJECT_ID);
            if (projectId != null) {
                formsUpdater.downloadUpdates(projectId);
                return true;
            } else {
                throw new IllegalArgumentException("No project ID provided!");
            }
        };
    }

    @NotNull
    @Override
    public Class<? extends WorkerAdapter> getWorkManagerAdapter() {
        return Adapter.class;
    }

    public static class Adapter extends WorkerAdapter {

        public Adapter(@NotNull Context context, @NotNull WorkerParameters workerParams) {
            super(new AutoUpdateTaskSpec(), context, workerParams);
        }
    }
}
