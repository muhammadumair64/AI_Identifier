package com.iobits.tech.app.ai_identifier.ui.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.iobits.tech.app.ai_identifier.MyApplication
import com.iobits.tech.app.ai_identifier.database.dataClasses.MainRvDataClass
import com.iobits.tech.app.ai_identifier.databinding.ItemAllObjectsRvBinding
import com.iobits.tech.app.ai_identifier.databinding.ItemAnimalsRvBinding
import com.iobits.tech.app.ai_identifier.databinding.ItemMainRvBinding
import com.iobits.tech.app.ai_identifier.manager.PreferenceManager
import com.iobits.tech.app.ai_identifier.ui.activities.PremiumProActivity
import com.iobits.tech.app.ai_identifier.utils.setSafeOnClickListener

class AllObjectsRvAdapter(
    private val context: Context,
    private val arrayList: ArrayList<MainRvDataClass>,
    private val onClickItem: (String)-> Unit,
    private val onPremium: (String)-> Unit
) : RecyclerView.Adapter<AllObjectsRvAdapter.ModRvViewHolder>() {

    class ModRvViewHolder(val binding: ItemAllObjectsRvBinding) : RecyclerView.ViewHolder(
        binding.root
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModRvViewHolder {
        val binding = ItemAllObjectsRvBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ModRvViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ModRvViewHolder, position: Int) {

        val item = arrayList[position]
        holder.binding.animalImg.setImageDrawable(ContextCompat.getDrawable(context, item.imgDrawableId))
        holder.binding.arrow.setImageDrawable(item.arrowDrawableId?.let {
            ContextCompat.getDrawable(context,
                it
            )
        })
        holder.binding.titleName.text = item.title
        holder.binding.description.text = item.desc
//        holder.binding.scanFreeCount.text =  "${item.freeScanCount} Free"
        if (MyApplication.mInstance?.preferenceManager?.getBoolean(PreferenceManager.Key.IS_APP_PREMIUM,false) == true){
//            holder.binding.freeCountCard.visibility = View.GONE
        }
        // Set the background tint using a color resource
        holder.binding.mainCards.backgroundTintList = ContextCompat.getColorStateList(context, item.bgColorId)
        holder.binding.root.setSafeOnClickListener {
            if (MyApplication.mInstance?.preferenceManager?.getBoolean(PreferenceManager.Key.IS_APP_PREMIUM,false) == true){
                onClickItem.invoke(item.typeIdentify)
            }else{
                if (item.freeScanCount < 1) onPremium.invoke(item.typeIdentify)
                else onClickItem.invoke(item.typeIdentify)
            }
        }
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }
}