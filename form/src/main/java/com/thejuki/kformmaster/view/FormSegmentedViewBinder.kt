package com.thejuki.kformmaster.view

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.AppCompatTextView
import com.github.vivchar.rendererrecyclerviewadapter.ViewHolder
import com.github.vivchar.rendererrecyclerviewadapter.ViewState
import com.github.vivchar.rendererrecyclerviewadapter.ViewStateProvider
import com.github.vivchar.rendererrecyclerviewadapter.binder.ViewBinder
import com.thejuki.kformmaster.R
import com.thejuki.kformmaster.helper.FormBuildHelper
import com.thejuki.kformmaster.model.FormSegmentedElement
import com.thejuki.kformmaster.state.FormSegmentedViewState

/**
 * Form Segmented ViewBinder
 *
 * View Binder for [FormSegmentedElement]
 *
 * @author **TheJuki** ([GitHub](https://github.com/TheJuki))
 * @version 1.0
 */
class FormSegmentedViewBinder(private val context: Context, private val formBuilder: FormBuildHelper, @LayoutRes private val layoutID: Int?) : BaseFormViewBinder() {
    val viewBinder = ViewBinder(layoutID
            ?: R.layout.form_element_segmented, FormSegmentedElement::class.java, { model, finder, _ ->
        val textViewTitle = finder.find(R.id.formElementTitle) as? AppCompatTextView
        val mainViewLayout = finder.find(R.id.formElementMainLayout) as? LinearLayout
        val textViewError = finder.find(R.id.formElementError) as? AppCompatTextView
        val dividerView = finder.find(R.id.formElementDivider) as? View
        val itemView = finder.getRootView() as View
        val segmented = finder.find(R.id.formElementValue) as com.thejuki.kformmaster.widget.SegmentedGroup
        baseSetup(model, dividerView, textViewTitle, textViewError, itemView, mainViewLayout, segmented)

        segmented.setProperties(model.marginDp, model.cornerRadius, model.tintColor,
                model.checkedTextColor, model.unCheckedTintColor, model.padding, model.textSize)

        if (model.value == null || model.options?.contains(model.value) == false) {
            segmented.holdup = false
        }

        segmented.setOnCheckedChangeListener { group, checkedId ->
            if (!segmented.holdup) {
                segmented.holdup = true
                val index = group.indexOfChild(group.findViewById(checkedId))
                model.error = null
                if (index < 0) {
                    model.setValue(null)
                } else {
                    model.setValue(model.options?.get(index))
                }
                formBuilder.onValueChanged(model)
            } else {
                segmented.holdup = false
            }
        }

        model.reInitGroup()

        itemView.setOnClickListener {
            // Invoke onClick Unit
            model.onClick?.invoke()
        }

    }, object : ViewStateProvider<FormSegmentedElement<*>, ViewHolder> {
        override fun createViewStateID(model: FormSegmentedElement<*>): Int {
            return model.id
        }

        override fun createViewState(holder: ViewHolder): ViewState<ViewHolder> {
            return FormSegmentedViewState(holder)
        }
    })
}
