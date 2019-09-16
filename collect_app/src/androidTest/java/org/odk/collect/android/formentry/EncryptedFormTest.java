/*
 * Copyright 2019 Nafundi
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

package org.odk.collect.android.formentry;

import android.Manifest;

import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.MainMenuActivity;
import org.odk.collect.android.espressoutils.pages.MainMenuPage;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;

public class EncryptedFormTest {
    @Rule
    public ActivityTestRule<MainMenuActivity> main = new ActivityTestRule<>(MainMenuActivity.class);

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE)
            )
            .around(new ResetStateRule())
            .around(new CopyFormRule("encrypted.xml"))
            .around(new CopyFormRule("encrypted-no-instanceID.xml"));

    @Test
    public void instanceOfEncryptedForm_cantBeEditedWhenFinalized() {
        new MainMenuPage(main)
                .startBlankForm("encrypted")
                .swipeToNextQuestion()
                .clickSaveAndExit()
                .checkIsToastWithMessageDisplayed(R.string.data_saved_ok)
                .clickEditSavedForm()
                .checkInstanceState("encrypted", InstanceProviderAPI.STATUS_COMPLETE)
                .clickSavedFormWithDialog("encrypted")
                .checkMessage(R.string.cannot_edit_completed_form);
    }

    @Test
    public void instanceOfEncryptedFormWithoutInstanceID_failsFinalizationWithMessage() {
        new MainMenuPage(main)
                .startBlankForm("encrypted-no-instanceID")
                .swipeToNextQuestion()
                .clickSaveAndExit()
                .checkIsToastWithMessageDisplayed("This form does not specify an instanceID. You must specify one to enable encryption. Form has not been saved as finalized.")
                .clickEditSavedForm()
                .checkInstanceState("encrypted-no-instanceID", InstanceProviderAPI.STATUS_INCOMPLETE);
    }
}
