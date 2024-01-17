package org.odk.collect.android.formhierarchy

import android.os.Bundle
import android.view.View
import android.widget.Button
import org.javarosa.core.model.FormIndex
import org.odk.collect.android.R
import org.odk.collect.android.javarosawrapper.FormController

/**
 * Displays the structure of a form along with the answers for the current instance. Disables all
 * features that allow the user to edit the form instance.
 */
class ViewOnlyFormHierarchyActivity : FormHierarchyActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedCallback.remove()
    }

    /**
     * Hides buttons to jump to the beginning and to the end of the form instance to edit it. Adds
     * an extra exit button that exits this activity.
     */
    public override fun configureButtons(formController: FormController) {
        val exitButton = findViewById<Button>(R.id.exitButton)
        exitButton.setOnClickListener {
            setResult(RESULT_OK)
            finish()
        }
        exitButton.visibility = View.VISIBLE
        jumpBeginningButton.visibility = View.GONE
        jumpEndButton.visibility = View.GONE
    }

    override fun showDeleteButton(shouldShow: Boolean) {
        // Disabled.
    }

    override fun showAddButton(shouldShow: Boolean) {
        // Disabled.
    }

    /**
     * Prevents the user from clicking on individual questions to jump into the form-filling view.
     */
    public override fun onQuestionClicked(index: FormIndex) {
        // Do nothing
    }
}
