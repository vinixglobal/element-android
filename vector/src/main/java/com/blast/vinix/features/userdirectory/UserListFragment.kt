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

package com.blast.vinix.features.userdirectory

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ScrollView
import androidx.core.view.forEach
import androidx.core.view.isVisible
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.args
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.google.android.material.chip.Chip
import com.jakewharton.rxbinding3.widget.textChanges
import com.blast.vinix.R
import com.blast.vinix.core.extensions.cleanup
import com.blast.vinix.core.extensions.configureWith
import com.blast.vinix.core.extensions.hideKeyboard
import com.blast.vinix.core.extensions.setupAsSearch
import com.blast.vinix.core.platform.VectorBaseFragment
import com.blast.vinix.core.utils.DimensionConverter
import com.blast.vinix.core.utils.startSharePlainTextIntent
import com.blast.vinix.features.homeserver.HomeServerCapabilitiesViewModel
import kotlinx.android.synthetic.main.fragment_user_list.*
import org.matrix.android.sdk.api.session.identity.ThreePid
import org.matrix.android.sdk.api.session.user.model.User
import javax.inject.Inject

class UserListFragment @Inject constructor(
        private val userListController: UserListController,
        private val dimensionConverter: DimensionConverter,
        val homeServerCapabilitiesViewModelFactory: HomeServerCapabilitiesViewModel.Factory
) : VectorBaseFragment(), UserListController.Callback {

    private val args: UserListFragmentArgs by args()
    private val viewModel: UserListViewModel by activityViewModel()
    private val homeServerCapabilitiesViewModel: HomeServerCapabilitiesViewModel by fragmentViewModel()
    private lateinit var sharedActionViewModel: UserListSharedActionViewModel

    override fun getLayoutResId() = R.layout.fragment_user_list

    override fun getMenuRes() = args.menuResId

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedActionViewModel = activityViewModelProvider.get(UserListSharedActionViewModel::class.java)
        userListTitle.text = args.title
        vectorBaseActivity.setSupportActionBar(userListToolbar)

        setupRecyclerView()
        setupSearchView()
        setupCloseView()

        homeServerCapabilitiesViewModel.subscribe {
            userListE2EbyDefaultDisabled.isVisible = !it.isE2EByDefault
        }

        viewModel.selectSubscribe(this, UserListViewState::pendingInvitees) {
            renderSelectedUsers(it)
        }

        viewModel.observeViewEvents {
            when (it) {
                is UserListViewEvents.OpenShareMatrixToLing -> {
                    val text = getString(R.string.invite_friends_text, it.link)
                    startSharePlainTextIntent(
                            fragment = this,
                            activityResultLauncher = null,
                            chooserTitle = getString(R.string.invite_friends),
                            text = text,
                            extraTitle = getString(R.string.invite_friends_rich_title)
                    )
                }
            }
        }
    }

    override fun onDestroyView() {
        userListRecyclerView.cleanup()
        super.onDestroyView()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        withState(viewModel) {
            val showMenuItem = it.pendingInvitees.isNotEmpty()
            menu.forEach { menuItem ->
                menuItem.isVisible = showMenuItem
            }
        }
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = withState(viewModel) {
        sharedActionViewModel.post(UserListSharedAction.OnMenuItemSelected(item.itemId, it.pendingInvitees))
        return@withState true
    }

    private fun setupRecyclerView() {
        userListController.callback = this
        // Don't activate animation as we might have way to much item animation when filtering
        userListRecyclerView.configureWith(userListController, disableItemAnimation = true)
    }

    private fun setupSearchView() {
        withState(viewModel) {
            userListSearch.hint = getString(R.string.user_directory_search_hint)
        }
        userListSearch
                .textChanges()
                .startWith(userListSearch.text)
                .subscribe { text ->
                    val searchValue = text.trim()
                    val action = if (searchValue.isBlank()) {
                        UserListAction.ClearSearchUsers
                    } else {
                        UserListAction.SearchUsers(searchValue.toString())
                    }
                    viewModel.handle(action)
                }
                .disposeOnDestroyView()

        userListSearch.setupAsSearch()
        userListSearch.requestFocus()
    }

    private fun setupCloseView() {
        userListClose.debouncedClicks {
            requireActivity().finish()
        }
    }

    override fun invalidate() = withState(viewModel) {
        userListController.setData(it)
    }

    private fun renderSelectedUsers(invitees: Set<PendingInvitee>) {
        invalidateOptionsMenu()

        val currentNumberOfChips = chipGroup.childCount
        val newNumberOfChips = invitees.size

        chipGroup.removeAllViews()
        invitees.forEach { addChipToGroup(it) }

        // Scroll to the bottom when adding chips. When removing chips, do not scroll
        if (newNumberOfChips >= currentNumberOfChips) {
            chipGroupScrollView.post {
                chipGroupScrollView.fullScroll(ScrollView.FOCUS_DOWN)
            }
        }
    }

    private fun addChipToGroup(pendingInvitee: PendingInvitee) {
        val chip = Chip(requireContext())
        chip.setChipBackgroundColorResource(android.R.color.transparent)
        chip.chipStrokeWidth = dimensionConverter.dpToPx(1).toFloat()
        chip.text = pendingInvitee.getBestName()
        chip.isClickable = true
        chip.isCheckable = false
        chip.isCloseIconVisible = true
        chipGroup.addView(chip)
        chip.setOnCloseIconClickListener {
            viewModel.handle(UserListAction.RemovePendingInvitee(pendingInvitee))
        }
    }

    override fun onInviteFriendClick() {
        viewModel.handle(UserListAction.ComputeMatrixToLinkForSharing)
    }

    override fun onContactBookClick() {
        sharedActionViewModel.post(UserListSharedAction.OpenPhoneBook)
    }

    override fun onItemClick(user: User) {
        view?.hideKeyboard()
        viewModel.handle(UserListAction.SelectPendingInvitee(PendingInvitee.UserPendingInvitee(user)))
    }

    override fun onMatrixIdClick(matrixId: String) {
        view?.hideKeyboard()
        viewModel.handle(UserListAction.SelectPendingInvitee(PendingInvitee.UserPendingInvitee(User(matrixId))))
    }

    override fun onThreePidClick(threePid: ThreePid) {
        view?.hideKeyboard()
        viewModel.handle(UserListAction.SelectPendingInvitee(PendingInvitee.ThreePidPendingInvitee(threePid)))
    }

    override fun onUseQRCode() {
        view?.hideKeyboard()
        sharedActionViewModel.post(UserListSharedAction.AddByQrCode)
    }
}
