package org.odk.collect.entities.javarosa.parse

import org.javarosa.core.model.instance.TreeReference
import org.javarosa.core.util.externalizable.ExtUtil
import org.javarosa.core.util.externalizable.Externalizable
import org.javarosa.core.util.externalizable.PrototypeFactory
import java.io.DataInputStream
import java.io.DataOutputStream

class SaveTo() : Externalizable {
    lateinit var reference: TreeReference
        private set
    lateinit var value: String
        private set
    lateinit var entityGroupReference: TreeReference
        private set

    constructor(
        reference: TreeReference,
        value: String
    ) : this() {
        this.reference = reference
        this.value = value
    }

    fun updateEntityReference(entityReference: TreeReference) {
        this.entityGroupReference = entityReference
    }

    override fun readExternal(input: DataInputStream, pf: PrototypeFactory) {
        reference = ExtUtil.read(input, TreeReference::class.java, pf) as TreeReference
        value = ExtUtil.read(input, String::class.java, pf) as String
        entityGroupReference = ExtUtil.read(input, TreeReference::class.java, pf) as TreeReference
    }

    override fun writeExternal(output: DataOutputStream) {
        ExtUtil.write(output, reference)
        ExtUtil.write(output, value)
        ExtUtil.write(output, entityGroupReference)
    }
}
