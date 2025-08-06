/*
 * Copyright 2018 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.odk.collect.android.formmanagement

import org.odk.collect.android.utilities.FormUtils
import org.odk.collect.android.utilities.WebCredentialsUtils
import org.odk.collect.forms.Form
import org.odk.collect.forms.FormSource
import org.odk.collect.forms.FormSourceException
import org.odk.collect.forms.FormsRepository
import org.odk.collect.forms.ManifestFile
import org.odk.collect.forms.MediaFile
import org.odk.collect.openrosa.forms.OpenRosaClient
import org.odk.collect.shared.strings.Md5.getMd5Hash
import timber.log.Timber

/**
 * Open to allow mocking (used in existing Java tests)
 */
open class ServerFormsDetailsFetcher(
    private val formsRepository: FormsRepository,
    private val formSource: FormSource
) {
    open fun updateUrl(url: String) {
        (formSource as OpenRosaClient).updateUrl(url)
    }

    open fun updateCredentials(webCredentialsUtils: WebCredentialsUtils) {
        (formSource as OpenRosaClient).updateWebCredentialsUtils(webCredentialsUtils)
    }

    @Throws(FormSourceException::class)
    open fun fetchFormDetails(): List<ServerFormDetails> {
        val formList = formSource.fetchFormList()
        return formList.map { listItem ->
            val manifestFile = listItem.manifestURL?.let {
                getManifestFile(formSource, it)
            }

            val forms = formsRepository.getAllNotDeletedByFormId(listItem.formID)
            val thisFormAlreadyDownloaded = forms.isNotEmpty()

            val formHash = listItem.hash
            val existingForm = if (formHash != null) {
                getFormByHash(formHash)
            } else {
                null
            }

            val isNewerFormVersionAvailable = listItem.hash.let {
                if (it == null) {
                    false
                } else if (thisFormAlreadyDownloaded) {
                    existingForm == null || existingForm.isDeleted
                } else {
                    false
                }
            }

            val areNewerMediaFilesAvailable = if (existingForm != null && manifestFile != null) {
                areNewerMediaFilesAvailable(existingForm, manifestFile.mediaFiles)
            } else {
                false
            }

            ServerFormDetails(
                listItem.name,
                listItem.downloadURL,
                listItem.formID,
                listItem.version,
                listItem.hash,
                !thisFormAlreadyDownloaded,
                isNewerFormVersionAvailable || areNewerMediaFilesAvailable,
                manifestFile
            )
        }
    }

    private fun getManifestFile(formSource: FormSource, manifestUrl: String): ManifestFile? {
        return try {
            formSource.fetchManifest(manifestUrl)
        } catch (formSourceException: FormSourceException) {
            Timber.w(formSourceException)
            null
        }
    }

    private fun areNewerMediaFilesAvailable(
        existingForm: Form,
        newMediaFiles: List<MediaFile>
    ): Boolean {
        if (newMediaFiles.isEmpty()) {
            return false
        }

        val localMediaHashes = FormUtils.getMediaFiles(existingForm)
            .map { it.getMd5Hash() }
            .toSet()

        return newMediaFiles.any {
            !it.filename.endsWith(".zip") && it.hash !in localMediaHashes
        }
    }

    private fun getFormByHash(hash: String): Form? {
        return formsRepository.getOneByMd5Hash(hash)
    }
}
