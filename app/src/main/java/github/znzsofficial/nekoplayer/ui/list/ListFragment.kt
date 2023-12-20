package github.znzsofficial.nekoplayer.ui.list

import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ObservableList
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import github.znzsofficial.nekoplayer.MainActivity
import github.znzsofficial.nekoplayer.Music
import github.znzsofficial.nekoplayer.SharedViewModel
import github.znzsofficial.nekoplayer.adapter.MusicListAdapter
import github.znzsofficial.nekoplayer.databinding.FragmentListBinding
import github.znzsofficial.utils.ExtendedMediaPlayer

class ListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null
    private lateinit var mActivity: MainActivity

    /**
     * dataSource 应当始终为 currentMusic
     */
    private lateinit var retriever: MediaMetadataRetriever
    private lateinit var mediaPlayer: ExtendedMediaPlayer
    private val sharedViewModel: SharedViewModel by activityViewModels()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val listViewModel =
            ViewModelProvider(this)[ListViewModel::class.java]

        _binding = FragmentListBinding.inflate(inflater, container, false)

        mActivity = activity as MainActivity
        retriever = mActivity.retriever
        mediaPlayer = mActivity.mediaPlayer

        //listViewModel.text.observe(viewLifecycleOwner) { }

        val musicList = sharedViewModel.musicList
        val adapter = MusicListAdapter()
        binding.musicList.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(mActivity)
        }
        adapter.submitList(musicList)


        musicList.addOnListChangedCallback(
            object : ObservableList.OnListChangedCallback<ObservableList<Music>>() {
                override fun onChanged(sender: ObservableList<Music>?) {
                    // 列表整体发生变化时触发
                    adapter.submitList(musicList)
                }

                override fun onItemRangeChanged(
                    sender: ObservableList<Music>?,
                    positionStart: Int,
                    itemCount: Int
                ) {
                    // 单个或多个元素发生变化时触发
                    adapter.submitList(musicList)
                }

                override fun onItemRangeInserted(
                    sender: ObservableList<Music>?,
                    positionStart: Int,
                    itemCount: Int
                ) {
                    // 元素插入时触发
                    adapter.submitList(musicList)
                }

                override fun onItemRangeMoved(
                    sender: ObservableList<Music>?,
                    fromPosition: Int,
                    toPosition: Int,
                    itemCount: Int
                ) {
                    // 元素移动时触发
                    adapter.submitList(musicList)
                }

                override fun onItemRangeRemoved(
                    sender: ObservableList<Music>?,
                    positionStart: Int,
                    itemCount: Int
                ) {
                    // 元素删除时触发
                    adapter.submitList(musicList)
                }
            })

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        val TAG: String = ListFragment::class.java.simpleName
    }
}