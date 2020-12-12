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

package com.blast.vinix.features.roomprofile.banned

import com.airbnb.epoxy.TypedEpoxyController
import com.blast.vinix.R
import com.blast.vinix.core.epoxy.dividerItem
import com.blast.vinix.core.epoxy.profiles.buildProfileSection
import com.blast.vinix.core.epoxy.profiles.profileMatrixItemWithProgress
import com.blast.vinix.core.extensions.join
import com.blast.vinix.core.resources.ColorProvider
import com.blast.vinix.core.resources.StringProvider
import com.blast.vinix.core.ui.list.genericFooterItem
import com.blast.vinix.features.home.AvatarRenderer
import com.blast.vinix.features.roomprofile.members.RoomMemberSummaryFilter
import org.matrix.android.sdk.api.session.room.model.RoomMemberSummary
import org.matrix.android.sdk.api.util.toMatrixItem
import javax.inject.Inject

class RoomBannedMemberListController @Inject constructor(
        private val avatarRenderer: AvatarRenderer,
        private val stringProvider: StringProvider,
        private val roomMemberSummaryFilter: RoomMemberSummaryFilter,
        colorProvider: ColorProvider
) : TypedEpoxyController<RoomBannedMemberListViewState>() {

    interface Callback {
        fun onUnbanClicked(roomMember: RoomMemberSummary)
    }

    private val dividerColor = colorProvider.getColorFromAttribute(R.attr.vctr_list_divider_color)

    var callback: Callback? = null

    init {
        setData(null)
    }

    override fun buildModels(data: RoomBannedMemberListViewState?) {
        val bannedList = data?.bannedMemberSummaries?.invoke() ?: return

        val quantityString = stringProvider.getQuantityString(R.plurals.room_settings_banned_users_count, bannedList.size, bannedList.size)

        if (bannedList.isEmpty()) {
            buildProfileSection(stringProvider.getString(R.string.room_settings_banned_users_title))

            genericFooterItem {
                id("footer")
                text(quantityString)
            }
        } else {
            buildProfileSection(quantityString)

            roomMemberSummaryFilter.filter = data.filter
            bannedList
                    .filter { roomMemberSummaryFilter.test(it) }
                    .join(
                            each = { _, roomMember ->
                                val actionInProgress = data.onGoingModerationAction.contains(roomMember.userId)
                                profileMatrixItemWithProgress {
                                    id(roomMember.userId)
                                    matrixItem(roomMember.toMatrixItem())
                                    avatarRenderer(avatarRenderer)
                                    apply {
                                        if (actionInProgress) {
                                            inProgress(true)
                                            editable(false)
                                        } else {
                                            inProgress(false)
                                            editable(true)
                                            clickListener { _ ->
                                                callback?.onUnbanClicked(roomMember)
                                            }
                                        }
                                    }
                                }
                            },
                            between = { _, roomMemberBefore ->
                                dividerItem {
                                    id("divider_${roomMemberBefore.userId}")
                                    color(dividerColor)
                                }
                            }
                    )
        }
    }
}
