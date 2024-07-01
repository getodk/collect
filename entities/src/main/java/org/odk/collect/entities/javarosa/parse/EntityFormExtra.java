package org.odk.collect.entities.javarosa.parse;

import kotlin.Pair;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapMap;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.model.xform.XPathReference;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EntityFormExtra implements Externalizable {

    private List<Pair<XPathReference, String>> saveTos = new ArrayList<>();

    public EntityFormExtra() {
    }

    public EntityFormExtra(List<Pair<XPathReference, String>> saveTos) {
        this.saveTos = saveTos;
    }

    public List<Pair<XPathReference, String>> getSaveTos() {
        return saveTos;
    }

    @Override
    public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
        HashMap<XPathReference, String> saveToMap = (HashMap<XPathReference, String>) ExtUtil.read(in, new ExtWrapMap(XPathReference.class, String.class), pf);
        saveTos = saveToMap.entrySet().stream().map(entry -> new Pair<>(entry.getKey(), entry.getValue())).collect(Collectors.toList());
    }

    @Override
    public void writeExternal(DataOutputStream out) throws IOException {
        Map<XPathReference, String> saveTosMap = saveTos.stream()
            .collect(Collectors.toMap(Pair<XPathReference, String>::getFirst, Pair<XPathReference, String>::getSecond));
        ExtUtil.write(out, new ExtWrapMap(new HashMap<>(saveTosMap)));
    }
}
