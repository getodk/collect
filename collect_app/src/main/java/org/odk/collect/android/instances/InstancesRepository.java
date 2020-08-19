package org.odk.collect.android.instances;

import java.util.List;

/**
 * Gives access to {@link Instance} objects representing filled form instances on the device.
 *
 * The goal of this layer is to separate domain objects from data fetching concerns to facilitate
 * testing and reduce duplicated code to access those domain objects. To start, it has specific
 * query methods. Over time, we should expand it to mediate addition and removal. If there ends up
 * being may specific query methods, we may want to introduce a way to query more generically
 * without introducing new specialized methods (e.g. get(Specification s) instead of getBy(XYZ).
 */
public interface InstancesRepository {

    List<Instance> getAllFinalized();

    Instance get(long databaseId);

    List<Instance> getAllByJrFormId(String formId);

    List<Instance> getAllByJrFormIdAndJrVersion(String jrFormId, String jrVersion);

    List<Instance> getAllByJrFormIdAndJrVersionNotDeleted(String jrFormId, String jrVersion);

    /**
     * Get the Instance corresponding to the given path or null if no unique Instance matches.
     */
    Instance getByPath(String instancePath);

    void delete(Long id);
}