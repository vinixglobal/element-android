/*
 * Copyright 2019 New Vector Ltd
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

import android.view.Gravity
import android.widget.TextView
import androidx.annotation.ColorInt
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.blast.vinix.R
import com.blast.vinix.core.epoxy.VectorEpoxyHolder
import com.blast.vinix.core.epoxy.VectorEpoxyModel
import com.blast.vinix.core.extensions.setTextOrHide
import com.blast.vinix.features.themes.ThemeUtils

/**
 * A generic list item.
 * Displays an item with a title, and optional description.
 * Can display an accessory on the right, that can be an image or an indeterminate progress.
 * If provided with an action, will display a button at the bottom of the list item.
 */
@EpoxyModelClass(layout = R.layout.item_generic_footer)
abstract class GenericFooterItem : VectorEpoxyModel<GenericFooterItem.Holder>() {

    @EpoxyAttribute
    var text: String? = null

    @EpoxyAttribute
    var style: GenericItem.STYLE = GenericItem.STYLE.NORMAL_TEXT

    @EpoxyAttribute
    var itemClickAction: GenericItem.Action? = null

    @EpoxyAttribute
    var centered: Boolean = true

    @EpoxyAttribute
    @ColorInt
    var textColor: Int? = null

    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.text.setTextOrHide(text)
        when (style) {
            GenericItem.STYLE.BIG_TEXT    -> holder.text.textSize = 18f
            GenericItem.STYLE.NORMAL_TEXT -> holder.text.textSize = 14f
        }
        holder.text.gravity = if (centered) Gravity.CENTER_HORIZONTAL else Gravity.START

        if (textColor != null) {
            holder.text.setTextColor(textColor!!)
        } else {
            holder.text.setTextColor(ThemeUtils.getColor(holder.view.context, R.attr.riotx_text_secondary))
        }

        holder.view.setOnClickListener {
            itemClickAction?.perform?.run()
        }
    }

    class Holder : VectorEpoxyHolder() {
        val text by bind<TextView>(R.id.itemGenericFooterText)
    }
}
