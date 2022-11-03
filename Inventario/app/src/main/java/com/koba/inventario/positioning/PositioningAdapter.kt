package com.koba.inventario.positioning

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.koba.inventario.R

class PositioningAdapter : RecyclerView.Adapter<PositioningAdapter.ViewHolder>() {

    private var pList: List<PositioningUiModel> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_view_items_positioning, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val itemsViewModel = pList[position]
        holder.amountTextView.text = itemsViewModel.amount.toString()
        holder.warehouseTextView.text = itemsViewModel.warehouse.toString()
        holder.floorTextView.text = itemsViewModel.position
    }

    override fun getItemCount(): Int {
        return pList.size
    }

    fun setData(list: List<PositioningUiModel>) {
        pList = list
        notifyDataSetChanged()
    }

    fun filterList(list: ArrayList<PositioningUiModel>) {
        this.pList = list
        notifyDataSetChanged()
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val amountTextView: TextView = itemView.findViewById(R.id.amountTextView)
        val warehouseTextView: TextView = itemView.findViewById(R.id.warehouseTextView)
        val floorTextView: TextView = itemView.findViewById(R.id.floorTextView)
    }

}