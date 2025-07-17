package com.iobits.tech.app.ai_identifier.ui.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.recyclerview.widget.RecyclerView
import com.iobits.tech.app.ai_identifier.R
import com.iobits.tech.app.ai_identifier.database.dataClasses.Collection
import com.iobits.tech.app.ai_identifier.databinding.ItemHistoryBinding
import com.iobits.tech.app.ai_identifier.utils.setSafeOnClickListener

class HistoryRvAdapter(
    private val context: Context,
    private val arrayList: List<Collection>,
    private val onClickItem: (img: String, stringToSplit: String, String) -> Unit,
    private val onClickToDlt: (itemID: Int) -> Unit,
    private val onClickToShare: (imgUrl: String, title: String) -> Unit
) : RecyclerView.Adapter<HistoryRvAdapter.ModRvViewHolder>() {

    class ModRvViewHolder(val binding: ItemHistoryBinding) : RecyclerView.ViewHolder(
        binding.root
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModRvViewHolder {
        val binding = ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ModRvViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ModRvViewHolder, position: Int) {

        val item = arrayList[position]
        holder.binding.imgView.setImageURI(Uri.parse(item.image))
        holder.binding.title.text = item.title
        holder.binding.desc.text = item.description

        holder.binding.root.setSafeOnClickListener {
            onClickItem.invoke(item.image, item.stringToSplit, item.detect)
        }
        holder.binding.menu.setSafeOnClickListener {
//            onClickToDlt.invoke(item.id)
            val inflater = LayoutInflater.from(context)
            val popupView = inflater.inflate(R.layout.history_menu_layout, null)

            val displayMetrics = context.resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels / 2

            val popupWindow = PopupWindow(popupView, screenWidth, ViewGroup.LayoutParams.WRAP_CONTENT, true)
            popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // Required for background to show correctly
            popupWindow.elevation = 10f

// Show the popup below the clicked view
            popupWindow.showAsDropDown(holder.binding.menu, 0, 0)

// Set click listeners
            popupView.findViewById<LinearLayout>(R.id.view).setOnClickListener {
                onClickItem.invoke(item.image, item.stringToSplit, item.detect)
                popupWindow.dismiss()
            }

            popupView.findViewById<LinearLayout>(R.id.share).setOnClickListener {
                // handle share here
                onClickToShare.invoke(item.image,item.title)
                popupWindow.dismiss()
            }

            popupView.findViewById<LinearLayout>(R.id.delete).setOnClickListener {
                onClickToDlt.invoke(item.id)
                popupWindow.dismiss()
            }


        }

    }

    override fun getItemCount(): Int {
        return arrayList.size
    }
}