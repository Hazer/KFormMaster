package com.thejuki.kformmaster.view

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatEditText
import android.support.v7.widget.AppCompatTextView
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.github.vivchar.rendererrecyclerviewadapter.binder.ViewBinder
import com.thejuki.kformmaster.R
import com.thejuki.kformmaster.helper.FormBuildHelper
import com.thejuki.kformmaster.model.FormTokenAutoCompleteElement

/**
 * Form EditText Binder
 *
 * Renderer for FormEditTextElement
 *
 * @author **TheJuki** ([GitHub](https://github.com/TheJuki))
 * @version 1.0
 */
class FormTokenAutoCompleteViewBinder(private val context: Context, private val formBuilder: FormBuildHelper) : BaseFormViewBinder() {
    var viewBinder = ViewBinder(R.layout.form_element_token_auto_complete, FormTokenAutoCompleteElement::class.java) { model, finder, _ ->
        val textViewTitle = finder.find(R.id.formElementTitle) as AppCompatTextView
        val textViewError = finder.find(R.id.formElementError) as AppCompatTextView
        val itemView = finder.getRootView() as View
        baseSetup(model, textViewTitle, textViewError, itemView)

        val editTextValue = finder.find(R.id.text) as AppCompatEditText

        editTextValue.setText(model.valueAsString)
        editTextValue.hint = model.mHint ?: ""

        setEditTextFocusEnabled(editTextValue, itemView)

        editTextValue.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                textViewTitle.setTextColor(ContextCompat.getColor(context,
                        R.color.colorFormMasterElementFocusedTitle))
            } else {
                textViewTitle.setTextColor(ContextCompat.getColor(context,
                        R.color.colorFormMasterElementTextTitle))
            }
        }

        editTextValue.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i2: Int, i3: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i2: Int, i3: Int) {

                // get current form element, existing value and new value
                val formElement = formBuilder.getFormElement(model.getTag())
                val currentValue = formElement!!.valueAsString
                val newValue = charSequence.toString()

                // trigger event only if the value is changed
                if (currentValue != newValue) {
                    formElement.setValue(newValue)
                    formElement.setError(null)
                    setError(textViewError, null)

                    formBuilder.onValueChanged(formElement)
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })
    }

    private fun setEditTextFocusEnabled(editTextValue: AppCompatEditText, itemView: View) {
        itemView.setOnClickListener {
            editTextValue.requestFocus()
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            editTextValue.setSelection(editTextValue.text.length)
            imm.showSoftInput(editTextValue, InputMethodManager.SHOW_IMPLICIT)
        }
    }
}
