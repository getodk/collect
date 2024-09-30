package org.odk.collect.android.formmanagement.metadata

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.odk.collect.android.formmanagement.metadata.FormMetadataParser.readMetadata

class FormMetadataParserTest {
    @Test
    fun readMetadata_canParseFormsWithComments() {
        readMetadata(
            """
                <?xml version="1.0"?>
                <!-- Blah -->
                <h:html xmlns:h="http://www.w3.org/1999/xhtml"
                        xmlns="http://www.w3.org/2002/xforms">
                    <!-- Blah -->
                    <h:head>
                        <!-- Blah -->
                        <h:title>Form with comments</h:title>
                        <!-- Blah -->
                        <model>
                            <!-- Blah -->
                            <instance>
                                <!-- Blah -->
                                <data id="form-with-comments">
                                    <!-- Blah -->
                                </data>
                                <!-- Blah -->
                            </instance>
                            <!-- Blah -->
                        </model>
                    </h:head>
                    <h:body>
                        <!-- Blah -->
                    </h:body>
                </h:html>
            """.trimIndent().byteInputStream()
        )
    }

    @Test
    fun readMetadata_returnsCorrectValuesForMandatoryElements() {
        val formMetadata = readMetadata(
            """
                <?xml version="1.0"?>
                <h:html xmlns="http://www.w3.org/2002/xforms"
                        xmlns:h="http://www.w3.org/1999/xhtml"
                        xmlns:orx="http://openrosa.org/xforms">
                    <h:head>
                        <h:title>My Survey</h:title>
                        <model>
                            <instance>
                                <data id="mysurvey">
                                </data>
                            </instance>
                        </model>
                    </h:head>
                    <h:body>
                    </h:body>
                </h:html>
            """.trimIndent().byteInputStream()
        )

        assertThat(formMetadata.title, equalTo("My Survey"))
        assertThat(formMetadata.id, equalTo("mysurvey"))
    }

    @Test
    fun readMetadata_withoutOptionalMetadata_returnsNullValuesForThoseElements() {
        val formMetadata = readMetadata(
            """
                <?xml version="1.0"?>
                <h:html xmlns="http://www.w3.org/2002/xforms"
                        xmlns:h="http://www.w3.org/1999/xhtml"
                        xmlns:orx="http://openrosa.org/xforms">
                    <h:head>
                        <h:title>My Survey</h:title>
                        <model>
                            <instance>
                                <data id="mysurvey">
                                </data>
                            </instance>
                        </model>
                    </h:head>
                    <h:body>
                    </h:body>
                </h:html>
            """.trimIndent().byteInputStream()
        )

        assertThat(formMetadata.version, equalTo(null))
        assertThat(formMetadata.submissionUri, equalTo(null))
        assertThat(formMetadata.autoSend, equalTo(null))
        assertThat(formMetadata.autoDelete, equalTo(null))
        assertThat(formMetadata.base64RsaPublicKey, equalTo(null))
        assertThat(formMetadata.geometryXPath, equalTo(null))
    }

    @Test
    fun readMetadata_witOptionalMetadata_returnsCorrectValuesForThoseElements() {
        val formMetadata = readMetadata(
            """
                <?xml version="1.0"?>
                <h:html xmlns="http://www.w3.org/2002/xforms"
                        xmlns:h="http://www.w3.org/1999/xhtml"
                        xmlns:orx="http://openrosa.org/xforms">
                    <h:head>
                        <h:title>My Survey</h:title>
                        <model>
                            <instance>
                                <data id="mysurvey" orx:version="2014083101">
                                    <location1 />
                                </data>
                            </instance>
                            <submission 
                                action="foo" 
                                orx:auto-send="bar" 
                                orx:auto-delete="baz" 
                                base64RsaPublicKey="quux" 
                            />
                            <bind nodeset="/data/location1" type="geopoint" />
                        </model>
                    </h:head>
                    <h:body>
                        <input ref="/data/location1">
                            <label>Location</label>
                        </input>
                    </h:body>
                </h:html>
            """.trimIndent().byteInputStream()
        )

        assertThat(formMetadata.version, equalTo("2014083101"))
        assertThat(formMetadata.submissionUri, equalTo("foo"))
        assertThat(formMetadata.autoSend, equalTo("bar"))
        assertThat(formMetadata.autoDelete, equalTo("baz"))
        assertThat(formMetadata.base64RsaPublicKey, equalTo("quux"))
        assertThat(formMetadata.geometryXPath, equalTo("/data/location1"))
    }

    @Test
    fun readMetadata_withEmptyFormVersion_returnsNullFormVersion() {
        val formMetadata = readMetadata(
            """
                <?xml version="1.0"?>
                <h:html xmlns="http://www.w3.org/2002/xforms"
                        xmlns:h="http://www.w3.org/1999/xhtml"
                        xmlns:orx="http://openrosa.org/xforms">
                    <h:head>
                        <h:title>My Survey</h:title>
                        <model>
                            <instance>
                                <data id="mysurvey" orx:version="   ">
                                </data>
                            </instance>
                        </model>
                    </h:head>
                    <h:body>
                    </h:body>
                </h:html>
            """.trimIndent().byteInputStream()
        )

        assertThat(formMetadata.version, equalTo(null))
    }

