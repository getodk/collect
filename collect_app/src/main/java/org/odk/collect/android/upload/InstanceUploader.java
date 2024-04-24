/*
 * Copyright (C) 2018 Nafundi
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

package org.odk.collect.android.upload;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.odk.collect.forms.instances.Instance;
import org.odk.collect.forms.instances.InstancesRepository;

import java.util.ArrayList;
import java.util.List;

public abstract class InstanceUploader {

    private final InstancesRepository instancesRepository;

    public InstanceUploader(InstancesRepository instancesRepository) {
        this.instancesRepository = instancesRepository;
    }

    public static final String FAIL = "Error: ";

    /**
     * Uploads the specified instance to the specified destination URL. It may return a custom
     * success message on completion or null if none is available. Errors result in an UploadException.
     * <p>
     * Updates the database status for the instance.
     */
    @Nullable
    public abstract String uploadOneSubmission(Instance instance, String destinationUrl) throws FormUploadException;

    @NonNull
    public abstract String getUrlToSubmitTo(Instance currentInstance, String deviceId, String overrideURL, String urlFromSettings);

    /**
     * Returns a list of Instance objects corresponding to the database IDs passed in.
     */
    public List<Instance> getInstancesFromIds(Long... instanceDatabaseIds) {
        List<Instance> instances = new ArrayList<>();

        for (Long id : instanceDatabaseIds) {
            instances.add(instancesRepository.get(id));
        }

        return instances;
    }

    public void markSubmissionFailed(Instance instance) {
        instancesRepository
                .save(new Instance.Builder(instance)
                        .status(Instance.STATUS_SUBMISSION_FAILED)
                        .build()
                );
    }

    public void markSubmissionComplete(Instance instance) {
        instancesRepository
                .save(new Instance.Builder(instance)
                        .status(Instance.STATUS_SUBMITTED)
                        .build()
                );
    }
}
