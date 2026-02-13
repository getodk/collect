package org.odk.collect.entities.javarosa.support

import org.javarosa.test.BindBuilderXFormsElement
import org.javarosa.test.BindBuilderXFormsElement.bind
import org.javarosa.test.XFormsElement
import org.javarosa.test.XFormsElement.setvalue
import org.javarosa.test.XFormsElement.t
import org.odk.collect.entities.javarosa.spec.EntityAction

object EntityXFormsElement {
    fun entityNode(
        dataset: String,
        action: EntityAction,
        optionalAction: Boolean = false
    ): XFormsElement {
        val actionValue = if (optionalAction) {
            ""
        } else {
            "1"
        }

        return when (action) {
            EntityAction.CREATE -> t(
                "entity dataset=\"$dataset\" create=\"$actionValue\" id=\"\"",
                t("label")
            )

            EntityAction.UPDATE -> t(
                "entity dataset=\"$dataset\" update=\"$actionValue\" id=\"\"",
                t("label")
            )
        }
    }

    fun entityLabelBind(ref: String): BindBuilderXFormsElement {
        return bind("/data/meta/entity/label").type("string").calculate(ref)
    }

    fun entityIdBind(): BindBuilderXFormsElement {
        return bind("/data/meta/entity/@id").type("string")
    }

    fun entityIdSetValue(): XFormsElement {
        return setvalue("odk-instance-first-load", "/data/meta/entity/@id", "uuid()")
    }

    fun BindBuilderXFormsElement.withSaveTo(property: String): BindBuilderXFormsElement {
        return withAttribute("entities", "saveto", property)
    }
}
