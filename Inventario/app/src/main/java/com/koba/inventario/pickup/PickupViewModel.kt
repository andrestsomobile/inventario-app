package com.koba.inventario.pickup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koba.inventario.ApiClient
import com.koba.inventario.database.AppDatabase
import com.koba.inventario.database.IncomeEntity
import com.koba.inventario.database.PickupEntity
import com.koba.inventario.service.ServiceResponse
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response

class PickupViewModel: ViewModel() {

    private lateinit var database: AppDatabase
    private val _createdResultLiveData = MutableLiveData<Boolean?>(null)
    val createdLiveData: LiveData<Boolean?> = _createdResultLiveData

    private val _syncResultLiveData = MutableLiveData(false)
    val syncLiveData: LiveData<Boolean> = _syncResultLiveData

    private val _pickupUpdateResultLiveData = MutableLiveData<Boolean?>(null)
    val pickupUpdateLiveData: LiveData<Boolean?> = _pickupUpdateResultLiveData

    private val _pickupServiceCreateResultLiveData = MutableLiveData<Boolean?>(null)
    val pickupServiceCreateLiveData: LiveData<Boolean?> = _pickupServiceCreateResultLiveData

    private val _pickupResultLiveData = MutableLiveData<String>()
    val pickupLiveData: LiveData<String> = _pickupResultLiveData

    fun setDataBase(database: AppDatabase) {
        this.database = database
    }

    fun findByUser(user: String) {
        viewModelScope.launch {
            val pickupSync = database.pickupDao().findPickupByIndSync(1,user)
            _syncResultLiveData.value = pickupSync.isNotEmpty()
        }
    }

    fun findByProduct(barcodeProduct: String,barcodeLocation: String,user: String){
        viewModelScope.launch {
            val pickupUpdateProduct = database.pickupDao().findByBarcodeProduct(barcodeProduct,barcodeLocation,user)
            _pickupUpdateResultLiveData.value = pickupUpdateProduct.isEmpty()
        }
    }

    fun update(barcodeProduct: String,barcodeLocation: String, user: String, novelty : String, amount: Int) {
        viewModelScope.launch {
            val pickupUpdateProduct = database.pickupDao().findByBarcodeProduct(barcodeProduct,barcodeLocation,user)
            var product: PickupEntity = pickupUpdateProduct[0]
            database.pickupDao().update(
                PickupEntity(
                    product.pickupId,barcodeProduct,barcodeLocation, user, product.amount+amount,novelty ,1
            ))
            _createdResultLiveData.value = true
        }
    }

    fun create(barcodeProduct: String,barcodeLocation: String, user: String,amount: Int, novelty : String, indSync: Int) {
        viewModelScope.launch {
            database.pickupDao().create(
                PickupEntity(
                    0,barcodeProduct,barcodeLocation, user, amount, novelty, indSync
                )
            )
            _createdResultLiveData.value = true
        }
    }

    fun createPickupService(requisitionId: String,requisitionNumber: String,novelty: String,productCode: String, position: String){
        viewModelScope.launch {
            var pickupMessage = ""
            val pickupResponseCall = ApiClient.requisitionService.finishRequisition(requisitionId,requisitionNumber,novelty,productCode,position)
            pickupResponseCall?.enqueue(object : retrofit2.Callback<ServiceResponse?> {
                override fun onResponse(
                    call: Call<ServiceResponse?>,
                    response: Response<ServiceResponse?>
                ) {
                    if (response.isSuccessful and response.body()?.status.equals("SUCESS")) {
                        pickupMessage = response.body()?.message.toString()
                        _pickupServiceCreateResultLiveData.value = true
                        _pickupResultLiveData.value = pickupMessage
                    } else {
                        pickupMessage =  response.body()?.message.toString()
                        _pickupServiceCreateResultLiveData.value = false
                        _pickupResultLiveData.value = pickupMessage
                    }
                }

                override fun onFailure(call: Call<ServiceResponse?>, t: Throwable) {
                    pickupMessage = "Fallo en el servicio, intente de nuevamente"
                    _pickupServiceCreateResultLiveData.value = false
                }
            })
        }
    }
}