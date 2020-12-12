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

import android.os.Bundle
import android.view.View
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.blast.vinix.R
import com.blast.vinix.core.extensions.cleanup
import com.blast.vinix.core.extensions.configureWith
import com.blast.vinix.core.platform.VectorBaseFragment
import com.blast.vinix.core.resources.ColorProvider
import com.blast.vinix.core.utils.createJSonViewerStyleProvider
import kotlinx.android.synthetic.main.fragment_generic_recycler.*
import org.billcarsonfr.jsonviewer.JSonViewerDialog
import org.matrix.android.sdk.api.session.events.model.Event
import javax.inject.Inject

class GossipingEventsPaperTrailFragment @Inject constructor(
        val viewModelFactory: GossipingEventsPaperTrailViewModel.Factory,
        private val epoxyController: GossipingTrailPagedEpoxyController,
        private val colorProvider: ColorProvider
) : VectorBaseFragment(), GossipingTrailPagedEpoxyController.InteractionListener {

    override fun getLayoutResId() = R.layout.fragment_generic_recycler

    private val viewModel: GossipingEventsPaperTrailViewModel by fragmentViewModel(GossipingEventsPaperTrailViewModel::class)

    override fun invalidate() = withState(viewModel) { state ->
        state.events.invoke()?.let {
            epoxyController.submitList(it)
        }
        Unit
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        genericRecyclerView.configureWith(epoxyController, showDivider = true)
        epoxyController.interactionListener = this
    }

    override fun onDestroyView() {
        super.onDestroyView()
        genericRecyclerView.cleanup()
        epoxyController.interactionListener = null
    }

    override fun didTap(event: Event) {
        if (event.isEncrypted()) {
            event.toClearContentStringWithIndent()
        } else {
            event.toContentStringWithIndent()
        }?.let {
            JSonViewerDialog.newInstance(
                    it,
                    -1,
                    createJSonViewerStyleProvider(colorProvider)
            ).show(childFragmentManager, "JSON_VIEWER")
        }
    }
}
