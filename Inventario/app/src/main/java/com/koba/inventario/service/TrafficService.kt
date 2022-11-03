package com.koba.inventario.service

import com.koba.inventario.login.LoginResponse
import com.koba.inventario.positioning.TrafficResponse
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface TrafficService {
    @POST("getTraficoMovil.do")
    fun getAllTraffics(): Call<TrafficResponse?>?

    @FormUrlEncoded
    @POST("finalizarIngresoMovil.do")
    fun finishTraffic(@Field("trafico") trafficId: Int? = null): Call<ServiceResponse?>?

}