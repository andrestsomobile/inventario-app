package com.koba.inventario.synchronization

import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koba.inventario.database.AppDatabase
import com.koba.inventario.database.PickupEntity
import com.koba.inventario.database.PositionEntity
import com.koba.inventario.database.ValidateEntity
import com.koba.inventario.report.ValidateViewModel
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
    private val _syncValidationResultLiveData = MutableLiveData<List<ValidateEntity>>(listOf())
    private val _syncPickupResultLiveData = MutableLiveData<List<PickupEntity>>(listOf())
    private val _syncPositionResultLiveData = MutableLiveData<List<PositionEntity>>(listOf())
    val syncLiveData: LiveData<List<ProcessSyncUiModel>> = _syncResultLiveData
    val syncValidationLiveData: LiveData<List<ValidateEntity>> = _syncValidationResultLiveData
    val syncPickupLiveData: LiveData<List<PickupEntity>> = _syncPickupResultLiveData
    val syncPositionLiveData: LiveData<List<PositionEntity>> = _syncPositionResultLiveData


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

    fun findSynchronizationValidationByUser(user: String) {
        viewModelScope.launch {
            val validateSync = database.validateDao().findValidateByIndSync(1,user)
            _syncValidationResultLiveData.value = validateSync
        }
    }

    fun findSynchronizationPickupByUser(user: String) {
        viewModelScope.launch {
            val validateSync = database.pickupDao().findPickupByIndSync(1,user)
            _syncPickupResultLiveData.value = validateSync
        }
    }

    fun findSynchronizationPositionByUser(user: String) {
        viewModelScope.launch {
            val validateSync = database.positionDao().findPositionByIndSync(1,user)
            _syncPositionResultLiveData.value = validateSync
        }
    }

}