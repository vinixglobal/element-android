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

package com.blast.vinix.features.settings

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.blast.vinix.R

class BackgroundSyncModeChooserDialog : DialogFragment() {

    var interactionListener: InteractionListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val initialMode = BackgroundSyncMode.fromString(arguments?.getString(ARG_INITIAL_MODE))

        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_background_sync_mode, null)
        val dialog = AlertDialog.Builder(requireActivity())
                .setTitle(R.string.settings_background_fdroid_sync_mode)
                .setView(view)
                .setPositiveButton(R.string.cancel, null)
                .create()

        view.findViewById<View>(R.id.backgroundSyncModeBattery).setOnClickListener {
            interactionListener
                    ?.takeIf { initialMode != BackgroundSyncMode.FDROID_BACKGROUND_SYNC_MODE_FOR_BATTERY }
                    ?.onOptionSelected(BackgroundSyncMode.FDROID_BACKGROUND_SYNC_MODE_FOR_BATTERY)
            dialog.dismiss()
        }
        view.findViewById<View>(R.id.backgroundSyncModeReal).setOnClickListener {
            interactionListener
                    ?.takeIf { initialMode != BackgroundSyncMode.FDROID_BACKGROUND_SYNC_MODE_FOR_REALTIME }
                    ?.onOptionSelected(BackgroundSyncMode.FDROID_BACKGROUND_SYNC_MODE_FOR_REALTIME)
            dialog.dismiss()
        }
        view.findViewById<View>(R.id.backgroundSyncModeOff).setOnClickListener {
            interactionListener
                    ?.takeIf { initialMode != BackgroundSyncMode.FDROID_BACKGROUND_SYNC_MODE_DISABLED }
                    ?.onOptionSelected(BackgroundSyncMode.FDROID_BACKGROUND_SYNC_MODE_DISABLED)
            dialog.dismiss()
        }
        return dialog
    }

    interface InteractionListener {
        fun onOptionSelected(mode: BackgroundSyncMode)
    }

    companion object {
        private const val ARG_INITIAL_MODE = "ARG_INITIAL_MODE"

        fun newInstance(selectedMode: BackgroundSyncMode): BackgroundSyncModeChooserDialog {
            val frag = BackgroundSyncModeChooserDialog()
            val args = Bundle()
            args.putString(ARG_INITIAL_MODE, selectedMode.name)
            frag.arguments = args
            return frag
        }
    }
}
