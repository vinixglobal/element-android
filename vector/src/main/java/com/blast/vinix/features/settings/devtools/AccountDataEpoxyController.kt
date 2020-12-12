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

package com.blast.vinix.features.settings.devtools

import android.view.View
import com.airbnb.epoxy.TypedEpoxyController
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.Success
import com.blast.vinix.R
import com.blast.vinix.core.epoxy.loadingItem
import com.blast.vinix.core.resources.StringProvider
import com.blast.vinix.core.ui.list.genericFooterItem
import com.blast.vinix.core.ui.list.genericItemWithValue
import com.blast.vinix.core.utils.DebouncedClickListener
import org.matrix.android.sdk.api.session.accountdata.UserAccountDataEvent
import javax.inject.Inject

class AccountDataEpoxyController @Inject constructor(
        private val stringProvider: StringProvider
) : TypedEpoxyController<AccountDataViewState>() {

    interface InteractionListener {
        fun didTap(data: UserAccountDataEvent)
        fun didLongTap(data: UserAccountDataEvent)
    }

    var interactionListener: InteractionListener? = null

    override fun buildModels(data: AccountDataViewState?) {
        if (data == null) return
        when (data.accountData) {
            is Loading -> {
                loadingItem {
                    id("loading")
                    loadingText(stringProvider.getString(R.string.loading))
                }
            }
            is Fail    -> {
                genericFooterItem {
                    id("fail")
                    text(data.accountData.error.localizedMessage)
                }
            }
            is Success -> {
                val dataList = data.accountData.invoke()
                if (dataList.isEmpty()) {
                    genericFooterItem {
                        id("noResults")
                        text(stringProvider.getString(R.string.no_result_placeholder))
                    }
                } else {
                    dataList.forEach { accountData ->
                        genericItemWithValue {
                            id(accountData.type)
                            title(accountData.type)
                            itemClickAction(DebouncedClickListener(View.OnClickListener {
                                interactionListener?.didTap(accountData)
                            }))
                            itemLongClickAction(View.OnLongClickListener {
                                interactionListener?.didLongTap(accountData)
                                true
                            })
                        }
                    }
                }
            }
        }
    }
}