    @Test
    fun readMetadata_withGeopointsAtTopLevel_returnsFirstGeopointXPath() {
        val formMetadata = readMetadata(
            """
                <?xml version="1.0"?>
                <h:html xmlns:h="http://www.w3.org/1999/xhtml"
                        xmlns="http://www.w3.org/2002/xforms">
                    <h:head>
                        <h:title>Two geopoints</h:title>
                        <model>
                            <instance>
                                <data id="two-geopoints">
                                    <location1 />
                                    <name />
                                    <location2 />
                                </data>
                            </instance>
                            <bind nodeset="/data/location1" type="geopoint" />
                            <bind nodeset="/data/name" type="string" />
                            <bind nodeset="/data/location2" type="geopoint" />
                        </model>
                    </h:head>
                    <h:body>
                        <input ref="/data/location1">
                            <label>Location</label>
                        </input>
                        <input ref="/data/name">
                            <label>Name</label>
                        </input>
                        <input ref="/data/location2">
                            <label>Location</label>
                        </input>
                    </h:body>
                </h:html>
            """.trimIndent().byteInputStream()
        )

        assertThat(formMetadata.geometryXPath, equalTo("/data/location1"))
    }

    @Test
    fun readMetadata_withGeopointsAtTopLevel_returnsGeopointXPathThatBelongsToSetgeopointActionIfItIsTheFirstOne() {
        val formMetadata = readMetadata(
            """
                <?xml version="1.0"?>
                <h:html xmlns:h="http://www.w3.org/1999/xhtml"
                        xmlns="http://www.w3.org/2002/xforms"
                        xmlns:odk="http://www.opendatakit.org/xforms">
                    <h:head>
                        <h:title>Two geopoints</h:title>
                        <model>
                            <instance>
                                <data id="two-geopoints">
                                    <location1 />
                                    <name />
                                    <location2 />
                                </data>
                            </instance>
                            <bind nodeset="/data/location1" type="geopoint" />
                            <bind nodeset="/data/name" type="string" />
                            <bind nodeset="/data/location2" type="geopoint" />
                            <odk:setgeopoint ref="/data/location1" event="odk-instance-first-load"/>
                        </model>
                    </h:head>
                    <h:body>
                        <input ref="/data/name">
                            <label>Name</label>
                        </input>
                        <input ref="/data/location2">
                            <label>Location</label>
                        </input>
                    </h:body>
                </h:html>
            """.trimIndent().byteInputStream()
        )

        assertThat(formMetadata.geometryXPath, equalTo("/data/location1"))
    }

    @Test
    fun readMetadata_withGeopointInGroup_returnsFirstGeopointXPath() {
        val formMetadata = readMetadata(
            """
                <?xml version="1.0"?>
                <h:html xmlns:h="http://www.w3.org/1999/xhtml"
                        xmlns="http://www.w3.org/2002/xforms">
                    <h:head>
                        <h:title>Two geopoints in group</h:title>
                        <model>
                            <instance>
                                <data id="two-geopoints-group">
                                    <my-group1>
                                        <my-group2>
                                            <location1 />
                                        </my-group2>
                                    </my-group1>
                                    <location2 />
                                </data>
                            </instance>
                            <bind nodeset="/data/my-group1/my-group2/location1" type="geopoint" />
                            <bind nodeset="/data/location2" type="geopoint" />
                        </model>
                    </h:head>
                    <h:body>
                        <group ref="/data/my-group1/my-group2">
                            <input ref="/data/my-group1/my-group2/location1">
                                <label>Location</label>
                            </input>
                        </group>
                        <input ref="/data/location2">
                            <label>Location</label>
                        </input>
                    </h:body>
                </h:html>
            """.trimIndent().byteInputStream()
        )

        assertThat(formMetadata.geometryXPath, equalTo("/data/my-group1/my-group2/location1"))
    }

    @Test
    fun readMetadata_withGeopointInRepeat_returnsFirstGeopointXPathThatIsNotInsideRepeat() {
        val formMetadata = readMetadata(
            """
                <?xml version="1.0"?>
                <h:html xmlns:h="http://www.w3.org/1999/xhtml"
                        xmlns="http://www.w3.org/2002/xforms">
                    <h:head>
                        <h:title>Two geopoints repeat</h:title>
                        <model>
                            <instance>
                                <data id="two-geopoints-repeat">
                                    <my-repeat>
                                        <location1 />
                                    </my-repeat>
                                    <location2 />
                                </data>
                            </instance>
                            <bind nodeset="/data/location2" type="geopoint" />
                            <bind nodeset="/data/my-repeat/location1" type="geopoint" />
                        </model>
                    </h:head>
                    <h:body>
                        <repeat nodeset="/data/my-repeat">
                            <input ref="/data/my-repeat/location1">
                                <label>Location</label>
                            </input>
                        </repeat>
                        <input ref="/data/location2">
                            <label>Location</label>
                        </input>
                    </h:body>
                </h:html>
            """.trimIndent().byteInputStream()
        )

        assertThat(formMetadata.geometryXPath, equalTo("/data/location2"))
    }
}
