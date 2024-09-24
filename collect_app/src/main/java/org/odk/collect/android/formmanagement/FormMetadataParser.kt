package org.odk.collect.android.formmanagement

import org.javarosa.core.model.actions.setgeopoint.SetGeopointActionHandler
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

        lateinit var mainInstance: Element
        var submission: Element? = null
        for (i in 0 until model.childCount) {
            val child = model.getElement(i) ?: continue

            if (child.name == "instance" && child.attributeCount == 0) {
                mainInstance = child
            } else if (child.name == "submission") {
                submission = child
            }
        }

        val id = mainInstance.getElement(0).getAttributeValue(null, "id")
        val version = mainInstance.getElement(0).getAttributeValue(null, "version")
        val submissionUri = submission?.getAttributeValue(null, "action")
        val base64RsaPublicKey = submission?.getAttributeValue(null, "base64RsaPublicKey")
        val autoDelete = submission?.getAttributeValue(null, "auto-delete")
        val autoSend = submission?.getAttributeValue(null, "auto-send")
        val geometryXPath = getOverallFirstGeoPointXPath(model, mainInstance, body)

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
     * Returns an XPath path representing the first geopoint of this form definition or null if the
     * definition does not contain any field of type geopoint.
     *
     * The first geopoint is either of:
     *      (1) the first geopoint in the body that is not in a repeat
     *      (2) if the form has a setgeopoint action, the first geopoint in the instance that occurs
     *          before (1) or (1) if there is no geopoint defined before it in the instance.
     */
    private fun getOverallFirstGeoPointXPath(model: Element, mainInstance: Element, element: Element): String? {
        val firstTopLevelBodyGeoPoint = getFirstToplevelBodyGeoPointXPath(model, mainInstance, element)

        return if (containsSetgeopointAction(model)) {
            getInstanceGeoPointBeforeXPath(firstTopLevelBodyGeoPoint, model, mainInstance)
        } else {
            firstTopLevelBodyGeoPoint
        }
    }

    /**
     * Returns the reference of the first geopoint in the body that is not in a repeat.
     */
    private fun getFirstToplevelBodyGeoPointXPath(model: Element, mainInstance: Element, element: Element): String? {
        for (elementId in 0 until element.childCount) {
            val child = element.getElement(elementId)
            val ref = child.getAttributeValue(null, "ref")
            if (child.name == "input" && isGeopoint(model, ref)) {
                return ref
            } else if (child.name == "group") {
                return getFirstToplevelBodyGeoPointXPath(model, mainInstance, child)
            }
        }
        return null
    }

    /**
     * Returns the XPath path for the first geopoint in the primary instance that is before the given
     * reference and not in a repeat.
     */
    private fun getInstanceGeoPointBeforeXPath(firstTopLevelBodyGeoPoint: String?, model: Element, mainInstance: Element): String? {
        val mainInstanceRoot = mainInstance.getElement(0)
        for (elementId in 0 until mainInstanceRoot.childCount) {
            val child = mainInstanceRoot.getElement(elementId)
            val ref = "/${mainInstanceRoot.name}/${child.name}"
            if (ref == firstTopLevelBodyGeoPoint) {
                return null
            } else if (isGeopoint(model, ref)) {
                return ref
            } else if (child.childCount > 0) {
                return getInstanceGeoPointBeforeXPath(firstTopLevelBodyGeoPoint, model, child)
            }
        }
        return null
    }

    private fun containsSetgeopointAction(model: Element): Boolean {
        for (elementId in 0 until model.childCount) {
            val child = model.getElement(elementId)
            if (child.name == SetGeopointActionHandler.ELEMENT_NAME) {
                return true
            }
        }
        return false
    }

    private fun isGeopoint(model: Element, ref: String): Boolean {
        for (elementId in 0 until model.childCount) {
            val child = model.getElement(elementId)
            if (child.name == "bind" &&
                child.getAttributeValue(null, "nodeset") == ref &&
                child.getAttributeValue(null, "type") == "geopoint"
            ) {
                return true
            }
        }
        return false
    }
}

data class FormMetadata(
    val title: String?,
    val id: String?,
    val version: String?,
    val submissionUri: String?,
    val base64RsaPublicKey: String?,
    val autoDelete: String?,
    val autoSend: String?,
    val geometryXPath: String?
)
