package org.odk.collect.entities.javarosa.internal;

import org.odk.collect.entities.javarosa.Entity;

import java.util.List;

public class EntitiesExtra {

    private final List<Entity> entities;

    public EntitiesExtra(List<Entity> entities) {
        this.entities = entities;
    }

    public List<Entity> getEntities() {
        return entities;
    }
}
