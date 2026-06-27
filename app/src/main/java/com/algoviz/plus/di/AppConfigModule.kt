package com.algoviz.plus.di

import com.algoviz.plus.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppConfigModule {

    @Provides
    @Singleton
    @Named("supabase_url")
    fun provideSupabaseUrl(): String = BuildConfig.SUPABASE_URL

    @Provides
    @Singleton
    @Named("supabase_key")
    fun provideSupabaseKey(): String = BuildConfig.SUPABASE_KEY
}
