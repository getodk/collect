package org.odk.collect.android.support.rules

import android.Manifest
import androidx.test.rule.GrantPermissionRule
import org.junit.rules.RuleChain
import org.odk.collect.android.support.CountingTaskExecutorIdlingResource
import org.odk.collect.android.support.SchedulerIdlingResource
import org.odk.collect.android.support.TestDependencies

object TestRuleChain {

    @JvmStatic
    @JvmOverloads
    fun chain(testDependencies: TestDependencies = TestDependencies()): RuleChain {
        val schedulerIdlingResource = SchedulerIdlingResource(testDependencies.scheduler)
        val countingTaskExecutorIdlingResource = CountingTaskExecutorIdlingResource()

        return RuleChain
            .outerRule(RetryOnDeviceErrorRule())
            .around(GrantPermissionRule.grant(Manifest.permission.READ_PHONE_STATE))
            .around(DisableDeviceAnimationsRule())
            .around(ResetStateRule(testDependencies))
            .around(countingTaskExecutorIdlingResource)
            .around(
                IdlingResourceRule(
                    listOf(schedulerIdlingResource, countingTaskExecutorIdlingResource)
                )
            )
    }
}
