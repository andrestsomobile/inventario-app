package com.koba.inventario.pickup

import android.util.Log
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koba.inventario.ApiClient
import com.koba.inventario.MainActivity
import com.koba.inventario.R
import com.koba.inventario.database.AppDatabase
import com.koba.inventario.database.DatabaseHandler
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response

class RequisitionViewModel: ViewModel() {

    private lateinit var database: AppDatabase
    private val _createdResultLiveData = MutableLiveData(false)
    val createdLiveData: LiveData<Boolean> = _createdResultLiveData

    private val _syncResultLiveData = MutableLiveData(false)
    val syncLiveData: LiveData<Boolean> = _syncResultLiveData

    private val _requisitionListResultLiveData = MutableLiveData<List<RequisitionUiModel>>(listOf())
    val requisitionListLiveData: LiveData<List<RequisitionUiModel>> = _requisitionListResultLiveData

    private val _requisitionLiveData = MutableLiveData(false)
    val requisitionLiveData: LiveData<Boolean> = _requisitionLiveData

    private val _requisitionNumberResultLiveData = MutableLiveData<String>()
    val requisitionNumberResultLiveData: LiveData<String> = _requisitionNumberResultLiveData

    private val _requisitionNumberLiveData = MutableLiveData(false)
    val requisitionNumberLiveData: LiveData<Boolean> = _requisitionNumberLiveData
    //private var databaseHandler: DatabaseHandler = DatabaseHandler()


    fun setDataBase(database: AppDatabase) {
        this.database = database
    }

    fun findRequisitionList(requisitionNumber: String, user: String){
        viewModelScope.launch {
            val requisitionList = ArrayList<RequisitionUiModel>()
            var requisitionObjectList: List<RequisitionObjectResponse>?
            var totalUnit: Int = 0
            var clientName: String = ""
            var statusRequisition: Boolean = false

            if(requisitionNumber != "") {
                val requisitionResponseCall = ApiClient.requisitionService.getRequisitionsList(requisitionNumber,user)
                requisitionResponseCall?.enqueue(object : retrofit2.Callback<RequisitionResponse?> {
                    override fun onResponse(
                        call: Call<RequisitionResponse?>,
                        response: Response<RequisitionResponse?>
                    ) {
                        if (response.isSuccessful and response.body()?.status.equals("SUCESS")) {
                            for (t in response.body()?.data!!) {
                                requisitionObjectList = listOf(t)
                                for(f in requisitionObjectList!!){
                                    totalUnit += f.refPedido?.refpcantidad?.toBigDecimal()?.toInt() ?: 0
                                    clientName = f.refPedido?.refpentrada.toString()
                                    statusRequisition = f.refPedido?.refpnacdetalle.toString() != "TRAMITE"
                                }
                            }

                            requisitionList.apply {
                                add(RequisitionUiModel(user,clientName,requisitionNumber,totalUnit, statusRequisition, response.body()?.data!!))
                            }
                            _requisitionListResultLiveData.value = requisitionList
                            _requisitionLiveData.value = false
                        } else {
                            _requisitionLiveData.value = true
                        }
                    }

                    override fun onFailure(call: Call<RequisitionResponse?>, t: Throwable) {
                        _requisitionLiveData.value = true
                    }
                })
            }
        }
    }

    fun findRequisitionNumber(requisitionNumber: String, user: String){
        viewModelScope.launch {
            var requisitionNumberObject: String = ""
            var requisitionObject: RequisitionNumberDataResponse
            val requisitionNumberResponseCall = ApiClient.requisitionService.getRequisitionNumber(requisitionNumber,user)
            requisitionNumberResponseCall?.enqueue(object : retrofit2.Callback<RequisitionNumberResponse?> {
                override fun onResponse(
                    call: Call<RequisitionNumberResponse?>,
                    response: Response<RequisitionNumberResponse?>
                ) {
                    if (response.isSuccessful and response.body()?.status.equals("SUCESS")) {
                        requisitionObject = response.body()?.data!!
                        requisitionNumberObject = requisitionObject.idRegistroPedido.toString()

                        _requisitionNumberResultLiveData.value = requisitionNumberObject
                        _requisitionNumberLiveData.value = false
                    } else {
                        _requisitionNumberResultLiveData.value = ""
                        _requisitionNumberLiveData.value = true
                    }
                }

                override fun onFailure(call: Call<RequisitionNumberResponse?>, t: Throwable) {
                    _requisitionNumberLiveData.value = true
                }
            })
        }
    }

}