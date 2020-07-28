package org.odk.collect.android.formmanagement;

import org.odk.collect.android.forms.Form;
import org.odk.collect.android.forms.FormsRepository;
import org.odk.collect.android.instances.Instance;
import org.odk.collect.android.instances.InstancesRepository;
import org.odk.collect.android.provider.InstanceProviderAPI;

import java.util.List;
import java.util.stream.Stream;

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
        Stream<Instance> instancesForVersion;

        if (form.getJrVersion() != null) {
            instancesForVersion = instances.stream().filter(instance -> instance.getJrVersion().equals(form.getJrVersion()));
        } else {
            instancesForVersion = instances.stream();
        }


        if (instancesForVersion.anyMatch(instance -> !instance.getStatus().equals(InstanceProviderAPI.STATUS_SUBMITTED))) {
            formsRepository.softDelete(form.getId());
        } else {
            formsRepository.delete(id);
        }
    }
}
