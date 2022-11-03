package com.koba.inventario.service

import com.koba.inventario.login.LoginResponse
import retrofit2.http.POST
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded

interface UserService {
    @FormUrlEncoded
    @POST("validarUsuarioMovil.do")
    fun userLogin(@Field("login") login: String? = null, @Field("clave") clave: String? = null): Call<LoginResponse?>?
    //fun userLogin(@Body loginRequestBody: LoginRequestBody?): Call<LoginResponse?>?
}