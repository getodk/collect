package org.odk.collect.androidshared.ui

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import com.vdurmont.emoji.EmojiParser

class OneSignTextWatcher(private val editText: EditText) : TextWatcher {
    lateinit var oldTextString: String

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        oldTextString = s.toString()
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }

    override fun afterTextChanged(editable: Editable?) {
        var newTextString = editable.toString()
        if (oldTextString != newTextString) {
            val onlyText = EmojiParser.removeAllEmojis(newTextString)
            val onlyEmojis = EmojiParser.extractEmojis(newTextString)
            if (Character.codePointCount(onlyText, 0, onlyText.length) > 0) {
                if (onlyEmojis.size > 0) {
                    newTextString = onlyEmojis[0]
                } else {
                    if (Character.codePointCount(newTextString, 0, newTextString.length) > 1) {
                        newTextString = oldTextString
                    }
                }
            } else {
                if (onlyEmojis.size > 0) {
                    newTextString = onlyEmojis[0]
                }
            }
            editText.setText(newTextString)
            editText.setSelection(newTextString.length)
        }
    }
}
