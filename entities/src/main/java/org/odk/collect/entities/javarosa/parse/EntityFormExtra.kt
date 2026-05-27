package org.odk.collect.entities.javarosa.parse

import org.javarosa.core.util.externalizable.ExtUtil
import org.javarosa.core.util.externalizable.Externalizable
import org.javarosa.core.util.externalizable.PrototypeFactory
import java.io.DataInputStream
import java.io.DataOutputStream

class EntityFormExtra @JvmOverloads constructor(
    saveTos: List<SaveTo> = emptyList()
) : Externalizable {
    private val _saveTos: MutableList<SaveTo> = saveTos.toMutableList()

    val saveTos: List<SaveTo>
        get() = _saveTos

    override fun readExternal(input: DataInputStream, pf: PrototypeFactory) {
        val size = ExtUtil.readInt(input)
        _saveTos.clear()
        repeat(size) {
            val entry = SaveTo()
            entry.readExternal(input, pf)
            _saveTos.add(entry)
        }
    }

    override fun writeExternal(output: DataOutputStream) {
        ExtUtil.writeNumeric(output, _saveTos.size.toLong())
        _saveTos.forEach { it.writeExternal(output) }
    }
}
