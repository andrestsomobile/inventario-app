package com.koba.inventario.positioning

data class TrafficUiModel(var description : String? = null, var trafficId : Int? = null){

    override fun toString(): String {
        return this.description ?: ""
    }

}
