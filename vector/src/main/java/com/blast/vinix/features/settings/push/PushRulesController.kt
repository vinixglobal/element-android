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

package com.blast.vinix.features.settings.push

import com.airbnb.epoxy.TypedEpoxyController
import com.blast.vinix.R
import com.blast.vinix.core.resources.StringProvider
import com.blast.vinix.core.ui.list.genericFooterItem
import javax.inject.Inject

class PushRulesController @Inject constructor(
        private val stringProvider: StringProvider
) : TypedEpoxyController<PushRulesViewState>() {

    override fun buildModels(data: PushRulesViewState?) {
        data?.let {
            it.rules.forEach {
                pushRuleItem {
                    id(it.ruleId)
                    pushRule(it)
                }
            }
        } ?: run {
            genericFooterItem {
                id("footer")
                text(stringProvider.getString(R.string.settings_push_rules_no_rules))
            }
        }
    }
}
