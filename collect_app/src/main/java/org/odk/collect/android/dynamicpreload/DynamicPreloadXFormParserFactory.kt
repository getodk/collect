package org.odk.collect.android.dynamicpreload

import org.javarosa.core.model.DataBinding
import org.javarosa.core.model.FormDef
import org.javarosa.core.model.QuestionDef
import org.javarosa.core.util.externalizable.ExtUtil
import org.javarosa.core.util.externalizable.Externalizable
import org.javarosa.core.util.externalizable.PrototypeFactory
import org.javarosa.xform.parse.IXFormParserFactory
import org.javarosa.xform.parse.XFormParser
import org.odk.collect.android.dynamicpreload.handler.ExternalDataHandlerPull
import java.io.DataInputStream
import java.io.DataOutputStream

class DynamicPreloadXFormParserFactory(base: IXFormParserFactory) :
    IXFormParserFactory.Wrapper(base) {

    override fun apply(xFormParser: XFormParser): XFormParser {
        return xFormParser.also {
            it.addProcessor(DynamicPreloadParseProcessor())
        }
    }
}

class DynamicPreloadParseProcessor :
    XFormParser.BindAttributeProcessor,
    XFormParser.QuestionProcessor,
    XFormParser.FormDefProcessor {

    private var containsSearchOrPullData = false

    override fun getBindAttributes(): Set<Pair<String, String>> {
        return setOf(
            Pair("", "calculate"),
            Pair("", "readonly"),
            Pair("", "required"),
            Pair("", "relevant")
        )
    }

    override fun processBindAttribute(name: String, value: String, binding: DataBinding) {
        val triggerable = when (name) {
            "calculate" -> {
                binding.calculate
            }

            "required" -> {
                binding.requiredCondition
            }

            "relevant" -> {
                binding.relevancyCondition
            }

            else -> {
                binding.readonlyCondition
            }
        }

        if (
            triggerable != null &&
            triggerable.expr.expr.containsFunc(ExternalDataHandlerPull.HANDLER_NAME)
        ) {
            containsSearchOrPullData = true
        }
    }

    override fun processQuestion(question: QuestionDef) {
        if (ExternalDataUtil.getSearchXPathExpression(question.appearanceAttr) != null) {
            containsSearchOrPullData = true
        }
    }

    override fun processFormDef(formDef: FormDef) {
        formDef.extras.put(DynamicPreloadExtra(containsSearchOrPullData))
    }
}

class DynamicPreloadExtra(usesDynamicPreload: Boolean) : Externalizable {

    constructor() : this(false)

    var usesDynamicPreload = usesDynamicPreload
        private set

    override fun readExternal(`in`: DataInputStream?, pf: PrototypeFactory?) {
        usesDynamicPreload = ExtUtil.read(`in`, Boolean::class.javaObjectType) as Boolean
    }

    override fun writeExternal(out: DataOutputStream?) {
        ExtUtil.write(out, usesDynamicPreload)
    }
}
