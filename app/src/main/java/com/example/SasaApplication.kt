package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.local.ChatLocalRepository

class SasaApplication : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val chatRepository: ChatLocalRepository by lazy { ChatLocalRepository(database.chatDao()) }
}
