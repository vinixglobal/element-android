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
package com.blast.vinix.features.discovery

import android.widget.TextView
import androidx.annotation.ColorRes
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.blast.vinix.R
import com.blast.vinix.core.epoxy.VectorEpoxyHolder
import com.blast.vinix.core.resources.ColorProvider

@EpoxyModelClass(layout = R.layout.item_settings_information)
abstract class SettingsInformationItem : EpoxyModelWithHolder<SettingsInformationItem.Holder>() {

    @EpoxyAttribute
    lateinit var colorProvider: ColorProvider

    @EpoxyAttribute
    lateinit var message: String

    @EpoxyAttribute
    @ColorRes
    var textColorId: Int = R.color.vector_info_color

    override fun bind(holder: Holder) {
        super.bind(holder)

        holder.textView.text = message
        holder.textView.setTextColor(colorProvider.getColor(textColorId))
    }

    class Holder : VectorEpoxyHolder() {
        val textView by bind<TextView>(R.id.settings_item_information)
    }
}
