package com.rayray.madcapstoneproject.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.rayray.madcapstoneproject.model.Product
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import java.lang.Exception

class ProductRepository {
    private var firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var productDocument = firestore.collection("products").document("product")

    private val _product: MutableLiveData<Product> = MutableLiveData()

    val product: LiveData<Product> get() = _product

    private val _createSuccess: MutableLiveData<Boolean> = MutableLiveData()

    val createSuccess: LiveData<Boolean> get() = _createSuccess

    suspend fun getProduct(){
        try {
            withTimeout(5_000){
                val data = productDocument
                    .get()
                    .await()

                val product = data.getString("").toString()
                //_product.value = Product()
            }
        }catch (e: Exception){
            throw ProductRetrievalError("Retrieval")
        }
    }

    class ProductSaveError(message: String, cause: Throwable) : Exception(message, cause)
    class ProductRetrievalError(message: String) : Exception(message)
}