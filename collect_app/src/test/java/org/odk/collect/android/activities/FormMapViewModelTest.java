package org.odk.collect.android.activities;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.odk.collect.android.activities.viewmodels.FormMapViewModel;
import org.odk.collect.forms.Form;
import org.odk.collect.forms.instances.Instance;
import org.odk.collect.forms.instances.InstancesRepository;
import org.odk.collect.formstest.InMemInstancesRepository;

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

    @Test public void getFormId_returnsFormDatabaseId() {
        FormMapViewModel viewModel = new FormMapViewModel(TEST_FORM_1, testInstancesRepository);
        assertThat(viewModel.getFormId(), is(0L));
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

    // Instances that failed to send are not editable starting in v2021.2
    @Test public void instanceThatFailedToSend_thatCanBeEdited_hasViewableStatus() {
        FormMapViewModel viewModel = new FormMapViewModel(TEST_FORM_1, testInstancesRepository);
        List<FormMapViewModel.MappableFormInstance> instances = viewModel.getMappableFormInstances();

        assertThat(instances.get(3).getClickAction(), is(FormMapViewModel.ClickAction.OPEN_READ_ONLY));
    }

    // Sent instances should never be editable.
    @Test public void submittedInstance_thatCanBeEdited_returnsViewableStatus() {
        FormMapViewModel viewModel = new FormMapViewModel(TEST_FORM_1, testInstancesRepository);
        List<FormMapViewModel.MappableFormInstance> instances = viewModel.getMappableFormInstances();

        assertThat(instances.get(4).getClickAction(), is(FormMapViewModel.ClickAction.OPEN_READ_ONLY));
    }

    // E.g. encrypted forms
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

        Instance newInstance = new Instance.Builder().dbId(8L)
                .formId("formId1")
                .formVersion("2019103101")
                .geometryType("Point")
                .geometry("{\"type\":\"Point\",\"coordinates\":[127.6, 11.1]}")
                .canEditWhenComplete(true)
                .status(Instance.STATUS_COMPLETE).build();

        ((InMemInstancesRepository) testInstancesRepository).save(newInstance);

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

        ((InMemInstancesRepository) testInstancesRepository).save(new Instance.Builder().dbId(6L)
                .formId("formId1")
                .formVersion("2019103101")
                .geometryType("")
                .geometry("")
                .canEditWhenComplete(false)
                .status(Instance.STATUS_SUBMISSION_FAILED).build());

        mappableInstances = viewModel.getMappableFormInstances();
        assertThat(viewModel.getTotalInstanceCount(), is(7));
        assertThat(mappableInstances.size(), is(5));
        assertThat(mappableInstances.get(4).getClickAction(), is(FormMapViewModel.ClickAction.OPEN_READ_ONLY));
    }

    static final Form TEST_FORM_1 = new Form.Builder().dbId(0L)
            .formId("formId1")
            .version("2019103104")
            .displayName("Form with ID 1")
            .geometryXpath("/data/my-point")
            .build();

    private static final Form TEST_FORM_2 = new Form.Builder().dbId(0L)
            .formId("formId2")
            .version("2019103103")
            .displayName("Form with ID 2")
            .geometryXpath("/data/my-point2")
            .build();

    static Instance[] testInstances = {
            new Instance.Builder().dbId(0L)
                    .displayName("Form1")
                    .lastStatusChangeDate(1487782554846L)
                    .formId("formId1")
                    .formVersion("2019103101")
                    .deletedDate(1487782554846L)
                    .geometryType("Point")
                    .geometry("{\"type\":\"Point\",\"coordinates\":[125.6, 10.0]}")
                    .status(Instance.STATUS_SUBMITTED).build(),

            new Instance.Builder().dbId(1L)
                    .displayName("Form2")
                    .lastStatusChangeDate(1488782558743L)
                    .formId("formId1")
                    .formVersion("2019103101")
                    .geometryType("Point")
                    .geometry("{\"type\":\"Point\",\"coordinates\":[125.6, 10.1]}")
                    .canEditWhenComplete(true)
                    .status(Instance.STATUS_COMPLETE).build(),

            new Instance.Builder().dbId(2L)
                    .displayName("Form3")
                    .lastStatusChangeDate(1484582553254L)
                    .formId("formId1")
                    .formVersion("2019103102")
                    .geometryType("Point")
                    .geometry("{\"type\":\"Point\",\"coordinates\":[126.6, 10.1]}")
                    .status(Instance.STATUS_INCOMPLETE).build(),

            new Instance.Builder().dbId(3L)
                    .displayName("Form4")
                    .lastStatusChangeDate(1488582557456L)
                    .formId("formId1")
                    .formVersion("2019103101")
                    .status(Instance.STATUS_COMPLETE).build(),

            new Instance.Builder().dbId(4L)
                    .displayName("Form5")
                    .lastStatusChangeDate(1483582557438L)
                    .formId("formId1")
                    .formVersion("2019103106")
                    .geometryType("Point")
                    .geometry("{\"type\":\"Point\",\"coordinates\":[125.6, 10.3]}")
                    .canEditWhenComplete(true)
                    .status(Instance.STATUS_SUBMISSION_FAILED).build(),

            new Instance.Builder().dbId(5L)
                    .displayName("Form6")
                    .lastStatusChangeDate(1482282559618L)
                    .formId("formId1")
                    .formVersion("2019103101")
                    .geometryType("Point")
                    .geometry("{\"type\":\"Point\",\"coordinates\":[125.7, 10.3]}")
                    .canEditWhenComplete(true)
                    .status(Instance.STATUS_SUBMITTED).build(),

            new Instance.Builder().dbId(6L)
                    .displayName("Form7")
                    .lastStatusChangeDate(1484782559836L)
                    .formId("formId1")
                    .formVersion("2019103101")
                    .geometryType("Point")
                    .geometry("{\"type\":\"Point\",\"coordinates\":[125.6, 10.4]}")
                    .canEditWhenComplete(false)
                    .status(Instance.STATUS_SUBMISSION_FAILED).build(),

            new Instance.Builder().dbId(7L)
                    .displayName("Form8")
                    .lastStatusChangeDate(1487982552254L)
                    .formId("formId2")
                    .formVersion("2019103101")
                    .geometryType("Point")
                    .geometry("Crazy stuff")
                    .status(Instance.STATUS_COMPLETE).build(),

            new Instance.Builder().dbId(8L)
                    .displayName("Form9")
                    .lastStatusChangeDate(1484682557369L)
                    .formId("formId2")
                    .formVersion("2019103101")
                    .geometryType("Crazy stuff")
                    .geometry("{\"type\":\"Point\",\"coordinates\":[125.6, 10.4]}")
                    .status(Instance.STATUS_COMPLETE).build(),
            };
}
