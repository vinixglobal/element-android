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

package com.blast.vinix.features.crypto.recover

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.airbnb.mvrx.parentFragmentViewModel
import com.airbnb.mvrx.withState
import com.blast.vinix.R
import com.blast.vinix.core.extensions.registerStartForActivityResult
import com.blast.vinix.core.platform.VectorBaseFragment
import com.blast.vinix.core.resources.ColorProvider
import com.blast.vinix.core.utils.startSharePlainTextIntent
import com.blast.vinix.core.utils.toast
import kotlinx.android.synthetic.main.fragment_bootstrap_save_key.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class BootstrapSaveRecoveryKeyFragment @Inject constructor(
        private val colorProvider: ColorProvider
) : VectorBaseFragment() {

    override fun getLayoutResId() = R.layout.fragment_bootstrap_save_key

    val sharedViewModel: BootstrapSharedViewModel by parentFragmentViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recoverySave.clickableView.debouncedClicks { downloadRecoveryKey() }
        recoveryCopy.clickableView.debouncedClicks { shareRecoveryKey() }
        recoveryContinue.clickableView.debouncedClicks {
            // We do not display the final Fragment anymore
            // TODO Do some cleanup
            // sharedViewModel.handle(BootstrapActions.GoToCompleted)
            sharedViewModel.handle(BootstrapActions.Completed)
        }
    }

    private fun downloadRecoveryKey() = withState(sharedViewModel) { _ ->

        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TITLE, "blast-recovery-key.txt")

        try {
            sharedViewModel.handle(BootstrapActions.SaveReqQueryStarted)
            saveStartForActivityResult.launch(Intent.createChooser(intent, getString(R.string.keys_backup_setup_step3_please_make_copy)))
        } catch (activityNotFoundException: ActivityNotFoundException) {
            requireActivity().toast(R.string.error_no_external_application_found)
            sharedViewModel.handle(BootstrapActions.SaveReqFailed)
        }
    }

    private val saveStartForActivityResult = registerStartForActivityResult { activityResult ->
        if (activityResult.resultCode == Activity.RESULT_OK) {
            val uri = activityResult.data?.data ?: return@registerStartForActivityResult
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    sharedViewModel.handle(BootstrapActions.SaveKeyToUri(requireContext().contentResolver!!.openOutputStream(uri)!!))
                } catch (failure: Throwable) {
                    sharedViewModel.handle(BootstrapActions.SaveReqFailed)
                }
            }
        } else {
            // result code seems to be always cancelled here.. so act as if it was saved
            sharedViewModel.handle(BootstrapActions.SaveReqFailed)
        }
    }

    private val copyStartForActivityResult = registerStartForActivityResult { activityResult ->
        if (activityResult.resultCode == Activity.RESULT_OK) {
            sharedViewModel.handle(BootstrapActions.RecoveryKeySaved)
        }
    }

    private fun shareRecoveryKey() = withState(sharedViewModel) { state ->
        val recoveryKey = state.recoveryKeyCreationInfo?.recoveryKey?.formatRecoveryKey()
                ?: return@withState

        startSharePlainTextIntent(
                this,
                copyStartForActivityResult,
                context?.getString(R.string.keys_backup_setup_step3_share_intent_chooser_title),
                recoveryKey,
                context?.getString(R.string.recovery_key)
        )
    }

    override fun invalidate() = withState(sharedViewModel) { state ->
        val step = state.step
        if (step !is BootstrapStep.SaveRecoveryKey) return@withState

        recoveryContinue.isVisible = step.isSaved
        bootstrapRecoveryKeyText.text = state.recoveryKeyCreationInfo?.recoveryKey?.formatRecoveryKey()
    }
}
