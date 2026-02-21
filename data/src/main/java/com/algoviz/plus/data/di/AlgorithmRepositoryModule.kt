package com.algoviz.plus.data.di

import com.algoviz.plus.data.repository.AlgorithmRepositoryImpl
import com.algoviz.plus.domain.repository.AlgorithmRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AlgorithmRepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindAlgorithmRepository(
        impl: AlgorithmRepositoryImpl
    ): AlgorithmRepository
}
