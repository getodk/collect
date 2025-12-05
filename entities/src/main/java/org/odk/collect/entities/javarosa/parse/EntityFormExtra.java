package org.odk.collect.entities.javarosa.parse;

import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EntityFormExtra implements Externalizable {

    private List<SaveTo> saveTos = new ArrayList<>();

    public EntityFormExtra() {
    }

    public EntityFormExtra(List<SaveTo> saveTos) {
        this.saveTos = saveTos;
    }

    public List<SaveTo> getSaveTos() {
        return saveTos;
    }

    @Override
    public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException {
        int size = ExtUtil.readInt(in);
        saveTos = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            SaveTo entry = new SaveTo();
            entry.readExternal(in, pf);
            saveTos.add(entry);
        }
    }

    @Override
    public void writeExternal(DataOutputStream out) throws IOException {
        ExtUtil.writeNumeric(out, saveTos.size());
        for (SaveTo entry : saveTos) {
            entry.writeExternal(out);
        }
    }
}
