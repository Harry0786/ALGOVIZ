package com.algoviz.plus.core.database.di

import android.content.Context
import androidx.room.Room
import com.algoviz.plus.core.common.constants.AppConstants
import com.algoviz.plus.core.database.AlgoVizDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAlgoVizDatabase(
        @ApplicationContext context: Context
    ): AlgoVizDatabase {
        return Room.databaseBuilder(
            context,
            AlgoVizDatabase::class.java,
            AppConstants.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }
}
