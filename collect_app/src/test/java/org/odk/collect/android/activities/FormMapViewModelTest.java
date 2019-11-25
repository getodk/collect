package org.odk.collect.android.activities;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.viewmodels.FormMapViewModel;
import org.odk.collect.android.forms.Form;
import org.odk.collect.android.geo.MapFragment;
import org.odk.collect.android.geo.MapPoint;
import org.odk.collect.android.instances.Instance;
import org.odk.collect.android.instances.InstancesRepository;
import org.odk.collect.android.instances.TestInstancesRepository;
import org.odk.collect.android.provider.InstanceProviderAPI;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

public class FormMapViewModelTest {
    @Rule public MockitoRule rule = MockitoJUnit.rule();

    private InstancesRepository testInstancesRepository;

    @Mock private MapFragment map;

    @Before public void setUp() {
        testInstancesRepository = new TestInstancesRepository(Arrays.asList(testInstances));

        when(map.addMarker(new MapPoint(10.0, 125.6), false)).thenReturn(0);
        when(map.addMarker(new MapPoint(10.1, 125.6), false)).thenReturn(1);
        when(map.addMarker(new MapPoint(10.1, 126.6), false)).thenReturn(2);
        when(map.addMarker(new MapPoint(10.3, 125.6), false)).thenReturn(4);
        when(map.addMarker(new MapPoint(10.3, 125.7), false)).thenReturn(5);
        when(map.addMarker(new MapPoint(10.4, 125.6), false)).thenReturn(6);
        when(map.addMarker(new MapPoint(11.1, 127.6), false)).thenReturn(8);
    }

    @Test public void getFormTitle_returnsFormTitle() {
        FormMapViewModel viewModel = new FormMapViewModel(testForm1, testInstancesRepository);
        assertThat(viewModel.getFormTitle(), is("Form with ID 1"));
    }

    @Test public void updatingMap_forFormWithoutMappedInstances_doesNotMapPoints() {
        FormMapViewModel viewModel = new FormMapViewModel(testForm2, testInstancesRepository);
        viewModel.mapUpdateRequested(map);

        verify(map, never()).addMarker(any(), anyBoolean());
        assertThat(viewModel.getMappedPointCount(), is(0));
    }

    @Test public void updatingMap_forFormWithoutMappedInstances_doesNotUpdateViewPort() {
        FormMapViewModel viewModel = new FormMapViewModel(testForm2, testInstancesRepository);
        viewModel.mapUpdateRequested(map);

        assertThat(viewModel.getMappedPointCount(), is(0));
        verify(map, never()).zoomToBoundingBox(any(), anyDouble(), anyBoolean());
    }

    @Test public void updatingMap_forFormWithMappedInstances_updatesViewPort() {
        FormMapViewModel viewModel = new FormMapViewModel(testForm1, testInstancesRepository);
        viewModel.mapUpdateRequested(map);

        assertThat(viewModel.getInstanceCount(), is(7));
        verify(map, times(1)).zoomToBoundingBox(any(), anyDouble(), anyBoolean());
    }

    @Test public void receivingLocationUpdate_withoutPoints_updatesViewPort() {
        FormMapViewModel viewModel = new FormMapViewModel(testForm2, testInstancesRepository);
        viewModel.mapUpdateRequested(map);
        verify(map, never()).zoomToBoundingBox(any(), anyDouble(), anyBoolean());

        assertThat(viewModel.getMappedPointCount(), is(0));
        viewModel.locationChanged(new MapPoint(0, 0), map);
        verify(map, times(1)).zoomToPoint(new MapPoint(0, 0), true);
    }

    @Test public void totalInstanceCount_countsAllInstancesForForm() {
        FormMapViewModel viewModel = new FormMapViewModel(testForm1, testInstancesRepository);
        viewModel.mapUpdateRequested(map);

        assertThat(viewModel.getInstanceCount(), is(7));
    }

    @Test public void mappedPointCount_omitsInstancesWithoutGeometry() {
        FormMapViewModel viewModel = new FormMapViewModel(testForm1, testInstancesRepository);
        viewModel.mapUpdateRequested(map);

        assertThat(viewModel.getMappedPointCount(), is(6));
    }

