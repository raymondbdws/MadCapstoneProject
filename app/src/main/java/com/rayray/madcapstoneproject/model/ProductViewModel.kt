package com.rayray.madcapstoneproject.model

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.rayray.madcapstoneproject.repository.ProductRepository
import kotlinx.coroutines.launch
import java.lang.Exception

class ProductViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "FIRESTORE"
    private val productRepository: ProductRepository = ProductRepository()

    val product: LiveData<List<Product>> = productRepository.product

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

    fun updateProduct(newQuantity: Int, product: Product) {
        val updatedProduct = Product(
            product.code,
            product.ean,
            product.brand,
            product.type,
            product.department,
            product.sell_price,
            product.purchased_price,
            newQuantity,
            product.register_at,
            product.image,
            product.specs,
            product.release_date
        )

        viewModelScope.launch {
            try {
                productRepository.updateProduct(updatedProduct)
            }catch (e: ProductRepository.ProductSaveError){
                Log.e(TAG, e.message ?: "can not update product")
            }
        }

    }
}