package org.odk.collect.android.instances;

import java.util.ArrayList;
import java.util.List;

public final class TestInstancesRepository implements InstancesRepository {
    List<Instance> instances;

    public TestInstancesRepository(List<Instance> instances) {
        this.instances = instances;
    }

    @Override
    public List<Instance> getAllBy(String formId) {
        List<Instance> result = new ArrayList<>();

        for (Instance instance : instances) {
            if (instance.getJrFormId().equals(formId)) {
                result.add(instance);
            }
        }

        return result;
    }

    public void addInstance(Instance instance) {
        instances.add(instance);
    }

    public void removeInstanceById(int databaseId) {
        for (Instance instance : instances) {
            if (instance.getDatabaseId() == databaseId) {
                instances.remove(instance);
                return;
            }
        }
    }
}
