/*
 * Copyright (C) 2020 ODK
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

package org.odk.collect.android.formmanagement;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
public class FormDownloadExceptionMapperTest {
    private Context context;
    private FormDownloadExceptionMapper mapper;

    @Before
    public void setup() {
        context = ApplicationProvider.getApplicationContext();
        mapper = new FormDownloadExceptionMapper(context);
    }

    @Test
    public void genericType_returnsGenericMessage() {
        String expectedString = context.getString(R.string.failure);
        assertThat(mapper.getMessage(new FormDownloadException(FormDownloadException.Type.GENERIC)), is(expectedString));
    }

    @Test
    public void duplicateFormIdVersionType_returnsDuplicateFormIdVersionMessage() {
        String expectedString = "You've already downloaded a form with the same ID and version but with different contents. " +
                "Before downloading, please send all data you have collected with the existing form and delete the data and blank form.";
        assertThat(mapper.getMessage(new FormDownloadException(FormDownloadException.Type.DUPLICATE_FORMID_VERSION)), is(expectedString));
    }
}
