package org.odk.collect.android.dynamicpreload

import org.javarosa.core.model.DataBinding
import org.javarosa.core.model.FormDef
import org.javarosa.core.model.QuestionDef
import org.javarosa.core.util.externalizable.ExtUtil
import org.javarosa.core.util.externalizable.Externalizable
import org.javarosa.core.util.externalizable.PrototypeFactory
import org.javarosa.xform.parse.IXFormParserFactory
import org.javarosa.xform.parse.XFormParser
import org.javarosa.xform.parse.XFormParserFactory
import org.kxml2.kdom.Document
import org.odk.collect.android.dynamicpreload.handler.ExternalDataHandlerPull
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.Reader

class DynamicPreloadXFormParserFactory(private val wrapped: IXFormParserFactory) :
    XFormParserFactory() {
    override fun getXFormParser(reader: Reader?): XFormParser {
        return configureDetector(wrapped.getXFormParser(reader))
    }

    override fun getXFormParser(doc: Document?): XFormParser {
        return configureDetector(wrapped.getXFormParser(doc))
    }

    override fun getXFormParser(form: Reader?, instance: Reader?): XFormParser {
        return configureDetector(wrapped.getXFormParser(form, instance))
    }

    override fun getXFormParser(form: Document?, instance: Document?): XFormParser {
        return configureDetector(wrapped.getXFormParser(form, instance))
    }

    private fun configureDetector(xFormParser: XFormParser): XFormParser {
        xFormParser.addProcessor(DynamicPreloadParseProcessor())

        return xFormParser
    }
}

private class DynamicPreloadParseProcessor :
    XFormParser.BindAttributeProcessor,
    XFormParser.QuestionProcessor,
    XFormParser.FormDefProcessor {

    private var containsSearchOrPullData = false

    override fun getBindAttributes(): Set<Pair<String, String>> {
        return setOf(Pair("", "calculate"))
    }

    override fun processBindAttribute(name: String, value: String, binding: DataBinding) {
        val bindingCalculate = binding.calculate
        if (
            bindingCalculate != null &&
            bindingCalculate.expr.expr.containsFunc(ExternalDataHandlerPull.HANDLER_NAME)
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
        usesDynamicPreload = ExtUtil.read(`in`, Boolean::class.java) as Boolean
    }

    override fun writeExternal(out: DataOutputStream?) {
        ExtUtil.write(out, usesDynamicPreload)
    }
}
