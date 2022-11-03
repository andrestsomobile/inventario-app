package com.koba.inventario.service

import com.koba.inventario.login.LoginResponse
import com.koba.inventario.pickup.RequisitionNumberResponse
import com.koba.inventario.pickup.RequisitionResponse
import com.koba.inventario.positioning.TrafficResponse
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface RequisitionService {
    @FormUrlEncoded
    @POST("listarPedidoMovil.do")
    fun getRequisitionsList(@Field("pedido" ) requisitionNumber: String? = null,
                            @Field("usuario" ) user: String? = null): Call<RequisitionResponse?>?

    @FormUrlEncoded
    @POST("finalizarPedidoMovil.do")
    fun finishRequisition(@Field("registroPedido") requisitionId: String? = null,
                          @Field("pedido") requisitionNumber: String? = null,
                          @Field("novedad") novelty: String? = null,
                          @Field("producto") productCode: String? = null,
                          @Field("posicion") position: String? = null): Call<ServiceResponse?>?

    @FormUrlEncoded
    @POST("registrarPedidoMovil.do")
    fun getRequisitionNumber(@Field("pedidos") requisitionNumber: String? = null,
                          @Field("usuario") user: String? = null): Call<RequisitionNumberResponse?>?

}