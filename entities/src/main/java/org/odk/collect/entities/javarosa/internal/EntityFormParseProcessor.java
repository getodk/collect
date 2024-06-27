package org.odk.collect.entities.javarosa.internal;

import kotlin.Pair;
import org.javarosa.core.model.DataBinding;
import org.javarosa.core.model.FormDef;
import org.odk.collect.entities.javarosa.UnrecognizedEntityVersionException;
import org.javarosa.model.xform.XPathReference;
import org.javarosa.xform.parse.XFormParser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class EntityFormParseProcessor implements XFormParser.BindAttributeProcessor, XFormParser.FormDefProcessor, XFormParser.ModelAttributeProcessor {

    private static final String ENTITIES_NAMESPACE = "http://www.opendatakit.org/xforms/entities";
    private static final String[] SUPPORTED_VERSIONS = {"2022.1", "2023.1"};

    private final List<Pair<XPathReference, String>> saveTos = new ArrayList<>();
    private boolean versionPresent;

    @Override
    public Set<Pair<String, String>> getModelAttributes() {
        HashSet<Pair<String, String>> attributes = new HashSet<>();
        attributes.add(new Pair<>(ENTITIES_NAMESPACE, "entities-version"));

        return attributes;
    }

    @Override
    public void processModelAttribute(String name, String value) throws XFormParser.ParseException {
        versionPresent = true;

        if (Stream.of(SUPPORTED_VERSIONS).noneMatch(value::startsWith)) {
            throw new UnrecognizedEntityVersionException();
        }
    }

    @Override
    public Set<Pair<String, String>> getBindAttributes() {
        HashSet<Pair<String, String>> attributes = new HashSet<>();
        attributes.add(new Pair<>(ENTITIES_NAMESPACE, "saveto"));

        return attributes;
    }

    @Override
    public void processBindAttribute(String name, String value, DataBinding binding) {
        saveTos.add(new Pair<>((XPathReference) binding.getReference(), value));
    }

    @Override
    public void processFormDef(FormDef formDef) throws XFormParser.ParseException {
        if (!versionPresent && EntityFormParser.getEntityElement(formDef.getMainInstance()) != null) {
            throw new XFormParser.MissingModelAttributeException(ENTITIES_NAMESPACE, "entities-version");
        }

        EntityFormExtra entityFormExtra = new EntityFormExtra(saveTos);
        formDef.getExtras().put(entityFormExtra);
    }
}
