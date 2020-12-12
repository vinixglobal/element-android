/*
 * Copyright 2019 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blast.vinix.core.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.content.res.Resources
import dagger.Binds
import dagger.Module
import dagger.Provides
import com.blast.vinix.core.error.DefaultErrorFormatter
import com.blast.vinix.core.error.ErrorFormatter
import com.blast.vinix.features.navigation.DefaultNavigator
import com.blast.vinix.features.navigation.Navigator
import com.blast.vinix.features.pin.PinCodeStore
import com.blast.vinix.features.pin.SharedPrefPinCodeStore
import com.blast.vinix.features.ui.SharedPreferencesUiStateRepository
import com.blast.vinix.features.ui.UiStateRepository
import org.matrix.android.sdk.api.Matrix
import org.matrix.android.sdk.api.auth.AuthenticationService
import org.matrix.android.sdk.api.legacy.LegacySessionImporter
import org.matrix.android.sdk.api.raw.RawService
import org.matrix.android.sdk.api.session.Session

@Module
abstract class VectorModule {

    @Module
    companion object {

        @Provides
        @JvmStatic
        fun providesResources(context: Context): Resources {
            return context.resources
        }

        @Provides
        @JvmStatic
        fun providesSharedPreferences(context: Context): SharedPreferences {
            return context.getSharedPreferences("im.vector.riot", MODE_PRIVATE)
        }

        @Provides
        @JvmStatic
        fun providesMatrix(context: Context): Matrix {
            return Matrix.getInstance(context)
        }

        @Provides
        @JvmStatic
        fun providesCurrentSession(activeSessionHolder: ActiveSessionHolder): Session {
            // TODO: handle session injection better
            return activeSessionHolder.getActiveSession()
        }

        @Provides
        @JvmStatic
        fun providesLegacySessionImporter(matrix: Matrix): LegacySessionImporter {
            return matrix.legacySessionImporter()
        }

        @Provides
        @JvmStatic
        fun providesAuthenticationService(matrix: Matrix): AuthenticationService {
            return matrix.authenticationService()
        }

        @Provides
        @JvmStatic
        fun providesRawService(matrix: Matrix): RawService {
            return matrix.rawService()
        }
    }

    @Binds
    abstract fun bindNavigator(navigator: DefaultNavigator): Navigator

    @Binds
    abstract fun bindErrorFormatter(formatter: DefaultErrorFormatter): ErrorFormatter

    @Binds
    abstract fun bindUiStateRepository(repository: SharedPreferencesUiStateRepository): UiStateRepository

    @Binds
    abstract fun bindPinCodeStore(store: SharedPrefPinCodeStore): PinCodeStore
}
