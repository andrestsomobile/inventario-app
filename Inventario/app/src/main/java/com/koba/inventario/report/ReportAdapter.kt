package com.koba.inventario.report

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.koba.inventario.R

class ReportAdapter : RecyclerView.Adapter<ReportAdapter.ViewHolder>() {

    private var pList : List<ReportUiModel> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_view_items_report, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val itemsViewModel = pList[position]
        holder.productIdTextView.text = itemsViewModel.productId.toString()
        holder.warehouseTextView.text = itemsViewModel.warehouse
        holder.floorTextView.text = itemsViewModel.floor
        holder.sideTextView.text = itemsViewModel.side
        holder.amountTextView.text = itemsViewModel.amount.toString()

    }

    override fun getItemCount(): Int {
        return pList.size
    }

    fun setData(list: List<ReportUiModel>) {
        pList = list
        notifyDataSetChanged()
    }

    fun filterList(list: ArrayList<ReportUiModel>) {
        this.pList = list
        notifyDataSetChanged()
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val productIdTextView: TextView = itemView.findViewById(R.id.productIdTextView)
        val warehouseTextView: TextView = itemView.findViewById(R.id.warehouseTextView)
        val floorTextView: TextView = itemView.findViewById(R.id.floorTextView)
        val sideTextView: TextView = itemView.findViewById(R.id.sideTextView)
        val amountTextView: TextView = itemView.findViewById(R.id.amountTextView)
    }

}