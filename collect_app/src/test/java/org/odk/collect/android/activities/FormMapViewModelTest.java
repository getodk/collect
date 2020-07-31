package org.odk.collect.android.activities;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.odk.collect.android.activities.viewmodels.FormMapViewModel;
import org.odk.collect.android.forms.Form;
import org.odk.collect.android.instances.Instance;
import org.odk.collect.android.instances.InstancesRepository;
import org.odk.collect.android.support.InMemInstancesRepository;
import org.odk.collect.android.provider.InstanceProviderAPI;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.not;

public class FormMapViewModelTest {
    @Rule public MockitoRule rule = MockitoJUnit.rule();

    private InstancesRepository testInstancesRepository;

    @Before public void setUp() {
        testInstancesRepository = new InMemInstancesRepository(Arrays.asList(testInstances));
    }

    @Test public void getFormTitle_returnsFormTitle() {
        FormMapViewModel viewModel = new FormMapViewModel(TEST_FORM_1, testInstancesRepository);
        assertThat(viewModel.getFormTitle(), is("Form with ID 1"));
    }

    @Test public void getFormId_returnsFormId() {
        FormMapViewModel viewModel = new FormMapViewModel(TEST_FORM_1, testInstancesRepository);
        assertThat(viewModel.getFormId(), is("formId1"));
    }

    @Test public void getTotalInstanceCount_returnsCountOfAllInstances() {
        FormMapViewModel viewModel = new FormMapViewModel(TEST_FORM_1, testInstancesRepository);
        assertThat(viewModel.getTotalInstanceCount(), is(7));
    }

    @Test public void getMappableInstances_excludesInstancesWithoutGeometry() {
        FormMapViewModel viewModel = new FormMapViewModel(TEST_FORM_1, testInstancesRepository);
        List<FormMapViewModel.MappableFormInstance> mappableFormInstances = viewModel.getMappableFormInstances();

        assertThat(mappableFormInstances.size(), is(6));
        assertThat(mappableFormInstances, not(contains(hasProperty("databaseId"), is(3L))));
    }

    @Test public void getMappableInstances_excludesInstancesWithCorruptGeometry() {
        FormMapViewModel viewModel = new FormMapViewModel(TEST_FORM_2, testInstancesRepository);
        List<FormMapViewModel.MappableFormInstance> mappableFormInstances = viewModel.getMappableFormInstances();

        assertThat(mappableFormInstances.size(), is(0));
    }

    @Test public void getDeletedDateOf_returnsDeletedDate() {
        FormMapViewModel viewModel = new FormMapViewModel(TEST_FORM_1, testInstancesRepository);

        assertThat(viewModel.getDeletedDateOf(0L), is(testInstances[0].getDeletedDate()));
    }

    // Should not actually be possible from UI because geometry is deleted on sent instance delete
    @Test public void deletedInstance_hasDeletedClickAction() {
        FormMapViewModel viewModel = new FormMapViewModel(TEST_FORM_1, testInstancesRepository);
        List<FormMapViewModel.MappableFormInstance> instances = viewModel.getMappableFormInstances();

        assertThat(instances.get(0).getClickAction(), is(FormMapViewModel.ClickAction.DELETED_TOAST));
    }

    @Test public void finalizedInstance_thatCanBeEdited_hasEditableClickAction() {
        FormMapViewModel viewModel = new FormMapViewModel(TEST_FORM_1, testInstancesRepository);
        List<FormMapViewModel.MappableFormInstance> instances = viewModel.getMappableFormInstances();

        assertThat(instances.get(1).getClickAction(), is(FormMapViewModel.ClickAction.OPEN_EDIT));
    }

    @Test public void unfinalizedInstance_hasEditableStatus() {
        FormMapViewModel viewModel = new FormMapViewModel(TEST_FORM_1, testInstancesRepository);
        List<FormMapViewModel.MappableFormInstance> instances = viewModel.getMappableFormInstances();

        assertThat(instances.get(2).getClickAction(), is(FormMapViewModel.ClickAction.OPEN_EDIT));
    }

