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

/**
 * @author Raymond Chang
 *
 * Deze class wordt gebruikt om artikelen af te schrijven.
 */
class AfschrijfFragment : Fragment() {

    /**
     * Variabelen die nodig zijn om het product af te schrijven.
     */
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var productAdapter: ProductAdapterForAfboeken

    private val viewModel: ProductViewModel by activityViewModels()

    private var products: ArrayList<Product> = arrayListOf()
    private var updateProducts: MutableMap<String, Int> = mutableMapOf()

    /**
     * OncreateView
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_afschrijf, container, false)
    }

    /**
     * onViewCreated
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRv()
        viewModel.getProduct()
        observeProduct()

        btn_save_product_quantity_afschrijving.setOnClickListener {
            for (product in products) {
                if (updateProducts.containsKey(product.ean)) {
                    val updatedProduct = Product(
                        product.code,
                        product.ean,
                        product.brand,
                        product.type,
                        product.department,
                        product.sell_price,
                        product.purchased_price,
                        updateProducts.get(product.ean) as Int,
                        product.register_at,
                        product.image,
                        product.specs,
                        product.release_date
                    )
                    viewModel.updateProduct(updatedProduct)
                }
            }
            viewModel.getProduct()
            productAdapter.notifyDataSetChanged()
            productAdapter.selectedNewQuantity = 0

            Snackbar.make(
                requireActivity().findViewById(R.id.clAfschrijf),
                R.string.opgeslagen,
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    /**
     * initialiseert Recycleview
     */
    private fun initRv() {
        productAdapter = ProductAdapterForAfboeken(products, ::onProductClick)
        viewManager = LinearLayoutManager(activity)

        rvProductsAfschrijving.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = productAdapter
        }
    }

    /**
     * Wanneer er op een minnetje klikt van een item in de RV, dan wordt ook de Quantity
     * bijgewerkt van het geselecteerde product.
     *
     * In dit geval is het alleen mogelijk om het aantal te verminderen.
     *
     * @param product Het product wordt hier meegegeven, en wordt alleen het ean nummer
     *                en Quantity  opgeslagen in de Map
     */
    private fun onProductClick(product: Product) {
        updateProducts.put(product.ean, (--product.stock_quantity))
        productAdapter.notifyDataSetChanged()
    }

    /**
     * Observing products. Automatisch wordt de RV bijgewerkt wanneer Productslijst wordt aangepast.
     */
    private fun observeProduct() {
        viewModel.products.observe(viewLifecycleOwner, Observer { products ->
            this@AfschrijfFragment.products.clear()
            this@AfschrijfFragment.products.addAll(products)
            productAdapter.notifyDataSetChanged()
        })
    }
}