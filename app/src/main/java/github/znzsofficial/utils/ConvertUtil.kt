package github.znzsofficial.utils

import android.media.MediaMetadataRetriever
import android.os.Build

object ConvertUtil {
    private const val ERROR_TEXT = "获取失败"

    fun getInfoOf(retriever: MediaMetadataRetriever): String {
        val title =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: ERROR_TEXT
        val album =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: ERROR_TEXT
        val artist =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: ERROR_TEXT
        val author =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_AUTHOR) ?: ERROR_TEXT
        val mimetype =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE) ?: ERROR_TEXT
        val composer =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COMPOSER) ?: ERROR_TEXT
        val originSampleRate: String
        val originBitsDeep: String
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            originSampleRate =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_SAMPLERATE)
                    ?: ERROR_TEXT

            originBitsDeep =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITS_PER_SAMPLE)
                    ?: ERROR_TEXT
        } else {
            originSampleRate = ERROR_TEXT
            originBitsDeep = ERROR_TEXT
        }
        val originBitRate =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE) ?: ERROR_TEXT
        return ((("""
    标题：$title
    专辑：$album
    艺术家：$artist
    作者：$author
    作曲家：$composer
    位深：${ConvertUtil.toBitsDeep(originBitsDeep, ERROR_TEXT)}
    """.trimIndent() + "\n采样率："
                + ConvertUtil.toSampleRate(originSampleRate, ERROR_TEXT)
                ) + "\n码率："
                + ConvertUtil.toBitRate(originBitRate, ERROR_TEXT)
                ) + "\n文件类型："
                + mimetype)
    }

    fun toBitRate(origin: String?, default: String): String {
        return if (origin == default) {
            default
        } else {
            val bitrate = origin!!.toLong().div(1000)
            bitrate.toString() + "kbps"
        }
    }

    fun toBitsDeep(origin: String?, default: String): String {
        return if (origin == default) {
            default
        } else {
            origin + "bit"
        }
    }

    fun toSampleRate(origin: String?, default: String): String {
        return if (origin == default) {
            default
        } else {
            origin + "hz"
        }
    }
}