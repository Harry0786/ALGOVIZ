package com.algoviz.plus.core.network.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.github.jan.supabase.storage.Storage
import kotlinx.serialization.json.Json
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(
        json: Json,
        @Named("supabase_url") supabaseUrl: String,
        @Named("supabase_key") supabaseKey: String
    ): SupabaseClient {
        // Basic validation to provide clearer error messages at DI time
        require(supabaseUrl.isNotBlank()) { "SUPABASE_URL is not provided. Set it in local.properties or CI secrets." }
        require(supabaseKey.isNotBlank()) { "SUPABASE_KEY is not provided. Set it in local.properties or CI secrets." }

        // Match the runtime plugin setup used by the library examples.
        // The app touches auth, postgrest, realtime, and storage during startup.
        return createSupabaseClient(supabaseUrl, supabaseKey) {
            defaultSerializer = KotlinXSerializer(json)
            install(Auth) {
                scheme = "algovizplus"
                host = "password-reset"
            }
            install(Postgrest)
            install(Realtime)
            install(Storage)
        }
    }
}
