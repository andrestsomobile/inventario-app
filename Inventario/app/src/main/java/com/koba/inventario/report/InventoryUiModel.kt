package com.koba.inventario.report

data class InventoryUiModel(var description : String? = null, var id : Int? = null){

    override fun toString(): String {
        return this.description ?: ""
    }

}
