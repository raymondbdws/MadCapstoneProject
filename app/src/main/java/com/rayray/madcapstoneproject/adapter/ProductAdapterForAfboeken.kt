package com.rayray.madcapstoneproject.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rayray.madcapstoneproject.R
import com.rayray.madcapstoneproject.model.Product
import kotlinx.android.synthetic.main.item_product_afboeken.view.*
import kotlin.reflect.KFunction2

class ProductAdapterForAfboeken(private val products: List<Product>, private val onClick: (Product) -> Unit):
    RecyclerView.Adapter<ProductAdapterForAfboeken.ViewHolder>() {
    var selectedNewQuantity = 0
    var newQuantity = 0

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        init {
//
//            itemView.ibAddQuantityAfboeken.setOnClickListener {
//                newQuantity = products[adapterPosition].stock_quantity
//                selectedNewQuantity++
//                newQuantity += selectedNewQuantity
//                onClick(products[adapterPosition], newQuantity)
//            }

            itemView.ibDeleteQuantityAfboeken.setOnClickListener {
                newQuantity = products[adapterPosition].stock_quantity
                selectedNewQuantity--
                newQuantity += selectedNewQuantity
                onClick(products[adapterPosition])
            }
        }

        fun databind(product: Product){

            itemView.tvItemProductCodeAfboeken.text = product.code
            itemView.tvItemProductNameAfboeken.text = "${product.brand} ${product.type}"
            itemView.tvItemProductQuantityAfboeken.text = "${product.stock_quantity}"
            itemView.tvItemDepartmentAfboeken.text = product.department.toString()
            itemView.tvItemProductPriceAfboeken.text = "€${product.purchased_price}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductAdapterForAfboeken.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_product_afboeken,
                parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ProductAdapterForAfboeken.ViewHolder, position: Int) {
        holder.databind(products[position])
    }

    override fun getItemCount(): Int {
        return products.size
    }


}