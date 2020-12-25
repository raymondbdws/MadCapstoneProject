package com.rayray.madcapstoneproject.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rayray.madcapstoneproject.R
import com.rayray.madcapstoneproject.model.Product
import kotlinx.android.synthetic.main.item_product_controle.view.*

class ProductAdapterForChecking (private val products: List<Product>): RecyclerView.Adapter<ProductAdapterForChecking.ViewHolder>(){

    inner class ViewHolder(itemView:View): RecyclerView.ViewHolder(itemView){
        fun databind(product: Product){
            itemView.tvItemProductCodeControle.text = product.code
            itemView.tvItemProductNameControle.text = "${product.brand} ${product.type}"
            itemView.tvItemProductQuantityControle.text = "${product.stock_quantity}x"
            itemView.tvItemDepartmentControle.text = product.department.toString()
            itemView.tvItemProductPriceControle.text = "€${product.purchased_price}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductAdapterForChecking.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_product,
                parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.databind(products[position])
    }

    override fun getItemCount(): Int {
        return products.size
    }
}