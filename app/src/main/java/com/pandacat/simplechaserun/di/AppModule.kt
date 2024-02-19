package com.pandacat.simplechaserun.di

import android.content.Context
import com.pandacat.simplechaserun.LocationProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Singleton
    @Provides
    fun provideLocationProvider(@ApplicationContext app: Context) = LocationProvider(app)
}