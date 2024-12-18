package com.oleggio.topchat.repository

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.oleggio.topchat.room.ChatDao
import com.oleggio.topchat.room.MessageDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ChatRepositoryModule {
    @Provides
    @Singleton
    fun provideChatRepository(dao : ChatDao): ChatRepository {
        return ChatRepository(dao)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(@ApplicationContext context : Context): LoginRepository {
        return LoginRepository(context)
    }

    @Provides
    @Singleton
    fun provideMessageRepository(dao : MessageDao): MessageRepository {
        return MessageRepository(dao)
    }
}