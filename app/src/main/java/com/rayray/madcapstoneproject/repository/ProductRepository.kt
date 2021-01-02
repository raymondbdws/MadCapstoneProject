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

/**
 * @author Raymond Chang
 *
 * Repository zorgt ervoor dat producten opgehaald kunnen worden vanuit Firestore Database van
 * Firebase. En het is mogelijk om een update uit te voeren.
 */
class ProductRepository {
    private var firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var productDocument = firestore.collection("products")

    private val _createSuccess: MutableLiveData<Boolean> = MutableLiveData()
    private val _product: MutableLiveData<List<Product>> = MutableLiveData()
    private val timeOut = 5_000.toLong()
    val product: LiveData<List<Product>> get() = _product

    /**
     * getProduct haalt alle producten op en wordt in een mutable list gestopt.
     */
    suspend fun getProduct(){
        val productList: MutableList<Product> = mutableListOf()

        try {
            withTimeout(timeOut){

                //Elk product moet een datum bevatten, aangezien dit niet wordt gebruikt
                //heeft elk product 7 november 2020 in releasedate en lastReceived
                val formatter = DateTimeFormatter.ofPattern("yyyy MM dd")
                val parsedRegisteredDate = LocalDate.parse("2020 11 07", formatter)
                val registerDate: ZonedDateTime = parsedRegisteredDate.atStartOfDay(ZoneId.systemDefault())
                val parsedReleaseDate = LocalDate.parse("2020 11 07", formatter)
                val releaseDate: ZonedDateTime = parsedReleaseDate.atStartOfDay(ZoneId.systemDefault())

                //Haalt de collectie op
                firestore.collection("products")
                    .get()
                    .addOnSuccessListener { products ->
                        //Haalt het document op en wordt het in de list gezet.
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
                                Date.from(registerDate.toInstant()),
                                product.data.get("image").toString(),
                                product.data.get("specs").toString(),
                                Date.from(releaseDate.toInstant()),
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
     * @param product bevat een product die geupdate moet worden.
     *
     * SetOption.merge() merge product if exist, or it will create
     * a new product in firestore.
     */
    suspend fun updateProduct(product: Product){
        try {
            withTimeout(timeOut){
                productDocument.document(product.code).set(product, SetOptions.merge()).await()

                _createSuccess.value = true
            }
        }catch (e: Exception){
            throw ProductSaveError(e.message.toString(), e)
        }
    }
    class ProductSaveError(message: String, cause: Throwable) : Exception(message, cause)
    class ProductRetrievalError(message: String) : Exception(message)
}