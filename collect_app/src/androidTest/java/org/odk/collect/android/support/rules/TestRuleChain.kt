package org.odk.collect.android.support.rules

import android.Manifest
import androidx.test.rule.GrantPermissionRule
import org.junit.rules.RuleChain
import org.odk.collect.android.support.TestDependencies

object TestRuleChain {

    @JvmStatic
    @JvmOverloads
    fun chain(testDependencies: TestDependencies = TestDependencies()): RuleChain =
        RuleChain
            .outerRule(GrantPermissionRule.grant(Manifest.permission.READ_PHONE_STATE))
            .around(DisableDeviceAnimationsRule())
            .around(ResetStateRule(testDependencies))
            .around(IdlingResourceRule(testDependencies.idlingResources))
}
