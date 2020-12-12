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

package com.blast.vinix.features.home.room.detail.readreceipts

import com.airbnb.epoxy.TypedEpoxyController
import com.blast.vinix.core.date.DateFormatKind
import com.blast.vinix.core.date.VectorDateFormatter
import com.blast.vinix.features.home.AvatarRenderer
import com.blast.vinix.features.home.room.detail.timeline.item.ReadReceiptData
import com.blast.vinix.features.home.room.detail.timeline.item.toMatrixItem
import org.matrix.android.sdk.api.session.Session
import javax.inject.Inject

/**
 * Epoxy controller for read receipt event list
 */
class DisplayReadReceiptsController @Inject constructor(private val dateFormatter: VectorDateFormatter,
                                                        private val session: Session,
                                                        private val avatarRender: AvatarRenderer)
    : TypedEpoxyController<List<ReadReceiptData>>() {

    var listener: Listener? = null

    override fun buildModels(readReceipts: List<ReadReceiptData>) {
        readReceipts.forEach {
            val timestamp = dateFormatter.format(it.timestamp, DateFormatKind.DEFAULT_DATE_AND_TIME)
            DisplayReadReceiptItem_()
                    .id(it.userId)
                    .matrixItem(it.toMatrixItem())
                    .avatarRenderer(avatarRender)
                    .timestamp(timestamp)
                    .userClicked { listener?.didSelectUser(it.userId) }
                    .addIf(session.myUserId != it.userId, this)
        }
    }

    interface Listener {
        fun didSelectUser(userId: String)
    }
}
