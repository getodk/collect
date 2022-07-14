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
            .around(
                GrantPermissionRule.grant(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.GET_ACCOUNTS
                )
            )
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
