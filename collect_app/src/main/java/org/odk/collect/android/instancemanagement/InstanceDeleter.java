package org.odk.collect.android.instancemanagement;

import org.odk.collect.android.forms.Form;
import org.odk.collect.android.forms.FormsRepository;
import org.odk.collect.android.instances.Instance;
import org.odk.collect.android.instances.InstancesRepository;

import java.util.List;

public class InstanceDeleter {

    private final InstancesRepository instancesRepository;
    private final FormsRepository formsRepository;

    public InstanceDeleter(InstancesRepository instancesRepository, FormsRepository formsRepository) {
        this.instancesRepository = instancesRepository;
        this.formsRepository = formsRepository;
    }

    public void delete(Long id) {
        Instance instance = instancesRepository.get(id);
        instancesRepository.delete(id);

        Form form = formsRepository.get(instance.getJrFormId(), instance.getJrVersion());
        if (form != null && form.isDeleted()) {
            List<Instance> otherInstances = instancesRepository.getAllByJrFormIdAndJrVersionNotDeleted(form.getJrFormId(), form.getJrVersion());
            if (otherInstances.isEmpty()) {
                formsRepository.delete(form.getId());
            }
        }
    }
}
