package github.znzsofficial.nekoplayer.ui.home

import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.PowerManager
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestBuilder
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import github.znzsofficial.nekoplayer.MainActivity
import github.znzsofficial.nekoplayer.SharedViewModel
import github.znzsofficial.nekoplayer.databinding.FragmentHomeBinding
import github.znzsofficial.utils.ConvertUtil
import github.znzsofficial.utils.ExtendedMediaPlayer
import github.znzsofficial.utils.TimeUtil
import java.io.IOException

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private lateinit var mActivity: MainActivity
    private lateinit var requestBuilder: RequestBuilder<Drawable>

    /**
     * dataSource 应当始终为 currentMusic
     */
    private lateinit var retriever: MediaMetadataRetriever
    private lateinit var mediaPlayer: ExtendedMediaPlayer

    private val sharedViewModel: SharedViewModel by activityViewModels()

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this)[HomeViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        mActivity = activity as MainActivity
        requestBuilder = mActivity.requestBuilder
        retriever = mActivity.retriever
        mediaPlayer = mActivity.mediaPlayer


        // 检测viewModel的变化，如果数据变化就在这里更新视图
        homeViewModel.progressNum.observe(viewLifecycleOwner) {
            binding.progress.value = it
            (TimeUtil.toMinSec(it.toLong()) + " / " + TimeUtil.toMinSec(
                sharedViewModel.currentMusic?.length ?: 0
            ))
                .let { str ->
                    binding.mTextState.text = str
                }
        }
        homeViewModel.progressEnabled.observe(viewLifecycleOwner) {
            binding.progress.isEnabled = it
        }
        homeViewModel.pictureArray.observe(viewLifecycleOwner) {
            requestBuilder.load(it).into(binding.mImg)
        }
        homeViewModel.inputPath.observe(viewLifecycleOwner) {
            if (it != binding.edit.text.toString())
                binding.edit.setText(it)
        }
        // 点击fab后向音乐列表里添加 EditText 里的路径对应的音乐
        binding.add.setOnClickListener {
            sharedViewModel.addMusic(homeViewModel.inputPath.value.toString(), retriever)
        }
        
        binding.edit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 当用户在 EditText 中输入或修改文本时，更新 ViewModel 中的数据
                val newText = s.toString()
                homeViewModel.updateInputPath(newText)
            }
        })

        binding.progress.apply {
            addOnChangeListener { _, value, fromUser ->
                // 如果是用户滑动
                if (fromUser) {
                    // seekTo需要传毫秒
                    mediaPlayer.seekTo(value.toInt() * 1000)
                    // 通知更新 viewModel 内的数据
                    homeViewModel.updateProgress(value)
                }
            }
            setLabelFormatter { v -> TimeUtil.toMinSec(v.toLong()) }
        }

        binding.btnPlay.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
            } else {
                mediaPlayer.start()
            }
        }

        binding.btnSelect.setOnClickListener {
            try {
                val music = sharedViewModel.currentMusic
                if (music == null) {
                    mActivity.sendMsg("列表中没有文件")
                    return@setOnClickListener
                }
                mediaPlayer.prepare(music)
                binding.progress.valueTo = music.length.toFloat()
                // 设置进度条
                homeViewModel.updateProgressEnabled(true)
                homeViewModel.updatePictureArray(music.picture)
                homeViewModel.updateProgress(0.0f)
            } catch (e: IOException) {
                mActivity.sendMsg(e.message.toString())
            }
        }

        binding.btnInfo.setOnClickListener {
            MaterialAlertDialogBuilder(mActivity)
                .setTitle("音频信息")
                .setMessage(ConvertUtil.getInfoOf(retriever))
                .setPositiveButton(android.R.string.ok, null)
                .show()
        }

        binding.loopSwitch.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            sharedViewModel.updateLoopState(isChecked)
        }

        mediaPlayer.setOnMediaStateListener(object : ExtendedMediaPlayer.OnMediaStateListener {
            override fun onPrepared() {
                mediaPlayer
                    .setWakeMode(activity?.applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
                mediaPlayer.start()
            }

            override fun onSeekUpdate(curTimeInt: Int) {
                homeViewModel.updateProgress((curTimeInt / 1000).toFloat())
            }

            override fun onCompletion() {
            }

            override fun onError(): Boolean {
                mediaPlayer.reset()
                // 重置UI状态
                return true
            }
        })
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}