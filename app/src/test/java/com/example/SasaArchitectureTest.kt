package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.ChatMessage
import com.example.data.GeminiModel
import com.example.data.MessageSender
import com.example.data.local.AppDatabase
import com.example.data.local.ChatDao
import com.example.data.local.ChatMessageEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SasaArchitectureTest {

    private lateinit var database: AppDatabase
    private lateinit var chatDao: ChatDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        chatDao = database.chatDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun `test ChatMessage and ChatMessageEntity transformation`() {
        val domainMsg = ChatMessage(
            sender = MessageSender.USER,
            text = "مرحباً صاصا"
        )
        val entity = ChatMessageEntity.fromDomain(domainMsg)
        assertEquals("USER", entity.sender)
        assertEquals("مرحباً صاصا", entity.text)

        val restoredDomain = entity.toDomain()
        assertEquals(MessageSender.USER, restoredDomain.sender)
        assertEquals("مرحباً صاصا", restoredDomain.text)
        assertFalse(restoredDomain.isError)
    }

    @Test
    fun `test Room DB insertion and retrieval via Flow`() = runBlocking {
        val userEntity = ChatMessageEntity(
            id = "1",
            sender = MessageSender.USER.name,
            text = "ما هو صاصا؟",
            timestamp = 1000L
        )
        val aiEntity = ChatMessageEntity(
            id = "2",
            sender = MessageSender.SASA_AI.name,
            text = "صاصا هو النموذج العربي الذكي",
            timestamp = 2000L
        )

        chatDao.insertMessage(userEntity)
        chatDao.insertMessage(aiEntity)

        val messages = chatDao.getAllMessages().first()
        assertEquals(2, messages.size)
        assertEquals("ما هو صاصا؟", messages[0].text)
        assertEquals("صاصا هو النموذج العربي الذكي", messages[1].text)
    }

    @Test
    fun `test GeminiModel enum definitions`() {
        assertEquals("3.6 Flash", GeminiModel.FLASH_3_6.displayName)
        assertEquals("3.5 Flash-Lite", GeminiModel.FLASH_LITE_3_5.displayName)
        assertEquals("3.1 Pro", GeminiModel.PRO_3_1.displayName)
        assertEquals("تفكير موسّع", GeminiModel.THINKING_EXP.displayName)
    }
}
