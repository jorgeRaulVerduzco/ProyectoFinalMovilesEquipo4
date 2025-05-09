package com.example.closetvirtual

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class OutfitAdapter(
    private val onClick: (Outfits) -> Unit
) : ListAdapter<Outfits, OutfitAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_outfit, parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombre: TextView = itemView.findViewById(R.id.tvNombreOutfitItem)
        private val tvCount: TextView = itemView.findViewById(R.id.tvPrendasCount)

        fun bind(outfit: Outfits) {
            tvNombre.text = outfit.nombre
            tvCount.text = "${outfit.items.size} prendas"
            itemView.setOnClickListener { onClick(outfit) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Outfits>() {
            override fun areItemsTheSame(a: Outfits, b: Outfits) = a.nombre == b.nombre
            override fun areContentsTheSame(a: Outfits, b: Outfits) = a == b
        }
    }
}
