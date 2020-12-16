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

package com.blast.vinix.features.media

import com.blast.vinix.core.date.VectorDateFormatter
import com.blast.vinix.core.resources.StringProvider
import org.matrix.android.sdk.api.session.Session
import org.matrix.android.sdk.api.session.room.Room
import org.matrix.android.sdk.api.session.room.timeline.TimelineEvent
import javax.inject.Inject

class AttachmentProviderFactory @Inject constructor(
        private val imageContentRenderer: ImageContentRenderer,
        private val vectorDateFormatter: VectorDateFormatter,
        private val stringProvider: StringProvider,
        private val session: Session
) {

    fun createProvider(attachments: List<TimelineEvent>): RoomEventsAttachmentProvider {
        return RoomEventsAttachmentProvider(
                attachments,
                imageContentRenderer,
                vectorDateFormatter,
                session.fileService(),
                stringProvider
        )
    }

    fun createProvider(attachments: List<AttachmentData>, room: Room?): DataAttachmentRoomProvider {
        return DataAttachmentRoomProvider(
                attachments,
                room,
                imageContentRenderer,
                vectorDateFormatter,
                session.fileService(),
                stringProvider
        )
    }
}