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
import androidx.recyclerview.widget.ItemTouchHelper
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
    private var displayProductList = ArrayList<Product>()

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

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                p1: View?,
                position: Int,
                p3: Long
            ) {
                val selectedItem = parent?.getItemAtPosition(position).toString()

                if (selectedItem.equals("Prijs aflopend")) {
                    this@ArtikelOverzichtFragment.products.sortByDescending {
                        it.sell_price
                    }
                }else if (selectedItem.equals("Prijs oplopend")) {
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

        // Inflate the layout for this fragment
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

    private fun filterList() {
        svSearchProducts.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText!!.isNotEmpty()){
                    products.clear()
                    val search = newText.toLowerCase(Locale.getDefault())
                    displayProductList.forEach {
                        if (it.brand.toLowerCase(Locale.getDefault()).contains(search)){
                            products.add(it)
                        }
                    }
                    productAdapter.notifyDataSetChanged()
                } else{
                    products.clear()
                    products.addAll(displayProductList)
                    productAdapter.notifyDataSetChanged()
                }
                return true
            }

        })
    }

    private fun observeProduct() {
        viewModel.products.observe(viewLifecycleOwner, Observer { products ->
            this@ArtikelOverzichtFragment.products.clear()
            this@ArtikelOverzichtFragment.products.addAll(products)
            this@ArtikelOverzichtFragment.displayProductList.addAll(products)
            productAdapter.notifyDataSetChanged()
        })
    }

    private fun initRv() {
        productAdapter = ProductAdapter(products)
        viewManager = LinearLayoutManager(activity)

        createItemTouchHelper().attachToRecyclerView(rvProducts)

        rvProducts.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = productAdapter
        }
    }

    private fun createItemTouchHelper(): ItemTouchHelper {
        val callback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
//                val productToDelete = products[position]
            }
        }
        return ItemTouchHelper(callback)
    }
}