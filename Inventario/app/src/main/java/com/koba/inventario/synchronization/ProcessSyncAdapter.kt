package com.koba.inventario.synchronization

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.koba.inventario.R

class ProcessSyncAdapter(private var pList: List<ProcessSyncUiModel>) : RecyclerView.Adapter<ProcessSyncAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_view_items_process_sync, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val itemsViewModel = pList[position]
        holder.imageView.isEnabled = false
        if(itemsViewModel.status == 1){
            holder.imageView.isEnabled = true
        }
        holder.textView.text = itemsViewModel.name
        holder.itemView.setOnClickListener { v ->
            if (v != null) {
                if(!holder.imageView.isEnabled) {
                    holder.imageView.isEnabled = true
                    Toast.makeText(v.context, "Proceso Actualizado", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun setItemsRecyclerView(list: List<ProcessSyncUiModel>) {
        this.pList = list
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return pList.size
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val imageView: ImageView = itemView.findViewById(R.id.icon)
        val textView: TextView = itemView.findViewById(R.id.processView)
    }

}