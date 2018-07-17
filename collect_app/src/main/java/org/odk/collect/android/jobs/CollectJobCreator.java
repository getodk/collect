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

package org.odk.collect.android.jobs;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

import org.odk.collect.android.tasks.FormDownloadJob;
import org.odk.collect.android.tasks.ServerPollingJob;

public class CollectJobCreator implements JobCreator {
    @Nullable
    @Override
    public Job create(@NonNull String tag) {
        switch (tag) {
            case SmsSenderJob.TAG:
                return new SmsSenderJob();

            case ServerPollingJob.TAG:
                return new ServerPollingJob();

            case FormDownloadJob.TAG:
                return new FormDownloadJob();

            default:
                return null;
        }
    }
}

