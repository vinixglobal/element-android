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

package com.blast.vinix.features.roomprofile.settings.historyvisibility

import com.blast.vinix.R
import com.blast.vinix.core.resources.StringProvider
import com.blast.vinix.core.ui.bottomsheet.BottomSheetGenericController
import com.blast.vinix.features.home.room.detail.timeline.format.RoomHistoryVisibilityFormatter
import org.matrix.android.sdk.api.session.room.model.RoomHistoryVisibility
import javax.inject.Inject

class RoomHistoryVisibilityController @Inject constructor(
        private val historyVisibilityFormatter: RoomHistoryVisibilityFormatter,
        private val stringProvider: StringProvider
) : BottomSheetGenericController<RoomHistoryVisibilityState, RoomHistoryVisibilityAction>() {

    override fun getTitle() = stringProvider.getString(R.string.room_settings_room_read_history_rules_pref_dialog_title)

    override fun getSubTitle() = stringProvider.getString(R.string.room_settings_room_read_history_dialog_subtitle)

    override fun getActions(state: RoomHistoryVisibilityState): List<RoomHistoryVisibilityAction> {
        return listOf(
                RoomHistoryVisibility.WORLD_READABLE,
                RoomHistoryVisibility.SHARED,
                RoomHistoryVisibility.INVITED,
                RoomHistoryVisibility.JOINED
        )
                .map { roomHistoryVisibility ->
                    RoomHistoryVisibilityAction(
                            roomHistoryVisibility = roomHistoryVisibility,
                            title = historyVisibilityFormatter.getSetting(roomHistoryVisibility),
                            iconResId = 0,
                            isSelected = roomHistoryVisibility == state.currentRoomHistoryVisibility
                    )
                }
    }
}
