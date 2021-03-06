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

package com.blast.vinix.features.usercode

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.airbnb.mvrx.activityViewModel
import com.google.zxing.Result
import com.google.zxing.ResultMetadataType
import com.blast.vinix.R
import com.blast.vinix.core.extensions.registerStartForActivityResult
import com.blast.vinix.core.platform.VectorBaseFragment
import com.blast.vinix.core.utils.PERMISSIONS_FOR_TAKING_PHOTO
import com.blast.vinix.core.utils.checkPermissions
import com.blast.vinix.core.utils.registerForPermissionsResult
import im.vector.lib.multipicker.MultiPicker
import im.vector.lib.multipicker.utils.ImageUtils
import kotlinx.android.synthetic.main.fragment_qr_code_scanner_with_button.*
import me.dm7.barcodescanner.zxing.ZXingScannerView
import org.matrix.android.sdk.api.extensions.tryOrNull
import javax.inject.Inject

class ScanUserCodeFragment @Inject constructor()
    : VectorBaseFragment(),
        ZXingScannerView.ResultHandler {

    override fun getLayoutResId() = R.layout.fragment_qr_code_scanner_with_button

    val sharedViewModel: UserCodeSharedViewModel by activityViewModel()

    var autoFocus = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userCodeMyCodeButton.debouncedClicks {
            sharedViewModel.handle(UserCodeActions.SwitchMode(UserCodeState.Mode.SHOW))
        }

        userCodeOpenGalleryButton.debouncedClicks {
            MultiPicker.get(MultiPicker.IMAGE).single().startWith(pickImageActivityResultLauncher)
        }
    }

    private val openCameraActivityResultLauncher = registerForPermissionsResult { allGranted ->
        if (allGranted) {
            startCamera()
        } else {
            // For now just go back
            sharedViewModel.handle(UserCodeActions.SwitchMode(UserCodeState.Mode.SHOW))
        }
    }

    private val pickImageActivityResultLauncher = registerStartForActivityResult { activityResult ->
        if (activityResult.resultCode == Activity.RESULT_OK) {
            MultiPicker
                    .get(MultiPicker.IMAGE)
                    .getSelectedFiles(requireActivity(), activityResult.data)
                    .firstOrNull()
                    ?.contentUri
                    ?.let { uri ->
                        // try to see if it is a valid matrix code
                        val bitmap = ImageUtils.getBitmap(requireContext(), uri)
                                ?: return@let Unit.also {
                                    Toast.makeText(requireContext(), getString(R.string.qr_code_not_scanned), Toast.LENGTH_SHORT).show()
                                }
                        handleResult(tryOrNull { QRCodeBitmapDecodeHelper.decodeQRFromBitmap(bitmap) })
                    }
        }
    }

    private fun startCamera() {
        userCodeScannerView.startCamera()
        userCodeScannerView.setAutoFocus(autoFocus)
        userCodeScannerView.debouncedClicks {
            this.autoFocus = !autoFocus
            userCodeScannerView.setAutoFocus(autoFocus)
        }
    }

    override fun onStart() {
        super.onStart()
        if (checkPermissions(PERMISSIONS_FOR_TAKING_PHOTO, requireActivity(), openCameraActivityResultLauncher)) {
            startCamera()
        }
    }

    override fun onResume() {
        super.onResume()
        // Register ourselves as a handler for scan results.
        userCodeScannerView.setResultHandler(this)
        if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)) {
            startCamera()
        }
    }

    override fun onPause() {
        super.onPause()
        userCodeScannerView.setResultHandler(null)
        // Stop camera on pause
        userCodeScannerView.stopCamera()
    }

    override fun handleResult(result: Result?) {
        if (result === null) {
            Toast.makeText(requireContext(), R.string.qr_code_not_scanned, Toast.LENGTH_SHORT).show()
            requireActivity().finish()
        } else {
            val rawBytes = getRawBytes(result)
            val rawBytesStr = rawBytes?.toString(Charsets.ISO_8859_1)
            val value = rawBytesStr ?: result.text
            sharedViewModel.handle(UserCodeActions.DecodedQRCode(value))
        }
    }

    // Copied from https://github.com/markusfisch/BinaryEye/blob/
    // 9d57889b810dcaa1a91d7278fc45c262afba1284/app/src/main/kotlin/de/markusfisch/android/binaryeye/activity/CameraActivity.kt#L434
    private fun getRawBytes(result: Result): ByteArray? {
        val metadata = result.resultMetadata ?: return null
        val segments = metadata[ResultMetadataType.BYTE_SEGMENTS] ?: return null
        var bytes = ByteArray(0)
        @Suppress("UNCHECKED_CAST")
        for (seg in segments as Iterable<ByteArray>) {
            bytes += seg
        }
        // byte segments can never be shorter than the text.
        // Zxing cuts off content prefixes like "WIFI:"
        return if (bytes.size >= result.text.length) bytes else null
    }
}
