package com.koba.inventario.pickup

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.koba.inventario.R

class RequisitionAdapter : RecyclerView.Adapter<RequisitionAdapter.ViewHolder>() {

    private var pList: List<RequisitionUiModel> = ArrayList()
    private lateinit var mListener :OnItemClickListener

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener){
        this.mListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_view_items_requisition, parent, false)
        return ViewHolder(view,mListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val itemsViewModel = pList[position]
        holder.clientTextView.text = itemsViewModel.client
        holder.requisitionTextView.text = itemsViewModel.requisition
        holder.unitTotalTextView.text = itemsViewModel.unitTotal.toString()
        holder.imageStatus.isEnabled = itemsViewModel.status
    }

    override fun getItemCount(): Int {
        return pList.size
    }

    fun setData(list: List<RequisitionUiModel>) {
        this.pList = list
        notifyDataSetChanged()
    }

    fun filterList(list: ArrayList<RequisitionUiModel>) {
        this.pList = list
        notifyDataSetChanged()
    }

    class ViewHolder(ItemView: View, listener: OnItemClickListener) : RecyclerView.ViewHolder(ItemView) {
        val clientTextView: TextView = itemView.findViewById(R.id.clientTextView)
        val requisitionTextView: TextView = itemView.findViewById(R.id.requisitionTextView)
        val unitTotalTextView: TextView = itemView.findViewById(R.id.unitTotalTextView)
        val imageStatus : ImageView = itemView.findViewById(R.id.iconStatus)

        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }

}