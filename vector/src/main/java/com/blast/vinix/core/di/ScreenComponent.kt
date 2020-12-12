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

package com.blast.vinix.core.di

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import dagger.BindsInstance
import dagger.Component
import com.blast.vinix.core.dialogs.UnrecognizedCertificateDialog
import com.blast.vinix.core.error.ErrorFormatter
import com.blast.vinix.core.preference.UserAvatarPreference
import com.blast.vinix.features.MainActivity
import com.blast.vinix.features.call.CallControlsBottomSheet
import com.blast.vinix.features.call.VectorCallActivity
import com.blast.vinix.features.call.conference.VectorJitsiActivity
import com.blast.vinix.features.createdirect.CreateDirectRoomActivity
import com.blast.vinix.features.crypto.keysbackup.settings.KeysBackupManageActivity
import com.blast.vinix.features.crypto.quads.SharedSecureStorageActivity
import com.blast.vinix.features.crypto.recover.BootstrapBottomSheet
import com.blast.vinix.features.crypto.verification.VerificationBottomSheet
import com.blast.vinix.features.debug.DebugMenuActivity
import com.blast.vinix.features.home.HomeActivity
import com.blast.vinix.features.home.HomeModule
import com.blast.vinix.features.home.room.detail.RoomDetailActivity
import com.blast.vinix.features.home.room.detail.readreceipts.DisplayReadReceiptsBottomSheet
import com.blast.vinix.features.home.room.detail.search.SearchActivity
import com.blast.vinix.features.home.room.detail.timeline.action.MessageActionsBottomSheet
import com.blast.vinix.features.home.room.detail.timeline.edithistory.ViewEditHistoryBottomSheet
import com.blast.vinix.features.home.room.detail.timeline.reactions.ViewReactionsBottomSheet
import com.blast.vinix.features.home.room.detail.widget.RoomWidgetsBottomSheet
import com.blast.vinix.features.home.room.filtered.FilteredRoomsActivity
import com.blast.vinix.features.home.room.list.RoomListModule
import com.blast.vinix.features.home.room.list.actions.RoomListQuickActionsBottomSheet
import com.blast.vinix.features.invite.InviteUsersToRoomActivity
import com.blast.vinix.features.invite.VectorInviteView
import com.blast.vinix.features.link.LinkHandlerActivity
import com.blast.vinix.features.login.LoginActivity
import com.blast.vinix.features.matrixto.MatrixToBottomSheet
import com.blast.vinix.features.media.BigImageViewerActivity
import com.blast.vinix.features.media.VectorAttachmentViewerActivity
import com.blast.vinix.features.navigation.Navigator
import com.blast.vinix.features.permalink.PermalinkHandlerActivity
import com.blast.vinix.features.pin.PinLocker
import com.blast.vinix.features.qrcode.QrCodeScannerActivity
import com.blast.vinix.features.rageshake.BugReportActivity
import com.blast.vinix.features.rageshake.BugReporter
import com.blast.vinix.features.rageshake.RageShake
import com.blast.vinix.features.reactions.EmojiReactionPickerActivity
import com.blast.vinix.features.reactions.widget.ReactionButton
import com.blast.vinix.features.roomdirectory.RoomDirectoryActivity
import com.blast.vinix.features.roomdirectory.createroom.CreateRoomActivity
import com.blast.vinix.features.roommemberprofile.RoomMemberProfileActivity
import com.blast.vinix.features.roommemberprofile.devices.DeviceListBottomSheet
import com.blast.vinix.features.roomprofile.RoomProfileActivity
import com.blast.vinix.features.roomprofile.alias.detail.RoomAliasBottomSheet
import com.blast.vinix.features.roomprofile.settings.historyvisibility.RoomHistoryVisibilityBottomSheet
import com.blast.vinix.features.roomprofile.settings.joinrule.RoomJoinRuleBottomSheet
import com.blast.vinix.features.settings.VectorSettingsActivity
import com.blast.vinix.features.settings.devices.DeviceVerificationInfoBottomSheet
import com.blast.vinix.features.share.IncomingShareActivity
import com.blast.vinix.features.signout.soft.SoftLogoutActivity
import com.blast.vinix.features.terms.ReviewTermsActivity
import com.blast.vinix.features.ui.UiStateRepository
import com.blast.vinix.features.usercode.UserCodeActivity
import com.blast.vinix.features.widgets.WidgetActivity
import com.blast.vinix.features.widgets.permissions.RoomWidgetPermissionBottomSheet
import com.blast.vinix.features.workers.signout.SignOutBottomSheetDialogFragment

