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

package org.odk.collect.android.feature.formentry;

import android.Manifest;

import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.R;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.FeatureTestRule;
import org.odk.collect.android.support.ResetStateRule;

public class EncryptedFormTest {

    @Rule
    public FeatureTestRule rule = new FeatureTestRule();

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
        rule.mainMenu()
                .startBlankForm("encrypted")
                .swipeToEndScreen()
                .clickSaveAndExit()
                .checkIsToastWithMessageDisplayed(R.string.data_saved_ok)
                .clickEditSavedForm()
                .checkInstanceState("encrypted", InstanceProviderAPI.STATUS_COMPLETE)
                .clickOnFormWithDialog("encrypted")
                .checkMessage(R.string.cannot_edit_completed_form);
    }

    @Test
    public void instanceOfEncryptedFormWithoutInstanceID_failsFinalizationWithMessage() {
        rule.mainMenu()
                .startBlankForm("encrypted-no-instanceID")
                .swipeToEndScreen()
                .clickSaveAndExit()
                .checkIsToastWithMessageDisplayed("This form does not specify an instanceID. You must specify one to enable encryption. Form has not been saved as finalized.")
                .clickEditSavedForm()
                .checkInstanceState("encrypted-no-instanceID", InstanceProviderAPI.STATUS_INCOMPLETE);
    }
}
