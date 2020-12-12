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

package com.blast.vinix.features.homeserver

import androidx.lifecycle.viewModelScope
import com.airbnb.mvrx.FragmentViewModelContext
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import com.blast.vinix.core.di.HasScreenInjector
import com.blast.vinix.core.platform.EmptyAction
import com.blast.vinix.core.platform.EmptyViewEvents
import com.blast.vinix.core.platform.VectorViewModel
import com.blast.vinix.features.raw.wellknown.getElementWellknown
import com.blast.vinix.features.raw.wellknown.isE2EByDefault
import com.blast.vinix.features.userdirectory.UserListFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.matrix.android.sdk.api.extensions.tryOrNull
import org.matrix.android.sdk.api.raw.RawService
import org.matrix.android.sdk.api.session.Session
import org.matrix.android.sdk.api.session.homeserver.HomeServerCapabilities

class HomeServerCapabilitiesViewModel @AssistedInject constructor(
        @Assisted initialState: HomeServerCapabilitiesViewState,
        private val session: Session,
        private val rawService: RawService
) : VectorViewModel<HomeServerCapabilitiesViewState, EmptyAction, EmptyViewEvents>(initialState) {

    @AssistedInject.Factory
    interface Factory {
        fun create(initialState: HomeServerCapabilitiesViewState): HomeServerCapabilitiesViewModel
    }

    companion object : MvRxViewModelFactory<HomeServerCapabilitiesViewModel, HomeServerCapabilitiesViewState> {
        @JvmStatic
        override fun create(viewModelContext: ViewModelContext, state: HomeServerCapabilitiesViewState): HomeServerCapabilitiesViewModel? {
            val fragment: UserListFragment = (viewModelContext as FragmentViewModelContext).fragment()
            return fragment.homeServerCapabilitiesViewModelFactory.create(state)
        }

        override fun initialState(viewModelContext: ViewModelContext): HomeServerCapabilitiesViewState? {
            val session = (viewModelContext.activity as HasScreenInjector).injector().activeSessionHolder().getSafeActiveSession()
            return HomeServerCapabilitiesViewState(
                    capabilities = session?.getHomeServerCapabilities() ?: HomeServerCapabilities()
            )
        }
    }

    init {
        initAdminE2eByDefault()
    }

    private fun initAdminE2eByDefault() {
        viewModelScope.launch(Dispatchers.IO) {
            val adminE2EByDefault = tryOrNull {
                rawService.getElementWellknown(session.myUserId)
                        ?.isE2EByDefault()
                        ?: true
            } ?: true

            setState {
                copy(
                        isE2EByDefault = adminE2EByDefault
                )
            }
        }
    }

    override fun handle(action: EmptyAction) {}
}
