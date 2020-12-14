package com.rayray.madcapstoneproject.model

import java.util.*

data class Product(
    var code: String,
    var ean: String,
    var brand: String,
    var type: String,
    var department: DepartmentEnum,
    var sell_price: Double,
    var purchased_price: Double,
    var stock_quantity: Int,
    var register_at: Date,
    var image: String,
    var specs: String,
    var release_date: Date,
)