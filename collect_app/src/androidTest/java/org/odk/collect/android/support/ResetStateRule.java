package org.odk.collect.android.support;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.odk.collect.android.utilities.ResetUtility;

import java.util.Arrays;
import java.util.List;

public class ResetStateRule implements TestRule {

    @Override
    public Statement apply(final Statement base, Description description) {
        return new ResetStateStatement(base);
    }

    private class ResetStateStatement extends Statement {

        private final Statement base;

        ResetStateStatement(Statement base) {
            this.base = base;
        }

        @Override
        public void evaluate() throws Throwable {
            List<Integer> resetActions = Arrays.asList(
                    ResetUtility.ResetAction.RESET_PREFERENCES, ResetUtility.ResetAction.RESET_INSTANCES,
                    ResetUtility.ResetAction.RESET_FORMS, ResetUtility.ResetAction.RESET_LAYERS,
                    ResetUtility.ResetAction.RESET_CACHE, ResetUtility.ResetAction.RESET_OSM_DROID
            );

            Context targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
            new ResetUtility().reset(targetContext, resetActions);

            base.evaluate();
        }
    }

}
