package com.koba.inventario.positioning

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koba.inventario.ApiClient
import com.koba.inventario.database.AppDatabase
import com.koba.inventario.database.PositionEntity
import com.koba.inventario.service.ServiceResponse
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response

class PositionViewModel: ViewModel() {

    private lateinit var database: AppDatabase
    private val _createdResultLiveData = MutableLiveData(false)
    val createdLiveData: LiveData<Boolean> = _createdResultLiveData

    private val _syncResultLiveData = MutableLiveData(false)
    val syncLiveData: LiveData<Boolean> = _syncResultLiveData

    private val _positionUpdateResultLiveData = MutableLiveData<Boolean?>(null)
    val positionUpdateLiveData: LiveData<Boolean?> = _positionUpdateResultLiveData

    private val _trafficResultLiveData = MutableLiveData<List<TrafficUiModel>>(listOf())
    val trafficLiveData: LiveData<List<TrafficUiModel>> = _trafficResultLiveData

    private val _validateTrafficLiveData = MutableLiveData(false)
    val validateTrafficLiveData: LiveData<Boolean> = _validateTrafficLiveData

    private val _positionServiceCreateResultLiveData = MutableLiveData<Boolean?>(null)
    val positionServiceCreateLiveData: LiveData<Boolean?> = _positionServiceCreateResultLiveData

    private val _positionResultLiveData = MutableLiveData<String>()
    val positionLiveData: LiveData<String> = _positionResultLiveData

    fun setDataBase(database: AppDatabase) {
        this.database = database
    }

    fun findByUser(user: String) {
        viewModelScope.launch {
            val positionSync = database.positionDao().findPositionByIndSync(1,user)

            positionSync.forEach {
                createPositionService(it.trafficId, it.barcodeProduct,it.barcodeLocation,it.amount,user )
            }

            _syncResultLiveData.value = positionSync.isNotEmpty()
        }
    }

    fun findByProduct(barcodeProduct: String,barcodeLocation: String,user: String){
        viewModelScope.launch {
            val positionUpdateProduct = database.positionDao().findByBarcodeProduct(barcodeProduct,barcodeLocation,user)
            _positionUpdateResultLiveData.value = positionUpdateProduct.isEmpty()
        }
    }

    fun update(barcodeProduct: String,barcodeLocation: String, user: String,amount: Int,trafficId: Int) {
        viewModelScope.launch {
            val positionUpdateProduct = database.positionDao().findByBarcodeProduct(barcodeProduct,barcodeLocation,user)

            if(positionUpdateProduct != null && positionUpdateProduct.isNotEmpty()) {
                val product: PositionEntity = positionUpdateProduct[0]
                database.positionDao().update(
                    PositionEntity(
                        product.positionId,barcodeProduct,barcodeLocation, user,amount,1,trafficId
                    ))
            } else {
                database.positionDao().create(
                    PositionEntity(
                        0,barcodeProduct,barcodeLocation, user,amount, 1,trafficId
                    )
                )
            }
            _createdResultLiveData.value = true

        }
    }

    fun create(barcodeProduct: String,barcodeLocation: String, user: String, indSync: Int,amount: Int,trafficId: Int) {
        viewModelScope.launch {
            database.positionDao().create(
                PositionEntity(
                    0,barcodeProduct,barcodeLocation, user,amount, indSync,trafficId
                )
            )
            _createdResultLiveData.value = true
        }
    }

    fun findAllTraffics(){
        viewModelScope.launch {
            val trafficList = ArrayList<TrafficUiModel>()
            trafficList.apply {
                add(TrafficUiModel("Seleccione", 0))
            }
            val trafficResponseCall = ApiClient.trafficService.getAllTraffics()
            trafficResponseCall?.enqueue(object : retrofit2.Callback<TrafficResponse?> {
                override fun onResponse(
                    call: Call<TrafficResponse?>,
                    response: Response<TrafficResponse?>
                ) {
                    if (response.isSuccessful and response.body()?.status.equals("SUCESS")) {
                        for (t in response.body()?.data!!) {
                            trafficList.apply {
                                add(TrafficUiModel(t.trafpedido.toString(), t.trafcodsx))
                            }
                        }
                        _trafficResultLiveData.value = trafficList
                    } else {
                        _validateTrafficLiveData.value = true
                    }
                }

                override fun onFailure(call: Call<TrafficResponse?>, t: Throwable) {
                    _validateTrafficLiveData.value = true
                }
            })
        }
    }

    fun createPositionService(trafficId: Int,barcodeProduct: String,barcodeLocation: String,amount: Int,user: String){
        viewModelScope.launch {
            var positionMessage = ""
            val positionResponseCall = ApiClient.positionService.finishPosition(trafficId,barcodeProduct,barcodeLocation,amount,user)
            positionResponseCall?.enqueue(object : retrofit2.Callback<ServiceResponse?> {
                override fun onResponse(
                    call: Call<ServiceResponse?>,
                    response: Response<ServiceResponse?>
                ) {
                    if (response.isSuccessful and response.body()?.status.equals("SUCESS")) {
                        positionMessage = response.body()?.message.toString()
                        _positionServiceCreateResultLiveData.value = true
                        _positionResultLiveData.value = positionMessage
                    } else {
                        positionMessage =  response.body()?.message.toString()
                        _positionServiceCreateResultLiveData.value = false
                        _positionResultLiveData.value = positionMessage
                    }
                }

                override fun onFailure(call: Call<ServiceResponse?>, t: Throwable) {
                    positionMessage = "Fallo en el servicio, intente de nuevamente"
                    _positionServiceCreateResultLiveData.value = false
                }
            })
        }
    }
}