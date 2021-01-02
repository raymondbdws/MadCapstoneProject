package com.rayray.madcapstoneproject.ui.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rayray.madcapstoneproject.R
import com.rayray.madcapstoneproject.adapter.ProductAdapter
import com.rayray.madcapstoneproject.model.Product
import com.rayray.madcapstoneproject.model.ProductViewModel
import kotlinx.android.synthetic.main.fragment_overzicht_artikel.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author Raymond Chang
 *
 * Deze class zorgt ervoor dat alle producten wordt weergeven dmv Adapter en dat
 * alle funtionaliteiten op ArtikelOverzichtFragment het doen.
 */
class ArtikelOverzichtFragment : Fragment() {

    /**
     * Variabelen
     */
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var productAdapter: ProductAdapter

    private val viewModel: ProductViewModel by activityViewModels()

    private var products: ArrayList<Product> = arrayListOf()
    private var originalProductList = ArrayList<Product>()

    /**
     * OncreateView
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val inflater = inflater.inflate(R.layout.fragment_overzicht_artikel, container, false)
        val spinner = inflater.findViewById<Spinner>(R.id.spinnerProducts)

        val adapter = ArrayAdapter.createFromResource(
            requireActivity().baseContext,
            R.array.sort_options,
            android.R.layout.simple_spinner_dropdown_item
        )

        spinner.adapter = adapter
        spinnerSelectedListener(spinner)

        return inflater
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
    }

    /**
     * Beschrijft wat het moet doen wanneer een waarde in de spinner wordt geselecteerd.
     */
    private fun spinnerSelectedListener(spinner: Spinner){
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                p1: View?,
                position: Int,
                p3: Long
            ) {
                val selectedItem = parent?.getItemAtPosition(position).toString()

                //Sorteert productlijst op basis van de geselecteerde waarde in de spinner.
                if (selectedItem.equals("Prijs aflopend")) {
                    this@ArtikelOverzichtFragment.products.sortByDescending {
                        it.sell_price
                    }
                } else if (selectedItem.equals("Prijs oplopend")) {
                    this@ArtikelOverzichtFragment.products.sortBy {
                        it.sell_price
                    }
                } else if (selectedItem.equals("Alphabetisch")) {
                    this@ArtikelOverzichtFragment.products.sortBy {
                        it.brand
                    }
                } else {
                    this@ArtikelOverzichtFragment.products.sortBy {
                        it.department
                    }
                }
                productAdapter.notifyDataSetChanged()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
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
        svSearchProducts.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                //controleert of de variabel niet leeg is.
                if (newText!!.isNotEmpty()) {

                    //Productlijst wordt tijdelijk leeggemaakt.
                    products.clear()

                    //Stopt waarde in lowercase in search var.
                    val search = newText.toLowerCase(Locale.getDefault())

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
     * Observing products. Automatisch wordt de RV bijgewerkt wanneer Productslijst wordt aangepast.
     */
    private fun observeProduct() {
        viewModel.products.observe(viewLifecycleOwner, Observer { products ->
            this@ArtikelOverzichtFragment.products.clear()
            this@ArtikelOverzichtFragment.products.addAll(products)
            this@ArtikelOverzichtFragment.originalProductList.clear()
            this@ArtikelOverzichtFragment.originalProductList.addAll(products)
            productAdapter.notifyDataSetChanged()
        })
    }

    /**
     * initialiseert Recycleview
     */
    private fun initRv() {
        productAdapter = ProductAdapter(products)
        viewManager = LinearLayoutManager(activity)

        rvProducts.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = productAdapter
        }
    }
}