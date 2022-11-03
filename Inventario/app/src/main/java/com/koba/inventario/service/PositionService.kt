package com.koba.inventario.service

import com.koba.inventario.login.LoginResponse
import com.koba.inventario.positioning.TrafficResponse
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface PositionService {
    @FormUrlEncoded
    @POST("entradaMovil.do")
    fun finishPosition(@Field("trafico") trafficId: Int? = null,@Field("codigoBarra") barcodeProduct: String? = null,
                       @Field("ubicacion") barcodeLocation: String? = null,@Field("cantidad") amount: Int? = null,
                       @Field("usuario") user: String? = null): Call<ServiceResponse?>?
}