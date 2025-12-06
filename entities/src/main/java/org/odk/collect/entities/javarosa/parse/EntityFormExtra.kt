package org.odk.collect.entities.javarosa.parse

import org.javarosa.core.util.externalizable.ExtUtil
import org.javarosa.core.util.externalizable.ExtWrapMap
import org.javarosa.core.util.externalizable.Externalizable
import org.javarosa.core.util.externalizable.PrototypeFactory
import org.javarosa.model.xform.XPathReference
import java.io.DataInputStream
import java.io.DataOutputStream
import kotlin.Pair
import kotlin.collections.HashMap

class EntityFormExtra() : Externalizable {
    constructor(saveTos: MutableList<Pair<XPathReference, String>>) : this() {
        this.saveTos = saveTos.toMutableList()
    }

    var saveTos = mutableListOf<Pair<XPathReference, String>>()

    override fun readExternal(`in`: DataInputStream, pf: PrototypeFactory) {
        val saveToMap = ExtUtil.read(`in`, ExtWrapMap(XPathReference::class.java, String::class.java), pf) as HashMap<XPathReference, String>
        saveTos = saveToMap.map { Pair(it.key, it.value) }.toMutableList()
    }

    override fun writeExternal(out: DataOutputStream) {
        val saveTosMap = saveTos.associate { it.first to it.second }
        ExtUtil.write(out, ExtWrapMap(HashMap(saveTosMap)))
    }
}
