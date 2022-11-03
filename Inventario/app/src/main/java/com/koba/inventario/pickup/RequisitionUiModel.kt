package com.koba.inventario.pickup

data class RequisitionUiModel(var user : String? = null, var client : String? = null,
                              var requisition : String? = null, var unitTotal: Int? = null,
                              var status: Boolean = false,
                              var requisitionList: List<RequisitionObjectResponse>? = null ) {
    fun searchableText(): String {
        return "${unitTotal?.toString().orEmpty()}${requisition.orEmpty()}" +
                "${client.orEmpty()}"
    }
}
