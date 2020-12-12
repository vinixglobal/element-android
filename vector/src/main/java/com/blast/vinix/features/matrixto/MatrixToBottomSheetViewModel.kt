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

package com.blast.vinix.features.matrixto

import androidx.lifecycle.viewModelScope
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.FragmentViewModelContext
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.Uninitialized
import com.airbnb.mvrx.ViewModelContext
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import com.blast.vinix.R
import com.blast.vinix.core.extensions.exhaustive
import com.blast.vinix.core.platform.VectorViewModel
import com.blast.vinix.core.resources.StringProvider
import com.blast.vinix.features.raw.wellknown.getElementWellknown
import com.blast.vinix.features.raw.wellknown.isE2EByDefault
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.matrix.android.sdk.api.extensions.tryOrNull
import org.matrix.android.sdk.api.raw.RawService
import org.matrix.android.sdk.api.session.Session
import org.matrix.android.sdk.api.session.permalinks.PermalinkData
import org.matrix.android.sdk.api.session.permalinks.PermalinkParser
import org.matrix.android.sdk.api.session.room.model.create.CreateRoomParams
import org.matrix.android.sdk.api.session.user.model.User
import org.matrix.android.sdk.api.util.toMatrixItem
import org.matrix.android.sdk.internal.util.awaitCallback

class MatrixToBottomSheetViewModel @AssistedInject constructor(
        @Assisted initialState: MatrixToBottomSheetState,
        private val session: Session,
        private val stringProvider: StringProvider,
        private val rawService: RawService) : VectorViewModel<MatrixToBottomSheetState, MatrixToAction, MatrixToViewEvents>(initialState) {

    @AssistedInject.Factory
    interface Factory {
        fun create(initialState: MatrixToBottomSheetState): MatrixToBottomSheetViewModel
    }

    init {
        setState {
            copy(matrixItem = Loading())
        }
        viewModelScope.launch(Dispatchers.IO) {
            resolveLink(initialState)
        }
    }

    private suspend fun resolveLink(initialState: MatrixToBottomSheetState) {
        val permalinkData = PermalinkParser.parse(initialState.deepLink)
        if (permalinkData is PermalinkData.FallbackLink) {
            setState {
                copy(
                        matrixItem = Fail(IllegalArgumentException(stringProvider.getString(R.string.permalink_malformed))),
                        startChattingState = Uninitialized
                )
            }
            return
        }

        when (permalinkData) {
            is PermalinkData.UserLink -> {
                val user = resolveUser(permalinkData.userId)
                setState {
                    copy(
                            matrixItem = Success(user.toMatrixItem()),
                            startChattingState = Success(Unit)
                    )
                }
            }
            is PermalinkData.RoomLink -> {
                // not yet supported
                _viewEvents.post(MatrixToViewEvents.Dismiss)
            }
            is PermalinkData.GroupLink -> {
                // not yet supported
                _viewEvents.post(MatrixToViewEvents.Dismiss)
            }
            is PermalinkData.FallbackLink -> {
                _viewEvents.post(MatrixToViewEvents.Dismiss)
            }
        }
    }

    private suspend fun resolveUser(userId: String): User {
        return tryOrNull {
            awaitCallback<User> {
                session.resolveUser(userId, it)
            }
        }
        // Create raw user in case the user is not searchable
                ?: User(userId, null, null)
    }

    companion object : MvRxViewModelFactory<MatrixToBottomSheetViewModel, MatrixToBottomSheetState> {
        override fun create(viewModelContext: ViewModelContext, state: MatrixToBottomSheetState): MatrixToBottomSheetViewModel? {
            val fragment: MatrixToBottomSheet = (viewModelContext as FragmentViewModelContext).fragment()

            return fragment.matrixToBottomSheetViewModelFactory.create(state)
        }
    }

    override fun handle(action: MatrixToAction) {
        when (action) {
            is MatrixToAction.StartChattingWithUser -> handleStartChatting(action)
        }.exhaustive
    }

    private fun handleStartChatting(action: MatrixToAction.StartChattingWithUser) {
        val mxId = action.matrixItem.id
        val existing = session.getExistingDirectRoomWithUser(mxId)
        if (existing != null) {
            // navigate to this room
            _viewEvents.post(MatrixToViewEvents.NavigateToRoom(existing))
        } else {
            setState {
                copy(startChattingState = Loading())
            }
            // we should create the room then navigate
            viewModelScope.launch(Dispatchers.IO) {
                val adminE2EByDefault = rawService.getElementWellknown(session.myUserId)
                        ?.isE2EByDefault()
                        ?: true

                val roomParams = CreateRoomParams()
                        .apply {
                            invitedUserIds.add(mxId)
                            setDirectMessage()
                            enableEncryptionIfInvitedUsersSupportIt = adminE2EByDefault
                        }

                val roomId = try {
                    awaitCallback<String> { session.createRoom(roomParams, it) }
                } catch (failure: Throwable) {
                    setState {
                        copy(startChattingState = Fail(Exception(stringProvider.getString(R.string.invite_users_to_room_failure))))
                    }
                    return@launch
                }
                setState {
                    // we can hide this button has we will navigate out
                    copy(startChattingState = Uninitialized)
                }
                _viewEvents.post(MatrixToViewEvents.NavigateToRoom(roomId))
            }
        }
    }
}
