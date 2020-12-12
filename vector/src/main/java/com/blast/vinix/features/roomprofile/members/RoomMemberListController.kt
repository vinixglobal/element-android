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

package com.blast.vinix.features.roomprofile.members

import com.airbnb.epoxy.TypedEpoxyController
import com.blast.vinix.R
import com.blast.vinix.core.epoxy.dividerItem
import com.blast.vinix.core.epoxy.profiles.buildProfileSection
import com.blast.vinix.core.epoxy.profiles.profileMatrixItem
import com.blast.vinix.core.extensions.join
import com.blast.vinix.core.resources.ColorProvider
import com.blast.vinix.core.resources.StringProvider
import com.blast.vinix.features.home.AvatarRenderer
import org.matrix.android.sdk.api.session.events.model.Event
import org.matrix.android.sdk.api.session.events.model.toModel
import org.matrix.android.sdk.api.session.room.model.RoomMemberSummary
import org.matrix.android.sdk.api.session.room.model.RoomThirdPartyInviteContent
import org.matrix.android.sdk.api.util.MatrixItem
import org.matrix.android.sdk.api.util.toMatrixItem
import javax.inject.Inject

class RoomMemberListController @Inject constructor(
        private val avatarRenderer: AvatarRenderer,
        private val stringProvider: StringProvider,
        private val roomMemberSummaryFilter: RoomMemberSummaryFilter,
        colorProvider: ColorProvider
) : TypedEpoxyController<RoomMemberListViewState>() {

    interface Callback {
        fun onRoomMemberClicked(roomMember: RoomMemberSummary)
        fun onThreePidInviteClicked(event: Event)
    }

    private val dividerColor = colorProvider.getColorFromAttribute(R.attr.vctr_list_divider_color)

    var callback: Callback? = null

    init {
        setData(null)
    }

    override fun buildModels(data: RoomMemberListViewState?) {
        data ?: return

        roomMemberSummaryFilter.filter = data.filter

        val roomMembersByPowerLevel = data.roomMemberSummaries.invoke() ?: return
        val threePidInvites = data.threePidInvites()
                ?.filter { event ->
                    event.content.toModel<RoomThirdPartyInviteContent>()
                            ?.takeIf {
                                data.filter.isEmpty() || it.displayName.contains(data.filter, ignoreCase = true)
                            } != null
                }
                .orEmpty()
        var threePidInvitesDone = threePidInvites.isEmpty()

        for ((powerLevelCategory, roomMemberList) in roomMembersByPowerLevel) {
            val filteredRoomMemberList = roomMemberList.filter { roomMemberSummaryFilter.test(it) }
            if (filteredRoomMemberList.isEmpty()) {
                continue
            }

            if (powerLevelCategory == RoomMemberListCategories.USER && !threePidInvitesDone) {
                // If there is no regular invite, display threepid invite before the regular user
                buildProfileSection(
                        stringProvider.getString(RoomMemberListCategories.INVITE.titleRes)
                )

                buildThreePidInvites(data)
                threePidInvitesDone = true
            }

            buildProfileSection(
                    stringProvider.getString(powerLevelCategory.titleRes)
            )
            filteredRoomMemberList.join(
                    each = { _, roomMember ->
                        profileMatrixItem {
                            id(roomMember.userId)
                            matrixItem(roomMember.toMatrixItem())
                            avatarRenderer(avatarRenderer)
                            userEncryptionTrustLevel(data.trustLevelMap.invoke()?.get(roomMember.userId))
                            clickListener { _ ->
                                callback?.onRoomMemberClicked(roomMember)
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
            if (powerLevelCategory == RoomMemberListCategories.INVITE && !threePidInvitesDone) {
                // Display the threepid invite after the regular invite
                dividerItem {
                    id("divider_threepidinvites")
                    color(dividerColor)
                }

                buildThreePidInvites(data)
                threePidInvitesDone = true
            }
        }

        if (!threePidInvitesDone) {
            // If there is not regular invite and no regular user, finally display threepid invite here
            buildProfileSection(
                    stringProvider.getString(RoomMemberListCategories.INVITE.titleRes)
            )

            buildThreePidInvites(data)
        }
    }

    private fun buildThreePidInvites(data: RoomMemberListViewState) {
        data.threePidInvites()
                ?.filter { it.content.toModel<RoomThirdPartyInviteContent>() != null }
                ?.join(
                        each = { idx, event ->
                            event.content.toModel<RoomThirdPartyInviteContent>()
                                    ?.let { content ->
                                        profileMatrixItem {
                                            id("3pid_$idx")
                                            matrixItem(content.toMatrixItem())
                                            avatarRenderer(avatarRenderer)
                                            editable(data.actionsPermissions.canRevokeThreePidInvite)
                                            clickListener { _ ->
                                                callback?.onThreePidInviteClicked(event)
                                            }
                                        }
                                    }
                        },
                        between = { idx, _ ->
                            dividerItem {
                                id("divider3_$idx")
                                color(dividerColor)
                            }
                        }
                )
    }

    private fun RoomThirdPartyInviteContent.toMatrixItem(): MatrixItem {
        return MatrixItem.UserItem("@", displayName = displayName)
    }
}
