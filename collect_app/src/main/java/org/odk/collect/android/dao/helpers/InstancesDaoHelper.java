/*
 * Copyright (C) 2018 Shobhit Agarwal
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

package org.odk.collect.android.dao.helpers;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.javarosawrapper.FormController;
import org.odk.collect.android.utilities.InstancesRepositoryProvider;
import org.odk.collect.forms.instances.Instance;
import org.odk.collect.forms.instances.InstancesRepository;

import timber.log.Timber;

/**
 * Provides abstractions over database calls for instances.
 *
 * @deprecated to favor {@link InstancesRepository}
 */
@Deprecated
public final class InstancesDaoHelper {

    private InstancesDaoHelper() {

    }

    /**
     * Checks the database to determine if the current instance being edited has
     * already been 'marked completed'. A form can be 'unmarked' complete and
     * then resaved.
     *
     * @return true if form has been marked completed, false otherwise.
     * <p>
     * TODO: replace with method in {@link InstancesRepository}
     * that returns an {@link Instance} object from a path.
     */
    public static boolean isInstanceComplete(boolean end, boolean completedByDefault) {
        // default to false if we're mid form
        boolean complete = false;

        FormController formController = Collect.getInstance().getFormController();
        if (formController != null && formController.getInstanceFile() != null) {
            // First check if we're at the end of the form, then check the preferences
            complete = end && completedByDefault;

            // Then see if we've already marked this form as complete before
            String path = formController.getInstanceFile().getAbsolutePath();
            Instance instance = new InstancesRepositoryProvider(Collect.getInstance()).get().getOneByPath(path);
            if (instance != null && instance.getStatus().equals(Instance.STATUS_COMPLETE)) {
                complete = true;
            }
        } else {
            Timber.w("FormController or its instanceFile field has a null value");
        }

        return complete;
    }

    // TODO: replace with method in {@link org.odk.collect.android.instances.InstancesRepository}
    // that returns an {@link Instance} object from a path.
    public static boolean isInstanceAvailable(String path) {
        if (path != null) {
            Instance instance = new InstancesRepositoryProvider(Collect.getInstance()).get().getOneByPath(path);
            return instance != null;
        } else {
            return false;
        }
    }
}
