package com.example.data

import com.example.data.dao.ChatDao
import com.example.data.model.ConversationEntity
import com.example.data.model.MessageEntity
import kotlinx.coroutines.flow.Flow

class ChatRepository(private val chatDao: ChatDao) {

    val allConversations: Flow<List<ConversationEntity>> = chatDao.getAllConversations()

    fun getConversation(id: Long): Flow<ConversationEntity?> = chatDao.getConversation(id)

    suspend fun getConversationOnce(id: Long): ConversationEntity? = chatDao.getConversationOnce(id)

    suspend fun createConversation(title: String, modelId: String): Long {
        val conv = ConversationEntity(
            title = title,
            modelId = modelId,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        return chatDao.insertConversation(conv)
    }

    suspend fun updateConversation(conversation: ConversationEntity) {
        chatDao.updateConversation(conversation.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun renameConversation(id: Long, newTitle: String) {
        val existing = chatDao.getConversationOnce(id)
        if (existing != null) {
            chatDao.updateConversation(existing.copy(title = newTitle, updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun deleteConversation(id: Long) {
        chatDao.deleteMessagesForConversation(id)
        chatDao.deleteConversation(id)
    }

    suspend fun clearAllConversations() {
        chatDao.deleteAllConversations()
    }

    fun getMessages(conversationId: Long): Flow<List<MessageEntity>> =
        chatDao.getMessages(conversationId)

    suspend fun getMessagesOnce(conversationId: Long): List<MessageEntity> =
        chatDao.getMessagesOnce(conversationId)

    suspend fun saveMessage(
        conversationId: Long,
        role: String,
        content: String,
        tokensPerSecond: Float = 0f,
        promptTokens: Int = 0,
        completionTokens: Int = 0,
        latencyMs: Long = 0L,
        isStreaming: Boolean = false
    ): Long {
        val msg = MessageEntity(
            conversationId = conversationId,
            role = role,
            content = content,
            timestamp = System.currentTimeMillis(),
            tokensPerSecond = tokensPerSecond,
            promptTokens = promptTokens,
            completionTokens = completionTokens,
            latencyMs = latencyMs,
            isStreaming = isStreaming
        )
        val id = chatDao.insertMessage(msg)
        // Update conversation timestamp
        val conv = chatDao.getConversationOnce(conversationId)
        if (conv != null) {
            chatDao.updateConversation(conv.copy(updatedAt = System.currentTimeMillis()))
        }
        return id
    }

    suspend fun updateMessage(message: MessageEntity) {
        chatDao.updateMessage(message)
    }

    suspend fun deleteMessage(id: Long) {
        chatDao.deleteMessage(id)
    }

    suspend fun getStats(): Pair<Int, Int> {
        return Pair(chatDao.getConversationCount(), chatDao.getTotalMessageCount())
    }
}
