package com.bytemanager.stats.hilt

import com.bytemanager.stats.interfaces.BatteryTempHistoryRepositoryInterface
import com.bytemanager.stats.repository.BatteryTempHistoryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class BatteryTempHistoryRepositoryModule {
    @Binds
    abstract fun bindBatteryTempHistoryRepositoryInterface(impl: BatteryTempHistoryRepository): BatteryTempHistoryRepositoryInterface
}