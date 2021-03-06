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

package com.blast.vinix.features.home.room.detail.timeline.helper

import com.blast.vinix.core.resources.UserPreferencesProvider
import org.matrix.android.sdk.api.session.events.model.EventType
import org.matrix.android.sdk.api.session.room.timeline.TimelineEventFilters
import org.matrix.android.sdk.api.session.room.timeline.TimelineSettings
import javax.inject.Inject

class TimelineSettingsFactory @Inject constructor(private val userPreferencesProvider: UserPreferencesProvider) {

    fun create(): TimelineSettings {
        return if (userPreferencesProvider.shouldShowHiddenEvents()) {
            TimelineSettings(
                    initialSize = 30,
                    filters = TimelineEventFilters(
                            filterEdits = false,
                            filterRedacted = userPreferencesProvider.shouldShowRedactedMessages().not(),
                            filterUseless = false,
                            filterTypes = false),
                    buildReadReceipts = userPreferencesProvider.shouldShowReadReceipts())
        } else {
            val allowedTypes = TimelineDisplayableEvents.DISPLAYABLE_TYPES.filterDisplayableTypes()
            TimelineSettings(
                    initialSize = 30,
                    filters = TimelineEventFilters(
                            filterEdits = true,
                            filterRedacted = userPreferencesProvider.shouldShowRedactedMessages().not(),
                            filterUseless = true,
                            filterTypes = true,
                            allowedTypes = allowedTypes),
                    buildReadReceipts = userPreferencesProvider.shouldShowReadReceipts())
        }
    }

    private fun List<String>.filterDisplayableTypes(): List<String> {
        return filter { type ->
            when (type) {
                EventType.STATE_ROOM_MEMBER -> userPreferencesProvider.shouldShowRoomMemberStateEvents()
                else                        -> true
            }
        }
    }
}
