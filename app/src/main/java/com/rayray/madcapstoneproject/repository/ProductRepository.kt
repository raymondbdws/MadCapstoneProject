package com.rayray.madcapstoneproject.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.rayray.madcapstoneproject.model.DepartmentEnum
import com.rayray.madcapstoneproject.model.Product
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import java.lang.Exception
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class ProductRepository {
    private var firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var productDocument = firestore.collection("products").document("Telecom").collection("Samsung").document("1652625")

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

                val formatter = DateTimeFormatter.ofPattern("yyyy MM dd")

                val parsedRegisteredDate = LocalDate.parse( "2020 11 07", formatter)
                val registerDate: ZonedDateTime = parsedRegisteredDate.atStartOfDay(ZoneId.systemDefault())

                val parsedReleaseDate = LocalDate.parse( "2020 11 07", formatter)
                val releaseDate: ZonedDateTime = parsedReleaseDate.atStartOfDay(ZoneId.systemDefault())

                val product = Product(
                    data.get("product_code").toString(),
                    data.get("ean_code").toString(),
                    data.get("product_brand").toString(),
                    data.get("product_type").toString(),
                    DepartmentEnum.TELECOM,
                    data.get("sell_price").toString().toDouble(),
                    data.get("purchased_price").toString().toDouble(),
                    data.get("stock_quantity").toString().toInt(),
                    Date.from(registerDate.toInstant()),
                    "image",
                    data.get("specs").toString(),
                    Date.from(releaseDate.toInstant())
                )

                _product.value = product


            }

        }catch (e: Exception){
            throw ProductRetrievalError("Retrieval")
        }
    }

    /**
     * @param
     *
     * SetOption.merge() merge product if exist, or it will create
     * a new product in firestore.
     */
    suspend fun updateProduct(product: Product){
        try {
            withTimeout(5_000){
                productDocument.set(product, SetOptions.merge()).await()

                _createSuccess.value = true
            }
        }catch (e: Exception){
            throw ProductSaveError(e.message.toString(), e)
        }

    }

    class ProductSaveError(message: String, cause: Throwable) : Exception(message, cause)
    class ProductRetrievalError(message: String) : Exception(message)
}