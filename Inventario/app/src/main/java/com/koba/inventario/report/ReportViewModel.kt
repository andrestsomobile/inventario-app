package com.koba.inventario.report

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koba.inventario.database.AppDatabase
import com.koba.inventario.database.ValidateEntity
import kotlinx.coroutines.launch

class ReportViewModel: ViewModel() {

    private lateinit var database: AppDatabase
    private val _createdResultLiveData = MutableLiveData(false)
    val createdLiveData: LiveData<Boolean> = _createdResultLiveData

    private val _syncResultLiveData = MutableLiveData(false)
    val syncLiveData: LiveData<Boolean> = _syncResultLiveData

    private val _validateUpdateResultLiveData = MutableLiveData(false)
    val validateUpdateLiveData: LiveData<Boolean> = _validateUpdateResultLiveData

    fun setDataBase(database: AppDatabase) {
        this.database = database
    }

    fun findByUser(user: String) {
        viewModelScope.launch {
            val validateSync = database.validateDao().findValidateByIndSync(1,user)
            _syncResultLiveData.value = validateSync.isNotEmpty()
        }
    }

    fun update(barcodeProduct: String,barcodeLocation: String, user: String,amount: Int) {
        viewModelScope.launch {
            val validateUpdateProduct = database.validateDao().findByBarcodeProduct(barcodeProduct,barcodeLocation,user,"")
            var product: ValidateEntity = validateUpdateProduct[0]
            database.validateDao().update(
                ValidateEntity(
                    product.validateId,barcodeProduct,barcodeLocation,product.amount+amount,user,"",1
            ))
            _createdResultLiveData.value = true
        }
    }
}