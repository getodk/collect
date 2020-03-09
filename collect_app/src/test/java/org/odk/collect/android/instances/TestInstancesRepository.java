package org.odk.collect.android.instances;

import java.util.ArrayList;
import java.util.List;

public final class TestInstancesRepository implements InstancesRepository {
    List<Instance> instances;

    public TestInstancesRepository(List<Instance> instances) {
        this.instances = new ArrayList<>(instances);
    }

    @Override
    public Instance getBy(long databaseId) {
        for (Instance instance : instances) {
            if (instance.getDatabaseId() == databaseId) {
                return instance;
            }
        }

        return null;
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

    @Override
    public Instance getByPath(String instancePath) {
        List<Instance> result = new ArrayList<>();

        for (Instance instance : instances) {
            if (instance.getAbsoluteInstanceFilePath().equals(instancePath)) {
                result.add(instance);
            }
        }

        if (result.size() == 1) {
            return result.get(0);
        } else {
            return null;
        }
    }

    public void addInstance(Instance instance) {
        instances.add(instance);
    }

    public void removeInstanceById(Long databaseId) {
        for (int i = 0; i < instances.size(); i++) {
            if (instances.get(i).getDatabaseId().equals(databaseId)) {
                instances.remove(i);
                return;
            }
        }
    }
}
