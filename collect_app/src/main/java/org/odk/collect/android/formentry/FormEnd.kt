package org.odk.collect.android.formentry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.odk.collect.android.R
import org.odk.collect.android.application.CollectTheme
import org.odk.collect.androidshared.ui.compose.marginExtraSmall
import org.odk.collect.androidshared.ui.compose.marginStandard
import org.odk.collect.androidshared.ui.multiclicksafe.MultiClickGuard
import org.odk.collect.strings.R.string

@Composable
fun FormEnd(
    formTitle: String,
    isEditableAfterFinalization: Boolean,
    shouldBeSentAutomatically: Boolean,
    saveAsDraftEnabled: Boolean,
    finalizeEnabled: Boolean,
    onSave: (Boolean) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(marginStandard()),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(string.save_enter_data_description, formTitle),
            style = MaterialTheme.typography.titleLarge
        )

        if (finalizeEnabled) {
            val (icon, title, message) = getWarning(
            )
            EditWarning(icon = icon, title = title, message = message)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 64.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val screeName = stringResource(org.odk.collect.android.R.string.form_end_screen)

            if (saveAsDraftEnabled) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (MultiClickGuard.allowClick(screeName)) {
                            onSave(false)
                        }
                    }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.ic_save_menu_24),
                            contentDescription = null
                        )

                        Text(
                            text = stringResource(string.save_as_draft),
                            modifier = Modifier.padding(start = marginExtraSmall())
                        )
                    }
                }
            }

            if (finalizeEnabled) {
                val finalizeText = if (shouldBeSentAutomatically) {
                    string.send
                } else {
                    string.finalize
                }

                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (MultiClickGuard.allowClick(screeName)) {
                            onSave(true)
                        }
                    }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.ic_send_24),
                            contentDescription = null
                        )

                        Text(
                            text = stringResource(finalizeText),
                            modifier = Modifier.padding(start = marginExtraSmall())
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun getWarning(): Triple<Painter, String, String> {
    return Triple(painterResource(id = R.drawable.ic_edit_24), "", "")
}

@Composable
private fun EditWarning(
    icon: Painter,
    title: String,
    message: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = marginStandard())
    ) {
        Row(modifier = Modifier.padding(marginStandard())) {
            Icon(
                painter = icon,
                contentDescription = null
            )

            Column(modifier = Modifier.padding(start = marginStandard())) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewFormEnd(saveAsDraftEnabled: Boolean = true, finalizeEnabled: Boolean = true) {
    CollectTheme {
        FormEnd(
            formTitle = "My form",
            isEditableAfterFinalization = false,
            shouldBeSentAutomatically = false,
            saveAsDraftEnabled = saveAsDraftEnabled,
            finalizeEnabled = finalizeEnabled
        )
    }
}

@Preview
@Composable
private fun PreviewFormEndSaveAsDraftDisabled() {
    PreviewFormEnd(saveAsDraftEnabled = false)
}

@Preview
@Composable
private fun PreviewFormEndFinalizeDisabled() {
    PreviewFormEnd(finalizeEnabled = false)
}