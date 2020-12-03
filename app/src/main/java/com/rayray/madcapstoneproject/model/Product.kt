package com.rayray.madcapstoneproject.model

import java.util.*

data class Product (
    var code: String,
    var ean: String,
    var name: String,
    var type: String,
    var department: DepartmentEnum,
    var sell_price: Double,
    var purchased_price: Double,
    var quantity: Int,
    var register_date: Date,
    var image: String,
    var specs: String,
    var release_date: Date,
)