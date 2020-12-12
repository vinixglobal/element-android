/*
 * Copyright 2019 New Vector Ltd
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

package com.blast.vinix.features.autocomplete.emoji

import android.graphics.Typeface
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.blast.vinix.R
import com.blast.vinix.core.epoxy.VectorEpoxyHolder
import com.blast.vinix.core.epoxy.VectorEpoxyModel
import com.blast.vinix.core.extensions.setTextOrHide
import com.blast.vinix.features.reactions.ReactionClickListener
import com.blast.vinix.features.reactions.data.EmojiItem

@EpoxyModelClass(layout = R.layout.item_autocomplete_emoji)
abstract class AutocompleteEmojiItem : VectorEpoxyModel<AutocompleteEmojiItem.Holder>() {

    @EpoxyAttribute
    lateinit var emojiItem: EmojiItem

    @EpoxyAttribute
    var emojiTypeFace: Typeface? = null

    @EpoxyAttribute
    var onClickListener: ReactionClickListener? = null

    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.emojiText.text = emojiItem.emoji
        holder.emojiText.typeface = emojiTypeFace ?: Typeface.DEFAULT
        holder.emojiNameText.text = emojiItem.name
        holder.emojiKeywordText.setTextOrHide(emojiItem.keywords.joinToString())

        holder.view.setOnClickListener {
            onClickListener?.onReactionSelected(emojiItem.emoji)
        }
    }

    class Holder : VectorEpoxyHolder() {
        val emojiText by bind<TextView>(R.id.itemAutocompleteEmoji)
        val emojiNameText by bind<TextView>(R.id.itemAutocompleteEmojiName)
        val emojiKeywordText by bind<TextView>(R.id.itemAutocompleteEmojiSubname)
    }
}
