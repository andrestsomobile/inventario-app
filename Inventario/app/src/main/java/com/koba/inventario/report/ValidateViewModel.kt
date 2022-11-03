package com.koba.inventario.report

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koba.inventario.ApiClient
import com.koba.inventario.database.AppDatabase
import com.koba.inventario.database.RelocationEntity
import com.koba.inventario.database.ValidateEntity
import com.koba.inventario.positioning.TrafficResponse
import com.koba.inventario.positioning.TrafficUiModel
import com.koba.inventario.service.ServiceResponse
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response

class ValidateViewModel: ViewModel() {

    private lateinit var database: AppDatabase
    private val _createdResultLiveData = MutableLiveData(false)
    val createdLiveData: LiveData<Boolean> = _createdResultLiveData

    private val _syncResultLiveData = MutableLiveData(false)
    val syncLiveData: LiveData<Boolean> = _syncResultLiveData

    private val _validateUpdateResultLiveData = MutableLiveData<Boolean?>(null)
    val validateUpdateLiveData: LiveData<Boolean?> = _validateUpdateResultLiveData

    private val _validateServiceCreateResultLiveData = MutableLiveData<Boolean?>(null)
    val validateServiceCreateLiveData: LiveData<Boolean?> = _validateServiceCreateResultLiveData

    private val _validateResultLiveData = MutableLiveData<String>()
    val validateLiveData: LiveData<String> = _validateResultLiveData

    private val _inventoryResultLiveData = MutableLiveData<List<InventoryUiModel>>(listOf())
    val inventoryLiveData: LiveData<List<InventoryUiModel>> = _inventoryResultLiveData

    private val _validateInventoryLiveData = MutableLiveData(false)
    val validateInventoryLiveData: LiveData<Boolean> = _validateInventoryLiveData

    fun setDataBase(database: AppDatabase) {
        this.database = database
    }

    fun findByUser(user: String) {
        viewModelScope.launch {
            val relocationSync = database.validateDao().findValidateByIndSync(1,user)
            _syncResultLiveData.value = relocationSync.isNotEmpty()
        }
    }

    fun findByProduct(barcodeProduct: String,barcodeLocation: String,user: String){
        viewModelScope.launch {
            val incomeUpdateProduct = database.validateDao().findByBarcodeProduct(barcodeProduct,barcodeLocation,user)
            _validateUpdateResultLiveData.value = incomeUpdateProduct.isNotEmpty()
        }
    }

    fun update(barcodeProduct: String,barcodeLocation: String, user: String,amount: String) {
        viewModelScope.launch {
            val relocationUpdateProduct = database.validateDao().findByBarcodeProduct(barcodeProduct,barcodeLocation,user)
            val product: ValidateEntity = relocationUpdateProduct[0]
            database.validateDao().update(
                ValidateEntity(
                    product.validateId,barcodeProduct,barcodeLocation,amount,user,1
                ))
            _createdResultLiveData.value = true
        }
    }

    fun create(barcodeProduct: String,barcodeLocation: String, user: String,amount: String, indSync: Int) {
        viewModelScope.launch {
            database.validateDao().create(
                ValidateEntity(
                    0,barcodeProduct,barcodeLocation,amount,user,indSync
                )
            )
            _createdResultLiveData.value = true
        }
    }

    fun createValidationService(barcodeProduct: String,barcodeLocation: String,user: String, amount: String, id: String){
        viewModelScope.launch {
            var validateMessage = ""
            val relocationResponseCall = ApiClient.validateService.finishValidate(barcodeProduct,barcodeLocation, user, "MOVIL",amount, id)
            relocationResponseCall?.enqueue(object : retrofit2.Callback<ServiceResponse?> {
                override fun onResponse(
                    call: Call<ServiceResponse?>,
                    response: Response<ServiceResponse?>
                ) {
                    if (response.isSuccessful and response.body()?.status.equals("SUCESS")) {
                        validateMessage = response.body()?.message.toString()
                        _validateServiceCreateResultLiveData.value = true
                        _validateResultLiveData.value = validateMessage
                    } else {
                        validateMessage =  response.body()?.message.toString()
                        _validateServiceCreateResultLiveData.value = false
                        _validateResultLiveData.value = validateMessage
                    }
                }

                override fun onFailure(call: Call<ServiceResponse?>, t: Throwable) {
                    validateMessage = "Fallo en el servicio, intente de nuevamente"
                    _validateResultLiveData.value = validateMessage
                    _validateServiceCreateResultLiveData.value = false
                }
            })
        }
    }

    fun clearModel(){
        _validateResultLiveData.value = null
        _validateServiceCreateResultLiveData.value = false
    }

    fun findAllInventory(){
        viewModelScope.launch {
            val trafficList = ArrayList<InventoryUiModel>()
            trafficList.apply {
                add(InventoryUiModel("Seleccione", 0))
            }
            val trafficResponseCall = ApiClient.validateService.listInventory()
            trafficResponseCall?.enqueue(object : retrofit2.Callback<InventoryResponse?> {
                override fun onResponse(
                    call: Call<InventoryResponse?>,
                    response: Response<InventoryResponse?>
                ) {
                    if (response.isSuccessful and response.body()?.status.equals("SUCESS")) {
                        for (t in response.body()?.data!!) {
                            trafficList.apply {
                                add(InventoryUiModel(t.name, t.inventoryId))
                            }
                        }
                        _inventoryResultLiveData.value = trafficList
                    } else {
                        _validateInventoryLiveData.value = true
                    }
                }

                override fun onFailure(call: Call<InventoryResponse?>, t: Throwable) {
                    _validateInventoryLiveData.value = true
                }
            })
        }
    }
}