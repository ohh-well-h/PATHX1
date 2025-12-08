package com.example.pathx01.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// Custom serializer for LocalDateTime
object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    
    override val descriptor: SerialDescriptor = 
        PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)
    
    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeString(value.format(formatter))
    }
    
    override fun deserialize(decoder: Decoder): LocalDateTime {
        return LocalDateTime.parse(decoder.decodeString(), formatter)
    }
}

@Serializable
data class WritingEntry(
    val id: Int,
    val title: String,
    val content: String,
    val type: WritingType,
    val mood: String? = null,
    val tags: List<String> = emptyList(),
    val attachments: List<Attachment> = emptyList(),
    val checklists: List<ChecklistItem> = emptyList(),
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime
)

@Serializable
enum class WritingType {
    JOURNAL,
    NOTE
}

@Serializable
data class Attachment(
    val type: AttachmentType,
    val path: String,
    val name: String,
    val size: Long? = null
)

@Serializable
enum class AttachmentType {
    IMAGE,
    AUDIO,
    VIDEO,
    FILE
}

@Serializable
data class ChecklistItem(
    val text: String,
    val isCompleted: Boolean
)
