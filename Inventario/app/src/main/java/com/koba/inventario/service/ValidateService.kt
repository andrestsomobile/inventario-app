package com.koba.inventario.service

import com.koba.inventario.login.LoginResponse
import com.koba.inventario.positioning.TrafficResponse
import com.koba.inventario.report.InventoryResponse
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ValidateService {
    @FormUrlEncoded
    @POST("crearInventarioMovil.do")
    fun finishValidate(@Field("referencia") barcodeProduct: String? = null,
                       @Field("bodega") location: String? = null,
                       @Field("usuario") user: String? = null,
                       @Field("terminal") terminal: String? = null,
                       @Field("conteo") amount: String? = null,
                       @Field("id") id: String? = null): Call<ServiceResponse?>?

    @POST("listarInventarioMovil.do")
    fun listInventory(): Call<InventoryResponse?>?
}