package com.thejuki.kformmaster.view

import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.thejuki.kformmaster.R
import com.thejuki.kformmaster.helper.FormBuildHelper
import com.thejuki.kformmaster.model.BaseFormElement
import com.thejuki.kformmaster.widget.ClearableEditText


/**
 * Base Form ViewBinder
 *
 * Base setup for title, error, and visibility
 *
 * @author **TheJuki** ([GitHub](https://github.com/TheJuki))
 * @version 1.0
 */
abstract class BaseFormViewBinder {

    /**
     * Initializes the base form fields
     */
    fun baseSetup(formElement: BaseFormElement<*>, dividerView: View?, textViewTitle: AppCompatTextView?,
                  textViewError: AppCompatTextView?,
                  itemView: View, mainViewLayout: View?) {

        formElement.itemView = itemView
        formElement.dividerView = dividerView
        formElement.titleView = textViewTitle
        formElement.errorView = textViewError
        formElement.mainLayoutView = mainViewLayout
    }

    /**
     * Sets the [ClearableEditText.Listener] to clear the form element
     */
    fun setClearableListener(formElement: BaseFormElement<*>) {
        formElement.editView?.let {
            if (it is ClearableEditText) {
                it.displayClear = formElement.clearable
                it.setListener(object : ClearableEditText.Listener {
                    override fun didClearText() {
                        formElement.clear()
                    }
                })
            }
        }
    }

    /**
     * Shows the [dialog] when the form element is clicked
     */
    fun setOnClickListener(context: Context, formElement: BaseFormElement<*>, itemView: View, dialog: Dialog) {
        formElement.editView?.isFocusable = false

        // display the dialog on click
        val listener = View.OnClickListener {
            if (!formElement.confirmEdit || formElement.valueAsString.isEmpty()) {
                dialog.show()
            } else if (formElement.confirmEdit && formElement.value != null) {
                AlertDialog.Builder(context)
                        .setTitle(formElement.confirmTitle
                                ?: context.getString(R.string.form_master_confirm_title))
                        .setMessage(formElement.confirmMessage
                                ?: context.getString(R.string.form_master_confirm_message))
                        // Set the action buttons
                        .setPositiveButton(android.R.string.ok) { _, _ ->
                            dialog.show()
                        }
                        .setNegativeButton(android.R.string.cancel) { _, _ -> }
                        .show()
            }
        }

        itemView.setOnClickListener(listener)
        formElement.editView?.setOnClickListener(listener)
    }

    /**
     * Sets the focus changed listener on the editView to update the form element value
     */
    fun setOnFocusChangeListener(context: Context, formElement: BaseFormElement<*>, formBuilder: FormBuildHelper) {
        val states = arrayOf(intArrayOf(android.R.attr.state_focused), intArrayOf())
        val colors = intArrayOf(formElement.titleFocusedTextColor ?: ContextCompat.getColor(context,
                R.color.colorFormMasterElementFocusedTitle),
                formElement.titleTextColor
                        ?: formElement.titleView?.textColors?.getColorForState(intArrayOf(),
                                ContextCompat.getColor(context, R.color.colorFormMasterElementTextTitle))
                        ?: -1
        )
        formElement.titleView?.setTextColor(ColorStateList(states, colors))

        formElement.editView?.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                if (formElement.clearOnFocus) {
                    formElement.value = null
                }

                formElement.titleView?.setTextColor(formElement.titleFocusedTextColor
                        ?: ContextCompat.getColor(context, R.color.colorFormMasterElementFocusedTitle))
            } else {
                formElement.titleView?.setTextColor(formElement.titleTextColor
                        ?: ContextCompat.getColor(context, R.color.colorFormMasterElementTextTitle))

                (formElement.editView as? AppCompatEditText)?.let {
                    if (it.text.toString() != formElement.valueAsString) {
                        formElement.error = null
                        formElement.setValue(it.text.toString())
                        formBuilder.onValueChanged(formElement)
                    }
                }
            }
        }
    }

    /**
     * Adds a text changed listener to the editView to update the form element value
     */
    fun addTextChangedListener(formElement: BaseFormElement<*>, formBuilder: FormBuildHelper) {
        if (!formElement.updateOnFocusChange) {
            (formElement.editView as? AppCompatEditText)?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(charSequence: CharSequence, i: Int, i2: Int, i3: Int) {}

                override fun onTextChanged(charSequence: CharSequence, i: Int, i2: Int, i3: Int) {

                    // get current form element, existing value and new value
                    val currentValue = formElement.valueAsString
                    val newValue = charSequence.toString()

                    // trigger event only if the value is changed
                    if (currentValue != newValue) {
                        formElement.error = null
                        formElement.setValue(newValue)
                        formBuilder.onValueChanged(formElement)
                    }
                }

                override fun afterTextChanged(editable: Editable) {}
            })
        }
    }

    /**
     * Sets the Done action listener to update the form element value
     */
    fun setOnEditorActionListener(formElement: BaseFormElement<*>, formBuilder: FormBuildHelper) {
        (formElement.editView as? AppCompatEditText)?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                formElement.error = null
                formElement.setValue((formElement.editView as? AppCompatEditText)?.text.toString())
                formBuilder.onValueChanged(formElement)
            }
            false
        }
    }
}