package com.example.closetvirtual

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class PrendaSelectionAdapter : ListAdapter<Prenda, PrendaSelectionAdapter.ViewHolder>(DIFF) {
    private val selected = mutableSetOf<Prenda>()

    fun getSelectedItems(): List<Prenda> = selected.toList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_prenda_seleccionable, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivPrenda: ImageView = itemView.findViewById(R.id.ivPrenda)

        fun bind(prenda: Prenda) {
            Glide.with(ivPrenda).load(prenda.imagen).into(ivPrenda)
            itemView.alpha = if (selected.contains(prenda)) 0.5f else 1f
            itemView.setOnClickListener {
                if (selected.contains(prenda)) selected.remove(prenda) else selected.add(prenda)
                itemView.alpha = if (selected.contains(prenda)) 0.5f else 1f
            }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Prenda>() {
            override fun areItemsTheSame(a: Prenda, b: Prenda) = a.id == b.id
            override fun areContentsTheSame(a: Prenda, b: Prenda) = a == b
        }
    }
}