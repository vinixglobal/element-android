/*
 * Copyright 2019 New Vector Ltd
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
package com.blast.vinix.features.crypto.verification.choose

import android.app.Activity
import android.os.Bundle
import android.view.View
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.parentFragmentViewModel
import com.airbnb.mvrx.withState
import com.blast.vinix.R
import com.blast.vinix.core.extensions.cleanup
import com.blast.vinix.core.extensions.configureWith
import com.blast.vinix.core.extensions.registerStartForActivityResult
import com.blast.vinix.core.platform.VectorBaseFragment
import com.blast.vinix.core.utils.PERMISSIONS_FOR_TAKING_PHOTO
import com.blast.vinix.core.utils.checkPermissions
import com.blast.vinix.core.utils.registerForPermissionsResult
import com.blast.vinix.features.crypto.verification.VerificationAction
import com.blast.vinix.features.crypto.verification.VerificationBottomSheetViewModel
import com.blast.vinix.features.qrcode.QrCodeScannerActivity
import kotlinx.android.synthetic.main.bottom_sheet_verification_child_fragment.*
import timber.log.Timber
import javax.inject.Inject

class VerificationChooseMethodFragment @Inject constructor(
        val verificationChooseMethodViewModelFactory: VerificationChooseMethodViewModel.Factory,
        val controller: VerificationChooseMethodController
) : VectorBaseFragment(), VerificationChooseMethodController.Listener {

    private val viewModel by fragmentViewModel(VerificationChooseMethodViewModel::class)

    private val sharedViewModel by parentFragmentViewModel(VerificationBottomSheetViewModel::class)

    override fun getLayoutResId() = R.layout.bottom_sheet_verification_child_fragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
    }

    override fun onDestroyView() {
        bottomSheetVerificationRecyclerView.cleanup()
        controller.listener = null
        super.onDestroyView()
    }

    private fun setupRecyclerView() {
        bottomSheetVerificationRecyclerView.configureWith(controller, hasFixedSize = false, disableItemAnimation = true)
        controller.listener = this
    }

    override fun invalidate() = withState(viewModel) { state ->
        controller.update(state)
    }

    override fun doVerifyBySas() = withState(sharedViewModel) { state ->
        sharedViewModel.handle(VerificationAction.StartSASVerification(
                state.otherUserMxItem?.id ?: "",
                state.pendingRequest.invoke()?.transactionId ?: ""))
    }

    private val openCameraActivityResultLauncher = registerForPermissionsResult { allGranted ->
        if (allGranted) {
            doOpenQRCodeScanner()
        }
    }

    override fun openCamera() {
        if (checkPermissions(PERMISSIONS_FOR_TAKING_PHOTO, requireActivity(), openCameraActivityResultLauncher)) {
            doOpenQRCodeScanner()
        }
    }

    override fun onClickOnWasNotMe() {
        sharedViewModel.itWasNotMe()
    }

    private fun doOpenQRCodeScanner() {
        QrCodeScannerActivity.startForResult(requireActivity(), scanActivityResultLauncher)
    }

    private val scanActivityResultLauncher = registerStartForActivityResult { activityResult ->
        if (activityResult.resultCode == Activity.RESULT_OK) {
            val scannedQrCode = QrCodeScannerActivity.getResultText(activityResult.data)
            val wasQrCode = QrCodeScannerActivity.getResultIsQrCode(activityResult.data)

            if (wasQrCode && !scannedQrCode.isNullOrBlank()) {
                onRemoteQrCodeScanned(scannedQrCode)
            } else {
                Timber.w("It was not a QR code, or empty result")
            }
        }
    }

    private fun onRemoteQrCodeScanned(remoteQrCode: String) = withState(sharedViewModel) { state ->
        sharedViewModel.handle(VerificationAction.RemoteQrCodeScanned(
                state.otherUserMxItem?.id ?: "",
                state.pendingRequest.invoke()?.transactionId ?: "",
                remoteQrCode
        ))
    }
}
