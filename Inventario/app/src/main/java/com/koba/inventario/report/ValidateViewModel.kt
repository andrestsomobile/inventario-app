package com.koba.inventario.report

import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koba.inventario.ApiClient
import com.koba.inventario.database.*
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

    private val _validateServiceCreateResultLiveData = MutableLiveData<Boolean>(null)
    val validateServiceCreateLiveData: LiveData<Boolean> = _validateServiceCreateResultLiveData

    private val _validateResultLiveData = MutableLiveData<String>()
    val validateLiveData: LiveData<String> = _validateResultLiveData

    private val _inventoryResultLiveData = MutableLiveData<List<InventoryUiModel>>(listOf())
    val inventoryLiveData: LiveData<List<InventoryUiModel>> = _inventoryResultLiveData

    private val _validateInventoryLiveData = MutableLiveData(false)
    val validateInventoryLiveData: LiveData<Boolean> = _validateInventoryLiveData

    private val _validateSyncData = MutableLiveData<Boolean>(null)
    val validateSyncData: LiveData<Boolean> = _validateSyncData

    private val _idBdLocal = MutableLiveData<String>()
    val idBdLocal: LiveData<String> = _idBdLocal

    fun setDataBase(database: AppDatabase) {
        this.database = database
    }

    fun clearData() {
        _validateServiceCreateResultLiveData.value = null
        _validateResultLiveData.value = null
    }

    fun findByUser(user: String, sync: String) {
        viewModelScope.launch {
            val relocationSync = database?.validateDao().findValidateByIndSync(1,user)

            //val relocationSyncCopy = database?.validateDao().findValidateByIndSyncCopy(1,user)

            relocationSync.forEach {
                _validateSyncData.value = true;
                _idBdLocal.value += it.barcodeProduct + ":" + it.validateId + ","
                createValidationService(it.barcodeProduct,it.barcodeLocation,user, it.amount, it.id, "inventario", sync)
            }

            /*relocationSyncCopy.forEach {
                createValidationService(it.barcodeProduct,it.barcodeLocation,user, it.amount, it.id, "inventario_backup", sync)
            }*/
            _syncResultLiveData.value = relocationSync.isNotEmpty()
        }
    }

    fun findByUserBackup(user: String, sync: String) {
        viewModelScope.launch {
            val relocationSync = database?.validateDao().findValidateByIndSyncBackup(1,user)


            relocationSync.forEach {
                createBackupValidationService(it.barcodeProduct,it.barcodeLocation,user, it.amount, it.id, "inventario_backup", sync)
            }
        }
    }

    fun update(barcodeProduct: String,barcodeLocation: String, user: String,amount: String, id: String) {
        viewModelScope.launch {
            val relocationUpdateProduct = database.validateDao().findByBarcodeProduct(barcodeProduct,barcodeLocation,user,amount,id)

            if(relocationUpdateProduct != null) {

                relocationUpdateProduct.forEach {
                    /*database.validateDao().createCopy(ValidateBackupEntity(
                        0,it.barcodeProduct,it.barcodeLocation, it.amount,user,it.id,1
                    ))

                    println("id: " + it.validateId);*/
                    database.validateDao().delete(
                        ValidateEntity(
                            it.validateId,it.barcodeProduct,it.barcodeLocation,it.amount,user,it.id,0
                        ))
                }

                _createdResultLiveData.value = true
            }
        }
    }

    fun createValidationService(barcodeProduct: String,barcodeLocation: String,user: String, amount: String, id: String, table: String, sync: String){
        viewModelScope.launch {
            var validateMessage = ""
            var relocationResponseCall = ApiClient.validateService.finishValidate(barcodeProduct,barcodeLocation, user, "MOVIL",amount, id, table)

            /*relocationResponseCall?.enqueue(object : retrofit2.Callback<ServiceResponse?> {
                override fun onResponse(
                    call: Call<ServiceResponse?>,
                    response: Response<ServiceResponse?>
                ) {
                    if (response.isSuccessful and response.body()?.status.equals("SUCESS")) {
                        validateMessage = response.body()?.message.toString()
                        _validateServiceCreateResultLiveData.value = true
                        _validateResultLiveData.value = validateMessage
                        update(barcodeProduct,barcodeLocation,user,amount,id)
                    } else {
                        validateMessage =  response.body()?.message.toString()
                        _validateServiceCreateResultLiveData.value = false
                        _validateResultLiveData.value = validateMessage
                        update(barcodeProduct,barcodeLocation,user,amount,id)
                    }
                }

                override fun onFailure(call: Call<ServiceResponse?>, t: Throwable) {
                    val message = t.message
                    if(sync != "1") {
                        create(barcodeProduct,barcodeLocation,user, amount, id);
                    }

                    validateMessage = "Fallo en el servicio, intente de nuevamente"
                    _validateResultLiveData.value = validateMessage
                    _validateServiceCreateResultLiveData.value = false
                }
            })*/
            try
            {
                var response = relocationResponseCall.execute();
                if (response.isSuccessful and response.body()?.status.equals("SUCESS")) {
                    validateMessage = response.body()?.message.toString()
                    _validateServiceCreateResultLiveData.value = true
                    _validateResultLiveData.value = validateMessage
                    update(barcodeProduct,barcodeLocation,user,amount,id)
                    println(validateMessage);
                } else {
                    validateMessage =  response.body()?.message.toString()
                    _validateServiceCreateResultLiveData.value = false
                    _validateResultLiveData.value = validateMessage
                    update(barcodeProduct,barcodeLocation,user,amount,id)
                }
            }
            catch (e: Exception)
            {
                if(sync != "1") {
                    create(barcodeProduct,barcodeLocation,user, amount, id);
                }

                validateMessage = "Fallo en el servicio, intente de nuevamente"
                _validateResultLiveData.value = validateMessage
                _validateServiceCreateResultLiveData.value = false
                e.printStackTrace();
            }
        }
    }

    fun createBackupValidationService(barcodeProduct: String,barcodeLocation: String,user: String, amount: String, id: String, table: String, sync: String){
        viewModelScope.launch {
            var relocationResponseCall = ApiClient.validateService.finishValidate(barcodeProduct,barcodeLocation, user, "MOVIL",amount, id, table)

            try
            {
                var response = relocationResponseCall.execute();
                print(response)
            }
            catch (e: Exception)
            {
                e.printStackTrace();
            }
        }
    }

    fun create(barcodeProduct: String,barcodeLocation: String,user: String, amount: String, id: String) {
        viewModelScope.launch {
            //val productList = database?.validateDao().findByBarcodeProduct(barcodeProduct,barcodeLocation,user,amount,id)

            //if(productList == null || productList.isEmpty()) {
                database.validateDao().create(ValidateEntity(
                    0,barcodeProduct,barcodeLocation, amount,user,id,1
                ))
                _createdResultLiveData.value = true
            //}
        }
    }

    fun clearModel(){
        _validateResultLiveData.value = null
        _validateServiceCreateResultLiveData.value = null
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