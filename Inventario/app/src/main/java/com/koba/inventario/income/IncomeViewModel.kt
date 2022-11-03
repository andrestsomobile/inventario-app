package com.koba.inventario.income

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koba.inventario.database.AppDatabase
import com.koba.inventario.database.IncomeEntity
import kotlinx.coroutines.launch

class IncomeViewModel: ViewModel() {

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
            val incomeSync = database.incomeDao().findIncomeByIndSync(1,user)
            _syncResultLiveData.value = incomeSync.isNotEmpty()
        }
    }

    fun findByProduct(barcodeProduct: String,user: String){
        viewModelScope.launch {
            val incomeUpdateProduct = database.incomeDao().findByBarcodeProduct(barcodeProduct,user)
            _validateUpdateResultLiveData.value = incomeUpdateProduct.isEmpty()
        }
    }

    fun update(barcodeProduct: String, user: String,amount: Int) {
        viewModelScope.launch {
            val incomeUpdateProduct = database.incomeDao().findByBarcodeProduct(barcodeProduct,user)
            val product: IncomeEntity = incomeUpdateProduct[0]
            database.incomeDao().update(
                IncomeEntity(
                    product.incomeId,barcodeProduct, user, product.amount+amount, 1
            ))
            _createdResultLiveData.value = true
        }
    }

    fun create(barcodeProduct: String, user: String,amount: Int, indSync: Int) {
        viewModelScope.launch {
            database.incomeDao().create(
                IncomeEntity(
                    0,barcodeProduct, user, amount, indSync
                )
            )
            _createdResultLiveData.value = true
        }
    }


}