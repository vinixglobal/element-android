package ${escapeKotlinIdentifiers(packageName)}

import com.airbnb.mvrx.ActivityViewModelContext
import com.airbnb.mvrx.FragmentViewModelContext
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import com.blast.vinix.core.extensions.exhaustive
import com.blast.vinix.core.platform.VectorViewModel

<#if createViewEvents>
<#else>
import com.blast.vinix.core.platform.EmptyViewEvents
</#if>

class ${viewModelClass} @AssistedInject constructor(@Assisted initialState: ${viewStateClass})
    <#if createViewEvents>
    : VectorViewModel<${viewStateClass}, ${actionClass}, ${viewEventsClass}>(initialState) {
    <#else>
    : VectorViewModel<${viewStateClass}, ${actionClass}, EmptyViewEvents>(initialState) {
    </#if>

    @AssistedInject.Factory
    interface Factory {
        fun create(initialState: ${viewStateClass}): ${viewModelClass}
    }

    companion object : MvRxViewModelFactory<${viewModelClass}, ${viewStateClass}> {

        @JvmStatic
        override fun create(viewModelContext: ViewModelContext, state: ${viewStateClass}): ${viewModelClass}? {
            val factory = when (viewModelContext) {
                is FragmentViewModelContext -> viewModelContext.fragment as? Factory
                is ActivityViewModelContext -> viewModelContext.activity as? Factory
            }
            return factory?.create(state) ?: error("You should let your activity/fragment implements Factory interface")
        }
    }

    override fun handle(action: ${actionClass}) {
        when (action) {

        }.exhaustive
    }
}
