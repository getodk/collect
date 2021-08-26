package org.odk.collect.android.support;

import android.Manifest;

import androidx.test.rule.GrantPermissionRule;

import org.junit.rules.RuleChain;

public final class TestRuleChain {

    private TestRuleChain() {

    }

    public static RuleChain chain() {
        return chain(new TestDependencies());
    }

    public static RuleChain chain(TestDependencies testDependencies) {
        return RuleChain
                .outerRule(GrantPermissionRule.grant(Manifest.permission.READ_PHONE_STATE))
                .around(new ResetStateRule(testDependencies))
                .around(new IdlingResourceRule(testDependencies.idlingResources));
    }
}
