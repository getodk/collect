package org.odk.collect.android.formmanagement;

import org.odk.collect.android.forms.Form;
import org.odk.collect.android.forms.FormsRepository;
import org.odk.collect.android.instances.Instance;
import org.odk.collect.android.instances.InstancesRepository;

import java.util.List;
import java.util.Objects;

public class FormDeleter {

    private final FormsRepository formsRepository;
    private final InstancesRepository instancesRepository;

    public FormDeleter(FormsRepository formsRepository, InstancesRepository instancesRepository) {
        this.formsRepository = formsRepository;
        this.instancesRepository = instancesRepository;
    }

    public void delete(Long id) {
        Form form = formsRepository.get(id);
        List<Instance> instances = instancesRepository.getAllByJrFormId(form.getJrFormId());
        long instancesForVersion = instances.stream().filter(instance -> Objects.equals(instance.getJrVersion(), form.getJrVersion())).count();

        if (instancesForVersion > 0) {
            formsRepository.softDelete(form.getId());
        } else {
            formsRepository.delete(id);
        }
    }
}
