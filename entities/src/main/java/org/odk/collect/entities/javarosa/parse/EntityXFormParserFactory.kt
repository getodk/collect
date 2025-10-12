package org.odk.collect.entities.javarosa.parse

import org.javarosa.xform.parse.IXFormParserFactory
import org.javarosa.xform.parse.XFormParser

class EntityXFormParserFactory(
    base: IXFormParserFactory,
    private val v2025enabled: () -> Boolean
) : IXFormParserFactory.Wrapper(base) {
    override fun apply(parser: XFormParser): XFormParser {
        val processor = EntityFormParseProcessor(v2025enabled)
        parser.addProcessor(processor)

        return parser
    }
}
