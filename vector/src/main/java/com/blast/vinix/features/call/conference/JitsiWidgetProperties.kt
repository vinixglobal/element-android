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

package com.blast.vinix.features.call.conference

import android.net.Uri
import com.blast.vinix.R
import com.blast.vinix.core.resources.StringProvider

class JitsiWidgetProperties(private val uriString: String, val stringProvider: StringProvider) {
    val domain: String by lazy { configs["conferenceDomain"] ?: stringProvider.getString(R.string.preferred_jitsi_domain) }
    val displayName: String? by lazy { configs["displayName"] }
    val avatarUrl: String? by lazy { configs["avatarUrl"] }

    private val configString: String? by lazy { Uri.parse(uriString).fragment }

    private val configs: Map<String, String?> by lazy {
        configString?.split("&")
                ?.map { it.split("=") }
                ?.map { (key, value) -> key to value }
                ?.toMap()
                ?: mapOf()
    }
}
