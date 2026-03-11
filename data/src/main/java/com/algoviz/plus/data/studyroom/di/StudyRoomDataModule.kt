package com.algoviz.plus.data.studyroom.di

import com.algoviz.plus.data.studyroom.repository.StudyRoomRepositoryImpl
import com.algoviz.plus.domain.repository.StudyRoomRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class StudyRoomDataModule {
    
    @Binds
    @Singleton
    abstract fun bindStudyRoomRepository(
        impl: StudyRoomRepositoryImpl
    ): StudyRoomRepository
}
