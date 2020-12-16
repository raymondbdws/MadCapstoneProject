package com.rayray.madcapstoneproject.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.*
import com.rayray.madcapstoneproject.model.DepartmentEnum
import com.rayray.madcapstoneproject.model.Product
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class ProductRepository {
    private var firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var productDocument = firestore.collection("products").document("Telecom").collection("Samsung").document(
        "1652625"
    )

    private val _product: MutableLiveData<List<Product>> = MutableLiveData()
    val product: LiveData<List<Product>> get() = _product
    private val _createSuccess: MutableLiveData<Boolean> = MutableLiveData()
    val createSuccess: LiveData<Boolean> get() = _createSuccess

    suspend fun getProduct(){
        val productList: MutableList<Product> = mutableListOf()

        try {
            withTimeout(5_000){
                val formatter = DateTimeFormatter.ofPattern("yyyy MM dd")

                val parsedRegisteredDate = LocalDate.parse("2020 11 07", formatter)
                val registerDate: ZonedDateTime = parsedRegisteredDate.atStartOfDay(ZoneId.systemDefault())

                val parsedReleaseDate = LocalDate.parse("2020 11 07", formatter)
                val releaseDate: ZonedDateTime = parsedReleaseDate.atStartOfDay(ZoneId.systemDefault())

                //get collection
                firestore.collection("products")
                    .get()
                    .addOnSuccessListener { products ->
                        //get Documents
                        for (product in products){
                            productList.add(Product(
                                product.data.get("product_code").toString(),
                                product.data.get("ean_code").toString(),
                                product.data.get("product_brand").toString(),
                                product.data.get("product_type").toString(),
                                DepartmentEnum.valueOf(product.data.get("department").toString()),
                                product.data.get("sell_price") as Double,
                                product.data.get("purchased_price") as Double,
                                (product.data.get("stock_quantity") as Long).toInt(),
                                //Date(product.data.get("registered_at") as Long),
                                Date.from(registerDate.toInstant()),
                                product.data.get("image").toString(),
                                product.data.get("specs").toString(),
                                Date.from(releaseDate.toInstant()),
                                //product.data.get("release_date") as Date,
                            ))
                        }

                        _product.value = productList
                    }
                    .addOnFailureListener { exception ->
                        Log.w("TAG", "Error getting documents: ", exception)
                    }



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