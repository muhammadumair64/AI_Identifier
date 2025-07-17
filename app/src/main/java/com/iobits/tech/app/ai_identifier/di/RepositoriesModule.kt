package com.iobits.tech.app.ai_identifier.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {



//    @Binds
//    abstract fun providesRepository(impl: HeartBeatRepoImpl): HeartRecordsRepo
//
//    @Binds
//    abstract fun providesPdfFilesRepository(impl: PdfFileRepoImpl): PdfFileRepo
//
//    @Binds
//    abstract fun providesdataFilesRepository(impl: DataRepositoryImpl): DataRepo
}