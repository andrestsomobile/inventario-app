package com.koba.inventario.service

import com.koba.inventario.login.LoginResponse
import com.koba.inventario.positioning.TrafficResponse
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface RelocationService {
    @FormUrlEncoded
    @POST("reubicacionMovil.do")
    fun finishRelocation(@Field("producto") barcodeProduct: String? = null,
                       @Field("origen") barcodeOrigin: String? = null,
                       @Field("destino") barcodeDestination: String? = null,
                       @Field("cantidad") amount: Int? = null): Call<ServiceResponse?>?
}