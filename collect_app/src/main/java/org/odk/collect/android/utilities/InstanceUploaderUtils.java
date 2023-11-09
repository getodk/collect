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

import android.content.Context;

import org.odk.collect.android.application.Collect;
import org.odk.collect.forms.instances.Instance;
import org.odk.collect.forms.instances.InstancesRepository;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static org.odk.collect.strings.localization.LocalizedApplicationKt.getLocalizedString;

public final class InstanceUploaderUtils {

    public static final String DEFAULT_SUCCESSFUL_TEXT = "full submission upload was successful!";

    private InstanceUploaderUtils() {
    }

    /**
     * Returns a formatted message including submission results for all the filled forms accessible
     * through instancesProcessed in the following structure:
     *
     * Instance name 1 - result
     *
     * Instance name 2 - result
     */
    public static String getUploadResultMessage(InstancesRepository instancesRepository, Context context, Map<String, String> result) {
        Set<String> keys = result.keySet();
        Iterator<String> it = keys.iterator();
        StringBuilder message = new StringBuilder();

        while (it.hasNext()) {
            Instance instance = instancesRepository.get(Long.valueOf(it.next()));
            message.append(getUploadResultMessageForInstances(instance, result));
        }

        if (message.length() == 0) {
            message = new StringBuilder(context.getString(org.odk.collect.strings.R.string.no_forms_uploaded));
        }

        return message.toString().trim();
    }

    private static String getUploadResultMessageForInstances(Instance instance, Map<String, String> resultMessagesByInstanceId) {
        StringBuilder uploadResultMessage = new StringBuilder();
        if (instance != null) {
            String name = instance.getDisplayName();
            String text = localizeDefaultAggregateSuccessfulText(resultMessagesByInstanceId.get(instance.getDbId().toString()));
            uploadResultMessage
                    .append(name)
                    .append(" - ")
                    .append(text)
                    .append("\n\n");
        }
        return uploadResultMessage.toString();
    }

    private static String localizeDefaultAggregateSuccessfulText(String text) {
        if (text != null && text.equals(DEFAULT_SUCCESSFUL_TEXT)) {
            text = getLocalizedString(Collect.getInstance(), org.odk.collect.strings.R.string.success);
        }
        return text;
    }
}
