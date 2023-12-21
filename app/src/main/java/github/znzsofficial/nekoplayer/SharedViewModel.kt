package github.znzsofficial.nekoplayer

import android.media.MediaMetadataRetriever
import androidx.databinding.ObservableArrayList
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import github.znzsofficial.nekoplayer.data.Music
import java.io.File

class SharedViewModel : ViewModel() {

    val musicList = ObservableArrayList<Music>()
    var currentMusicIndex: Int = 0
    val currentMusic: Music? get() = musicList.getOrNull(currentMusicIndex)


    private val _isLoop = MutableLiveData<Boolean>().apply {
        value = false
    }

    /**
     * MediaPlayer是否循环
     */
    val isLoop: LiveData<Boolean> = _isLoop

    fun addMusic(path: String, retriever: MediaMetadataRetriever) {
        // 如果文件不存在就返回
        if (!File(path).isFile) {
            return
        }
        // 将新音乐设为当前音乐
        retriever.setDataSource(path)
        // 获取长度
        val duration =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
                ?: 0L
        // 获取图片
        val picture = retriever.embeddedPicture ?: byteArrayOf()
        val author = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "未知"
        val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: "未知"
        val music = Music(path, title, author, duration / 1000, picture)
        if (!musicList.contains(music)) {
            if (musicList.add(music)) {
                currentMusicIndex = musicList.lastIndex
            }
        }
    }


    fun updateLoopState(bool: Boolean) {
        _isLoop.value = bool
    }
}
