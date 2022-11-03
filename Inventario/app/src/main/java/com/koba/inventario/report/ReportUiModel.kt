package com.koba.inventario.report

data class ReportUiModel(var user : String? = null, var productId : Int? = null,
                         var warehouse : String? = null, var floor : String? = null,
                         var side : String? = null, var amount : Int? = null  ) {
    fun searchableText () :String {
        return "${productId?.toString().orEmpty()}${warehouse.orEmpty()}" +
                "${floor.orEmpty()}${side.orEmpty()}${amount?.toString().orEmpty()}"
    }
}
