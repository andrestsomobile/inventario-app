package com.koba.inventario

import com.koba.inventario.service.*
import retrofit2.Retrofit
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.OkHttpClient
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private val retrofit: Retrofit
        get() {
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            val okHttpClient: OkHttpClient =
                OkHttpClient.Builder().addInterceptor(httpLoggingInterceptor).build()
            return Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("http://34.195.71.159:8085/sglkoba/")
                //.baseUrl("http://10.1.54.245:8080/sglpruebas/")
                .client(okHttpClient)
                .build()
        }
    val userService: UserService
        get() = retrofit.create(UserService::class.java)
    val trafficService: TrafficService
        get() = retrofit.create(TrafficService::class.java)
    val positionService: PositionService
        get() = retrofit.create(PositionService::class.java)
    val requisitionService: RequisitionService
        get() = retrofit.create(RequisitionService::class.java)
    val relocationService: RelocationService
        get() = retrofit.create(RelocationService::class.java)
    val positioningService: PositioningService
        get() = retrofit.create(PositioningService::class.java)
    val validateService: ValidateService
        get() = retrofit.create(ValidateService::class.java)
}