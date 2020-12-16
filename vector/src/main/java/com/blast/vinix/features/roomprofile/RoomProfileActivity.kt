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
 *
 */

package com.blast.vinix.features.roomprofile

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.airbnb.mvrx.MvRx
import com.airbnb.mvrx.viewModel
import com.blast.vinix.R
import com.blast.vinix.core.di.ScreenComponent
import com.blast.vinix.core.extensions.addFragment
import com.blast.vinix.core.extensions.addFragmentToBackstack
import com.blast.vinix.core.platform.ToolbarConfigurable
import com.blast.vinix.core.platform.VectorBaseActivity
import com.blast.vinix.features.home.room.detail.RoomDetailPendingActionStore
import com.blast.vinix.features.room.RequireActiveMembershipViewEvents
import com.blast.vinix.features.room.RequireActiveMembershipViewModel
import com.blast.vinix.features.room.RequireActiveMembershipViewState
import com.blast.vinix.features.roomprofile.banned.RoomBannedMemberListFragment
import com.blast.vinix.features.roomprofile.members.RoomMemberListFragment
import com.blast.vinix.features.roomprofile.settings.RoomSettingsFragment
import com.blast.vinix.features.roomprofile.alias.RoomAliasFragment
import com.blast.vinix.features.roomprofile.uploads.RoomUploadsFragment
import javax.inject.Inject

class RoomProfileActivity :
        VectorBaseActivity(),
        ToolbarConfigurable,
        RequireActiveMembershipViewModel.Factory {

    companion object {

        private const val EXTRA_DIRECT_ACCESS = "EXTRA_DIRECT_ACCESS"

        const val EXTRA_DIRECT_ACCESS_ROOM_ROOT = 0
        const val EXTRA_DIRECT_ACCESS_ROOM_SETTINGS = 1

        fun newIntent(context: Context, roomId: String, directAccess: Int?): Intent {
            val roomProfileArgs = RoomProfileArgs(roomId)
            return Intent(context, RoomProfileActivity::class.java).apply {
                putExtra(MvRx.KEY_ARG, roomProfileArgs)
                putExtra(EXTRA_DIRECT_ACCESS, directAccess)
            }
        }
    }

    private lateinit var sharedActionViewModel: RoomProfileSharedActionViewModel
    private lateinit var roomProfileArgs: RoomProfileArgs

    private val requireActiveMembershipViewModel: RequireActiveMembershipViewModel by viewModel()

    @Inject
    lateinit var requireActiveMembershipViewModelFactory: RequireActiveMembershipViewModel.Factory

    @Inject
    lateinit var roomDetailPendingActionStore: RoomDetailPendingActionStore

    override fun create(initialState: RequireActiveMembershipViewState): RequireActiveMembershipViewModel {
        return requireActiveMembershipViewModelFactory.create(initialState)
    }

    override fun injectWith(injector: ScreenComponent) {
        super.injectWith(injector)
        injector.inject(this)
    }

    override fun getLayoutRes() = R.layout.activity_simple

    override fun initUiAndData() {
        sharedActionViewModel = viewModelProvider.get(RoomProfileSharedActionViewModel::class.java)
        roomProfileArgs = intent?.extras?.getParcelable(MvRx.KEY_ARG) ?: return
        if (isFirstCreation()) {
            when (intent?.extras?.getInt(EXTRA_DIRECT_ACCESS, EXTRA_DIRECT_ACCESS_ROOM_ROOT)) {
                EXTRA_DIRECT_ACCESS_ROOM_SETTINGS -> {
                    addFragment(R.id.simpleFragmentContainer, RoomProfileFragment::class.java, roomProfileArgs)
                    addFragmentToBackstack(R.id.simpleFragmentContainer, RoomSettingsFragment::class.java, roomProfileArgs)
                }
                else -> addFragment(R.id.simpleFragmentContainer, RoomProfileFragment::class.java, roomProfileArgs)
            }
        }
        sharedActionViewModel
                .observe()
                .subscribe { sharedAction ->
                    when (sharedAction) {
                        is RoomProfileSharedAction.OpenRoomMembers         -> openRoomMembers()
                        is RoomProfileSharedAction.OpenRoomSettings        -> openRoomSettings()
                        is RoomProfileSharedAction.OpenRoomAliasesSettings -> openRoomAlias()
                        is RoomProfileSharedAction.OpenRoomUploads         -> openRoomUploads()
                        is RoomProfileSharedAction.OpenBannedRoomMembers   -> openBannedRoomMembers()
                    }
                }
                .disposeOnDestroy()

        requireActiveMembershipViewModel.observeViewEvents {
            when (it) {
                is RequireActiveMembershipViewEvents.RoomLeft -> handleRoomLeft(it)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (roomDetailPendingActionStore.data != null) {
            finish()
        }
    }

    private fun handleRoomLeft(roomLeft: RequireActiveMembershipViewEvents.RoomLeft) {
        if (roomLeft.leftMessage != null) {
            Toast.makeText(this, roomLeft.leftMessage, Toast.LENGTH_LONG).show()
        }
        finish()
    }

    private fun openRoomUploads() {
        addFragmentToBackstack(R.id.simpleFragmentContainer, RoomUploadsFragment::class.java, roomProfileArgs)
    }

    private fun openRoomSettings() {
        addFragmentToBackstack(R.id.simpleFragmentContainer, RoomSettingsFragment::class.java, roomProfileArgs)
    }

    private fun openRoomAlias() {
        addFragmentToBackstack(R.id.simpleFragmentContainer, RoomAliasFragment::class.java, roomProfileArgs)
    }

    private fun openRoomMembers() {
        addFragmentToBackstack(R.id.simpleFragmentContainer, RoomMemberListFragment::class.java, roomProfileArgs)
    }

    private fun openBannedRoomMembers() {
        addFragmentToBackstack(R.id.simpleFragmentContainer, RoomBannedMemberListFragment::class.java, roomProfileArgs)
    }

    override fun configure(toolbar: Toolbar) {
        configureToolbar(toolbar)
    }
}