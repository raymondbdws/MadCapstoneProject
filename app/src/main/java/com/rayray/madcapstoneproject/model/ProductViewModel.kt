package com.rayray.madcapstoneproject.model

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.rayray.madcapstoneproject.repository.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

class ProductViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "FIRESTORE"
    private val productRepository: ProductRepository = ProductRepository()

    val products: LiveData<List<Product>> = productRepository.product
    val createSuccess: LiveData<Boolean> = productRepository.createSuccess
    private val _errorText: MutableLiveData<String> = MutableLiveData()
    val errorText: LiveData<String> get() = _errorText

    fun getProduct() {
        viewModelScope.launch {
            try {
                productRepository.getProduct()
            } catch (e: ProductRepository.ProductRetrievalError) {
                Log.e(TAG, e.message ?: "Can not retrieve data")
            }
        }
    }

    fun updateProduct(updatedProduct: Product) {
        viewModelScope.launch {
            try {
                productRepository.updateProduct(updatedProduct)
            }catch (e: ProductRepository.ProductSaveError){
                Log.e(TAG, e.message ?: "can not update product")
            }
        }
    }
}