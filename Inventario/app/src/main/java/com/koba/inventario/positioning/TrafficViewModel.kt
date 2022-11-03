package com.koba.inventario.positioning

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koba.inventario.ApiClient
import com.koba.inventario.database.AppDatabase
import com.koba.inventario.database.TrafficEntity
import com.koba.inventario.service.ServiceResponse
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response

class TrafficViewModel: ViewModel() {

    private lateinit var database: AppDatabase

    private val _createdResultLiveData = MutableLiveData(false)
    val createdLiveData: LiveData<Boolean> = _createdResultLiveData

    private val _syncResultLiveData = MutableLiveData(false)
    val syncLiveData: LiveData<Boolean> = _syncResultLiveData

    private val _trafficUpdateResultLiveData = MutableLiveData<Boolean?>(null)
    val trafficUpdateLiveData: LiveData<Boolean?> = _trafficUpdateResultLiveData

    private val _trafficServiceCreateResultLiveData = MutableLiveData<Boolean?>(null)
    val trafficServiceCreateLiveData: LiveData<Boolean?> = _trafficServiceCreateResultLiveData

    private val _trafficResultLiveData = MutableLiveData<String>()
    val trafficLiveData: LiveData<String> = _trafficResultLiveData

    fun setDataBase(database: AppDatabase) {
        this.database = database
    }

    fun findByUser(user: String) {
        viewModelScope.launch {
            val trafficSync = database.trafficDao().findTrafficByIndSync(1,user)
            _syncResultLiveData.value = trafficSync.isNotEmpty()
        }
    }

    fun findByTrafficCode(trafficCode: Int,user: String){
        viewModelScope.launch {
            val trafficUpdateProduct = database.trafficDao().findByTrafficCode(trafficCode,user)
            _trafficUpdateResultLiveData.value = trafficUpdateProduct.isEmpty()
        }
    }

    fun update(trafficCode: Int,user: String) {
        viewModelScope.launch {
            val trafficUpdateProduct = database.trafficDao().findByTrafficCode(trafficCode,user)
            val product: TrafficEntity = trafficUpdateProduct[0]
            database.trafficDao().update(
                TrafficEntity(
                    product.trafficId,trafficCode, user,1
            ))
            _createdResultLiveData.value = true
        }
    }

    fun create(trafficCode: Int, user: String, indSync: Int) {
        viewModelScope.launch {
            database.trafficDao().create(
                TrafficEntity(
                    0,trafficCode,user, indSync
                )
            )
            _createdResultLiveData.value = true
        }
    }

    fun createTraffic(trafficId: Int){
        viewModelScope.launch {
            var trafficMessage = ""
            val trafficResponseCall = ApiClient.trafficService.finishTraffic(trafficId)
            trafficResponseCall?.enqueue(object : retrofit2.Callback<ServiceResponse?> {
                override fun onResponse(
                    call: Call<ServiceResponse?>,
                    response: Response<ServiceResponse?>
                ) {
                    if (response.isSuccessful and response.body()?.status.equals("SUCESS")) {
                        trafficMessage = response.body()?.message.toString()
                        _trafficServiceCreateResultLiveData.value = true
                        _trafficResultLiveData.value = trafficMessage
                    } else {
                        trafficMessage =  response.body()?.message.toString()
                        _trafficServiceCreateResultLiveData.value = false
                        _trafficResultLiveData.value = trafficMessage
                    }
                }

                override fun onFailure(call: Call<ServiceResponse?>, t: Throwable) {
                    trafficMessage = "Fallo en el servicio, intente de nuevamente"
                    _trafficServiceCreateResultLiveData.value = false
                }
            })
        }
    }
}