@Component(
        dependencies = [
            VectorComponent::class
        ],
        modules = [
            AssistedInjectModule::class,
            ViewModelModule::class,
            FragmentModule::class,
            HomeModule::class,
            RoomListModule::class,
            ScreenModule::class
        ]
)
@ScreenScope
interface ScreenComponent {

    /* ==========================================================================================
     * Shortcut to VectorComponent elements
     * ========================================================================================== */

    fun activeSessionHolder(): ActiveSessionHolder
    fun fragmentFactory(): FragmentFactory
    fun viewModelFactory(): ViewModelProvider.Factory
    fun bugReporter(): BugReporter
    fun rageShake(): RageShake
    fun navigator(): Navigator
    fun pinLocker(): PinLocker
    fun errorFormatter(): ErrorFormatter
    fun uiStateRepository(): UiStateRepository
    fun unrecognizedCertificateDialog(): UnrecognizedCertificateDialog

    /* ==========================================================================================
     * Activities
     * ========================================================================================== */

    fun inject(activity: HomeActivity)
    fun inject(activity: RoomDetailActivity)
    fun inject(activity: RoomProfileActivity)
    fun inject(activity: RoomMemberProfileActivity)
    fun inject(activity: VectorSettingsActivity)
    fun inject(activity: KeysBackupManageActivity)
    fun inject(activity: EmojiReactionPickerActivity)
    fun inject(activity: LoginActivity)
    fun inject(activity: LinkHandlerActivity)
    fun inject(activity: MainActivity)
    fun inject(activity: RoomDirectoryActivity)
    fun inject(activity: BugReportActivity)
    fun inject(activity: FilteredRoomsActivity)
    fun inject(activity: CreateRoomActivity)
    fun inject(activity: CreateDirectRoomActivity)
    fun inject(activity: IncomingShareActivity)
    fun inject(activity: SoftLogoutActivity)
    fun inject(activity: PermalinkHandlerActivity)
    fun inject(activity: QrCodeScannerActivity)
    fun inject(activity: DebugMenuActivity)
    fun inject(activity: SharedSecureStorageActivity)
    fun inject(activity: BigImageViewerActivity)
    fun inject(activity: InviteUsersToRoomActivity)
    fun inject(activity: ReviewTermsActivity)
    fun inject(activity: WidgetActivity)
    fun inject(activity: VectorCallActivity)
    fun inject(activity: VectorAttachmentViewerActivity)
    fun inject(activity: VectorJitsiActivity)
    fun inject(activity: SearchActivity)
    fun inject(activity: UserCodeActivity)

    /* ==========================================================================================
     * BottomSheets
     * ========================================================================================== */

    fun inject(bottomSheet: MessageActionsBottomSheet)
    fun inject(bottomSheet: ViewReactionsBottomSheet)
    fun inject(bottomSheet: ViewEditHistoryBottomSheet)
    fun inject(bottomSheet: DisplayReadReceiptsBottomSheet)
    fun inject(bottomSheet: RoomListQuickActionsBottomSheet)
    fun inject(bottomSheet: RoomAliasBottomSheet)
    fun inject(bottomSheet: RoomHistoryVisibilityBottomSheet)
    fun inject(bottomSheet: RoomJoinRuleBottomSheet)
    fun inject(bottomSheet: VerificationBottomSheet)
    fun inject(bottomSheet: DeviceVerificationInfoBottomSheet)
    fun inject(bottomSheet: DeviceListBottomSheet)
    fun inject(bottomSheet: BootstrapBottomSheet)
    fun inject(bottomSheet: RoomWidgetPermissionBottomSheet)
    fun inject(bottomSheet: RoomWidgetsBottomSheet)
    fun inject(bottomSheet: CallControlsBottomSheet)
    fun inject(bottomSheet: SignOutBottomSheetDialogFragment)
    fun inject(bottomSheet: MatrixToBottomSheet)

    /* ==========================================================================================
     * Others
     * ========================================================================================== */

    fun inject(view: VectorInviteView)
    fun inject(preference: UserAvatarPreference)
    fun inject(button: ReactionButton)

    /* ==========================================================================================
     * Factory
     * ========================================================================================== */

    @Component.Factory
    interface Factory {
        fun create(vectorComponent: VectorComponent,
                   @BindsInstance context: AppCompatActivity
        ): ScreenComponent
    }
}
