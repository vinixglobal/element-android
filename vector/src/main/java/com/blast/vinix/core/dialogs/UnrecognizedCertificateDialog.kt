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
package com.blast.vinix.core.dialogs

import android.app.Activity
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.blast.vinix.R
import com.blast.vinix.core.di.ActiveSessionHolder
import com.blast.vinix.core.resources.StringProvider
import org.matrix.android.sdk.internal.network.ssl.Fingerprint
import timber.log.Timber
import java.util.HashMap
import java.util.HashSet
import javax.inject.Inject

/**
 * This class displays the unknown certificate dialog
 */
class UnrecognizedCertificateDialog @Inject constructor(
        private val activeSessionHolder: ActiveSessionHolder,
        private val stringProvider: StringProvider
) {
    private val ignoredFingerprints: MutableMap<String, MutableSet<Fingerprint>> = HashMap()
    private val openDialogIds: MutableSet<String> = HashSet()

    /**
     * Display a certificate dialog box, asking the user about an unknown certificate
     * To use when user is currently logged in
     *
     * @param unrecognizedFingerprint the fingerprint for the unknown certificate
     * @param callback                callback to fire when the user makes a decision
     */
    fun show(activity: Activity,
             unrecognizedFingerprint: Fingerprint,
             callback: Callback) {
        val userId = activeSessionHolder.getSafeActiveSession()?.myUserId
        val hsConfig = activeSessionHolder.getSafeActiveSession()?.sessionParams?.homeServerConnectionConfig ?: return

        internalShow(activity, unrecognizedFingerprint, true, callback, userId, hsConfig.homeServerUri.toString(), hsConfig.allowedFingerprints.isNotEmpty())
    }

    /**
     * To use during login flow
     */
    fun show(activity: Activity,
             unrecognizedFingerprint: Fingerprint,
             homeServerUrl: String,
             callback: Callback) {
        internalShow(activity, unrecognizedFingerprint, false, callback, null, homeServerUrl, false)
    }

    /**
     * Display a certificate dialog box, asking the user about an unknown certificate
     *
     * @param unrecognizedFingerprint the fingerprint for the unknown certificate
     * @param existing                the current session already exist, so it mean that something has changed server side
     * @param callback                callback to fire when the user makes a decision
     */
    private fun internalShow(activity: Activity,
                             unrecognizedFingerprint: Fingerprint,
                             existing: Boolean,
                             callback: Callback,
                             userId: String?,
                             homeServerUrl: String,
                             homeServerConnectionConfigHasFingerprints: Boolean) {
        val dialogId = userId ?: homeServerUrl + unrecognizedFingerprint.displayableHexRepr

        if (openDialogIds.contains(dialogId)) {
            Timber.i("Not opening dialog $dialogId as one is already open.")
            return
        }

        if (userId != null) {
            val f: Set<Fingerprint>? = ignoredFingerprints[userId]
            if (f != null && f.contains(unrecognizedFingerprint)) {
                callback.onIgnore()
                return
            }
        }

        val builder = AlertDialog.Builder(activity)
        val inflater = activity.layoutInflater
        val layout: View = inflater.inflate(R.layout.dialog_ssl_fingerprint, null)
        val sslFingerprintTitle = layout.findViewById<TextView>(R.id.ssl_fingerprint_title)
        sslFingerprintTitle.text = stringProvider.getString(R.string.ssl_fingerprint_hash, unrecognizedFingerprint.hashType.toString())
        val sslFingerprint = layout.findViewById<TextView>(R.id.ssl_fingerprint)
        sslFingerprint.text = unrecognizedFingerprint.displayableHexRepr
        val sslUserId = layout.findViewById<TextView>(R.id.ssl_user_id)
        if (userId != null) {
            sslUserId.text = stringProvider.getString(R.string.generic_label_and_value,
                    stringProvider.getString(R.string.username),
                    userId)
        } else {
            sslUserId.text = stringProvider.getString(R.string.generic_label_and_value,
                    stringProvider.getString(R.string.hs_url),
                    homeServerUrl)
        }
        val sslExpl = layout.findViewById<TextView>(R.id.ssl_explanation)
        if (existing) {
            if (homeServerConnectionConfigHasFingerprints) {
                sslExpl.text = stringProvider.getString(R.string.ssl_expected_existing_expl)
            } else {
                sslExpl.text = stringProvider.getString(R.string.ssl_unexpected_existing_expl)
            }
        } else {
            sslExpl.text = stringProvider.getString(R.string.ssl_cert_new_account_expl)
        }
        builder.setView(layout)
        builder.setTitle(R.string.ssl_could_not_verify)
        builder.setPositiveButton(R.string.ssl_trust) { _, _ ->
            callback.onAccept()
        }
        if (existing) {
            builder.setNegativeButton(R.string.ssl_remain_offline) { _, _ ->
                if (userId != null) {
                    var f = ignoredFingerprints[userId]
                    if (f == null) {
                        f = HashSet()
                        ignoredFingerprints[userId] = f
                    }
                    f.add(unrecognizedFingerprint)
                }
                callback.onIgnore()
            }
            builder.setNeutralButton(R.string.ssl_logout_account) { _, _ -> callback.onReject() }
        } else {
            builder.setNegativeButton(R.string.cancel) { _, _ -> callback.onReject() }
        }

        builder.setOnDismissListener {
            Timber.d("Dismissed!")
            openDialogIds.remove(dialogId)
        }

        builder.show()
        openDialogIds.add(dialogId)
    }

    interface Callback {
        /**
         * The certificate was explicitly accepted
         */
        fun onAccept()

        /**
         * The warning was ignored by the user
         */
        fun onIgnore()

        /**
         * The unknown certificate was explicitly rejected
         */
        fun onReject()
    }
}
