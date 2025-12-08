package com.example.pathx01.utils

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import java.io.File

object FileUtils {
    
    fun getFileNameFromUri(context: Context, uri: Uri): String {
        return try {
            // Try to get filename from content resolver
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        val fileName = it.getString(nameIndex)
                        if (!fileName.isNullOrBlank()) {
                            return fileName
                        }
                    }
                }
            }
            
            // Fallback to URI path segment
            val fileName = uri.lastPathSegment
            if (!fileName.isNullOrBlank()) {
                fileName
            } else {
                "Unknown File"
            }
        } catch (e: Exception) {
            "Unknown File"
        }
    }
    
    fun getMimeType(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.getType(uri)
        } catch (e: Exception) {
            null
        }
    }
    
    fun getMimeTypeFromExtension(fileName: String): String? {
        val extension = FileTypeUtils.getFileExtension(fileName)
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }
    
    fun isImageFile(context: Context, uri: Uri): Boolean {
        val mimeType = getMimeType(context, uri)
        val fileName = getFileNameFromUri(context, uri)
        
        // Check by MIME type first
        if (mimeType?.startsWith("image/") == true) {
            return true
        }
        
        // Check by file extension as fallback
        val extension = FileTypeUtils.getFileExtension(fileName)
        return extension in FileTypeUtils.SUPPORTED_IMAGE_EXTENSIONS
    }
    
    fun isImageFile(fileName: String): Boolean {
        val extension = FileTypeUtils.getFileExtension(fileName)
        return extension in FileTypeUtils.SUPPORTED_IMAGE_EXTENSIONS
    }
    
    fun isDocumentFile(context: Context, uri: Uri): Boolean {
        val mimeType = getMimeType(context, uri)
        val fileName = getFileNameFromUri(context, uri)
        
        // Check by MIME type first
        val documentMimeTypes = getDocumentMimeTypes()
        if (mimeType in documentMimeTypes) {
            return true
        }
        
        // Check by file extension as fallback
        val extension = FileTypeUtils.getFileExtension(fileName)
        return extension in FileTypeUtils.SUPPORTED_DOCUMENT_EXTENSIONS
    }
    
    fun isDocumentFile(fileName: String): Boolean {
        val extension = FileTypeUtils.getFileExtension(fileName)
        return extension in FileTypeUtils.SUPPORTED_DOCUMENT_EXTENSIONS
    }
    
    fun getImageMimeTypes(): Array<String> {
        return arrayOf(
            "image/jpeg",
            "image/jpg", 
            "image/png",
            "image/gif",
            "image/webp",
            "image/bmp",
            "image/svg+xml",
            "image/x-icon",
            "image/vnd.microsoft.icon",
            "image/tiff",
            "image/tif",
            "image/jfif",
            "image/pjpeg",
            "image/pjp",
            "image/avif",
            "image/heic",
            "image/heif"
        )
    }
    
    fun getDocumentMimeTypes(): Array<String> {
        return arrayOf(
            "text/plain",           // txt
            "application/pdf",      // pdf
            "application/msword",   // doc
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document" // docx
        )
    }
    
    fun getImageMimeTypePattern(): String {
        return "image/*"
    }
    
    fun getDocumentMimeTypePattern(): String {
        // Use specific MIME types for better file picker filtering
        return "application/*"
    }

    fun getAudioMimeTypePattern(): String {
        return "audio/*"
    }

    fun getVideoMimeTypePattern(): String {
        return "video/*"
    }
}
