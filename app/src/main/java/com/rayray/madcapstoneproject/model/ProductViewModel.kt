package com.rayray.madcapstoneproject.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.rayray.madcapstoneproject.repository.ProductRepository

class ProductViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "FIRESTORE"
    private val productRepository: ProductRepository = ProductRepository()
}