package com.shepeliev.webrtckmp.media

internal val MimeType.fileExtension: String
    get() = when (this) {
        MimeType.VideoMp4 -> "mp4"
    }
