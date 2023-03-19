package org.odk.collect.android.feature.formentry.backgroundlocation;

import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static org.hamcrest.Matchers.equalTo;

import android.app.Application;

import org.apache.commons.csv.CSVRecord;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.R;
import org.odk.collect.android.support.FakeLocationClient;
import org.odk.collect.android.support.StorageUtils;
import org.odk.collect.android.support.TestDependencies;
import org.odk.collect.android.support.rules.FormActivityTestRule;
import org.odk.collect.android.support.rules.TestRuleChain;
import org.odk.collect.location.LocationClient;
import org.odk.collect.testshared.FakeLocation;

import java.io.IOException;
import java.util.List;

public class LocationTrackingAuditTest {

    private final FakeLocationClient locationClient = new FakeLocationClient();

    public FormActivityTestRule rule = new FormActivityTestRule("location-audit.xml", "Audit with Location");

    @Rule
    public RuleChain copyFormChain = TestRuleChain.chain(new TestDependencies() {
                @Override
                public LocationClient providesFusedLocationClient(Application application) {
                    return locationClient;
                }
            })
            .around(rule);

    @Test
    public void locationTrackingIsLogged_andLocationIsLoggedForEachQuestion() throws IOException {
        FakeLocation location1 = new FakeLocation(null);
        location1.setLatitude(1.0);
        location1.setLongitude(1.0);
        locationClient.setLocation(location1);

        rule.startInFormEntry()
                .assertBackgroundLocationSnackbarShown()
                .assertQuestion("Text1")
                .swipeToNextQuestion("Text2")
                .clickSave();

        List<CSVRecord> auditLog = StorageUtils.getAuditLogForFirstInstance();
        assertThat(auditLog.get(1).get(0), equalTo("location tracking enabled"));
        assertThat(auditLog.get(2).get(0), equalTo("location permissions granted"));
        assertThat(auditLog.get(3).get(0), equalTo("location providers enabled"));

        CSVRecord firstQuestionEvent = auditLog.get(4);
        assertThat(firstQuestionEvent.get(0), equalTo("question"));
        assertThat(firstQuestionEvent.get(4), equalTo("1.0"));
        assertThat(firstQuestionEvent.get(5), equalTo("1.0"));

        CSVRecord secondQuestionEvent = auditLog.get(5);
        assertThat(secondQuestionEvent.get(0), equalTo("question"));
        assertThat(secondQuestionEvent.get(4), equalTo("1.0"));
        assertThat(secondQuestionEvent.get(5), equalTo("1.0"));
    }

    @Test
    public void locationCollectionToggle_ShouldBeAvailable() {
        rule.startInFormEntry()
                .clickOptionsIcon()
                .assertText(R.string.track_location);
    }
}
