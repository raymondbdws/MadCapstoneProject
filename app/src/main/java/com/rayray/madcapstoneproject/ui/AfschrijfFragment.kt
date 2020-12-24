package com.rayray.madcapstoneproject.ui.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.rayray.madcapstoneproject.R
import com.rayray.madcapstoneproject.adapter.ProductAdapterForAfboeken
import com.rayray.madcapstoneproject.model.Product
import com.rayray.madcapstoneproject.model.ProductViewModel
import kotlinx.android.synthetic.main.fragment_afschrijf.*

class AfschrijfFragment : Fragment(){

    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var productAdapter: ProductAdapterForAfboeken
    private lateinit var selectedProduct: Product

    private val viewModel: ProductViewModel by activityViewModels()
    private var products: ArrayList<Product> = arrayListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_afschrijf, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRv()
        viewModel.getProduct()
        observeProduct()

        btn_save_product_quantity_afschrijving.setOnClickListener {
            viewModel.updateProduct(productAdapter.newQuantity, selectedProduct)
            viewModel.getProduct()
            productAdapter.notifyDataSetChanged()
            productAdapter.selectedNewQuantity = 0
        }
    }
    private fun initRv() {
        productAdapter = ProductAdapterForAfboeken(products, ::onProductClick)
        viewManager = LinearLayoutManager(activity)

        rvProductsAfschrijving.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = productAdapter
        }
    }

    //zonder -, wordt het plus 1 x 2
    private fun onProductClick(product: Product) {
        selectedProduct = product

        viewModel.updateProduct(productAdapter.newQuantity, selectedProduct)
        viewModel.getProduct()
        productAdapter.notifyDataSetChanged()
        productAdapter.selectedNewQuantity = 0

        //Snackbar.make(rvProductsAfschrijving, "This color is: ${product.type}", Snackbar.LENGTH_LONG).show()
    }

    private fun observeProduct() {
        viewModel.product.observe(viewLifecycleOwner, Observer { products ->
            this@AfschrijfFragment.products.clear()
            this@AfschrijfFragment.products.addAll(products)
            productAdapter.notifyDataSetChanged()
        })
    }
}