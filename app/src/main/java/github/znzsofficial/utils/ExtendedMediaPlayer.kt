package github.znzsofficial.utils

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.util.Log
import github.znzsofficial.nekoplayer.data.Music
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ExtendedMediaPlayer : MediaPlayer() {

    private lateinit var mUiHandler: Handler // 刷新ui
    private lateinit var mTimerHandler: Handler // 定时轮询：更新seekBar
    private lateinit var timerRun: Runnable // 定时任务
    private var singleExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var status = MediaPlayStatus.IDLE

    init {
        initData()
        initPlay()
    }

    private fun initData() {
        mUiHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                // 更新当前时间
                try {
                    val curP = currentPosition
                    if (mListener != null) {
                        mListener!!.onSeekUpdate(curP)
                    }
                } catch (e: Exception) {
                    Log.i(TAG, "handleMessage: e = $e")
                    reset()
                }
            }
        }
        val handlerThread = HandlerThread("media_timer")
        handlerThread.start()
        mTimerHandler = Handler(handlerThread.looper)
        timerRun = object : Runnable {
            override fun run() {
                mTimerHandler.postDelayed(this, TIMER_SEEK.toLong())
                val msg = Message.obtain()
                msg.what = HANDLER_PLAY_TIME
                mUiHandler.sendMessage(msg)
            }
        }
    }

    private fun initPlay() {
        Log.i(TAG, "initPlay: status = $status")
        status = MediaPlayStatus.IDLE

        // 设置音频属性
        setAudioAttributes(
            AudioAttributes.Builder() // 检测采样率输出
                .setUsage(AudioAttributes.USAGE_UNKNOWN)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )

        // 当流媒体播放完毕的时候回调。
        setOnCompletionListener {
            Log.i(TAG, "OnCompletion: status = $status")
            stopTimer()
            status = MediaPlayStatus.PLAYBACK_COMPLETED
            if (mListener != null) {
                mListener!!.onCompletion()
            }
        }

        // 需要注意的是，一旦发生错误，即使应用程序尚未注册错误侦听器，MediaPlayer 对象也会进入Error状态。
        // 为了从处于 Error状态的 MediaPlayer 对象并从错误中恢复,reset()可以调用将对象恢复到其Idle 状态。
        setOnErrorListener { _: MediaPlayer?, _: Int, _: Int ->
            status = MediaPlayStatus.ERROR
            if (mListener != null) {
                return@setOnErrorListener mListener!!.onError() // 在外部onError回调中调用reset()重置状态，并重置相关UI状态
            }
            true
        }

        // prepare完成后，才可以播放->start
        setOnPreparedListener {
            Log.i(TAG, "Prepared: status = $status")
            status = MediaPlayStatus.PREPARED

            // 取消转圈
            if (mListener != null) {
                mListener!!.onPrepared()
            }
        }
    }

    // 资源可能为空,或者太大，获取其他原因导致无法加载，所以加上try catch
    @Throws(IOException::class)
    fun prepare(audio: Music) {
        // 先保证恢复空闲状态
        reset()

        // 设置资源
        if (status == MediaPlayStatus.IDLE) {
            setDataSource(audio.path)
            status = MediaPlayStatus.INITIALIZED
        }

        // 准备
        if (status == MediaPlayStatus.INITIALIZED || status == MediaPlayStatus.STOPPED) {
            prepareAsync()
            status = MediaPlayStatus.PREPARING
        }
    }

    // 暂停播放。 调用 start() 恢复。
    override fun pause() {
        Log.i(TAG, "pause: status = $status")
        if (status == MediaPlayStatus.STARTED || status == MediaPlayStatus.PAUSED) {
            singleExecutor.execute {
                super.pause()
                status = MediaPlayStatus.PAUSED
            }
            // 停止刷新进度条
            stopTimer()
        }
    }

    // 播放开始或暂停后停止播放。
    // 调用 stop()停止播放并导致处于Started、Paused、Prepared 或PlaybackCompleted状态的 MediaPlayer进入 Stopped状态。
    // 一旦处于Stopped状态，播放不能开始，直到prepare()或被prepareAsync()调用以将 MediaPlayer 对象再次设置为Prepared状态。
    override fun stop() {
        Log.i(TAG, "stop: status = $status")
        if (status == MediaPlayStatus.PAUSED || status == MediaPlayStatus.STARTED || status == MediaPlayStatus.PREPARED || status == MediaPlayStatus.PLAYBACK_COMPLETED || status == MediaPlayStatus.STOPPED) {
            singleExecutor.execute {
                super.stop()
                status = MediaPlayStatus.STOPPED
            }
            stopTimer()
        }
    }

    // 开始或恢复播放。 如果之前已暂停播放，将从暂停的位置继续播放。 如果播放已停止或从未开始，播放将从头开始。
    // isPlaying()可以调用来测试MediaPlayer对象是否处于Started状态
    override fun start() {
        Log.i(TAG, "start: status = $status")
        if (status == MediaPlayStatus.PREPARED || status == MediaPlayStatus.STARTED || status == MediaPlayStatus.PAUSED || status == MediaPlayStatus.PLAYBACK_COMPLETED) {
            singleExecutor.execute {
                super.start()
                status = MediaPlayStatus.STARTED
            }
            startTimer()
        }
    }


    /**
     * @param position 毫秒单位的int
     */
    override fun seekTo(position: Int) {
        Log.i(TAG, "seekTo: status = $status")
        if (status == MediaPlayStatus.PREPARED || status == MediaPlayStatus.STARTED || status == MediaPlayStatus.PAUSED || status == MediaPlayStatus.PLAYBACK_COMPLETED) {
            singleExecutor.execute { super.seekTo(position) }
            if (mListener != null) {
                mListener!!.onSeekUpdate(position)
            }
        }
    }

    // 将 MediaPlayer 重置为其未初始化状态。 调用此方法后，您必须通过设置数据源并调用prepare() 来再次初始化它
    override fun reset() {
        Log.i(TAG, "reset: status = $status")
        super.reset()
        status = MediaPlayStatus.IDLE
    }

    // 開始計時
    private fun startTimer() {
        Log.i(TAG, "startTimer: ")
        mTimerHandler.postDelayed(timerRun, TIMER_SEEK.toLong())
    }

    // 停止计时
    private fun stopTimer() {
        Log.i(TAG, "stopTimer: ")
        mTimerHandler.removeCallbacks(timerRun)
    }

    // 释放与此 MediaPlayer 对象关联的资源。 使用完 MediaPlayer 后调用此方法被认为是一种很好的做法
    override fun release() {
        Log.i(TAG, "release: status = $status")
        singleExecutor.execute {
            super.release()
            status = MediaPlayStatus.END
        }
        stopTimer()
    }

    fun destroy() {
        release()
        mUiHandler.removeCallbacksAndMessages(null)
        mListener = null
    }

    // 播放状态监听
    interface OnMediaStateListener {
        // 准备完成
        fun onPrepared()

        /**
         * @param curTimeInt 毫秒单位的Int
         */
        fun onSeekUpdate(curTimeInt: Int)

        // 播放完成
        fun onCompletion()
        fun onError(): Boolean
    }

    private var mListener: OnMediaStateListener? = null
    fun setOnMediaStateListener(listener: OnMediaStateListener?) {
        mListener = listener
    }

    // MediaPlay生命周期在END和IDLE状态中间流转
    internal enum class MediaPlayStatus {
        // 空闲状态
        // 1.使用new创建->IDLE
        // 2.调用reset()->IDLE
        // 这两种方式的区别是：如果测试发生错误（调用getCurrentPosition()等方法），
        // 1方式不会回调方法 OnErrorListener.onError() 并且对象状态保持不变
        // 2方式会回调方法 OnErrorListener.onError()并且对象将转移到ERROR状态
        IDLE,

        // 初始化状态
        // 1.setDataSource()->INITIALIZED
        INITIALIZED,

        // 1.INITIALIZED->prepareAsync()->PREPARING
        // 2.STOPPED->prepareAsync()->PREPARING
        PREPARING,

        // MediaPlayer 对象必须先进入Prepared状态，然后才能开始播放
        // 1.PREPARING->OnPreparedListener.onPrepare()->PREPARED
        // 2.INITIALIZED->prepare()->PREPARED
        // 3.STOPPED->prepare()->PREPARED
        // 4.PREPARED->seekTo()->PREPARED
        PREPARED,

        // 1.PREPARED->start()->STARTED
        // 2.PAUSED->start()->STARTED
        // 3.STARTED->seekTo(),start()->STARTED
        // 4.PLAYBACK_COMPLETED&&setLooping(true)->STARTED
        // 5.PLAYBACK_COMPLETED->start()->STARTED
        STARTED,

        // 1.STARTED->pause()->PAUSED
        // 2.PAUSED->pause(),seekTo()->PAUSED
        PAUSED,

        // 1.STARTED->stop()->STOPPED
        // 2.PAUSED->stop()->PAUSE
        // 3.PLAYBACK_COMPLETED->stop()->PAUSE
        // 4.STOPPED->stop()->STOPPED
        // 5.PREPARED->stop()->STOPPED
        STOPPED,

        // 1.STARTED->PLAYBACK_COMPLETED&&setLooping(true)->PLAYBACK_COMPLETED
        // 1.PLAYBACK_COMPLETED->seekTo()->PLAYBACK_COMPLETED
        PLAYBACK_COMPLETED,

        // 播放完成，在此状态调用seekTo()
        // 1.onErrorListener.onError()
        ERROR,

        // onErrorListener.onError -> ERROR
        // release()->END
        // 一旦不再使用 MediaPlayer 对象，release()立即调用
        // 一旦 MediaPlayer 对象处于End状态，就不能再使用它，也无法将其恢复到任何其他状态。
        END //
    }

    companion object {
        private val TAG = ExtendedMediaPlayer::class.java.simpleName
        private const val HANDLER_PLAY_TIME = 1

        // 进度条状态刷新时间间隔
        private const val TIMER_SEEK = 20
    }
}
