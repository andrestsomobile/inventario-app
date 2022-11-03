package com.koba.inventario.positioning

data class PositioningUiModel(var amount : Int? = null, var warehouse : Int? = null,
                              var position : String? = null) {
    fun searchableText(): String {
        return "${amount?.toString().orEmpty()}${warehouse.toString().orEmpty()}" +
                "${position.orEmpty()}"
    }
}
