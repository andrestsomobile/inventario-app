package com.koba.inventario.synchronization

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koba.inventario.database.AppDatabase
import kotlinx.coroutines.launch

val trafficInventory = "Trafico Inventario"
val validateInventory = "Validación Inventario"
val pickupInventory = "Recogida Inventario"
val positionInventory = "Posicionar Inventario"
val relocateInventory= "Reubicación Inventario"
val statusOk = 1
val statusNotOk = 0

class SynchronizationViewModel: ViewModel() {

    private lateinit var database: AppDatabase

    private val _syncResultLiveData = MutableLiveData<List<ProcessSyncUiModel>>(listOf())
    val syncLiveData: LiveData<List<ProcessSyncUiModel>> = _syncResultLiveData

    fun setDataBase(database: AppDatabase) {
        this.database = database
    }

    fun findSynchronizationByUser(user: String) {
        viewModelScope.launch {

            //val incomeSync = database.incomeDao().findIncomeByIndSync(1,user)

            val pickupSync = database.pickupDao().findPickupByIndSync(1,user)

            val positionSync = database.positionDao().findPositionByIndSync(1,user)

            val relocationSync = database.relocationDao().findRelocationByIndSync(1,user)

            val validateSync = database.validateDao().findValidateByIndSync(1,user)

            val trafficSync = database.trafficDao().findTrafficByIndSync(1,user)

            val results = listOf(
                ProcessSyncUiModel (name=trafficInventory, status=if(trafficSync.isNotEmpty()) statusNotOk else statusOk),
                ProcessSyncUiModel (name=validateInventory, status=if(validateSync.isNotEmpty()) statusNotOk else statusOk),
                ProcessSyncUiModel (name= pickupInventory, status=if(pickupSync.isNotEmpty()) statusNotOk else statusOk),
                ProcessSyncUiModel (name= positionInventory, status=if(positionSync.isNotEmpty()) statusNotOk else statusOk),
                ProcessSyncUiModel (name=relocateInventory, status=if(relocationSync.isNotEmpty()) statusNotOk else statusOk),
            )
            _syncResultLiveData.value = results
        }
    }

}