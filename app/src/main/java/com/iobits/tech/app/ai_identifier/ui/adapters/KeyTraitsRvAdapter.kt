package com.iobits.tech.app.ai_identifier.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.iobits.tech.app.ai_identifier.database.dataClasses.Fact
import com.iobits.tech.app.ai_identifier.databinding.ItemKeyTraitsBinding

class KeyTraitsRvAdapter(
    private val arrayList: ArrayList<Fact>
) : RecyclerView.Adapter<KeyTraitsRvAdapter.ModRvViewHolder>() {

    class ModRvViewHolder(val binding: ItemKeyTraitsBinding) : RecyclerView.ViewHolder(
        binding.root
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModRvViewHolder {
        val binding = ItemKeyTraitsBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ModRvViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ModRvViewHolder, position: Int) {

        val item = arrayList[position]
        holder.binding.title.text = replaceStarWithSpace(item.title)
        holder.binding.des.text = replaceStarWithSpace(item.description)

    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    private fun replaceStarWithSpace(input: String): String {
        return input.replace('*', ' ')
    }

}