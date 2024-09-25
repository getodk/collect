package org.odk.collect.android.formmanagement

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.odk.collect.android.formmanagement.FormMetadataParser.readMetadata

class FormMetadataParserTest {
    @Test
    fun readMetadata_withoutSubmission_returnsFormMetadata() {
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
        assertThat(formMetadata.version, equalTo(null))
        assertThat(formMetadata.base64RsaPublicKey, equalTo(null))
    }

    @Test
    fun readMetadata_withSubmission_returnsFormMetadata() {
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
                                    <orx:meta>
                                        <orx:instanceID/>
                                    </orx:meta>
                                </data>
                            </instance>
                            <submission 
                                action="foo" 
                                orx:auto-send="bar" 
                                orx:auto-delete="baz" 
                                base64RsaPublicKey="quux" 
                            />
                            <bind 
                                nodeset="/data/orx:meta/orx:instanceID" 
                                preload="uid" 
                                type="string" 
                            />
                        </model>
                    </h:head>
                    <h:body>
                    </h:body>
                </h:html>
            """.trimIndent().byteInputStream()
        )

        assertThat(formMetadata.title, equalTo("My Survey"))
        assertThat(formMetadata.id, equalTo("mysurvey"))
        assertThat(formMetadata.version, equalTo("2014083101"))
        assertThat(formMetadata.submissionUri, equalTo("foo"))
        assertThat(formMetadata.autoSend, equalTo("bar"))
        assertThat(formMetadata.autoDelete, equalTo("baz"))
        assertThat(formMetadata.base64RsaPublicKey, equalTo("quux"))
        assertThat(formMetadata.geometryXPath, equalTo(null))
    }

    @Test
    fun readMetadata_withGeopointsAtTopLevel_returnsFirstGeopointBasedOnBodyOrder() {
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
                                    <location2 />
                                    <name />
                                    <location1 />
                                </data>
                            </instance>
                            <bind nodeset="/data/name" type="string" />
                            <bind nodeset="/data/location2" type="geopoint" />
                            <bind nodeset="/data/location1" type="geopoint" />
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

        assertThat(formMetadata.title, equalTo("Two geopoints"))
        assertThat(formMetadata.id, equalTo("two-geopoints"))
        assertThat(formMetadata.geometryXPath, equalTo("/data/location1"))
    }

    @Test
    fun readMetadata_withGeopointInGroup_returnsFirstGeopointBasedOnBodyOrder() {
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
                                    <my-group>
                                        <location1 />
                                    </my-group>
                                    <location2 />
                                </data>
                            </instance>
                            <bind nodeset="/data/location2" type="geopoint" />
                            <bind nodeset="/data/my-group/location1" type="geopoint" />
                        </model>
                    </h:head>
                    <h:body>
                        <group ref="/data/my-group">
                            <input ref="/data/my-group/location1">
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

        assertThat(formMetadata.title, equalTo("Two geopoints in group"))
        assertThat(formMetadata.id, equalTo("two-geopoints-group"))
        assertThat(formMetadata.geometryXPath, equalTo("/data/my-group/location1"))
    }

    @Test
    fun readMetadata_withGeopointInRepeat_returnsFirstGeopointBasedOnBodyOrder() {
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

        assertThat(formMetadata.title, equalTo("Two geopoints repeat"))
        assertThat(formMetadata.id, equalTo("two-geopoints-repeat"))
        assertThat(formMetadata.geometryXPath, equalTo("/data/location2"))
    }

    @Test
    fun readMetadata_withSetGeopointBeforeBodyGeopoint_returnsFirstGeopointInInstance() {
        val formMetadata = readMetadata(
            """
                <?xml version="1.0"?>
                <h:html xmlns:h="http://www.w3.org/1999/xhtml"
                        xmlns:odk="http://www.opendatakit.org/xforms"
                        xmlns="http://www.w3.org/2002/xforms">
                    <h:head>
                        <h:title>Setgeopoint before</h:title>
                        <model>
                            <instance>
                                <data id="set-geopoint-before">
                                    <location1 />
                                    <location2 />
                                </data>
                            </instance>
                            <bind nodeset="/data/location2" type="geopoint" />
                            <bind nodeset="/data/location1" type="geopoint" />
                            <odk:setgeopoint ref="/data/location1" event="odk-instance-first-load"/>
                        </model>
                    </h:head>
                    <h:body>
                        <input ref="/data/location2">
                            <label>Location</label>
                        </input>
                    </h:body>
                </h:html>
            """.trimIndent().byteInputStream()
        )

        assertThat(formMetadata.title, equalTo("Setgeopoint before"))
        assertThat(formMetadata.id, equalTo("set-geopoint-before"))
        assertThat(formMetadata.geometryXPath, equalTo("/data/location1"))
    }

    @Test
    fun whenFormVersionIsEmpty_shouldBeTreatedAsNull() {
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
    fun formWithComments_isParsedSuccessfully() {
        readMetadata(
            """
                <?xml version="1.0"?>
                <!-- Blah -->
                <h:html xmlns:h="http://www.w3.org/1999/xhtml"
                        xmlns="http://www.w3.org/2002/xforms">
                    <!-- Blah -->
                    <h:head>
                        <!-- Blah -->
                        <h:title>Two geopoints</h:title>
                        <!-- Blah -->
                        <model>
                            <!-- Blah -->
                            <instance>
                                <!-- Blah -->
                                <data id="two-geopoints">
                                    <!-- Blah -->
                                    <location2 />
                                    <name />
                                    <location1 />
                                </data>
                                <!-- Blah -->
                            </instance>
                            <!-- Blah -->
                            <bind nodeset="/data/name" type="string" />
                            <bind nodeset="/data/location2" type="geopoint" />
                            <bind nodeset="/data/location1" type="geopoint" />
                        </model>
                    </h:head>
                    <h:body>
                        <!-- Blah -->
                        <input ref="/data/location1">
                            <!-- Blah -->
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
    }
}
