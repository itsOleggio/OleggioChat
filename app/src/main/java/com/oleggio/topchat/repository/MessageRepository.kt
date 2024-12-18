package com.oleggio.topchat.repository

import com.oleggio.topchat.room.MessageDao
import com.oleggio.topchat.room.MessageEntity
import com.oleggio.topchat.api.Image
import com.oleggio.topchat.api.Message
import com.oleggio.topchat.api.MessageData
import com.oleggio.topchat.api.Text
import javax.inject.Inject

class MessageRepository @Inject constructor(private val dao : MessageDao) {
    suspend fun getChatMessages(chatId: String) : List<Message> {
        val messageEntities = dao.getMessages(chatId)
        val messages : MutableList<Message> = mutableListOf()

        for (i : MessageEntity in messageEntities) {
            val message = Message(
                id = i.id,
                from = i.from,
                to = i.to,
                data = MessageData(
                    text = Text(text = i.text),
                    image = if (i.image != null) Image(link = i.image) else null
                ),
                time = i.time,
            )
            messages.add(message)
        }

        return messages.reversed()
    }

    suspend fun insertAllMessages(messages : List<Message>) {
        val entities : MutableList<MessageEntity> = mutableListOf()
        for (msg : Message in messages) {
            val entity = MessageEntity(
                id = msg.id,
                from = msg.from,
                to = msg.to,
                text = msg.data?.text?.text ?: "",
                image = msg.data?.image?.link,
                time = msg.time
            )
            entities.add(entity)
        }

        return dao.insertAllMessages(entities)
    }
}