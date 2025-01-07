package org.odk.collect.android.dynamicpreload

import org.javarosa.core.model.FormDef
import org.javarosa.core.model.QuestionDef
import org.javarosa.core.util.externalizable.Externalizable
import org.javarosa.core.util.externalizable.PrototypeFactory
import org.javarosa.xform.parse.IXFormParserFactory
import org.javarosa.xform.parse.XFormParser
import org.javarosa.xpath.expr.XPathExpression
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
    XFormParser.XPathProcessor,
    XFormParser.QuestionProcessor,
    XFormParser.FormDefProcessor {

    private var containsPullData = false
    private var containsSearch = false

    override fun processXPath(xPathExpression: XPathExpression) {
        if (containsPullData) {
            return // No need to search if we already found pulldata
        }

        if (xPathExpression.containsFunc("pulldata")) {
            containsPullData = true
        }
    }

    override fun processQuestion(question: QuestionDef) {
        if (containsSearch) {
            return // No need to search if we already found search
        }

        if (ExternalDataUtil.getSearchXPathExpression(question.appearanceAttr) != null) {
            containsSearch = true
        }
    }

    override fun processFormDef(formDef: FormDef) {
        if (containsPullData || containsSearch) {
            formDef.extras.put(DynamicPreloadExtra())
        }
    }
}

class DynamicPreloadExtra : Externalizable {
    override fun readExternal(`in`: DataInputStream?, pf: PrototypeFactory?) = Unit

    override fun writeExternal(out: DataOutputStream?) = Unit
}
