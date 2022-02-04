package com.shepeliev.webrtckmp.mediarecorder

internal val MimeType.fileExtension: String
    get() = when (this) {
        MimeType.VideoMp4 -> "mp4"
    }
