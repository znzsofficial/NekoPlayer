package github.znzsofficial.utils

import android.content.Context
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

class ImageLoader(context: Context) {

    private val options = RequestOptions()
        .skipMemoryCache(true)
        .diskCacheStrategy(DiskCacheStrategy.NONE)

    lateinit var requestBuilder: RequestBuilder<Drawable>

    init {
        requestBuilder = Glide.with(context).asDrawable().apply(options)
    }


}