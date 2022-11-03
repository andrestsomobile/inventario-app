package com.koba.inventario.relocation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koba.inventario.ApiClient
import com.koba.inventario.database.AppDatabase
import com.koba.inventario.database.RelocationEntity
import com.koba.inventario.service.ServiceResponse
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response

class RelocationViewModel: ViewModel() {

    private lateinit var database: AppDatabase
    private val _createdResultLiveData = MutableLiveData(false)
    val createdLiveData: LiveData<Boolean> = _createdResultLiveData

    private val _syncResultLiveData = MutableLiveData(false)
    val syncLiveData: LiveData<Boolean> = _syncResultLiveData

    private val _relocationUpdateResultLiveData = MutableLiveData<Boolean?>(null)
    val relocationUpdateLiveData: LiveData<Boolean?> = _relocationUpdateResultLiveData

    private val _relocationServiceCreateResultLiveData = MutableLiveData<Boolean?>(null)
    val relocationServiceCreateLiveData: LiveData<Boolean?> = _relocationServiceCreateResultLiveData

    private val _relocationResultLiveData = MutableLiveData<String>()
    val relocationLiveData: LiveData<String> = _relocationResultLiveData

    fun setDataBase(database: AppDatabase) {
        this.database = database
    }

    fun findByUser(user: String) {
        viewModelScope.launch {
            val relocationSync = database.relocationDao().findRelocationByIndSync(1,user)
            _syncResultLiveData.value = relocationSync.isNotEmpty()
        }
    }

    fun findByProduct(barcodeProduct: String,barcodeOrigin: String,barcodeDestination: String,user: String){
        viewModelScope.launch {
            val incomeUpdateProduct = database.relocationDao().findByBarcodeProduct(barcodeProduct,barcodeOrigin,barcodeDestination,user)
            _relocationUpdateResultLiveData.value = incomeUpdateProduct.isNotEmpty()
        }
    }

    fun update(barcodeProduct: String,barcodeOrigin: String,barcodeDestination: String, user: String,amount: Int) {
        viewModelScope.launch {
            val relocationUpdateProduct = database.relocationDao().findByBarcodeProduct(barcodeProduct,barcodeOrigin,barcodeDestination,user)
            val product: RelocationEntity = relocationUpdateProduct[0]
            database.relocationDao().update(
                RelocationEntity(
                    product.relocationId,barcodeProduct,barcodeOrigin,barcodeDestination,user,amount,1
            ))
            _createdResultLiveData.value = true
        }
    }

    fun create(barcodeProduct: String,barcodeOrigin: String,barcodeDestination: String, user: String, indSync: Int,amount: Int) {
        viewModelScope.launch {
            database.relocationDao().create(
                RelocationEntity(
                    0,barcodeProduct,barcodeOrigin,barcodeDestination,user,amount,indSync
                )
            )
            _createdResultLiveData.value = true
        }
    }

    fun createRelocationService(barcodeProduct: String,barcodeOrigin: String,barcodeDestination: String, amount: Int){
        viewModelScope.launch {
            var relocationMessage = ""
            val relocationResponseCall = ApiClient.relocationService.finishRelocation(barcodeProduct,barcodeOrigin,barcodeDestination,amount)
            relocationResponseCall?.enqueue(object : retrofit2.Callback<ServiceResponse?> {
                override fun onResponse(
                    call: Call<ServiceResponse?>,
                    response: Response<ServiceResponse?>
                ) {
                    if (response.isSuccessful and response.body()?.status.equals("SUCESS")) {
                        relocationMessage = response.body()?.message.toString()
                        _relocationServiceCreateResultLiveData.value = true
                        _relocationResultLiveData.value = relocationMessage
                    } else {
                        relocationMessage =  response.body()?.message.toString()
                        _relocationServiceCreateResultLiveData.value = false
                        _relocationResultLiveData.value = relocationMessage
                    }
                }

                override fun onFailure(call: Call<ServiceResponse?>, t: Throwable) {
                    relocationMessage = "Fallo en el servicio, intente de nuevamente"
                    _relocationServiceCreateResultLiveData.value = false
                }
            })
        }
    }
}