    @Test public void mapMarkersPlaced_forEachInstanceWithGeometry() {
        FormMapViewModel viewModel = new FormMapViewModel(testForm1, testInstancesRepository);
        viewModel.mapUpdateRequested(map);

        verify(map, times(1)).addMarker(new MapPoint(10.0, 125.6), false);
        verify(map, times(1)).addMarker(new MapPoint(10.1, 125.6), false);
        verify(map, times(1)).addMarker(new MapPoint(10.1, 126.6), false);
        verify(map, times(1)).addMarker(new MapPoint(10.3, 125.6), false);
        verify(map, times(1)).addMarker(new MapPoint(10.3, 125.7), false);
        verify(map, times(1)).addMarker(new MapPoint(10.4, 125.6), false);
    }

    @Test public void mapMakersHaveExpectedIcon_forInstanceStatus() {
        FormMapViewModel viewModel = new FormMapViewModel(testForm1, testInstancesRepository);
        viewModel.mapUpdateRequested(map);

        verify(map, times(1)).setMarkerIcon(0, R.drawable.ic_room_green_24dp);
        verify(map, times(1)).setMarkerIcon(1, R.drawable.ic_room_deep_purple_24dp);
        verify(map, times(1)).setMarkerIcon(2, R.drawable.ic_room_blue_24dp);
        verify(map, times(1)).setMarkerIcon(4, R.drawable.ic_room_red_24dp);
        verify(map, times(1)).setMarkerIcon(5, R.drawable.ic_room_green_24dp);
        verify(map, times(1)).setMarkerIcon(6, R.drawable.ic_room_red_24dp);
    }

    // Should not actually be possible from UI because geometry is deleted on sent instance delete
    @Test public void tappingMapMarker_forDeletedInstance_returnsDeletedStatus() {
        FormMapViewModel viewModel = new FormMapViewModel(testForm1, testInstancesRepository);
        viewModel.mapUpdateRequested(map);

        assertThat(viewModel.getStatusOfClickedFeature(0), is(FormMapViewModel.FeatureStatus.DELETED));
    }

    @Test public void tappingMapMarker_forFinalizedInstance_thatCanBeEdited_returnsEditableStatus() {
        FormMapViewModel viewModel = new FormMapViewModel(testForm1, testInstancesRepository);
        viewModel.mapUpdateRequested(map);

        assertThat(viewModel.getStatusOfClickedFeature(1), is(FormMapViewModel.FeatureStatus.EDITABLE));
    }

    @Test public void tappingMapMarker_forUnfinalizedInstance_returnsEditableStatus() {
        FormMapViewModel viewModel = new FormMapViewModel(testForm1, testInstancesRepository);
        viewModel.mapUpdateRequested(map);

        assertThat(viewModel.getStatusOfClickedFeature(2), is(FormMapViewModel.FeatureStatus.EDITABLE));
    }

    @Test public void tappingMapMarker_forInstanceThatFailedToSend_thatCanBeEdited_returnsEditableStatus() {
        FormMapViewModel viewModel = new FormMapViewModel(testForm1, testInstancesRepository);
        viewModel.mapUpdateRequested(map);

        assertThat(viewModel.getStatusOfClickedFeature(4), is(FormMapViewModel.FeatureStatus.EDITABLE));
    }

    // Sent instances should never be editable.
    @Test public void tappingMapMarker_forSubmittedInstance_thatCanBeEdited_returnsViewableStatus() {
        FormMapViewModel viewModel = new FormMapViewModel(testForm1, testInstancesRepository);
        viewModel.mapUpdateRequested(map);

        assertThat(viewModel.getStatusOfClickedFeature(5), is(FormMapViewModel.FeatureStatus.VIEW_ONLY));
    }

    @Test public void tappingMapMarker_forInstanceThatFailedToSend_thatCantBeEdited_returnsNotViewableStatus() {
        FormMapViewModel viewModel = new FormMapViewModel(testForm1, testInstancesRepository);
        viewModel.mapUpdateRequested(map);

        assertThat(viewModel.getStatusOfClickedFeature(6), is(FormMapViewModel.FeatureStatus.NOT_VIEWABLE));
    }

    @Test public void addingAnInstance_shouldBeReflectedInInstanceCountsAndMap() {
        FormMapViewModel viewModel = new FormMapViewModel(testForm1, testInstancesRepository);
        viewModel.mapUpdateRequested(map);

        assertThat(viewModel.getInstanceCount(), is(7));
        assertThat(viewModel.getMappedPointCount(), is(6));
        assertThat(viewModel.getStatusOfClickedFeature(8), is(FormMapViewModel.FeatureStatus.UNKNOWN));

        Instance newInstance = new Instance.Builder().databaseId(8L)
                .jrFormId("formId1")
                .jrVersion("2019103101")
                .geometryType("Point")
                .geometry("{\"type\":\"Point\",\"coordinates\":[127.6, 11.1]}")
                .canEditWhenComplete(true)
                .status(InstanceProviderAPI.STATUS_COMPLETE).build();

        ((TestInstancesRepository) testInstancesRepository).addInstance(newInstance);

        viewModel.mapUpdateRequested(map);
        assertThat(viewModel.getInstanceCount(), is(8));
        assertThat(viewModel.getMappedPointCount(), is(7));
        assertThat(viewModel.getStatusOfClickedFeature(8), is(FormMapViewModel.FeatureStatus.EDITABLE));
    }

