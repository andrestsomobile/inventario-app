package com.koba.inventario.positioning

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koba.inventario.ApiClient
import com.koba.inventario.database.AppDatabase
import com.koba.inventario.database.PositionEntity
import com.koba.inventario.service.ServiceResponse
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response

class PositioningViewModel: ViewModel() {

    private val _positioningResultLiveData = MutableLiveData<List<PositioningUiModel>>(listOf())
    val positioningLiveData: LiveData<List<PositioningUiModel>> = _positioningResultLiveData

    private val _validatePositioningLiveData = MutableLiveData(false)
    val validatePositioningLiveData: LiveData<Boolean> = _validatePositioningLiveData


    fun findAllPositioning(){
        viewModelScope.launch {
            val positioningList = ArrayList<PositioningUiModel>()
            val positioningResponseCall = ApiClient.positioningService.getAllPositioning()
            positioningResponseCall?.enqueue(object : retrofit2.Callback<PositioningResponse?> {
                override fun onResponse(
                    call: Call<PositioningResponse?>,
                    response: Response<PositioningResponse?>
                ) {
                    if (response.isSuccessful and response.body()?.status.equals("SUCESS")) {
                        for (t in response.body()?.data!!) {
                            positioningList.apply {
                                add(PositioningUiModel(t.cantidad, t.bodega,t.posicion))
                            }
                        }
                        _positioningResultLiveData.value = positioningList
                    } else {
                        _validatePositioningLiveData.value = true
                    }
                }

                override fun onFailure(call: Call<PositioningResponse?>, t: Throwable) {
                    _validatePositioningLiveData.value = true
                }
            })
        }
    }


}