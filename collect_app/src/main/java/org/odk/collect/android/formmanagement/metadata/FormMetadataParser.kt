package org.odk.collect.android.formmanagement.metadata

import org.javarosa.xform.parse.XFormParser
import org.kxml2.kdom.Element
import java.io.File
import java.io.InputStream

object FormMetadataParser {
    @JvmStatic
    fun readMetadata(formFile: File): FormMetadata {
        return readMetadata(formFile.inputStream())
    }

    @JvmStatic
    fun readMetadata(formFile: InputStream): FormMetadata {
        val doc = XFormParser.getXMLDocument(formFile.reader())
        val head = doc.getRootElement().getElement(null, "head")
        val model = head.getElement(null, "model")
        val body = doc.getRootElement().getElement(null, "body")
        val title = head.getElement(null, "title").getChild(0).toString()

        lateinit var mainInstanceRoot: Element
        var submission: Element? = null
        for (i in 0 until model.childCount) {
            val child = model.getElement(i) ?: continue

            if (child.name == "instance" && child.attributeCount == 0) {
                for (j in 0 until child.childCount) {
                    val mainInstanceChild = child.getElement(j) ?: continue
                    mainInstanceRoot = mainInstanceChild
                    break
                }
            } else if (child.name == "submission") {
                submission = child
            }
        }

        val id = mainInstanceRoot.getAttributeValue(null, "id")
        val version = mainInstanceRoot.getAttributeValue(null, "version")
        val submissionUri = submission?.getAttributeValue(null, "action")
        val base64RsaPublicKey = submission?.getAttributeValue(null, "base64RsaPublicKey")
        val autoDelete = submission?.getAttributeValue(null, "auto-delete")
        val autoSend = submission?.getAttributeValue(null, "auto-send")
        val geometryXPath = getFirstGeopointXPath(model, mainInstanceRoot, body)

        return FormMetadata(
            title,
            id,
            if (version.isNullOrBlank()) null else version,
            submissionUri,
            base64RsaPublicKey,
            autoDelete,
            autoSend,
            geometryXPath
        )
    }

    /**
     * Finds the first geopoint reference in the primary instance by:
     * 1. Retrieving all geopoint binds from the model.
     * 2. Iterating through the elements of the primary instance root.
     * 3. Returning the first reference found in the primary instance that matches one of
     *    the geopoint binds and is not inside a repeat.
     *
     * This solution is not perfect because it assumes that the references in the model
     * appear in the same order as in the body, which is not guaranteed by XForms.
     * However, in practice, this is typically the case.
     *
     */
    private fun getFirstGeopointXPath(model: Element, mainInstanceRoot: Element, body: Element): String? {
        val geopointXPaths = getGeopointXPaths(model)
        return if (geopointXPaths.isEmpty()) {
            null
        } else {
            val repeatXPaths = getRepeatXPaths(body)
            getFirstPrimaryInstanceGeopointXPath(geopointXPaths, repeatXPaths, mainInstanceRoot, null)
        }
    }

    private fun getGeopointXPaths(model: Element): List<String> {
        val geopointXPaths = mutableListOf<String>()
        for (position in 0 until model.childCount) {
            val child = model.getElement(position) ?: continue
            if (child.name == "bind" && child.getAttributeValue(null, "type") == "geopoint") {
                geopointXPaths.add(child.getAttributeValue(null, "nodeset"))
            }
        }
        return geopointXPaths
    }

    private fun getRepeatXPaths(body: Element): List<String> {
        val repeatXPaths = mutableListOf<String>()
        for (position in 0 until body.childCount) {
            val child = body.getElement(position) ?: continue
            if (child.name == "repeat") {
                repeatXPaths.add(child.getAttributeValue(null, "nodeset"))
            } else if (child.childCount > 0) {
                repeatXPaths.addAll(getRepeatXPaths(child))
            }
        }
        return repeatXPaths
    }

    private fun getFirstPrimaryInstanceGeopointXPath(
        geopointXPaths: List<String>,
        repeatXPaths: List<String>,
        parentRoot: Element,
        parentXPath: String?
    ): String? {
        for (position in 0 until parentRoot.childCount) {
            val child = parentRoot.getElement(position) ?: continue
            val xpath = if (parentXPath == null) {
                "/${parentRoot.name}/${child.name}"
            } else {
                "$parentXPath/${child.name}"
            }
            if (geopointXPaths.contains(xpath)) {
                return xpath
            } else if (child.childCount > 0 && !repeatXPaths.contains(xpath)) {
                return getFirstPrimaryInstanceGeopointXPath(geopointXPaths, repeatXPaths, child, xpath)
            }
        }
        return null
    }
}