    @Test public void instanceThatFailedToSend_thatCanBeEdited_hasEditableStatus() {
        FormMapViewModel viewModel = new FormMapViewModel(TEST_FORM_1, testInstancesRepository);
        List<FormMapViewModel.MappableFormInstance> instances = viewModel.getMappableFormInstances();

        assertThat(instances.get(3).getClickAction(), is(FormMapViewModel.ClickAction.OPEN_EDIT));
    }

    // Sent instances should never be editable.
    @Test public void submittedInstance_thatCanBeEdited_returnsViewableStatus() {
        FormMapViewModel viewModel = new FormMapViewModel(TEST_FORM_1, testInstancesRepository);
        List<FormMapViewModel.MappableFormInstance> instances = viewModel.getMappableFormInstances();

        assertThat(instances.get(4).getClickAction(), is(FormMapViewModel.ClickAction.OPEN_READ_ONLY));
    }

    @Test public void instanceThatFailedToSend_thatCantBeEdited_returnsNotViewableStatus() {
        FormMapViewModel viewModel = new FormMapViewModel(TEST_FORM_1, testInstancesRepository);
        List<FormMapViewModel.MappableFormInstance> instances = viewModel.getMappableFormInstances();

        assertThat(instances.get(5).getClickAction(), is(FormMapViewModel.ClickAction.NOT_VIEWABLE_TOAST));
    }

    @Test public void addingAnInstance_isReflectedInInstanceCountsAndList() {
        FormMapViewModel viewModel = new FormMapViewModel(TEST_FORM_1, testInstancesRepository);

        List<FormMapViewModel.MappableFormInstance> instances = viewModel.getMappableFormInstances();
        assertThat(viewModel.getTotalInstanceCount(), is(7));
        assertThat(instances.size(), is(6));

        Instance newInstance = new Instance.Builder().id(8L)
                .jrFormId("formId1")
                .jrVersion("2019103101")
                .geometryType("Point")
                .geometry("{\"type\":\"Point\",\"coordinates\":[127.6, 11.1]}")
                .canEditWhenComplete(true)
                .status(InstanceProviderAPI.STATUS_COMPLETE).build();

        ((InMemInstancesRepository) testInstancesRepository).addInstance(newInstance);

        instances = viewModel.getMappableFormInstances();
        assertThat(viewModel.getTotalInstanceCount(), is(8));
        assertThat(instances.size(), is(7));
        assertThat(instances.get(6).getClickAction(), is(FormMapViewModel.ClickAction.OPEN_EDIT));
    }

    @Test public void clearingInstanceGeometry_isReflectedInInstanceCountsAndList() {
        FormMapViewModel viewModel = new FormMapViewModel(TEST_FORM_1, testInstancesRepository);

        List<FormMapViewModel.MappableFormInstance> mappableInstances = viewModel.getMappableFormInstances();
        assertThat(viewModel.getTotalInstanceCount(), is(7));
        assertThat(mappableInstances.size(), is(6));

        assertThat(mappableInstances.get(5).getClickAction(), is(FormMapViewModel.ClickAction.NOT_VIEWABLE_TOAST));
        ((InMemInstancesRepository) testInstancesRepository).removeInstanceById(6L);

        ((InMemInstancesRepository) testInstancesRepository).addInstance(new Instance.Builder().id(6L)
                .jrFormId("formId1")
                .jrVersion("2019103101")
                .geometryType("")
                .geometry("")
                .canEditWhenComplete(false)
                .status(InstanceProviderAPI.STATUS_SUBMISSION_FAILED).build());

        mappableInstances = viewModel.getMappableFormInstances();
        assertThat(viewModel.getTotalInstanceCount(), is(7));
        assertThat(mappableInstances.size(), is(5));
        assertThat(mappableInstances.get(4).getClickAction(), is(FormMapViewModel.ClickAction.OPEN_READ_ONLY));
    }

    static final Form TEST_FORM_1 = new Form.Builder().id(0L)
            .jrFormId("formId1")
            .jrVersion("2019103104")
            .displayName("Form with ID 1")
            .geometryXpath("/data/my-point")
            .build();

