/*
 * Copyright (c) 2020 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.blast.vinix.core.ui.list

import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.google.android.material.button.MaterialButton
import com.blast.vinix.R
import com.blast.vinix.core.epoxy.VectorEpoxyHolder
import com.blast.vinix.core.epoxy.VectorEpoxyModel
import com.blast.vinix.features.themes.ThemeUtils

/**
 * A generic button list item.
 */
@EpoxyModelClass(layout = R.layout.item_generic_button)
abstract class GenericButtonItem : VectorEpoxyModel<GenericButtonItem.Holder>() {

    @EpoxyAttribute
    var text: String? = null

    @EpoxyAttribute
    var buttonClickAction: View.OnClickListener? = null

    @EpoxyAttribute
    @ColorInt
    var textColor: Int? = null

    @EpoxyAttribute
    @DrawableRes
    var iconRes: Int? = null

    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.button.text = text
        val textColor = textColor ?: ThemeUtils.getColor(holder.view.context, R.attr.riotx_text_primary)
        holder.button.setTextColor(textColor)
        if (iconRes != null) {
            holder.button.setIconResource(iconRes!!)
        } else {
            holder.button.icon = null
        }

        buttonClickAction?.let { holder.button.setOnClickListener(it) }
    }

    class Holder : VectorEpoxyHolder() {
        val button by bind<MaterialButton>(R.id.itemGenericItemButton)
    }
}
