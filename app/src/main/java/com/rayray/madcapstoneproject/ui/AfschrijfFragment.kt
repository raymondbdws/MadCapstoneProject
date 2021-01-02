package com.rayray.madcapstoneproject.ui.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
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
import kotlinx.android.synthetic.main.fragment_overzicht_artikel.*
import java.util.*
import kotlin.collections.ArrayList

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
    private var originalProductList = ArrayList<Product>()

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
        filterList()
        btnSaveOnclick()
    }

    /**
     * Save knop onclick listener
     */
    private fun btnSaveOnclick(){
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
     * Zoek funtionaliteit. Het filtert de lijst op basis van de zoektermen. Er is een
     * kopie van de productenLijst, het kopie blijft ongewijzigd.
     *
     * Kopie is nodig om het totaal aantal producten te kunnen tellen dat nodig is om
     * te loopen.
     */
    private fun filterList() {
        svSearchProductsAfschrijving.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return true
            }

            /**
             * Voert uit bij elke wijziging in de zoekbalk.
             */
            override fun onQueryTextChange(searchWord: String?): Boolean {

                //controleert of de variabel niet leeg is.
                if (searchWord!!.isNotEmpty()) {

                    //Productlijst wordt tijdelijk leeggemaakt.
                    products.clear()

                    //Stopt waarde in lowercase in search var.
                    val search = searchWord.toLowerCase(Locale.getDefault())

                    //Loopt door alle producten om te kijken of de zoekwoorden matchen., zoja,
                    //dan wordt het aan de productLijst toegevoegd.
                    originalProductList.forEach {
                        if (it.brand.toLowerCase(Locale.getDefault()).contains(search) ||
                            it.type.toLowerCase(Locale.getDefault()).contains(search)
                        ) {
                            //Voegt matchend product toe aan de lijst.
                            products.add(it)
                        }
                    }
                    productAdapter.notifyDataSetChanged()
                } else {
                    products.clear()
                    products.addAll(originalProductList)
                    productAdapter.notifyDataSetChanged()
                }
                return true
            }
        })
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
            this@AfschrijfFragment.originalProductList.clear()
            this@AfschrijfFragment.originalProductList.addAll(products)
            productAdapter.notifyDataSetChanged()
        })
    }
}