    private static final Form TEST_FORM_2 = new Form.Builder().id(0L)
            .jrFormId("formId2")
            .jrVersion("2019103103")
            .displayName("Form with ID 2")
            .geometryXpath("/data/my-point2")
            .build();

    static Instance[] testInstances = {
            new Instance.Builder().id(0L)
                    .displayName("Form1")
                    .lastStatusChangeDate(1487782554846L)
                    .jrFormId("formId1")
                    .jrVersion("2019103101")
                    .deletedDate(1487782554846L)
                    .geometryType("Point")
                    .geometry("{\"type\":\"Point\",\"coordinates\":[125.6, 10.0]}")
                    .status(InstanceProviderAPI.STATUS_SUBMITTED).build(),

            new Instance.Builder().id(1L)
                    .displayName("Form2")
                    .lastStatusChangeDate(1488782558743L)
                    .jrFormId("formId1")
                    .jrVersion("2019103101")
                    .geometryType("Point")
                    .geometry("{\"type\":\"Point\",\"coordinates\":[125.6, 10.1]}")
                    .canEditWhenComplete(true)
                    .status(InstanceProviderAPI.STATUS_COMPLETE).build(),

            new Instance.Builder().id(2L)
                    .displayName("Form3")
                    .lastStatusChangeDate(1484582553254L)
                    .jrFormId("formId1")
                    .jrVersion("2019103102")
                    .geometryType("Point")
                    .geometry("{\"type\":\"Point\",\"coordinates\":[126.6, 10.1]}")
                    .status(InstanceProviderAPI.STATUS_INCOMPLETE).build(),

            new Instance.Builder().id(3L)
                    .displayName("Form4")
                    .lastStatusChangeDate(1488582557456L)
                    .jrFormId("formId1")
                    .jrVersion("2019103101")
                    .status(InstanceProviderAPI.STATUS_COMPLETE).build(),

            new Instance.Builder().id(4L)
                    .displayName("Form5")
                    .lastStatusChangeDate(1483582557438L)
                    .jrFormId("formId1")
                    .jrVersion("2019103106")
                    .geometryType("Point")
                    .geometry("{\"type\":\"Point\",\"coordinates\":[125.6, 10.3]}")
                    .canEditWhenComplete(true)
                    .status(InstanceProviderAPI.STATUS_SUBMISSION_FAILED).build(),

            new Instance.Builder().id(5L)
                    .displayName("Form6")
                    .lastStatusChangeDate(1482282559618L)
                    .jrFormId("formId1")
                    .jrVersion("2019103101")
                    .geometryType("Point")
                    .geometry("{\"type\":\"Point\",\"coordinates\":[125.7, 10.3]}")
                    .canEditWhenComplete(true)
                    .status(InstanceProviderAPI.STATUS_SUBMITTED).build(),

            new Instance.Builder().id(6L)
                    .displayName("Form7")
                    .lastStatusChangeDate(1484782559836L)
                    .jrFormId("formId1")
                    .jrVersion("2019103101")
                    .geometryType("Point")
                    .geometry("{\"type\":\"Point\",\"coordinates\":[125.6, 10.4]}")
                    .canEditWhenComplete(false)
                    .status(InstanceProviderAPI.STATUS_SUBMISSION_FAILED).build(),

            new Instance.Builder().id(7L)
                    .displayName("Form8")
                    .lastStatusChangeDate(1487982552254L)
                    .jrFormId("formId2")
                    .jrVersion("2019103101")
                    .geometryType("Point")
                    .geometry("Crazy stuff")
                    .status(InstanceProviderAPI.STATUS_COMPLETE).build(),

            new Instance.Builder().id(8L)
                    .displayName("Form9")
                    .lastStatusChangeDate(1484682557369L)
                    .jrFormId("formId2")
                    .jrVersion("2019103101")
                    .geometryType("Crazy stuff")
                    .geometry("{\"type\":\"Point\",\"coordinates\":[125.6, 10.4]}")
                    .status(InstanceProviderAPI.STATUS_COMPLETE).build(),
            };
}
