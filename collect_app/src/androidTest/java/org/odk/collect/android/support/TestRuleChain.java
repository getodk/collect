package org.odk.collect.android.support;

import android.Manifest;

import androidx.test.rule.GrantPermissionRule;

import org.junit.rules.RuleChain;

public class TestRuleChain {

    private TestRuleChain() {

    }

    public static RuleChain chain() {
        return chain(new TestDependencies());
    }

    public static RuleChain chain(TestDependencies testDependencies) {
        return chain(testDependencies, true);
    }

    public static RuleChain chain(boolean upgrade) {
        return chain(new TestDependencies(), upgrade);
    }

    public static RuleChain chain(TestDependencies testDependencies, boolean upgrade) {
        return RuleChain
                .outerRule(GrantPermissionRule.grant(Manifest.permission.READ_PHONE_STATE))
                .around(new ResetStateRule(testDependencies, upgrade))
                .around(new IdlingResourceRule(testDependencies.idlingResources));
    }
}
