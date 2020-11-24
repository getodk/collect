package org.odk.collect.android.support;

import android.Manifest;

import androidx.test.rule.GrantPermissionRule;

import org.junit.rules.RuleChain;

public class TestRuleChain {

    private TestRuleChain() {

    }

    public static RuleChain chain() {
        return chain(true, new TestDependencies());
    }

    public static RuleChain chain(TestDependencies testDependencies) {
        return chain(true, testDependencies);
    }

    public static RuleChain chain(boolean useScopedStorage, TestDependencies testDependencies) {
        return RuleChain
                .outerRule(GrantPermissionRule.grant(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_PHONE_STATE
                ))
                .around(new ResetStateRule(useScopedStorage, testDependencies))
                .around(new IdlingResourceRule(testDependencies.idlingResources));
    }
}
