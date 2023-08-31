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
import org.odk.collect.android.utilities.InstancesRepositoryProvider;
import org.odk.collect.forms.instances.Instance;
import org.odk.collect.forms.instances.InstancesRepository;

/**
 * Provides abstractions over database calls for instances.
 *
 * @deprecated to favor {@link InstancesRepository}
 */
@Deprecated
public final class InstancesDaoHelper {

    private InstancesDaoHelper() {

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
