package com.koba.inventario.service


import com.koba.inventario.positioning.PositioningResponse
import retrofit2.Call
import retrofit2.http.POST

interface PositioningService {
    @POST("listarEstanteriaMovil.do")
    fun getAllPositioning(): Call<PositioningResponse?>?

}