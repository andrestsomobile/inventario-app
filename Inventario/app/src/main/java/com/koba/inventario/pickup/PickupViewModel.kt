package com.koba.inventario.pickup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koba.inventario.ApiClient
import com.koba.inventario.database.AppDatabase
import com.koba.inventario.database.IncomeEntity
import com.koba.inventario.database.PickupEntity
import com.koba.inventario.database.ValidateEntity
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

            pickupSync.forEach {
                createPickupService(it.requisitionId,it.requisitionNumber,it.novelty, it.barcodeProduct, it.barcodeLocation, user)
            }
            _syncResultLiveData.value = pickupSync.isNotEmpty()
        }
    }

    fun findByProduct(barcodeProduct: String,barcodeLocation: String,user: String){
        viewModelScope.launch {
            val pickupUpdateProduct = database.pickupDao().findByBarcodeProduct(barcodeProduct,barcodeLocation,user)
            _pickupUpdateResultLiveData.value = pickupUpdateProduct.isEmpty()
        }
    }

    fun update(requisitionId: String,barcodeProduct: String,barcodeLocation: String, user: String, novelty : String, requisitionNumber: String) {
        viewModelScope.launch {
            val pickupUpdateProduct = database.pickupDao().findByBarcodeProduct(barcodeProduct,barcodeLocation,user)
            pickupUpdateProduct.forEach {
                database.pickupDao().delete(
                    PickupEntity(
                        it.pickupId,barcodeProduct,barcodeLocation, user, requisitionNumber,novelty, requisitionId ,1
                    )
                )
            }
            _createdResultLiveData.value = true
        }
    }

    fun create(requisitionId: String,barcodeProduct: String,barcodeLocation: String, user: String,requisitionNumber: String, novelty : String) {
        viewModelScope.launch {
            val pickupList = database.pickupDao().findById(barcodeProduct,barcodeLocation,requisitionNumber,novelty,requisitionId)

            //if(pickupList == null && pickupList.isEmpty()) {
                database.pickupDao().create(
                    PickupEntity(
                        0,barcodeProduct,barcodeLocation, user, requisitionNumber, novelty, requisitionId, 1
                    )
                )
            //}
            _createdResultLiveData.value = true
        }
    }

    fun createPickupService(requisitionId: String,requisitionNumber: String,novelty: String,productCode: String, position: String, user: String){
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
                        update(requisitionId,productCode,position, "", novelty, requisitionNumber)
                    } else {
                        pickupMessage =  response.body()?.message.toString()
                        _pickupServiceCreateResultLiveData.value = false
                        _pickupResultLiveData.value = pickupMessage
                    }
                }

                override fun onFailure(call: Call<ServiceResponse?>, t: Throwable) {
                    create(requisitionId,productCode,position, user, novelty, requisitionNumber)
                    pickupMessage = "Fallo en el servicio, intente de nuevamente"
                    _pickupServiceCreateResultLiveData.value = false
                    _pickupResultLiveData.value = pickupMessage
                }
            })
        }
    }
}