package org.odk.collect.android.activities;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.activities.viewmodels.FormMapViewModel.ClickAction.DELETED_TOAST;
import static org.odk.collect.android.activities.viewmodels.FormMapViewModel.ClickAction.NOT_VIEWABLE_TOAST;
import static org.odk.collect.android.activities.viewmodels.FormMapViewModel.ClickAction.OPEN_EDIT;
import static org.odk.collect.android.activities.viewmodels.FormMapViewModel.ClickAction.OPEN_READ_ONLY;

import android.content.Intent;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.material.chip.Chip;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.viewmodels.FormMapViewModel;
import org.odk.collect.android.external.InstanceProvider;
import org.odk.collect.android.geo.MapProvider;
import org.odk.collect.android.geo.TestMapFragment;
import org.odk.collect.android.injection.config.AppDependencyComponent;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.support.CollectHelpers;
import org.odk.collect.forms.Form;
import org.odk.collect.forms.FormsRepository;
import org.odk.collect.forms.instances.Instance;
import org.odk.collect.forms.instances.InstancesRepository;
import org.odk.collect.formstest.FormUtils;
import org.odk.collect.geo.maps.MapPoint;
import org.odk.collect.shared.TempFiles;
import org.robolectric.android.controller.ActivityController;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class FormMapActivityTest {
    private FormMapActivity activity;

    private final TestMapFragment map = new TestMapFragment();

    private final List<MapPoint> expectedPoints = Arrays.asList(new MapPoint(10.0, 125.6),
            new MapPoint(10.1, 125.6), new MapPoint(10.1, 126.6),
            new MapPoint(10.3, 125.6), new MapPoint(10.3, 125.7),
            new MapPoint(10.4, 125.6));

    @Before
    public void setUpActivity() {
        CollectHelpers.setupDemoProject();

        AppDependencyComponent component = CollectHelpers.overrideAppDependencyModule(new AppDependencyModule() {
            @Override
            public MapProvider providesMapProvider() {
                MapProvider mapProvider = mock(MapProvider.class);
                when(mapProvider.createMapFragment(ArgumentMatchers.any())).thenReturn(map);
                return mapProvider;
            }
        });

        FormsRepository formsRepository = component.formsRepositoryProvider().get();
        InstancesRepository instancesRepository = component.instancesRepositoryProvider().get();
        Form form = formsRepository.save(testForm);
        Arrays.stream(testInstances).forEach(instancesRepository::save);

        Intent intent = new Intent();
        intent.putExtra(FormMapActivity.EXTRA_FORM_ID, form.getDbId());
        ActivityController activityController = CollectHelpers.buildThemedActivity(FormMapActivity.class, intent);
        activity = (FormMapActivity) activityController.get();
        activityController.setup();
    }

    // Note that there's a point with deleted status included. This shouldn't be possible in real
    // usage because deleting a form removes the geometry from the database. However, the database
    // allows a deleted instance with geometry so we test it.
    @Test
    public void mappedPoints_matchInstancesWithGeometry() {
        assertThat(map.getMappedPointCount(), is(expectedPoints.size()));
        for (MapPoint expectedPoint : expectedPoints) {
            assertThat(map.isMapped(expectedPoint), is(true));
        }
    }

    @Test
    public void tappingOnEditableInstance_showsSubmissionSummaryWithAppropriateMessage() {
        Pair<Instance, MapPoint> editableAndFinalized = new Pair<>(testInstances[1], new MapPoint(10.1, 125.6));
        Pair<Instance, MapPoint> unfinalized = new Pair<>(testInstances[2], new MapPoint(10.1, 126.6));
        Pair<Instance, MapPoint> failedToSend = new Pair<>(testInstances[4], new MapPoint(10.3, 125.6));

        activity.onFeatureClicked(map.getFeatureIdFor(editableAndFinalized.second));
        assertSubmissionSummaryContent(editableAndFinalized.first.getDisplayName(), editableAndFinalized.first.getStatus(), new Date(editableAndFinalized.first.getLastStatusChangeDate()), OPEN_EDIT);

        activity.onFeatureClicked(map.getFeatureIdFor(unfinalized.second));
        assertSubmissionSummaryContent(unfinalized.first.getDisplayName(), unfinalized.first.getStatus(), new Date(unfinalized.first.getLastStatusChangeDate()), OPEN_EDIT);

        activity.onFeatureClicked(map.getFeatureIdFor(failedToSend.second));
        assertSubmissionSummaryContent(failedToSend.first.getDisplayName(), failedToSend.first.getStatus(), new Date(failedToSend.first.getLastStatusChangeDate()), OPEN_READ_ONLY);
    }

    @Test
    public void tappingOnUneditableInstances_showsSubmissionSummaryWithAppropriateMessage() {
        Pair<Instance, MapPoint> sent = new Pair<>(testInstances[5], new MapPoint(10.3, 125.7));

        int featureId = map.getFeatureIdFor(sent.second);
        activity.onFeatureClicked(featureId);
        assertSubmissionSummaryContent(sent.first.getDisplayName(), sent.first.getStatus(), new Date(sent.first.getLastStatusChangeDate()), OPEN_READ_ONLY);
    }

    @Test
    public void tappingOnFailedSubmission_showsSubmissionSummaryWithAppropriateMessage() {
        Pair<Instance, MapPoint> submissionFailedCantEditWhenFinalized = new Pair<>(testInstances[6], new MapPoint(10.4, 125.6));

        int featureId = map.getFeatureIdFor(submissionFailedCantEditWhenFinalized.second);
        activity.onFeatureClicked(featureId);
        assertSubmissionSummaryContent(submissionFailedCantEditWhenFinalized.first.getDisplayName(), submissionFailedCantEditWhenFinalized.first.getStatus(), new Date(submissionFailedCantEditWhenFinalized.first.getLastStatusChangeDate()), NOT_VIEWABLE_TOAST);
    }

    // Geometry is removed from the database on instance deletion but just in case there is a
    // deleted instance with geometry available, show a deleted toast.
    @Test
    public void tappingOnDeletedInstances_showsSubmissionSummaryWithAppropriateMessage() {
        Pair<Instance, MapPoint> deleted = new Pair<>(testInstances[0], new MapPoint(10.0, 125.6));

        int featureId = map.getFeatureIdFor(deleted.second);
        activity.onFeatureClicked(featureId);
        assertSubmissionSummaryContent(deleted.first.getDisplayName(), deleted.first.getStatus(), new Date(deleted.first.getLastStatusChangeDate()), DELETED_TOAST);
    }

    private void assertSubmissionSummaryContent(String instanceName, String status, Date lastStatusChangeDate, FormMapViewModel.ClickAction clickAction) {
        assertThat(((TextView) activity.findViewById(R.id.name)).getText().toString(), is(instanceName));
        String instanceLastStatusChangeDate = InstanceProvider.getDisplaySubtext(activity, status, lastStatusChangeDate);
        assertThat(((TextView) activity.findViewById(R.id.status_text)).getText().toString(), is(instanceLastStatusChangeDate));

        switch (clickAction) {
            case DELETED_TOAST:
                assertThat(activity.findViewById(R.id.info).getVisibility(), is(View.VISIBLE));
                assertThat(activity.findViewById(R.id.action).getVisibility(), is(View.GONE));
                break;
            case NOT_VIEWABLE_TOAST:
                assertThat(activity.findViewById(R.id.info).getVisibility(), is(View.VISIBLE));
                assertThat(((TextView) activity.findViewById(R.id.info)).getText().toString(), is(activity.getString(R.string.cannot_edit_completed_form)));
                assertThat(activity.findViewById(R.id.action).getVisibility(), is(View.GONE));
                break;
            case OPEN_READ_ONLY:
                assertThat(activity.findViewById(R.id.info).getVisibility(), is(View.GONE));
                assertThat(activity.findViewById(R.id.action).getVisibility(), is(View.VISIBLE));
                assertThat(((Chip) activity.findViewById(R.id.action)).getText(), is(activity.getString(R.string.view_data)));
                break;
            case OPEN_EDIT:
                assertThat(activity.findViewById(R.id.info).getVisibility(), is(View.GONE));
                assertThat(activity.findViewById(R.id.action).getVisibility(), is(View.VISIBLE));
                assertThat(((Chip) activity.findViewById(R.id.action)).getText(), is(activity.getString(R.string.review_data)));
                break;
        }
    }

    private final Form testForm = FormUtils
            .buildForm("formId1", "2019103101", TempFiles.createTempDir().getAbsolutePath())
            .build();

    private static Instance[] testInstances = {
            new Instance.Builder()
                    .instanceFilePath("")
                    .displayName("Form1")
                    .lastStatusChangeDate(1487782554846L)
                    .formId("formId1")
                    .formVersion("2019103101")
                    .deletedDate(1487782554846L)
                    .geometryType("Point")
                    .geometry("{\"type\":\"Point\",\"coordinates\":[125.6, 10.0]}")
                    .status(Instance.STATUS_SUBMITTED).build(),

            new Instance.Builder()
                    .instanceFilePath("")
                    .displayName("Form2")
                    .lastStatusChangeDate(1488782558743L)
                    .formId("formId1")
                    .formVersion("2019103101")
                    .geometryType("Point")
                    .geometry("{\"type\":\"Point\",\"coordinates\":[125.6, 10.1]}")
                    .canEditWhenComplete(true)
                    .status(Instance.STATUS_COMPLETE).build(),

            new Instance.Builder()
                    .instanceFilePath("")
                    .displayName("Form3")
                    .lastStatusChangeDate(1484582553254L)
                    .formId("formId1")
                    .formVersion("2019103102")
                    .geometryType("Point")
                    .geometry("{\"type\":\"Point\",\"coordinates\":[126.6, 10.1]}")
                    .status(Instance.STATUS_INCOMPLETE).build(),

            new Instance.Builder()
                    .instanceFilePath("")
                    .displayName("Form4")
                    .lastStatusChangeDate(1488582557456L)
                    .formId("formId1")
                    .formVersion("2019103101")
                    .status(Instance.STATUS_COMPLETE).build(),

            new Instance.Builder()
                    .instanceFilePath("")
                    .displayName("Form5")
                    .lastStatusChangeDate(1483582557438L)
                    .formId("formId1")
                    .formVersion("2019103106")
                    .geometryType("Point")
                    .geometry("{\"type\":\"Point\",\"coordinates\":[125.6, 10.3]}")
                    .canEditWhenComplete(true)
                    .status(Instance.STATUS_SUBMISSION_FAILED).build(),

            new Instance.Builder()
                    .instanceFilePath("")
                    .displayName("Form6")
                    .lastStatusChangeDate(1482282559618L)
                    .formId("formId1")
                    .formVersion("2019103101")
                    .geometryType("Point")
                    .geometry("{\"type\":\"Point\",\"coordinates\":[125.7, 10.3]}")
                    .canEditWhenComplete(true)
                    .status(Instance.STATUS_SUBMITTED).build(),

            new Instance.Builder()
                    .instanceFilePath("")
                    .displayName("Form7")
                    .lastStatusChangeDate(1484782559836L)
                    .formId("formId1")
                    .formVersion("2019103101")
                    .geometryType("Point")
                    .geometry("{\"type\":\"Point\",\"coordinates\":[125.6, 10.4]}")
                    .canEditWhenComplete(false)
                    .status(Instance.STATUS_SUBMISSION_FAILED).build(),

            new Instance.Builder()
                    .instanceFilePath("")
                    .displayName("Form8")
                    .lastStatusChangeDate(1487982552254L)
                    .formId("formId2")
                    .formVersion("2019103101")
                    .geometryType("Point")
                    .geometry("Crazy stuff")
                    .status(Instance.STATUS_COMPLETE).build(),

            new Instance.Builder()
                    .instanceFilePath("")
                    .displayName("Form9")
                    .lastStatusChangeDate(1484682557369L)
                    .formId("formId2")
                    .formVersion("2019103101")
                    .geometryType("Crazy stuff")
                    .geometry("{\"type\":\"Point\",\"coordinates\":[125.6, 10.4]}")
                    .status(Instance.STATUS_COMPLETE).build(),
    };
}
