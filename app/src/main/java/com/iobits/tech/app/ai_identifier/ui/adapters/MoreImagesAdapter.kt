package com.iobits.tech.app.ai_identifier.ui.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.facebook.shimmer.ShimmerFrameLayout
import com.iobits.tech.app.ai_identifier.R
import com.iobits.tech.app.ai_identifier.network.models.ImageData
import com.iobits.tech.app.ai_identifier.utils.setSafeOnClickListener


class MoreImagesAdapter(private val context: Context, private val imageList: List<ImageData>, private val onImageClicked: (String)-> Unit) :
    RecyclerView.Adapter<MoreImagesAdapter.PremiumViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PremiumViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_more_images_rv, parent, false)
        return PremiumViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PremiumViewHolder, position: Int) {
        val currentItem = imageList[position]
//        val keywords = listOf("insect", "spider")
//        val containsKeyword = keywords.any { keyword -> currentItem.tags.contains(keyword, ignoreCase = true) }
//        if (!containsKeyword) return
        // Start shimmer
        holder.shimmerLayout.startShimmer()

        // Load the image with Glide
        Glide.with(context)
            .load(currentItem.largeImageURL)
            .apply(RequestOptions().placeholder(R.drawable.no_item_bg))
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<Drawable?>,
                    isFirstResource: Boolean
                ): Boolean {
                    // Stop shimmer on failure
                    holder.shimmerLayout.stopShimmer()
                    holder.shimmerLayout.hideShimmer()
                    return false // Pass the error to Glide
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: com.bumptech.glide.request.target.Target<Drawable?>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    // Stop shimmer when the image is loaded
                    holder.shimmerLayout.stopShimmer()
                    holder.shimmerLayout.hideShimmer()
                    return false // Pass the resource to the ImageView
                }
            })
            .into(holder.image)

        holder.itemView.setSafeOnClickListener{
            onImageClicked.invoke(currentItem.largeImageURL)
        }
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    inner class PremiumViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.image)
        val shimmerLayout: ShimmerFrameLayout = itemView.findViewById(R.id.shimmerLayout)

    }
}