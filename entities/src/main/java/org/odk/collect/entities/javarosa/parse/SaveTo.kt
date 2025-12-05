package org.odk.collect.entities.javarosa.parse

import org.javarosa.core.model.instance.TreeReference
import org.javarosa.core.util.externalizable.ExtUtil
import org.javarosa.core.util.externalizable.Externalizable
import org.javarosa.core.util.externalizable.PrototypeFactory
import org.javarosa.model.xform.XPathReference
import java.io.DataInputStream
import java.io.DataOutputStream

data class SaveTo(
    var reference: XPathReference? = null,
    var value: String? = null,
    var entityReference: TreeReference? = null
) : Externalizable {

    fun updateEntityReference(entityReference: TreeReference) {
        this.entityReference = entityReference
    }

    override fun readExternal(input: DataInputStream, pf: PrototypeFactory) {
        reference = ExtUtil.read(input, XPathReference::class.java, pf) as XPathReference
        value = ExtUtil.read(input, String::class.java, pf) as String
        entityReference = ExtUtil.read(input, TreeReference::class.java, pf) as TreeReference
    }

    override fun writeExternal(output: DataOutputStream) {
        ExtUtil.write(output, reference)
        ExtUtil.write(output, value)
        ExtUtil.write(output, entityReference)
    }
}
