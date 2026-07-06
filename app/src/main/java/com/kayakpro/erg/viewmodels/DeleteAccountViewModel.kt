package com.kayakpro.erg.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kayakpro.erg.model.DeleteAccountResponse
import com.kayakpro.erg.network.ApiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeleteAccountViewModel @Inject constructor(
    private val apiRepository: ApiRepository
) : ViewModel() {

    val deleteResult = MutableLiveData<DeleteAccountResponse?>()
    val error = MutableLiveData<String>()
    val loading = MutableLiveData<Boolean>()

    fun deleteAccount(token: String) {
        viewModelScope.launch {
            loading.postValue(true)
            try {
                apiRepository.deleteAccount(token).let {
                    loading.postValue(false)
                    if (it.isSuccessful) {
                        deleteResult.postValue(it.body())
                    } else {
                        error.postValue(it.errorBody()?.string())
                    }
                }
            } catch (e: Exception) {
                loading.postValue(false)
                error.postValue(e.message)
            }
        }
    }
}