    @Test public void deletingAnInstance_shouldBeReflectedInInstanceCountsAndMap() {
        FormMapViewModel viewModel = new FormMapViewModel(testForm1, testInstancesRepository);
        viewModel.mapUpdateRequested(map);

        assertThat(viewModel.getInstanceCount(), is(7));
        assertThat(viewModel.getMappedPointCount(), is(6));
        assertThat(viewModel.getStatusOfClickedFeature(6), is(FormMapViewModel.FeatureStatus.NOT_VIEWABLE));

        ((TestInstancesRepository) testInstancesRepository).removeInstanceById(6);

        viewModel.mapUpdateRequested(map);
        assertThat(viewModel.getInstanceCount(), is(6));
        assertThat(viewModel.getMappedPointCount(), is(5));
    }

    private final Form testForm1 = new Form.Builder().id(0L)
            .jrFormId("formId1")
            .jrVersion("2019103104")
            .displayName("Form with ID 1")
            .geometryXpath("/data/my-point")
            .build();

    private final Form testForm2 = new Form.Builder().id(0L)
            .jrFormId("formId2")
            .jrVersion("2019103103")
            .displayName("Form with ID 2")
            .geometryXpath("/data/my-point2")
            .build();

    private static Instance[] testInstances = {
            new Instance.Builder().databaseId(0L)
                    .jrFormId("formId1")
                    .jrVersion("2019103101")
                    .deletedDate(1487782554846L)
                    .geometryType("Point")
                    .geometry("{\"type\":\"Point\",\"coordinates\":[125.6, 10.0]}")
                    .status(InstanceProviderAPI.STATUS_SUBMITTED).build(),

            new Instance.Builder().databaseId(1L)
                    .jrFormId("formId1")
                    .jrVersion("2019103101")
                    .geometryType("Point")
                    .geometry("{\"type\":\"Point\",\"coordinates\":[125.6, 10.1]}")
                    .canEditWhenComplete(true)
                    .status(InstanceProviderAPI.STATUS_COMPLETE).build(),

            new Instance.Builder().databaseId(2L)
                    .jrFormId("formId1")
                    .jrVersion("2019103102")
                    .geometryType("Point")
                    .geometry("{\"type\":\"Point\",\"coordinates\":[126.6, 10.1]}")
                    .status(InstanceProviderAPI.STATUS_INCOMPLETE).build(),

            new Instance.Builder().databaseId(3L)
                    .jrFormId("formId1")
                    .jrVersion("2019103101")
                    .status(InstanceProviderAPI.STATUS_COMPLETE).build(),

            new Instance.Builder().databaseId(4L)
                    .jrFormId("formId1")
                    .jrVersion("2019103106")
                    .geometryType("Point")
                    .geometry("{\"type\":\"Point\",\"coordinates\":[125.6, 10.3]}")
                    .canEditWhenComplete(true)
                    .status(InstanceProviderAPI.STATUS_SUBMISSION_FAILED).build(),

            new Instance.Builder().databaseId(5L)
                    .jrFormId("formId1")
                    .jrVersion("2019103101")
                    .geometryType("Point")
                    .geometry("{\"type\":\"Point\",\"coordinates\":[125.7, 10.3]}")
                    .canEditWhenComplete(true)
                    .status(InstanceProviderAPI.STATUS_SUBMITTED).build(),

            new Instance.Builder().databaseId(6L)
                    .jrFormId("formId1")
                    .jrVersion("2019103101")
                    .geometryType("Point")
                    .geometry("{\"type\":\"Point\",\"coordinates\":[125.6, 10.4]}")
                    .canEditWhenComplete(false)
                    .status(InstanceProviderAPI.STATUS_SUBMISSION_FAILED).build(),

            new Instance.Builder().databaseId(7L)
                    .jrFormId("formId2")
                    .jrVersion("2019103101")
                    .status(InstanceProviderAPI.STATUS_COMPLETE).build(),
            };
}
