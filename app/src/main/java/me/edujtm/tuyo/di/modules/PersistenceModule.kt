package me.edujtm.tuyo.di.modules

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import me.edujtm.tuyo.data.persistence.YoutubeDatabase
import javax.inject.Singleton

@Module
object PersistenceModule {

    @JvmStatic
    @Provides
    @Singleton
    fun providesYoutubeDatabase(context: Context): YoutubeDatabase {
        return Room.databaseBuilder(
            context,
            YoutubeDatabase::class.java,
            "Youtube.db")
            .build()
    }
}