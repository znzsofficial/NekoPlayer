package github.znzsofficial.nekoplayer.adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestBuilder
import github.znzsofficial.nekoplayer.R
import github.znzsofficial.nekoplayer.data.Music
import github.znzsofficial.nekoplayer.databinding.ItemLayoutBinding

class MusicListAdapter : ListAdapter<Music, MusicListAdapter.ViewHolder>(MusicListDiffCallback()) {
    lateinit var requestBuilder: RequestBuilder<Drawable>

    fun setImageLoader(builder: RequestBuilder<Drawable>) {
        requestBuilder = builder
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class ViewHolder(private val binding: ItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Music) {
            binding.itemContent.setOnClickListener{

            }
            binding.title.text = item.title
            binding.author.text = item.author
            if (item.picture.isEmpty())
                binding.pic.setImageResource(R.drawable.music_note_24px)
            else
                requestBuilder.load(item.picture).into(binding.pic)
        }
    }
}

class MusicListDiffCallback : DiffUtil.ItemCallback<Music>() {

    override fun areItemsTheSame(oldItem: Music, newItem: Music): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Music, newItem: Music): Boolean {
        return oldItem == newItem
    }
}
