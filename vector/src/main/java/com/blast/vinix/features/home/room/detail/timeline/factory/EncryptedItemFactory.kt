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

package com.blast.vinix.features.home.room.detail.timeline.factory

import com.blast.vinix.R
import com.blast.vinix.core.epoxy.VectorEpoxyModel
import com.blast.vinix.core.resources.ColorProvider
import com.blast.vinix.core.resources.DrawableProvider
import com.blast.vinix.core.resources.StringProvider
import com.blast.vinix.features.home.room.detail.timeline.TimelineEventController
import com.blast.vinix.features.home.room.detail.timeline.helper.AvatarSizeProvider
import com.blast.vinix.features.home.room.detail.timeline.helper.MessageInformationDataFactory
import com.blast.vinix.features.home.room.detail.timeline.helper.MessageItemAttributesFactory
import com.blast.vinix.features.home.room.detail.timeline.item.MessageTextItem_
import com.blast.vinix.features.home.room.detail.timeline.tools.createLinkMovementMethod
import com.blast.vinix.features.settings.VectorPreferences
import me.gujun.android.span.image
import me.gujun.android.span.span
import org.matrix.android.sdk.api.session.crypto.MXCryptoError
import org.matrix.android.sdk.api.session.events.model.EventType
import org.matrix.android.sdk.api.session.events.model.toModel
import org.matrix.android.sdk.api.session.room.timeline.TimelineEvent
import org.matrix.android.sdk.internal.crypto.model.event.EncryptedEventContent
import javax.inject.Inject

// This class handles timeline events who haven't been successfully decrypted
class EncryptedItemFactory @Inject constructor(private val messageInformationDataFactory: MessageInformationDataFactory,
                                               private val colorProvider: ColorProvider,
                                               private val stringProvider: StringProvider,
                                               private val avatarSizeProvider: AvatarSizeProvider,
                                               private val drawableProvider: DrawableProvider,
                                               private val attributesFactory: MessageItemAttributesFactory,
                                               private val vectorPreferences: VectorPreferences) {

    fun create(event: TimelineEvent,
               nextEvent: TimelineEvent?,
               highlight: Boolean,
               callback: TimelineEventController.Callback?): VectorEpoxyModel<*>? {
        event.root.eventId ?: return null

        return when {
            EventType.ENCRYPTED == event.root.getClearType() -> {
                val cryptoError = event.root.mCryptoError

                val spannableStr = if (vectorPreferences.developerMode()) {
                    val errorDescription =
                            if (cryptoError == MXCryptoError.ErrorType.UNKNOWN_INBOUND_SESSION_ID) {
                                stringProvider.getString(R.string.notice_crypto_error_unkwown_inbound_session_id)
                            } else {
                                // TODO i18n
                                cryptoError?.name
                            }

                    val message = stringProvider.getString(R.string.encrypted_message).takeIf { cryptoError == null }
                            ?: stringProvider.getString(R.string.notice_crypto_unable_to_decrypt, errorDescription)
                    span(message) {
                        textStyle = "italic"
                        textColor = colorProvider.getColorFromAttribute(R.attr.riotx_text_secondary)
                    }
                } else {
                    val colorFromAttribute = colorProvider.getColorFromAttribute(R.attr.riotx_text_secondary)
                    if (cryptoError == null) {
                        span(stringProvider.getString(R.string.encrypted_message)) {
                            textStyle = "italic"
                            textColor = colorFromAttribute
                        }
                    } else {
                        when (cryptoError) {
                            MXCryptoError.ErrorType.KEYS_WITHHELD -> {
                                span {
                                    apply {
                                        drawableProvider.getDrawable(R.drawable.ic_forbidden, colorFromAttribute)?.let {
                                            image(it, "baseline")
                                        }
                                    }
                                    span(stringProvider.getString(R.string.notice_crypto_unable_to_decrypt_final)) {
                                        textStyle = "italic"
                                        textColor = colorFromAttribute
                                    }
                                }
                            }
                            else                                  -> {
                                span {
                                    apply {
                                        drawableProvider.getDrawable(R.drawable.ic_clock, colorFromAttribute)?.let {
                                            image(it, "baseline")
                                        }
                                    }
                                    span(stringProvider.getString(R.string.notice_crypto_unable_to_decrypt_friendly)) {
                                        textStyle = "italic"
                                        textColor = colorFromAttribute
                                    }
                                }
                            }
                        }
                    }
                }

                val informationData = messageInformationDataFactory.create(event, nextEvent)
                val attributes = attributesFactory.create(event.root.content.toModel<EncryptedEventContent>(), informationData, callback)
                return MessageTextItem_()
                        .leftGuideline(avatarSizeProvider.leftGuideline)
                        .highlighted(highlight)
                        .attributes(attributes)
                        .message(spannableStr)
                        .movementMethod(createLinkMovementMethod(callback))
            }
            else                                             -> null
        }
    }
}