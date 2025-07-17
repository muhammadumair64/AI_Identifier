package com.iobits.tech.app.ai_identifier.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.iobits.tech.app.ai_identifier.R
import com.iobits.tech.app.ai_identifier.databinding.ItemProfilePostsRvBinding
import com.iobits.tech.app.ai_identifier.utils.setSafeOnClickListener

class ImagesAdapter(private val arrayList: List<Uri>, private val context: Context, private val galleryRvClick: (String)-> Unit) :
    RecyclerView.Adapter<ImagesAdapter.RvViewHolder>() {
    class RvViewHolder(val binding: ItemProfilePostsRvBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RvViewHolder =
        RvViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_profile_posts_rv,
                parent, false
            )
        )

    override fun getItemCount(): Int {
        return arrayList.size
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: RvViewHolder, position: Int) {
        val items = arrayList[position]
//        holder.binding.postImg.setImageDrawable(items)
        Glide.with(context)
            .load(items)
            .into(holder.binding.postImg)

        holder.binding.root.setSafeOnClickListener {
            Log.d("CHECK_VALUE", "onBindViewHolder: ${items.toString()}")

            galleryRvClick.invoke(items.toString())
        }

    }
}