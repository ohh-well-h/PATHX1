package com.example.pathx01.utils

import com.example.pathx01.data.model.AttachmentType

object FileTypeUtils {
    
    // Supported file extensions by category
    val SUPPORTED_IMAGE_EXTENSIONS = setOf(
        "jpg", "jpeg", "png", "gif", "webp", "bmp", "svg", "ico",
        "tiff", "tif", "jfif", "pjpeg", "pjp", "avif", "heic", "heif"
    )
    
    val SUPPORTED_AUDIO_EXTENSIONS = setOf(
        "mp3", "wav", "m4a", "aac", "ogg", "oga", "flac", "amr"
    )
    
    val SUPPORTED_VIDEO_EXTENSIONS = setOf(
        "mp4", "m4v", "mov", "3gp", "3gpp", "webm", "mkv"
    )
    
    val SUPPORTED_DOCUMENT_EXTENSIONS = setOf(
        "txt", "pdf", "doc", "docx"
    )
    
    // All supported extensions (images + documents)
    val ALL_SUPPORTED_EXTENSIONS = SUPPORTED_IMAGE_EXTENSIONS + SUPPORTED_AUDIO_EXTENSIONS + SUPPORTED_VIDEO_EXTENSIONS + SUPPORTED_DOCUMENT_EXTENSIONS
    
    fun getFileExtension(fileName: String): String {
        return fileName.substringAfterLast('.', "").lowercase()
    }
    
    fun isFileSupported(fileName: String): Boolean {
        val extension = getFileExtension(fileName)
        return extension in ALL_SUPPORTED_EXTENSIONS
    }
    
    fun getAttachmentType(fileName: String): AttachmentType {
        val extension = getFileExtension(fileName)
        return when {
            extension in SUPPORTED_IMAGE_EXTENSIONS -> AttachmentType.IMAGE
            extension in SUPPORTED_AUDIO_EXTENSIONS -> AttachmentType.AUDIO
            extension in SUPPORTED_VIDEO_EXTENSIONS -> AttachmentType.VIDEO
            else -> AttachmentType.FILE
        }
    }
    
    fun getSupportedFormatsMessage(): String {
        return """
            🖼️ Supported Image Formats:
            • JPG, JPEG, PNG, GIF, WEBP, BMP, SVG, ICO
            • TIFF, JFIF, AVIF, HEIC (additional formats)
            
            🔊 Supported Audio Formats:
            • MP3, WAV, M4A, AAC, OGG, FLAC, AMR

            🎬 Supported Video Formats:
            • MP4, M4V, MOV, 3GP, WEBM, MKV

            📄 Supported Document Formats:
            • TXT - Text files
            • PDF - Portable Document Format
            • DOC - Microsoft Word Document
            • DOCX - Microsoft Word Document (new format)
        """.trimIndent()
    }
    
    fun getFileCategoryDescription(extension: String): String {
        return when {
            extension in SUPPORTED_IMAGE_EXTENSIONS -> "Image File"
            extension in SUPPORTED_AUDIO_EXTENSIONS -> "Audio File"
            extension in SUPPORTED_VIDEO_EXTENSIONS -> "Video File"
            extension in SUPPORTED_DOCUMENT_EXTENSIONS -> "Document File"
            else -> "Unsupported File Type"
        }
    }
}

