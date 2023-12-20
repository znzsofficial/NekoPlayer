package github.znzsofficial.nekoplayer.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _progressNum = MutableLiveData<Float>().apply {
        value = 0.0f
    }

    /**
     * 单位为秒的float
     */
    val progressNum: LiveData<Float> = _progressNum


    private val _inputPath = MutableLiveData<String>().apply {
        value = ""
    }

    /**
     * 输入的二路径字符串
     */
    val inputPath: LiveData<String> = _inputPath


    private val _progressEnabled = MutableLiveData<Boolean>().apply {
        value = false
    }
    val progressEnabled: LiveData<Boolean> = _progressEnabled

    private val _pictureArray = MutableLiveData<ByteArray>()
    val pictureArray: LiveData<ByteArray> = _pictureArray

    /**
     * 更新进度
     * @param newValue 单位为秒的float
     */
    fun updateProgress(newValue: Float) {
        _progressNum.value = newValue
    }

    fun updateInputPath(newValue: String) {
        _inputPath.value = newValue
    }

    fun updateProgressEnabled(newValue: Boolean) {
        _progressEnabled.value = newValue
    }

    fun updatePictureArray(newValue: ByteArray) {
        _pictureArray.value = newValue
    }

}