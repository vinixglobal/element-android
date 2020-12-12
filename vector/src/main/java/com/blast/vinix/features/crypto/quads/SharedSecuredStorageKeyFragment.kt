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

package com.blast.vinix.features.crypto.quads

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import com.airbnb.mvrx.activityViewModel
import com.jakewharton.rxbinding3.widget.editorActionEvents
import com.jakewharton.rxbinding3.widget.textChanges
import com.blast.vinix.R
import com.blast.vinix.core.extensions.registerStartForActivityResult
import com.blast.vinix.core.platform.VectorBaseFragment
import com.blast.vinix.core.utils.startImportTextFromFileIntent
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_ssss_access_from_key.*
import org.matrix.android.sdk.api.extensions.tryOrNull
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SharedSecuredStorageKeyFragment @Inject constructor() : VectorBaseFragment() {

    override fun getLayoutResId() = R.layout.fragment_ssss_access_from_key

    val sharedViewModel: SharedSecureStorageViewModel by activityViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ssss_restore_with_key_text.text = getString(R.string.enter_secret_storage_input_key)

        ssss_key_enter_edittext.editorActionEvents()
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (it.actionId == EditorInfo.IME_ACTION_DONE) {
                        submit()
                    }
                }
                .disposeOnDestroyView()

        ssss_key_enter_edittext.textChanges()
                .skipInitialValue()
                .subscribe {
                    ssss_key_enter_til.error = null
                    ssss_key_submit.isEnabled = it.isNotBlank()
                }
                .disposeOnDestroyView()

        ssss_key_use_file.debouncedClicks { startImportTextFromFileIntent(requireContext(), importFileStartForActivityResult) }

        ssss_key_reset.clickableView.debouncedClicks {
            sharedViewModel.handle(SharedSecureStorageAction.ForgotResetAll)
        }

        sharedViewModel.observeViewEvents {
            when (it) {
                is SharedSecureStorageViewEvent.KeyInlineError -> {
                    ssss_key_enter_til.error = it.message
                }
            }
        }

        ssss_key_submit.debouncedClicks { submit() }
    }

    fun submit() {
        val text = ssss_key_enter_edittext.text.toString()
        if (text.isBlank()) return // Should not reach this point as button disabled
        ssss_key_submit.isEnabled = false
        sharedViewModel.handle(SharedSecureStorageAction.SubmitKey(text))
    }

    private val importFileStartForActivityResult = registerStartForActivityResult { activityResult ->
        if (activityResult.resultCode == Activity.RESULT_OK) {
            activityResult.data?.data?.let { dataURI ->
                tryOrNull {
                    activity?.contentResolver?.openInputStream(dataURI)
                            ?.bufferedReader()
                            ?.use { it.readText() }
                            ?.let {
                                ssss_key_enter_edittext.setText(it)
                            }
                }
            }
        }
    }
}
