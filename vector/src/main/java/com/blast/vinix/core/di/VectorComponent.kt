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

import android.content.Context
import android.content.res.Resources
import dagger.BindsInstance
import dagger.Component
import com.blast.vinix.ActiveSessionDataSource
import com.blast.vinix.EmojiCompatFontProvider
import com.blast.vinix.EmojiCompatWrapper
import com.blast.vinix.VectorApplication
import com.blast.vinix.core.dialogs.UnrecognizedCertificateDialog
import com.blast.vinix.core.error.ErrorFormatter
import com.blast.vinix.core.pushers.PushersManager
import com.blast.vinix.core.utils.AssetReader
import com.blast.vinix.core.utils.DimensionConverter
import com.blast.vinix.features.call.WebRtcPeerConnectionManager
import com.blast.vinix.features.configuration.VectorConfiguration
import com.blast.vinix.features.crypto.keysrequest.KeyRequestHandler
import com.blast.vinix.features.crypto.verification.IncomingVerificationRequestHandler
import com.blast.vinix.features.grouplist.SelectedGroupDataSource
import com.blast.vinix.features.home.AvatarRenderer
import com.blast.vinix.features.home.HomeRoomListDataSource
import com.blast.vinix.features.home.room.detail.RoomDetailPendingActionStore
import com.blast.vinix.features.home.room.detail.timeline.helper.MatrixItemColorProvider
import com.blast.vinix.features.html.EventHtmlRenderer
import com.blast.vinix.features.html.VectorHtmlCompressor
import com.blast.vinix.features.login.ReAuthHelper
import com.blast.vinix.features.navigation.Navigator
import com.blast.vinix.features.notifications.NotifiableEventResolver
import com.blast.vinix.features.notifications.NotificationBroadcastReceiver
import com.blast.vinix.features.notifications.NotificationDrawerManager
import com.blast.vinix.features.notifications.NotificationUtils
import com.blast.vinix.features.notifications.PushRuleTriggerListener
import com.blast.vinix.features.pin.PinCodeStore
import com.blast.vinix.features.pin.PinLocker
import com.blast.vinix.features.popup.PopupAlertManager
import com.blast.vinix.features.rageshake.BugReporter
import com.blast.vinix.features.rageshake.VectorFileLogger
import com.blast.vinix.features.rageshake.VectorUncaughtExceptionHandler
import com.blast.vinix.features.reactions.data.EmojiDataSource
import com.blast.vinix.features.session.SessionListener
import com.blast.vinix.features.settings.VectorPreferences
import com.blast.vinix.features.ui.UiStateRepository
import org.matrix.android.sdk.api.Matrix
import org.matrix.android.sdk.api.auth.AuthenticationService
import org.matrix.android.sdk.api.raw.RawService
import org.matrix.android.sdk.api.session.Session
import javax.inject.Singleton

@Component(modules = [VectorModule::class])
@Singleton
interface VectorComponent {

    fun inject(notificationBroadcastReceiver: NotificationBroadcastReceiver)

    fun inject(vectorApplication: VectorApplication)

    fun matrix(): Matrix

    fun matrixItemColorProvider(): MatrixItemColorProvider

    fun sessionListener(): SessionListener

    fun currentSession(): Session

    fun notificationUtils(): NotificationUtils

    fun notificationDrawerManager(): NotificationDrawerManager

    fun appContext(): Context

    fun resources(): Resources

    fun assetReader(): AssetReader

    fun dimensionConverter(): DimensionConverter

    fun vectorConfiguration(): VectorConfiguration

    fun avatarRenderer(): AvatarRenderer

    fun activeSessionHolder(): ActiveSessionHolder

    fun unrecognizedCertificateDialog(): UnrecognizedCertificateDialog

    fun emojiCompatFontProvider(): EmojiCompatFontProvider

    fun emojiCompatWrapper(): EmojiCompatWrapper

    fun eventHtmlRenderer(): EventHtmlRenderer

    fun vectorHtmlCompressor(): VectorHtmlCompressor

    fun navigator(): Navigator

    fun errorFormatter(): ErrorFormatter

    fun homeRoomListObservableStore(): HomeRoomListDataSource

    fun selectedGroupStore(): SelectedGroupDataSource

    fun roomDetailPendingActionStore(): RoomDetailPendingActionStore

    fun activeSessionObservableStore(): ActiveSessionDataSource

    fun incomingVerificationRequestHandler(): IncomingVerificationRequestHandler

    fun incomingKeyRequestHandler(): KeyRequestHandler

    fun authenticationService(): AuthenticationService

    fun rawService(): RawService

    fun bugReporter(): BugReporter

    fun vectorUncaughtExceptionHandler(): VectorUncaughtExceptionHandler

    fun pushRuleTriggerListener(): PushRuleTriggerListener

    fun pusherManager(): PushersManager

    fun notifiableEventResolver(): NotifiableEventResolver

    fun vectorPreferences(): VectorPreferences

    fun vectorFileLogger(): VectorFileLogger

    fun uiStateRepository(): UiStateRepository

    fun pinCodeStore(): PinCodeStore

    fun emojiDataSource(): EmojiDataSource

    fun alertManager(): PopupAlertManager

    fun reAuthHelper(): ReAuthHelper

    fun pinLocker(): PinLocker

    fun webRtcPeerConnectionManager(): WebRtcPeerConnectionManager

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): VectorComponent
    }
}
